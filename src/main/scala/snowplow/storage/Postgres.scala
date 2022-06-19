package snowplow.storage

import cats.effect.{IO, Resource}
import doobie.Transactor
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts.fixedThreadPool
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.output.MigrateResult
import snowplow.config.Config

object Postgres {
  def migrate(connectionString: String, username: String, password: String): IO[MigrateResult] = {
    IO {
      Flyway
        .configure()
        .dataSource(
          connectionString,
          username,
          password
        )
        .load()
        .migrate()
    }
  }

  def transactor(config: Config): Resource[IO, Transactor[IO]] =
    for {
      connectionExecutionContext <- fixedThreadPool[IO](config.databasePoolSize)
      xa <- HikariTransactor.newHikariTransactor[IO](
        driverClassName = "org.postgresql.Driver",
        url = config.databaseConnectionString,
        user = config.databaseUsername,
        pass = config.databasePassword.value,
        connectEC = connectionExecutionContext
      )
      _ <- Resource.eval(xa.configure { ds =>
        IO(ds.setMaximumPoolSize(config.databasePoolSize))
      })
    } yield xa
}

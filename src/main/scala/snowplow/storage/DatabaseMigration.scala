package snowplow.storage

import cats.effect.IO
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.output.MigrateResult

object DatabaseMigration {
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
}

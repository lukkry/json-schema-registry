package snowplow

import cats.effect.{IO, IOApp}
import cats.implicits._
import snowplow.config.Config
import snowplow.domain.Processor
import snowplow.http.HttpServer
import snowplow.storage.{Postgres, PostgresSchemaRepository}

object Main extends IOApp.Simple {
  val run: IO[Unit] =
    Config.load().flatMap { config => runWithConfig(config) }

  def runWithConfig(config: Config): IO[Unit] =
    migrateDatabase(config) >>
      Postgres.transactor(config).use { transactor =>
        val schemaRepository = PostgresSchemaRepository.create(transactor)
        val processor = Processor.create(schemaRepository)

        HttpServer.create(processor).use { server =>
          IO.println(s"Started HTTP server ${server.baseUri}") >>
            IO.never
        }
      }

  private def migrateDatabase(config: Config): IO[Unit] =
    Postgres
      .migrate(
        config.databaseConnectionString,
        config.databaseUsername,
        config.databasePassword.value
      )
      .void
}

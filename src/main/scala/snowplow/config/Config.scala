package snowplow.config

import cats.effect.IO
import ciris._
import cats.implicits._

case class Config(
    databaseConnectionString: String,
    databaseUsername: String,
    databasePassword: Secret[String],
    databasePoolSize: Int
)

object Config {
  def load(): IO[Config] =
    (
      env("DATABASE_CONNECTION_STRING").as[String],
      env("DATABASE_USERNAME").as[String],
      env("DATABASE_PASSWORD").as[String].secret
    ).parMapN { (databaseConnectionString, databaseUsername, databasePassword) =>
      Config(
        databaseConnectionString = databaseConnectionString,
        databaseUsername = databaseUsername,
        databasePassword = databasePassword,
        databasePoolSize = 4
      )
    }.load[IO]
}

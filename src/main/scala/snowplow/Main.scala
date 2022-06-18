package snowplow

import cats.effect.{IO, IOApp}
import snowplow.http.HttpServer

object Main extends IOApp.Simple {
  val run: IO[Unit] =
    HttpServer.create.use(_ => IO.never).void
}

package snowplow.http

import cats.effect._
import com.comcast.ip4s._
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.ember.server._
import org.http4s.implicits._
import org.http4s.server.{Router, Server}

object HttpServer {
  val routes: HttpRoutes[IO] = HttpRoutes.of[IO] { case GET -> Root / "schema" / schemaId =>
    Ok(s"Hello $schemaId")
  }

  def create: Resource[IO, Server] = {
    val httpApp = Router("/" -> routes).orNotFound

    EmberServerBuilder
      .default[IO]
      .withHost(ipv4"0.0.0.0")
      .withPort(port"8080")
      .withHttpApp(httpApp)
      .build
  }
}

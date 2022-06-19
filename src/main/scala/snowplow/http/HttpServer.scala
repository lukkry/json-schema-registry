package snowplow.http

import cats.effect._
import com.comcast.ip4s._
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.ember.server._
import org.http4s.implicits._
import org.http4s.server.{Router, Server}
import snowplow.domain.Processor

object HttpServer {
  def routes(processor: Processor): HttpRoutes[IO] = {
    val httpService = HttpService(processor)

    HttpRoutes.of[IO] {
      case GET -> Root / "schema" / schemaId =>
        httpService.retrieve(schemaId)
      case request @ POST -> Root / "schema" / schemaId =>
        httpService.storeSchema(schemaId, request)
    }
  }

  def create(processor: Processor): Resource[IO, Server] = {
    val httpApp = Router("/" -> routes(processor)).orNotFound

    EmberServerBuilder
      .default[IO]
      .withHost(ipv4"0.0.0.0")
      .withPort(port"8080")
      .withHttpApp(httpApp)
      .build
  }
}

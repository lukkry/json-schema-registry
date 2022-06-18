package snowplow.http

import cats.effect.IO
import io.circe.Decoder
import org.http4s.circe._
import org.http4s.dsl.io._
import org.http4s.{EntityDecoder, Request, Response}
import snowplow.domain.{JsonSchema, JsonSchemaContent, JsonSchemaId, Processor}
import snowplow.http.HttpService._

case class HttpService(processor: Processor) {
  def storeSchema(schemaId: String, request: Request[IO]): IO[Response[IO]] = {
    request.as[JsonSchemaContent].flatMap { schemaContent =>
      val schema = JsonSchema(
        id = JsonSchemaId(schemaId),
        content = schemaContent
      )

      processor.storeSchema(schema).flatMap { result =>
        result.fold(_ => BadRequest(), _ => Ok())
      }
    }
  }

  def retrieve(schemaId: String): IO[Response[IO]] = ???

  def validate(schemaId: String, instance: String): IO[Response[IO]] = ???
}

object HttpService {
  implicit val jsonSchemaContentDecoder: Decoder[JsonSchemaContent] =
    Decoder.decodeJson.map(json => JsonSchemaContent(json))

  implicit val jsonSchemaContentEntityDecoder: EntityDecoder[IO, JsonSchemaContent] =
    jsonOf[IO, JsonSchemaContent]
}

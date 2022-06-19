package snowplow.http

import cats.effect.IO
import io.circe.syntax.EncoderOps
import io.circe.{Decoder, Encoder, Json}
import org.http4s.circe._
import org.http4s.dsl.io._
import org.http4s.{EntityDecoder, MalformedMessageBodyFailure, Request, Response}
import snowplow.domain.{
  JsonInstance,
  JsonSchema,
  JsonSchemaContent,
  JsonSchemaId,
  Processor,
  SchemaCreationError,
  ValidationError
}
import snowplow.http.HttpService._

case class HttpService(processor: Processor) {
  def storeSchema(schemaId: String, request: Request[IO]): IO[Response[IO]] =
    request
      .as[JsonSchemaContent]
      .flatMap { schemaContent =>
        val schema = JsonSchema(
          id = JsonSchemaId(schemaId.trim),
          content = schemaContent
        )
        processor.storeSchema(schema).flatMap {
          case Left(error) =>
            error match {
              case SchemaCreationError.SchemaAlreadyExists =>
                val response = HttpResponse(
                  action = "uploadSchema",
                  id = schemaId,
                  status = ResponseStatus.Error("Schema with provided id already exists")
                )
                Conflict(response.asJson)
              case SchemaCreationError.SchemaIdInvalid(errors) =>
                val response = HttpResponse(
                  action = "uploadSchema",
                  id = schemaId,
                  status = ResponseStatus.Error(errors.mkString(". "))
                )
                BadRequest(response.asJson)
            }
          case Right(_) =>
            val response = HttpResponse(
              action = "uploadSchema",
              id = schemaId,
              status = ResponseStatus.Success
            )
            Created(response.asJson)
        }
      }
      .handleErrorWith {
        case _: MalformedMessageBodyFailure => malformedJsonResponse("uploadSchema", schemaId)
        case t: Throwable                   => IO.raiseError(t)
      }

  def retrieve(schemaId: String): IO[Response[IO]] =
    processor.retrieve(JsonSchemaId(schemaId)).flatMap { maybeSchema =>
      maybeSchema.fold(NotFound()) { schema =>
        Ok(schema.content.value)
      }
    }

  def validate(schemaId: String, request: Request[IO]): IO[Response[IO]] =
    request
      .as[JsonInstance]
      .flatMap { instance =>
        processor.validate(JsonSchemaId(schemaId), instance).flatMap {
          case Left(validationError) =>
            validationError match {
              case ValidationError.InvalidInstance(errors) =>
                val response = HttpResponse(
                  action = "validateDocument",
                  id = schemaId,
                  status = ResponseStatus.Error(errors.toList.mkString(" "))
                )
                Ok(response.asJson)
              case ValidationError.SchemaNotFound => NotFound()
            }
          case Right(_) =>
            val response = HttpResponse(
              action = "validateDocument",
              id = schemaId,
              status = ResponseStatus.Success
            )
            Ok(response.asJson)
        }
      }
      .handleErrorWith {
        case _: MalformedMessageBodyFailure => malformedJsonResponse("validateDocument", schemaId)
        case t: Throwable                   => IO.raiseError(t)
      }

  private def malformedJsonResponse(action: String, schemaId: String): IO[Response[IO]] = {
    val response = HttpResponse(
      action = action,
      id = schemaId,
      status = ResponseStatus.Error("Invalid JSON")
    )
    BadRequest(response.asJson)
  }
}

object HttpService {
  sealed trait ResponseStatus
  object ResponseStatus {
    case object Success extends ResponseStatus
    case class Error(message: String) extends ResponseStatus
  }

  case class HttpResponse(
      action: String,
      id: String,
      status: ResponseStatus
  )

  implicit val storeSchemaResponseEncoder: Encoder[HttpResponse] =
    (response: HttpResponse) =>
      response.status match {
        case ResponseStatus.Success =>
          Json.obj(
            ("action", Json.fromString(response.action)),
            ("id", Json.fromString(response.id)),
            ("status", Json.fromString("success"))
          )
        case ResponseStatus.Error(message) =>
          Json.obj(
            ("action", Json.fromString(response.action)),
            ("id", Json.fromString(response.id)),
            ("status", Json.fromString("error")),
            ("message", Json.fromString(message))
          )
      }

  implicit val jsonSchemaContentDecoder: Decoder[JsonSchemaContent] =
    Decoder.decodeJson.map(json => JsonSchemaContent(json))

  implicit val jsonSchemaContentEntityDecoder: EntityDecoder[IO, JsonSchemaContent] =
    jsonOf[IO, JsonSchemaContent]

  implicit val jsonInstanceDecoder: Decoder[JsonInstance] =
    Decoder.decodeJson.map(json => JsonInstance(json))

  implicit val jsonInstanceEntityDecoder: EntityDecoder[IO, JsonInstance] =
    jsonOf[IO, JsonInstance]
}

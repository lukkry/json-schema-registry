package snowplow.domain

import cats.data.NonEmptyList
import io.circe.Json

case class JsonSchemaContent(value: Json)

case class JsonSchemaId(value: String)

case class JsonSchema(
    id: JsonSchemaId,
    content: JsonSchemaContent
)

case class JsonInstance(value: Json) {
  val valueNoNull: Json = value.deepDropNullValues
}

sealed trait ValidationError
object ValidationError {
  case class InvalidInstance(errors: NonEmptyList[String]) extends ValidationError
  case object SchemaNotFound extends ValidationError
}

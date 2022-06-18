package snowplow.domain

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

case class ValidationError(value: String)

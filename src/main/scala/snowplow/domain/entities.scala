package snowplow.domain

import io.circe.Json

case class JsonSchema(value: Json)

case class JsonInstance(value: Json) {
  val valueNoNull: Json = value.deepDropNullValues
}

case class ValidationError(value: String)

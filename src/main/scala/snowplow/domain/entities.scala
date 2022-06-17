package snowplow.domain

import io.circe.Json

case class JsonSchema(value: Json)

case class JsonInstance(value: Json)

case class ValidationError(value: String)

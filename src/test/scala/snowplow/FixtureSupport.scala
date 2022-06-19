package snowplow

import io.circe.literal._
import snowplow.domain.{JsonInstance, JsonSchema, JsonSchemaContent, JsonSchemaId}

object FixtureSupport {
  val schemaContent: JsonSchemaContent =
    JsonSchemaContent(
      json"""
{
  "schema": "http://json-schema.org/draft-04/schema#",
  "type": "object",
  "properties": {
    "source": {
      "type": "string"
    },
    "destination": {
      "type": "string"
    },
    "timeout": {
      "type": "integer",
      "minimum": 0,
      "maximum": 32767
    },
    "chunks": {
      "type": "object",
      "properties": {
        "size": {
          "type": "integer"
        },
        "number": {
          "type": "integer"
        }
      },
      "required": ["size"]
    }
  },
  "required": ["source", "destination"]
}
"""
    )

  val schemaId: JsonSchemaId = JsonSchemaId("test-schema-id")

  val schema: JsonSchema =
    JsonSchema(
      id = schemaId,
      content = schemaContent
    )

  val validInstance: JsonInstance =
    JsonInstance(
      json"""
{
  "source": "/home/alice/image.iso",
  "destination": "/mnt/storage"
}
"""
    )

  val validInstanceWithNull: JsonInstance =
    JsonInstance(
      json"""
{
  "source": "/home/alice/image.iso",
  "destination": "/mnt/storage",
  "timeout": null,
  "chunks": {
    "size": 1024,
    "number": null
  }
}
"""
    )

  val invalidInstance: JsonInstance =
    JsonInstance(
      json"""
{
  "source": null,
  "destination": "/mnt/storage",
  "timeout": null,
  "chunks": {
    "size": 1024,
    "number": null
  }
}
"""
    )

  val successStoreSchemaResponse = json"""
{
    "action": "uploadSchema",
    "id": ${schemaId.value},
    "status": "success"
}
"""

  val malformedJsonStoreSchemaResponse = json"""
{
    "action": "uploadSchema",
    "id": ${schemaId.value},
    "status": "error",
    "message": "Invalid JSON"
}
"""

  val schemaAlreadyExistsStoreSchemaResponse = json"""
{
    "action": "uploadSchema",
    "id": ${schemaId.value},
    "status": "error",
    "message": "Schema with provided id already exists"
}
"""

  def schemaIdTooLongStoreSchemaResponse(schemaId: String) = json"""
{
    "action": "uploadSchema",
    "id": $schemaId,
    "status": "error",
    "message": "Schema id is too long (max: 255 characters)"
}
"""

  val successValidateInstanceResponse =
    json"""
{
    "action": "validateDocument",
    "id": ${schemaId.value},
    "status": "success"
}
"""

  val invalidInstanceValidateInstanceResponse =
    json"""
{
    "action": "validateDocument",
    "id": ${schemaId.value},
    "status": "error",
    "message": "object has missing required properties ([\"source\"])"
}
"""

  val malformedJsonValidateInstanceResponse =
    json"""
{
    "action": "validateDocument",
    "id": ${schemaId.value},
    "status": "error",
    "message": "Invalid JSON"
}
"""
}

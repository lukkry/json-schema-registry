package snowplow.domain

import cats.data.NonEmptyList
import cats.implicits._
import io.circe.parser._
import munit.FunSuite
import snowplow.domain.JsonValidatorTest._

class JsonValidatorTest extends FunSuite {
  test("should return a provided json instance when it is valid") {
    (parse(rawSchema), parse(rawValidInstance)).tupled.map {
      case (schema, instance) =>
        val result =
          JsonValidator.validate(JsonSchema(schema), JsonInstance(instance))

        assertEquals(result, Right(JsonInstance(instance)))
    }
  }

  test("should return validation errors when a json instance is invalid") {
    (parse(rawSchema), parse(rawInvalidInstance)).tupled.map {
      case (schema, instance) =>
        val result =
          JsonValidator.validate(JsonSchema(schema), JsonInstance(instance))
        val expectedMessage =
          "instance type (null) does not match any allowed primitive type (allowed: [\"integer\"])"

        assertEquals(
          result,
          Left(
            NonEmptyList.of(
              ValidationError(expectedMessage),
              ValidationError(expectedMessage)
            )
          )
        )
    }
  }
}

object JsonValidatorTest {
  val rawValidInstance: String = """
{
  "source": "/home/alice/image.iso",
  "destination": "/mnt/storage",
}
"""

  val rawInvalidInstance: String = """
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

  val rawSchema: String = """
{
  "$schema": "http://json-schema.org/draft-04/schema#",
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
}

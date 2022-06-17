package snowplow.domain

import cats.data.NonEmptyList
import cats.implicits._
import io.circe.ParsingFailure
import io.circe.parser._
import munit.FunSuite
import snowplow.FixtureSupport._
import snowplow.domain.JsonValidatorTest.withSchemaInstance

class JsonValidatorTest extends FunSuite {
  test("should return success for a valid json instance") {
    withSchemaInstance(rawSchema, rawValidInstance) { (schema, instance) =>
      val result = JsonValidator.validate(schema, instance)
      assertEquals(result, Right(()))
    }
  }

  test("should return success for a valid json instance with null fields") {
    withSchemaInstance(rawSchema, rawValidInstanceWithNull) { (schema, instance) =>
      val result = JsonValidator.validate(schema, instance)
      assertEquals(result, Right(()))
    }
  }

  test("should return validation errors when a json instance is invalid") {
    withSchemaInstance(rawSchema, rawInvalidInstance) { (schema, instance) =>
      val result = JsonValidator.validate(schema, instance)
      val expectedResult =
        NonEmptyList.one(
          ValidationError("object has missing required properties ([\"source\"])")
        )
      assertEquals(result, Left(expectedResult))
    }
  }
}

object JsonValidatorTest {
  def withSchemaInstance(schema: String, instance: String)(
      test: (JsonSchema, JsonInstance) => Unit
  ): Either[ParsingFailure, Unit] =
    (parse(schema), parse(instance)).tupled.map { case (schema, instance) =>
      test(JsonSchema(schema), JsonInstance(instance))
    }
}

package snowplow.domain

import cats.data.NonEmptyList
import munit.FunSuite
import snowplow.FixtureSupport

class JsonValidatorTest extends FunSuite {
  test("should return success for a valid json instance") {
    assertEquals(
      JsonValidator.validate(FixtureSupport.schemaContent, FixtureSupport.validInstance),
      Right(())
    )
  }

  test("should return success for a valid json instance with null fields") {
    assertEquals(
      JsonValidator.validate(FixtureSupport.schemaContent, FixtureSupport.validInstanceWithNull),
      Right(())
    )
  }

  test("should return validation errors when a json instance is invalid") {
    val expectedResult =
      ValidationError.InvalidInstance(
        NonEmptyList.one("object has missing required properties ([\"source\"])")
      )
    assertEquals(
      JsonValidator.validate(FixtureSupport.schemaContent, FixtureSupport.invalidInstance),
      Left(expectedResult)
    )
  }
}

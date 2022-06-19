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

  test("should return validation errors when a json instance has multiple errors") {
    val expectedResult =
      ValidationError.InvalidInstance(
        NonEmptyList.of(
          "Field: /properties/destination. Error: instance type (integer) does not match any allowed primitive type (allowed: [\"string\"]).",
          "Field: /properties/source. Error: instance type (integer) does not match any allowed primitive type (allowed: [\"string\"])."
        )
      )
    assertEquals(
      JsonValidator
        .validate(FixtureSupport.schemaContent, FixtureSupport.invalidInstanceIncorrectType),
      Left(expectedResult)
    )
  }

  test("should return validation error when a json instance breaches numeric constraint") {
    val expectedResult =
      ValidationError.InvalidInstance(
        NonEmptyList.of(
          "Field: /properties/source. Error: instance type (integer) does not match any allowed primitive type (allowed: [\"string\"]).",
          "Field: /properties/timeout. Error: numeric instance is greater than the required maximum (maximum: 32767, found: 42767)."
        )
      )
    assertEquals(
      JsonValidator
        .validate(FixtureSupport.schemaContent, FixtureSupport.invalidInstanceNumericConstraint),
      Left(expectedResult)
    )
  }
}

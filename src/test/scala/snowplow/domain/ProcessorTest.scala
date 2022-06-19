package snowplow.domain

import munit.CatsEffectSuite
import snowplow.{FixtureSupport, InMemorySchemaRepository}
import snowplow.domain.ValidationError.SchemaNotFound

class ProcessorTest extends CatsEffectSuite {
  test("should successfully store and retrieve a schema") {
    InMemorySchemaRepository.create().flatMap { schemaRepository =>
      val processor = Processor.create(schemaRepository)
      assertIO(processor.storeSchema(FixtureSupport.schema), Right(())) >>
        assertIO(processor.retrieve(FixtureSupport.schema.id), Some(FixtureSupport.schema))
    }
  }

  test("should return an error if schema id is too long") {
    InMemorySchemaRepository.create().flatMap { schemaRepository =>
      val processor = Processor.create(schemaRepository)
      val schemaWithTooLongId = JsonSchema(
        id = JsonSchemaId("long-schema-id" * 100),
        content = FixtureSupport.schemaContent
      )
      assertIO(
        processor.storeSchema(schemaWithTooLongId),
        Left(
          SchemaCreationError.SchemaIdInvalid(
            List(
              "Schema id is either too short or too long. Min: 1 character. Max: 255 characters."
            )
          )
        )
      )
    }
  }

  test("should return an error if schema id contains non-alphanumeric characters") {
    InMemorySchemaRepository.create().flatMap { schemaRepository =>
      val processor = Processor.create(schemaRepository)
      val schemaWithNonAlphaNumericChars = JsonSchema(
        id = JsonSchemaId("schema-id-!#."),
        content = FixtureSupport.schemaContent
      )
      assertIO(
        processor.storeSchema(schemaWithNonAlphaNumericChars),
        Left(
          SchemaCreationError.SchemaIdInvalid(
            List(
              "Schema id contains illegal characters. Only alphanumeric characters and '-' allowed."
            )
          )
        )
      )
    }
  }

  test("should return an error if schema is not found") {
    InMemorySchemaRepository.create().flatMap { schemaRepository =>
      val processor = Processor.create(schemaRepository)

      assertIO(
        processor.validate(FixtureSupport.schema.id, FixtureSupport.validInstance),
        Left(SchemaNotFound)
      )
    }
  }

  test("should return success if instance is valid") {
    InMemorySchemaRepository.create().flatMap { schemaRepository =>
      val processor = Processor.create(schemaRepository)

      processor.storeSchema(FixtureSupport.schema) >>
        assertIO(
          processor.validate(FixtureSupport.schema.id, FixtureSupport.validInstance),
          Right(())
        )
    }
  }
}

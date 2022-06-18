package snowplow.domain

import munit.CatsEffectSuite
import snowplow.FixtureSupport
import snowplow.domain.ValidationError.SchemaNotFound
import snowplow.storage.InMemorySchemaRepository

class ProcessorTest extends CatsEffectSuite {
  test("should successfully store and retrieve a schema") {
    InMemorySchemaRepository.create().flatMap { schemaRepository =>
      val processor = Processor.create(schemaRepository)
      assertIO(processor.storeSchema(FixtureSupport.schema), Right(())) >>
        assertIO(processor.retrieve(FixtureSupport.schema.id), Some(FixtureSupport.schema))
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

package snowplow.domain

import munit.CatsEffectSuite
import snowplow.FixtureSupport
import snowplow.domain.ProcessorTest._
import snowplow.domain.ValidationError.SchemaNotFound
import snowplow.storage.InMemorySchemaRepository

class ProcessorTest extends CatsEffectSuite {
  test("should successfully store and retrieve a schema") {
    InMemorySchemaRepository.create().flatMap { schemaRepository =>
      val processor = Processor.create(schemaRepository)
      assertIO(processor.storeSchema(schema), Right(())) >>
        assertIO(processor.retrieve(schema.id), Some(schema))
    }
  }

  test("should return an error if schema is not found") {
    InMemorySchemaRepository.create().flatMap { schemaRepository =>
      val processor = Processor.create(schemaRepository)
      val instance = FixtureSupport.generateJsonInstance()

      assertIO(processor.validate(JsonSchemaId(schemaId), instance), Left(SchemaNotFound))
    }
  }

  test("should return success if instance is valid") {
    InMemorySchemaRepository.create().flatMap { schemaRepository =>
      val processor = Processor.create(schemaRepository)
      val schema = FixtureSupport.generateJsonSchema(schemaId)
      val instance = FixtureSupport.generateJsonInstance()

      processor.storeSchema(schema) >>
        assertIO(processor.validate(JsonSchemaId(schemaId), instance), Right(()))
    }
  }
}

object ProcessorTest {
  val schemaId = "test-schema-id"
  val schema: JsonSchema = FixtureSupport.generateJsonSchema(schemaId)
}

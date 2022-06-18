package snowplow.storage

import munit.CatsEffectSuite
import snowplow.FixtureSupport
import snowplow.domain.{JsonSchema, JsonSchemaId}
import snowplow.domain.SchemaRepository.SchemaAlreadyExists
import snowplow.storage.InMemorySchemaRepositoryTest._

class InMemorySchemaRepositoryTest extends CatsEffectSuite {
  test("should successfully store and retrieve a schema") {
    InMemorySchemaRepository.create().flatMap { repository =>
      assertIO(repository.store(schema), Right(())) >>
        assertIO(repository.retrieve(schema.id), Some(schema))
    }
  }

  test("should return an error if provided schema already exists") {
    InMemorySchemaRepository.create().flatMap { repository =>
      assertIO(repository.store(schema), Right(())) >>
        assertIO(repository.store(schema), Left(SchemaAlreadyExists))
    }
  }

  test("should return nothing if schema doesn't exist") {
    InMemorySchemaRepository.create().flatMap { repository =>
      assertIO(repository.retrieve(JsonSchemaId(schemaId)), None)
    }
  }
}

object InMemorySchemaRepositoryTest {
  val schemaId = "test-schema-id"
  val schema: JsonSchema = FixtureSupport.generateJsonSchema(schemaId)
}

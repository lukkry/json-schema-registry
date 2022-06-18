package snowplow.storage

import munit.CatsEffectSuite
import snowplow.FixtureSupport
import snowplow.domain.SchemaRepository.SchemaAlreadyExists

class InMemorySchemaRepositoryTest extends CatsEffectSuite {
  test("should successfully store and retrieve a schema") {
    InMemorySchemaRepository.create().flatMap { repository =>
      assertIO(repository.store(FixtureSupport.schema), Right(())) >>
        assertIO(repository.retrieve(FixtureSupport.schema.id), Some(FixtureSupport.schema))
    }
  }

  test("should return an error if provided schema already exists") {
    InMemorySchemaRepository.create().flatMap { repository =>
      assertIO(repository.store(FixtureSupport.schema), Right(())) >>
        assertIO(repository.store(FixtureSupport.schema), Left(SchemaAlreadyExists))
    }
  }

  test("should return nothing if schema doesn't exist") {
    InMemorySchemaRepository.create().flatMap { repository =>
      assertIO(repository.retrieve(FixtureSupport.schema.id), None)
    }
  }
}

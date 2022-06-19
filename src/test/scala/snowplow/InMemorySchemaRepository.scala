package snowplow

import cats.effect.{IO, Ref}
import snowplow.domain.{JsonSchema, JsonSchemaId, SchemaCreationError, SchemaRepository}

object InMemorySchemaRepository {
  def create(): IO[SchemaRepository] = {
    Ref.of[IO, Map[JsonSchemaId, JsonSchema]](Map.empty).map { buffer =>
      new SchemaRepository {
        override def retrieve(schemaId: JsonSchemaId): IO[Option[JsonSchema]] =
          buffer.get.map(_.get(schemaId))

        override def store(schema: JsonSchema): IO[Either[SchemaCreationError, Unit]] =
          buffer.modify { b =>
            if (b.contains(schema.id)) (b, Left(SchemaCreationError.SchemaAlreadyExists))
            else (b.updated(schema.id, schema), Right(()))
          }
      }
    }
  }
}

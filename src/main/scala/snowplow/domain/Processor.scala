package snowplow.domain

import cats.effect.IO
import snowplow.domain.SchemaRepository.SchemaRepositoryError
import snowplow.domain.ValidationError.SchemaNotFound

trait Processor {
  def storeSchema(schema: JsonSchema): IO[Either[SchemaRepositoryError, Unit]]
  def retrieve(schemaId: JsonSchemaId): IO[Option[JsonSchema]]
  def validate(schemaId: JsonSchemaId, instance: JsonInstance): IO[Either[ValidationError, Unit]]
}

object Processor {
  def create(schemaRepository: SchemaRepository): Processor = new Processor {
    override def storeSchema(schema: JsonSchema): IO[Either[SchemaRepositoryError, Unit]] =
      schemaRepository.store(schema)

    override def retrieve(schemaId: JsonSchemaId): IO[Option[JsonSchema]] =
      schemaRepository.retrieve(schemaId)

    override def validate(
        schemaId: JsonSchemaId,
        instance: JsonInstance
    ): IO[Either[ValidationError, Unit]] =
      schemaRepository.retrieve(schemaId).map { maybeSchema =>
        maybeSchema.fold[Either[ValidationError, Unit]](Left(SchemaNotFound)) { schema =>
          JsonValidator.validate(schema.content, instance)
        }
      }
  }
}

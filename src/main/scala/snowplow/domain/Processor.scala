package snowplow.domain

import cats.effect.IO
import snowplow.domain.ValidationError.SchemaNotFound

trait Processor {
  def storeSchema(schema: JsonSchema): IO[Either[SchemaCreationError, Unit]]
  def retrieve(schemaId: JsonSchemaId): IO[Option[JsonSchema]]
  def validate(schemaId: JsonSchemaId, instance: JsonInstance): IO[Either[ValidationError, Unit]]
}

object Processor {
  def create(schemaRepository: SchemaRepository): Processor = new Processor {
    override def storeSchema(schema: JsonSchema): IO[Either[SchemaCreationError, Unit]] =
      validateSchemaId(schema.id)
        .fold(error => IO.pure(Left(error)), _ => schemaRepository.store(schema))

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

    private def validateSchemaId(
        schemaId: JsonSchemaId
    ): Either[SchemaCreationError, JsonSchemaId] = {
      val ensureSize =
        if (schemaId.value.nonEmpty && schemaId.value.length <= 255) None
        else Some("Schema Id is too long. Max 255 characters.")

      val ensureAlphaNumeric =
        if (schemaId.value.forall(c => c.isLetterOrDigit || c == '-'))
          None
        else
          Some(
            "Schema Id contains illegal characters. Only alphanumeric characters and '-' allowed"
          )

      val errors = List(ensureSize, ensureAlphaNumeric).flatten

      if (errors.isEmpty) Right(schemaId)
      else Left(SchemaCreationError.SchemaIdInvalid(errors))
    }
  }
}

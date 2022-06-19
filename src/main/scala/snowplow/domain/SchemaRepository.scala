package snowplow.domain

import cats.effect.IO

trait SchemaRepository {
  def retrieve(schemaId: JsonSchemaId): IO[Option[JsonSchema]]
  def store(schema: JsonSchema): IO[Either[SchemaCreationError, Unit]]
}

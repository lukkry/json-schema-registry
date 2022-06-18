package snowplow.domain

import cats.effect.IO
import snowplow.domain.SchemaRepository.SchemaRepositoryError

trait SchemaRepository {
  def retrieve(schemaId: JsonSchemaId): IO[Option[JsonSchema]]
  def store(schema: JsonSchema): IO[Either[SchemaRepositoryError, Unit]]
}

object SchemaRepository {
  sealed trait SchemaRepositoryError
  case object SchemaAlreadyExists extends SchemaRepositoryError
}

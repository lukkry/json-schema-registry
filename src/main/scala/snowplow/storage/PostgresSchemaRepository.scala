package snowplow.storage

import cats.effect.IO
import cats.implicits._
import doobie._
import doobie.implicits._
import doobie.postgres.circe.jsonb.implicits.{jsonbGet, jsonbPut}
import doobie.postgres.sqlstate
import doobie.util.transactor.Transactor
import io.circe.Json
import snowplow.domain.{JsonSchema, JsonSchemaContent, JsonSchemaId, SchemaRepository}

object PostgresSchemaRepository {
  def create(transactor: Transactor[IO]): SchemaRepository = new SchemaRepository {
    override def retrieve(schemaId: JsonSchemaId): IO[Option[JsonSchema]] =
      retrieveSchemaQuery(schemaId.value).option.transact(transactor)

    override def store(
        schema: JsonSchema
    ): IO[Either[SchemaRepository.SchemaRepositoryError, Unit]] =
      insertSchema(schema).run.void
        .attemptSomeSqlState[SchemaRepository.SchemaRepositoryError] {
          case sqlstate.class23.UNIQUE_VIOLATION => SchemaRepository.SchemaAlreadyExists
        }
        .transact(transactor)
  }

  implicit val jsonSchemaIdMeta: Meta[JsonSchemaId] =
    Meta[String].imap(JsonSchemaId.apply)(_.value)

  implicit val jsonSchemaContentGet: Get[JsonSchemaContent] =
    Get[Json].tmap(JsonSchemaContent.apply)

  implicit val jsonSchemaContentPut: Put[JsonSchemaContent] =
    Put[Json].tcontramap(_.value)

  def retrieveSchemaQuery(schemaId: String): Query0[JsonSchema] =
    sql"""
         | SELECT schema_id, content
         | FROM json_schema
         | WHERE schema_id = $schemaId
         |""".stripMargin.query[JsonSchema]

  def insertSchema(schema: JsonSchema): Update0 =
    sql"""
         | INSERT INTO json_schema (schema_id, content)
         | VALUES (${schema.id}, ${schema.content})
         |""".stripMargin.update
}

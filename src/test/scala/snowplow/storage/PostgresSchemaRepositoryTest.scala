package snowplow.storage

import cats.effect.IO
import com.dimafeng.testcontainers.PostgreSQLContainer
import com.dimafeng.testcontainers.munit.fixtures.TestContainersFixtures
import doobie.Transactor
import doobie.implicits._
import doobie.util.transactor.Transactor.Aux
import munit.CatsEffectSuite
import org.testcontainers.utility.DockerImageName
import snowplow.FixtureSupport
import snowplow.domain.SchemaRepository.SchemaAlreadyExists

class PostgresSchemaRepositoryTest extends CatsEffectSuite with TestContainersFixtures {
  val databaseName = "test-database"
  val username = "test-user"
  val password = "test-user-password"

  val postgresFixture: ForAllContainerFixture[PostgreSQLContainer] = ForAllContainerFixture(
    PostgreSQLContainer(
      DockerImageName.parse("postgres:14"),
      databaseName,
      username,
      password
    )
  )

  def transactor: Aux[IO, Unit] = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver",
    postgresFixture().jdbcUrl,
    username,
    password
  )

  def prepareDatabase(): IO[Unit] = {
    DatabaseMigration.migrate(postgresFixture().jdbcUrl, username, password) >>
      fr"TRUNCATE TABLE json_schema".update.run.transact(transactor).void
  }

  override def munitFixtures = List(postgresFixture)

  test("should successfully store and retrieve a schema") {
    val repository = PostgresSchemaRepository.create(transactor)
    prepareDatabase() >>
      assertIO(repository.store(FixtureSupport.schema), Right(())) >>
      assertIO(repository.retrieve(FixtureSupport.schema.id), Some(FixtureSupport.schema))
  }

  test("should return an error if provided schema already exists") {
    val repository = PostgresSchemaRepository.create(transactor)
    prepareDatabase() >>
      assertIO(repository.store(FixtureSupport.schema), Right(())) >>
      assertIO(repository.store(FixtureSupport.schema), Left(SchemaAlreadyExists))
  }

  test("should return nothing if schema doesn't exist") {
    val repository = PostgresSchemaRepository.create(transactor)
    prepareDatabase() >>
      assertIO(repository.retrieve(FixtureSupport.schema.id), None)
  }
}

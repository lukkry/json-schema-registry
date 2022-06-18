package snowplow.http

import munit.CatsEffectSuite
import org.http4s.Status
import org.http4s.circe._
import org.http4s.client.dsl.io._
import org.http4s.dsl.io._
import org.http4s.implicits._
import snowplow.FixtureSupport
import snowplow.domain.Processor
import snowplow.storage.InMemorySchemaRepository

class HttpServerTest extends CatsEffectSuite {
  test("POST create schema should return 201 Created if schema is successfully stored") {
    InMemorySchemaRepository.create().flatMap { schemaRepository =>
      val processor = Processor.create(schemaRepository)
      val request = POST(FixtureSupport.schemaContent.value, uri"/schema/test-schema-id")
      val response = HttpServer.routes(processor).orNotFound.run(request)

      assertIO(response.map(_.status), Status.Ok)
    }
  }

  test("POST create schema should return 400 Bad Request if schema is not a valid JSON") {}

  test("POST create schema should return 409 Conflict if schema already exists") {}

  test("GET retrieve schema should return 200 Ok if schema is found") {}

  test("GET retrieve schema should return 404 Not Found if schema is not found") {}

  test("POST validate should return 200 Ok if instance is valid") {}

  test("POST validate should return 200 Ok if instance is invalid") {}

  test("POST validate should return 404 Not Found if schema is not found") {}

  test("POST validate should return 400 Bad Request is provided instance is not a valid JSON") {}
}

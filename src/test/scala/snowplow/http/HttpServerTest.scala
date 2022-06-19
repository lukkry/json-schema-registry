package snowplow.http

import io.circe.Json
import munit.CatsEffectSuite
import org.http4s.circe._
import org.http4s.client.dsl.io._
import org.http4s.dsl.io._
import org.http4s.implicits._
import snowplow.{FixtureSupport, InMemorySchemaRepository}
import snowplow.domain.Processor

class HttpServerTest extends CatsEffectSuite {
  test("POST create schema should return 201 Created if schema is successfully stored") {
    InMemorySchemaRepository.create().flatMap { schemaRepository =>
      val processor = Processor.create(schemaRepository)
      val request = POST(
        FixtureSupport.schema.content.value,
        uri"/schema".addSegment(FixtureSupport.schemaId.value)
      )
      val responseIO = HttpServer.routes(processor).orNotFound.run(request)
      responseIO.flatMap { response =>
        assertEquals(response.status.code, 201)
        assertIO(response.as[Json], FixtureSupport.successStoreSchemaResponse)
      }
    }
  }

  test("POST create schema should return 400 Bad Request if schema is not a valid JSON") {
    InMemorySchemaRepository.create().flatMap { schemaRepository =>
      val processor = Processor.create(schemaRepository)
      val malformedJson = """{ "key": ??? }"""
      val request = POST(malformedJson, uri"/schema".addSegment(FixtureSupport.schemaId.value))
      val responseIO = HttpServer.routes(processor).orNotFound.run(request)
      responseIO.flatMap { response =>
        assertEquals(response.status.code, 400)
        assertIO(response.as[Json], FixtureSupport.malformedJsonStoreSchemaResponse)
      }
    }
  }

  test(
    "POST create schema should return 400 Bad Request if schema id is longer than 255 characters"
  ) {
    InMemorySchemaRepository.create().flatMap { schemaRepository =>
      val processor = Processor.create(schemaRepository)
      val tooLongSchemaId = "long-schema-id" * 100
      val request = POST(
        FixtureSupport.schema.content.value,
        uri"/schema".addSegment(tooLongSchemaId)
      )
      val responseIO = HttpServer.routes(processor).orNotFound.run(request)
      responseIO.flatMap { response =>
        assertEquals(response.status.code, 400)
        assertIO(
          response.as[Json],
          FixtureSupport.schemaIdTooLongStoreSchemaResponse(tooLongSchemaId)
        )
      }
    }
  }

  test("POST create schema should return 409 Conflict if schema already exists") {
    InMemorySchemaRepository.create().flatMap { schemaRepository =>
      val processor = Processor.create(schemaRepository)
      val request = POST(
        FixtureSupport.schema.content.value,
        uri"/schema".addSegment(FixtureSupport.schemaId.value)
      )
      val httpApp = HttpServer.routes(processor).orNotFound
      val responseIO = httpApp.run(request) >> httpApp.run(request)
      responseIO.flatMap { response =>
        assertEquals(response.status.code, 409)
        assertIO(response.as[Json], FixtureSupport.schemaAlreadyExistsStoreSchemaResponse)
      }
    }
  }

  test("GET retrieve schema should return 200 Ok if schema is found") {
    InMemorySchemaRepository.create().flatMap { schemaRepository =>
      val processor = Processor.create(schemaRepository)
      val uri = uri"/schema".addSegment(FixtureSupport.schemaId.value)
      val storeSchemaRequest = POST(FixtureSupport.schema.content.value, uri)
      val getSchemaRequest = GET(uri)
      val httpApp = HttpServer.routes(processor).orNotFound
      val responseIO = httpApp.run(storeSchemaRequest) >> httpApp.run(getSchemaRequest)
      responseIO.flatMap { response =>
        assertEquals(response.status.code, 200)
        assertIO(response.as[Json], FixtureSupport.schemaContent.value)
      }
    }
  }

  test("GET retrieve schema should return 404 Not Found if schema is not found") {
    InMemorySchemaRepository.create().flatMap { schemaRepository =>
      val processor = Processor.create(schemaRepository)
      val getSchemaRequest = GET(uri"/schema".addSegment(FixtureSupport.schemaId.value))
      val responseIO = HttpServer.routes(processor).orNotFound.run(getSchemaRequest)
      responseIO.flatMap { response =>
        assertEquals(response.status.code, 404)
        assertIO(response.bodyText.compile.toList, List.empty)
      }
    }
  }

  test("POST validate should return 200 Ok if instance is valid") {
    InMemorySchemaRepository.create().flatMap { schemaRepository =>
      val processor = Processor.create(schemaRepository)
      val storeSchemaRequest = POST(
        FixtureSupport.schema.content.value,
        uri"/schema".addSegment(FixtureSupport.schemaId.value)
      )
      val validateSchemaRequest = POST(
        FixtureSupport.validInstanceWithNull.valueNoNull,
        uri"/validate".addSegment(FixtureSupport.schemaId.value)
      )
      val httpApp = HttpServer.routes(processor).orNotFound
      val responseIO = httpApp.run(storeSchemaRequest) >> httpApp.run(validateSchemaRequest)
      responseIO.flatMap { response =>
        assertEquals(response.status.code, 200)
        assertIO(response.as[Json], FixtureSupport.successValidateInstanceResponse)
      }
    }
  }

  test("POST validate should return 200 Ok if instance is invalid") {
    InMemorySchemaRepository.create().flatMap { schemaRepository =>
      val processor = Processor.create(schemaRepository)
      val storeSchemaRequest = POST(
        FixtureSupport.schema.content.value,
        uri"/schema".addSegment(FixtureSupport.schemaId.value)
      )
      val validateSchemaRequest = POST(
        FixtureSupport.invalidInstance.value,
        uri"/validate".addSegment(FixtureSupport.schemaId.value)
      )
      val httpApp = HttpServer.routes(processor).orNotFound
      val responseIO = httpApp.run(storeSchemaRequest) >> httpApp.run(validateSchemaRequest)
      responseIO.flatMap { response =>
        assertEquals(response.status.code, 200)
        assertIO(response.as[Json], FixtureSupport.invalidInstanceValidateInstanceResponse)
      }
    }
  }

  test("POST validate should return 404 Not Found if schema is not found") {
    InMemorySchemaRepository.create().flatMap { schemaRepository =>
      val processor = Processor.create(schemaRepository)
      val validateSchemaRequest = POST(
        FixtureSupport.invalidInstance.value,
        uri"/validate".addSegment(FixtureSupport.schemaId.value)
      )
      val httpApp = HttpServer.routes(processor).orNotFound
      val responseIO = httpApp.run(validateSchemaRequest)
      responseIO.flatMap { response =>
        assertEquals(response.status.code, 404)
        assertIO(response.bodyText.compile.toList, List.empty)
      }
    }
  }

  test("POST validate should return 400 Bad Request is provided instance is not a valid JSON") {
    InMemorySchemaRepository.create().flatMap { schemaRepository =>
      val processor = Processor.create(schemaRepository)
      val malformedJson = """{ "key": ??? }"""
      val validateSchemaRequest =
        POST(malformedJson, uri"/validate".addSegment(FixtureSupport.schemaId.value))
      val httpApp = HttpServer.routes(processor).orNotFound
      val responseIO = httpApp.run(validateSchemaRequest)
      responseIO.flatMap { response =>
        assertEquals(response.status.code, 400)
        assertIO(response.as[Json], FixtureSupport.malformedJsonValidateInstanceResponse)
      }
    }
  }
}

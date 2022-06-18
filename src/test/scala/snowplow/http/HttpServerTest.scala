package snowplow.http

import munit.CatsEffectSuite

class HttpServerTest extends CatsEffectSuite {
  test("POST create schema should return 201 Created if schema is successfully stored") {}

  test("POST create schema should return 400 Bad Request if schema is not a valid JSON") {}

  test("POST create schema should return 409 Conflict if schema already exists") {}

  test("GET retrieve schema should return 200 Ok if schema is found") {}

  test("GET retrieve schema should return 404 Not Found if schema is not found") {}

  test("POST validate should return 200 Ok if instance is valid") {}

  test("POST validate should return 200 Ok if instance is invalid") {}

  test("POST validate should return 404 Not Found if schema is not found") {}

  test("POST validate should return 400 Bad Request is provided instance is not a valid JSON") {}
}

# JSON Schema Registry

## Architecture
The service follows Hexagonal Architecture (aka Ports & Adapters) and Domain Driven Design.
One of the benefits of Hexagonal Architecture is ability to test domain layer in a complete isolation without a need to integrate with external components.
[SchemaRepository](src/main/scala/snowplow/domain/SchemaRepository.scala) is an example of a port which has 2 adapters:
* [PostgresSchemaRepository](src/main/scala/snowplow/storage/PostgresSchemaRepository.scala) - used by a production code
* [InMemorySchemaRepository](src/test/scala/snowplow/InMemorySchemaRepository.scala) - use for testing purposes, e.g. all domain tests are written using in memory adapter

More details re Hexagonal can be found under below links:
* https://alistair.cockburn.us/hexagonal-architecture/
* https://en.wikipedia.org/wiki/Hexagonal_architecture_(software)

## Running locally
```shell
DATABASE_CONNECTION_STRING=jdbc:postgresql://localhost:5432/snowplow \
DATABASE_USERNAME=lukkry \
DATABASE_PASSWORD= \
sbt run
```

## Running test scenarios locally
```shell
cd test-data

# Create a new schema
curl http://localhost:8080/schema/config-schema -X POST -d @schema.json -v | jq
< HTTP/1.1 201 Created
{
  "action": "uploadSchema",
  "id": "config-schema",
  "status": "success"
}

# Try to create a schema with malformed JSON
curl http://localhost:8080/schema/config-schema -X POST -d @malformed.json -v | jq
< HTTP/1.1 400 Bad Request
{
  "action": "uploadSchema",
  "id": "config-schema",
  "status": "error",
  "message": "Invalid JSON"
}

# Re-submit already existing schema
curl http://localhost:8080/schema/config-schema -X POST -d @schema.json -v | jq
< HTTP/1.1 409 Conflict
{
  "action": "uploadSchema",
  "id": "config-schema",
  "status": "error",
  "message": "Schema with provided id already exists"
}

# Retrieve an existing schema
curl http://localhost:8080/schema/config-schema -X GET -v | jq
< HTTP/1.1 200 OK
{
  "type": "object",
  ...
}

# Retrieve a non-existing schema
curl http://localhost:8080/schema/non-existing-schema -X GET -v | jq
< HTTP/1.1 404 Not Found

# Validate a valid instance
curl http://localhost:8080/validate/config-schema -X POST -d @valid-instance-with-nulls.json -v | jq
< HTTP/1.1 200 OK
{
  "action": "validateDocument",
  "id": "config-schema",
  "status": "success"
}

# Validate an invalid instance
curl http://localhost:8080/validate/config-schema -X POST -d @invalid-instance.json -v | jq
< HTTP/1.1 200 OK
{
  "action": "validateDocument",
  "id": "config-schema",
  "status": "error",
  "message": "object has missing required properties ([\"source\"])"
}

# Validate against non-existing schema
curl http://localhost:8080/validate/non-existing-schema -X POST -d @valid-instance.json -v | jq
< HTTP/1.1 404 Not Found

# Validate malformed instance
curl http://localhost:8080/validate/config-schema -X POST -d @malformed.json -v | jq
< HTTP/1.1 400 Bad Request
{
  "action": "validateDocument",
  "id": "config-schema",
  "status": "error",
  "message": "Invalid JSON"
}
```

# Missing features
* Logs
* Metrics
* Run PostgreSQL inside Docker locally

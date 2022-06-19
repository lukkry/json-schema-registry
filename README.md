# JSON Schema Registry

## Architecture
### High level overview


![Untitled-2022-06-19-1620(1)](https://user-images.githubusercontent.com/191244/174489741-b24c09e6-f01b-4f07-a8c5-8788a73b16f6.png)


### Hexagonal Architecture
The service follows Hexagonal Architecture (aka Ports & Adapters) and Domain Driven Design.
One of the benefits of Hexagonal Architecture is ability to test domain layer in a complete isolation without a need to integrate with external components.

[SchemaRepository](src/main/scala/snowplow/domain/SchemaRepository.scala) is an example of a port which has 2 adapters:
* [PostgresSchemaRepository](src/main/scala/snowplow/storage/PostgresSchemaRepository.scala) - used by a production code
* [InMemorySchemaRepository](src/test/scala/snowplow/InMemorySchemaRepository.scala) - use for testing purposes, e.g. all domain tests are written using in memory adapter

![Untitled-2022-06-19-1620(2)](https://user-images.githubusercontent.com/191244/174490028-116c15fe-85bb-4c55-b48a-6285fda51bd9.png)

All technology specific concerns, e.g. PostgreSQL queries, HTTP routes, codecs etc. are part of relevant adapters. This helps to keep domain layer technology agnostic.

More details re Hexagonal can be found under below links:
* https://alistair.cockburn.us/hexagonal-architecture/
* https://en.wikipedia.org/wiki/Hexagonal_architecture_(software)

### Storage
Service has to be able to store and retrieve JSON schemas by ID. There is no need to query schemas by anything else other than their id. 
Any reasonable key/value storage should be a good fit. I decided to go with PostgreSQL as it can be easily modeled as a table with 2 columns and an index. 

```sql
CREATE TABLE json_schema(
  id BIGSERIAL PRIMARY KEY,
  schema_id VARCHAR(255) NOT NULL,
  content JSONB NOT NULL,

  UNIQUE(schema_id)
)
```

Postgres should provide a good mileage before becoming a performance bottleneck. If it ever does, then there are a few mitigation options:
* Start using read replicas. This assumes workload is read heavy and strong consistency between HTTP queries is not required. Schema might not be available to all users immediatelly after POST request completes successfully.
* Shard databases by schema namespace. This is a functional change which requires introducing a concept of "namespace" which groups related schemas. This allows to create multiple Postgres primary instances and load balance namespaces between them. It can suffer from performance issues in case there are namespaces with so many schemas so that they can be hosted on a single primary.

Alternatively other key/value storages might be considered, e.g. S3.

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

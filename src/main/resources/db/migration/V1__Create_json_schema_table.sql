CREATE TABLE json_schema(
  id BIGSERIAL PRIMARY KEY,
  schema_id VARCHAR(255) NOT NULL,
  content JSONB NOT NULL,

  UNIQUE(schema_id)
)
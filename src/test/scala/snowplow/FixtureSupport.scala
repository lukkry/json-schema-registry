package snowplow

object FixtureSupport {
  val rawSchema =
    """
{
  "$schema": "http://json-schema.org/draft-04/schema#",
  "type": "object",
  "properties": {
    "source": {
      "type": "string"
    },
    "destination": {
      "type": "string"
    },
    "timeout": {
      "type": "integer",
      "minimum": 0,
      "maximum": 32767
    },
    "chunks": {
      "type": "object",
      "properties": {
        "size": {
          "type": "integer"
        },
        "number": {
          "type": "integer"
        }
      },
      "required": ["size"]
    }
  },
  "required": ["source", "destination"]
}
"""

  val rawValidInstance =
    """
{
  "source": "/home/alice/image.iso",
  "destination": "/mnt/storage"
}
"""

  val rawValidInstanceWithNull =
    """
{
  "source": "/home/alice/image.iso",
  "destination": "/mnt/storage",
  "timeout": null,
  "chunks": {
    "size": 1024,
    "number": null
  }
}
"""

  val rawInvalidInstance =
    """
{
  "source": null,
  "destination": "/mnt/storage",
  "timeout": null,
  "chunks": {
    "size": 1024,
    "number": null
  }
}
"""
}

package snowplow.domain

import cats.data.NonEmptyList
import com.github.fge.jsonschema.SchemaVersion
import com.github.fge.jsonschema.cfg.ValidationConfiguration
import com.github.fge.jsonschema.core.report.{ListReportProvider, LogLevel}
import com.github.fge.jsonschema.main.JsonSchemaFactory
import io.circe.jackson.circeToJackson

import scala.jdk.CollectionConverters._

object JsonValidator {
  def validate(
      schemaContent: JsonSchemaContent,
      instance: JsonInstance
  ): Either[ValidationError, Unit] = {
    val schemaJsonNode = circeToJackson(schemaContent.value)
    val instanceJsonNode = circeToJackson(instance.valueNoNull)
    val report = validator.validateUnchecked(schemaJsonNode, instanceJsonNode)
    val messages = report.iterator.asScala.toList
      .map(_.getMessage)

    messages match {
      case h :: t => Left(ValidationError.InvalidInstance(NonEmptyList.of(h, t: _*)))
      case Nil    => Right(())
    }
  }

  private val validator = {
    val reportProvider = new ListReportProvider(LogLevel.ERROR, LogLevel.NONE)
    val config = ValidationConfiguration.newBuilder
      .setDefaultVersion(SchemaVersion.DRAFTV4)
      .freeze
    val factory = JsonSchemaFactory
      .newBuilder()
      .setReportProvider(reportProvider)
      .setValidationConfiguration(config)
      .freeze()
    factory.getValidator
  }
}

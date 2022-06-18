ThisBuild / scalaVersion := "2.13.8"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "com.snowplow"
ThisBuild / organizationName := "snowplow"

val circeVersion = "0.14.2"
val http4sVersion = "0.23.12"

lazy val root = (project in file("."))
  .settings(
    name := "json-schema-registry",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core" % "2.7.0",
      "org.typelevel" %% "cats-effect" % "3.3.12",
      "com.github.java-json-tools" % "json-schema-validator" % "2.2.14",
      "com.github.java-json-tools" % "json-schema-core" % "1.2.14",
      "io.circe" %% "circe-core" % circeVersion,
      "io.circe" %% "circe-parser" % circeVersion,
      "io.circe" %% "circe-literal" % circeVersion,
      "io.circe" % "circe-jackson28_2.13" % "0.14.0",
      "io.circe" %% "circe-jackson28" % "0.14.0",
      "com.fasterxml.jackson.core" % "jackson-databind" % "2.11.0",
      "org.http4s" %% "http4s-dsl" % http4sVersion,
      "org.http4s" %% "http4s-ember-server" % http4sVersion,
      "org.http4s" %% "http4s-ember-client" % http4sVersion,
      "org.http4s" %% "http4s-circe" % http4sVersion,
      "org.scalameta" %% "munit" % "0.7.29" % Test,
      "org.typelevel" %% "munit-cats-effect-3" % "1.0.7" % Test
    )
  )

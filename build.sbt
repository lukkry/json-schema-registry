ThisBuild / scalaVersion     := "2.13.8"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.snowplow"
ThisBuild / organizationName := "snowplow"

val circeVersion = "0.14.2"

lazy val root = (project in file("."))
  .settings(
    name := "json-schema-registry",
    libraryDependencies ++= Seq(
      "com.github.java-json-tools" % "json-schema-validator" % "2.2.14",
      "io.circe" %% "circe-core" % circeVersion,
      "io.circe" %% "circe-parser" % circeVersion,
      "io.circe" % "circe-jackson28_2.13" % "0.14.0",
      "org.typelevel" %% "cats-effect" % "3.3.12",

      "org.scalameta" %% "munit" % "0.7.29" % Test
    )
  )
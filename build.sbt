import Dependencies._

ThisBuild / scalaVersion := "2.13.2"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "com.example"
ThisBuild / organizationName := "example"

//-XX:MetaspaceSize=
scalacOptions ++= Seq(
  "-deprecation",
  "-feature",
  "-Ywarn-unused:imports",
  "-language:postfixOps"
)

lazy val kindProjectorVersion = "0.11.0"

addCompilerPlugin(
  ("org.typelevel" %% "kind-projector" % kindProjectorVersion).cross(
    CrossVersion.full
  )
)

addCommandAlias("build", "prepare; test")
addCommandAlias("prepare", "fix; fmt")
addCommandAlias("check", "fixCheck; fmtCheck")
addCommandAlias("fix", "all compile:scalafix test:scalafix")
addCommandAlias(
  "fixCheck",
  "compile:scalafix --check; test:scalafix --check"
)
addCommandAlias("fmt", "all scalafmtSbt scalafmt test:scalafmt")
addCommandAlias(
  "fmtCheck",
  "all scalafmtSbtCheck scalafmtCheck test:scalafmtCheck"
)

lazy val root = (project in file("."))
  .settings(
    name := "zio-example",
    libraryDependencies ++= Seq(
      catsEffect,
      akkaHttp,
      akkaStream,
      circeCore,
      circeGeneric,
      akkaHttpCirce,
      postgresql,
      druidDataSource,
      zio,
      zioIntRS,
      zioConfig,
      zioConfigMagnolia,
      zioConfigTypeSafe,
      pureConfig,
      zioInteropCats,
      zioLogging,
      zioLoggingSlf4j,
      flywayDb,
      doobieHikari,
      zioAkkaHttpInterop,
      doobie,
      doobieQuill,
      logback,
      akkaSlf4j,
      jedis,
      redis4cat,
      scalaTest        % Test,
      scalaTestMockito % Test,
      akkaTestkit      % Test,
      akkaHttpTestkit  % Test
    )
  )

// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.

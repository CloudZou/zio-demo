import sbt._

object Dependencies {

  val akkaVersion     = "2.6.8"
  val akkaHttpVersion = "10.1.12"
  val zioVersion        = "1.0.0-RC21"
  val zioLoggingVersion = "0.3.1"
  val zioConfigVersion  = "1.0.0-RC20"
  val DoobieVersion     = "0.8.6"
  val CirceVersion      = "0.12.3"

  lazy val akkaHttp         = "com.typesafe.akka"  %% "akka-http"            % akkaHttpVersion
  lazy val akkaStream       = "com.typesafe.akka"  %% "akka-stream"          % akkaVersion
  lazy val akkaSlf4j        = "com.typesafe.akka"  %% "akka-slf4j"           % akkaVersion
  lazy val akkaHttpTestkit  = "com.typesafe.akka"  %% "akka-http-testkit"    % akkaHttpVersion
  lazy val akkaTestkit      = "com.typesafe.akka"  %% "akka-testkit"         % akkaVersion
  lazy val scalaTest        = "org.scalatest"      %% "scalatest"            % "3.1.0"
  lazy val scalaTestMockito = "org.scalatestplus"  %% "mockito-1-10"         % "3.2.0.0-M2"
  lazy val logback          = "ch.qos.logback"     %  "logback-classic"      % "1.2.3"
  lazy val postgresql       = "org.postgresql"     %  "postgresql"           % "42.2.5"
  lazy val doobieQuill      = "org.tpolecat"       %% "doobie-quill"         % DoobieVersion
  lazy val doobie           = "org.tpolecat"          %% "doobie-core"          % DoobieVersion
  lazy val pureConfig       = "com.github.pureconfig"    %% "pureconfig"          % "0.12.1"

  lazy val circeCore        = "io.circe"                 %% "circe-core"          % CirceVersion
  lazy val circeGeneric     = "io.circe"                 %% "circe-generic"       % CirceVersion
  lazy val akkaHttpCirce    = "de.heikoseeberger" %% "akka-http-circe"   % "1.33.0"

  lazy val zio        = "dev.zio"            %% "zio"                  % zioVersion
  lazy val zioIntRS   = "dev.zio"            %% "zio-interop-reactivestreams" % "1.0.3.5-RC2"
  lazy val zioInteropCats   = "dev.zio" %% "zio-interop-cats" % "2.1.3.0-RC16"
  lazy val zioLogging       = "dev.zio"                  %% "zio-logging"         % zioLoggingVersion
  lazy val zioLoggingSlf4j  = "dev.zio"                  %% "zio-logging-slf4j"   % zioLoggingVersion
  lazy val zioConfig        = "dev.zio"               %% "zio-config"                  % "1.0.0-RC20"
  lazy val flywayDb         = "org.flywaydb"             % "flyway-core"          % "5.2.4"
  lazy val doobieHikari     = "org.tpolecat"             %% "doobie-hikari"       % DoobieVersion
  lazy val zioAkkaHttpInterop = "io.scalac"             %% "zio-akka-http-interop"       % "0.1.0"
  lazy val zioConfigMagnolia = "dev.zio"               %% "zio-config-magnolia"         % "1.0.0-RC20"
  lazy val zioConfigTypeSafe = "dev.zio"               %% "zio-config-typesafe"         % "1.0.0-RC20"

  lazy val druidDataSource   = "com.alibaba" % "druid" % "1.1.23"

  lazy val catsEffect = "org.typelevel" %% "cats-effect" % "2.1.4"
//  lazy val doobiePostgresql = "org.tpolecat" %% "doobie-postgres" % "0.8.8"
}
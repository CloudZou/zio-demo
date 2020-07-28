package example

import akka.actor.ActorSystem
import akka.http.interop.HttpServer
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import com.typesafe.config.{Config, ConfigFactory}
import example.Api.Api
import example.domain.{DoobieProductRepository, ProductRepository}
import example.layer.Layers

import scala.io.StdIn
import example.layer.Layers.live
import example.layer.config.{AppConfigProvider, ConfigProvider, DbConfigProvider}
import zio.clock.Clock
import zio.console.{Console, putStrLn}
import zio.logging.{LogAnnotation, Logging}
import zio.logging.slf4j.Slf4jLogger
import zio._
import zio.config.typesafe.TypesafeConfig

object Boot extends zio.App {
  def run(args: List[String]): ZIO[zio.ZEnv, Nothing, ExitCode] =
    ZIO(ConfigFactory.load.resolve)
      .flatMap(rawConfig => program.provideCustomLayer(prepareEnvironment(rawConfig)))
      .exitCode

  private val program: ZIO[HttpServer with Console, Throwable, Unit] =
    HttpServer.start.tapM(_ => putStrLn(s"Server online.")).useForever

  private def prepareEnvironment(rawConfig: Config): ZLayer[blocking.Blocking, Throwable, HttpServer] = {
    val configLayer = TypesafeConfig.fromTypesafeConfig(rawConfig, AppConfig.descriptor)
    val actorSystemLayer: TaskLayer[Has[ActorSystem]] = ZLayer.fromManaged {
      ZManaged.make(ZIO(ActorSystem("zio-akka-quickstart-system")))(s => ZIO.fromFuture(_ => s.terminate()).either)
    }

    val apiConfigLayer = configLayer.map(c => Has(c.get.api))

    val loggingLayer: ULayer[Logging] = Slf4jLogger.make { (context, message) =>
      val logFormat = "[correlation-id = %s] %s"
      val correlationId = LogAnnotation.CorrelationId.render(
        context.get(LogAnnotation.CorrelationId)
      )
      logFormat.format(correlationId, message)
    }
    val apiLayer = (apiConfigLayer ++ live.appLayer ++ actorSystemLayer) >>> Api.live

    val routesLayer: ZLayer[Api, Nothing, Has[Route]] =
      ZLayer.fromService[Api.Service, Route](api => api.routes)

    (actorSystemLayer ++ apiConfigLayer ++ (apiLayer  >>> routesLayer)) >>> HttpServer.live
  }
}

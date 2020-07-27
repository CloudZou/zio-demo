package example

import akka.actor.ActorSystem
import akka.http.interop.HttpServer
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import com.typesafe.config.{Config, ConfigFactory}

import scala.io.StdIn
import example.layer.Layers.live
import example.layer.config.ConfigProvider
import zio.clock.Clock
import zio.console.{Console, putStrLn}
import zio.logging.{LogAnnotation, Logging}
import zio.logging.slf4j.Slf4jLogger
import zio._
import zio.logging.Logging.Logging

object Boot extends zio.App {
  def run(args: List[String]): ZIO[zio.ZEnv, Nothing, ExitCode] =
    ZIO(ConfigFactory.load.resolve)
      .flatMap(rawConfig => program.provideCustomLayer(prepareEnvironment(rawConfig)))
      .exitCode

  private val program: ZIO[HttpServer with Console, Throwable, Unit] =
    HttpServer.start.tapM(_ => putStrLn(s"Server online.")).useForever

  private def prepareEnvironment(rawConfig: Config): TaskLayer[HttpServer] = {

    // using raw config since it's recommended and the simplest to work with slick
    val dbConfigLayer = ZLayer.fromEffect(ZIO(rawConfig.getConfig("db")))
    val dbBackendLayer = ZLayer.succeed(slick.jdbc.H2Profile.backend)

    val actorSystemLayer: TaskLayer[Has[ActorSystem]] = ZLayer.fromManaged {
      ZManaged.make(ZIO(ActorSystem("zio-akka-quickstart-system")))(s => ZIO.fromFuture(_ => s.terminate()).either)
    }

    val loggingLayer: ULayer[Logging] = Slf4jLogger.make { (context, message) =>
      val logFormat = "[correlation-id = %s] %s"
      val correlationId = LogAnnotation.CorrelationId.render(
        context.get(LogAnnotation.CorrelationId)
      )
      logFormat.format(correlationId, message)
    }

    val dbLayer: TaskLayer[ItemRepository] =
      (((dbConfigLayer ++ dbBackendLayer) >>> DatabaseProvider.live) ++ loggingLayer) >>> SlickItemRepository.live

    val apiLayer: TaskLayer[Api] = (apiConfigLayer ++ dbLayer ++ actorSystemLayer) >>> Api.live

    val graphQLApiLayer: TaskLayer[GraphQLApi] =
      (dbLayer ++ actorSystemLayer ++ loggingLayer ++ Clock.live) >>> GraphQLApi.live

    val routesLayer: ZLayer[Api, Nothing, Has[Route]] =
      ZLayer.fromServices[Api.Service, , Route]{ (api, gApi) => api.routes ~ gApi.routes }
    (actorSystemLayer ++ apiConfigLayer ++ (apiLayer  ++ graphQLApiLayer >>> routesLayer)) >>> HttpServer.live
  }
}

package example

import akka.actor.ActorSystem
import akka.http.interop.{HttpServer, ZIOSupport}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.headers.Location
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.{Directives, Route}
import akka.http.scaladsl.server.Directives._
import example.application.ApplicationService
import example.domain._
import example.infrastructure._
import example.interop.akka.{ErrorMapper, ZioSupport}
import example.layer.Layers
import example.layer.Layers.AppEnv
import spray.json._
import zio.{Has, Runtime, URIO, ZIO, ZLayer}
import zio.blocking.Blocking
import zio.config.Config
import zio.internal.Platform

import scala.concurrent.ExecutionContext

case class CreateAssetRequest(name: String, price: BigDecimal)
case class UpdateAssetRequest(name: String, price: BigDecimal)
case class UpdatePortfolioRequest(assetId: Long, amount: BigDecimal)

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val productFormat = jsonFormat7(Product)
}

object Api {
  type Api = Has[Api.Service]

  trait Service {
    def routes: Route
  }
  val live: ZLayer[Config[HttpServer.Config] with Has[ActorSystem] with ProductRepository, Nothing, Api] = ZLayer.fromFunction(env =>
    new Service with JsonSupport with ZIOSupport {
      def routes: Route = productRoute

      val productRoute =
        pathPrefix(("product")) {
          pathEnd {
            get {
              complete(ApplicationService.getProducts.provide(env))
            }
          }
        }
    })

  val routes: URIO[Api, Route] = ZIO.access[Api](a => Route.seal(a.get.routes))
}
//
//class Api(env: AppEnv, port: Int)(implicit ec: ExecutionContext) extends JsonSupport with ZioSupport {
//
//  override val environment: Unit = Runtime.default.environment
//
//  override val platform: Platform = Runtime.default.platform
//
//  lazy val route =  productRoute
//
//  implicit val domainErrorMapper = new ErrorMapper[DomainError] {
//    def toHttpResponse(e: DomainError): HttpResponse = e match {
//      case RepositoryError(cause) => HttpResponse(StatusCodes.InternalServerError)
//      case ValidationError(msg)   => HttpResponse(StatusCodes.BadRequest)
//    }
//  }


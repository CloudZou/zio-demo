package example

import akka.actor.ActorSystem
import akka.http.interop.ZIOSupport
import akka.http.scaladsl.server.{ Directives, Route }
import akka.http.scaladsl.server.Directives._
import example.application.ApplicationService
import example.domain._
import example.domain.Product._
import zio.{ Has, Runtime, URIO, ZIO, ZLayer }

import de.heikoseeberger.akkahttpcirce.ErrorAccumulatingCirceSupport._

object Api {
  type Api = Has[Api.Service]

  trait Service {
    def routes: Route
  }

  val live: ZLayer[Has[ActorSystem] with ProductRepository, Nothing, Api] =
    ZLayer.fromFunction(env =>
      new Service with ZIOSupport {
        def routes: Route = productRoute

        val productRoute =
          pathPrefix("product") {
            pathEnd {
              get {
                complete(ApplicationService.getProducts.provide(env))
              }
            }
          }
      }
    )

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

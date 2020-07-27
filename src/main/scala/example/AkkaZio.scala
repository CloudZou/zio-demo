package example

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import example.domain.ProductRepository
import example.layer.Layers
import example.layer.Layers.{AppEnv, live}
import zio._
import zio.console._

import scala.concurrent.ExecutionContextExecutor
import scala.io.StdIn

trait Helpers {
  implicit class ZioExtension[R, E, A](z: ZIO[R, E, A]) {
    def exited: ZIO[R, Nothing, Int] = z.fold(_ => 1, _ => 0)
  }
}

object Akka extends App with Helpers {
  override def run(args: List[String]): ZIO[blocking.Blocking, Nothing, Int] = {
    (for {
      as <- Managed.make(Task(ActorSystem("fabio-akka-test")))(sys => Task.fromFuture(_ => sys.terminate()).ignore).use {
        actorSystem =>
          implicit val system: ActorSystem = actorSystem
          implicit val materializer: ActorMaterializer = ActorMaterializer()
          implicit val executionContext: ExecutionContextExecutor = system.dispatcher
          val route =
            path("hello") {
              get {
                complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Say hello to akka-http</h1>"))
              }
            }
          val port = 8080
          val api = new Api(ZIO.accessM[AppEnv](_.get), port)
          val tt = Http().bindAndHandle(route, "0.0.0.0", 8080)
          ZIO.fromFuture(executionContext => tt).forever
      }.provideSomeLayer(ProductRepository.withTracing(Layers.live.appLayer))
    } yield 0).exited
  }
}
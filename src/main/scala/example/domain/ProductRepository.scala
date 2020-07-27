package example.domain

import zio.logging.Logging
import zio.logging.Logging.Logging
import zio.{IO, Task, UIO, ZLayer}

import scala.concurrent.ExecutionContext

object ProductRepository {
  trait Service {
    def getAll: UIO[List[Product]]
  }

  def withTracing[RIn, ROut <: ProductRepository with Logging, E](
                                                                   layer: ZLayer[RIn, E, ROut]
                                                                 ): ZLayer[RIn, E, ROut] =
    layer >>> ZLayer.fromFunctionMany[ROut, ROut] { env =>
      val logging                = env.get[Logging.Service]
      def trace(call: => String) = logging.logger.trace(s"ProductRepository.$call")

      env.update[ProductRepository.Service] { service =>
        new Service {
          val getAll: UIO[List[Product]] =
            trace("getAll") *> service.getAll
        }
      }
    }
}
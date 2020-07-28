package example.domain

import zio.logging.Logging
import zio.{IO, Task, UIO, ZLayer}

import scala.concurrent.ExecutionContext

object ProductRepository {
  trait Service {
    def getAll: UIO[List[Product]]
  }

}
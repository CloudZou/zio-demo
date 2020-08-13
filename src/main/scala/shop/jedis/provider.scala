package shop.jedis

import cats.effect.{Async, Blocker, ContextShift, Resource}
import com.typesafe.config.ConfigFactory
import io.lettuce.core.{RedisClient, RedisURI}
import io.lettuce.core.api.StatefulRedisConnection
import shop.jedis.provider.RedisManaged
import shop.jedis.provider.RedisManaged.RedisConnection
import zio.blocking.Blocking
import zio.{ExitCode, Has, Managed, URIO, ZIO, ZLayer, ZManaged}
import zio._
import zio.interop.catz._

import scala.concurrent.ExecutionContext

object provider {
  object RedisManaged {
    type RedisConnection = StatefulRedisConnection[String, String]
    val layer: ZLayer[Blocking, Throwable, Has[RedisConnection]] = ZLayer.fromManaged(
      Managed.make(Task.effect( RedisClient.create(RedisURI.create("127.0.0.1", 6379)).connect()))( conn => UIO.succeed(conn.close))
    )
  }
}

object ProviderService extends BootstrapRuntime {
  import zio.console._

  final def main(args0: Array[String]): Unit                 =
    try sys.exit {
      unsafeRun(
        for {
          fiber  <- run(args0.toList).fork
          _      <- IO.effectTotal(
                      java.lang.Runtime.getRuntime.addShutdownHook(new Thread {
                        override def run() = {
                          val _ = unsafeRunSync(fiber.interrupt)
                        }
                      })
                    )
          result <- fiber.join
          _      <- fiber.interrupt
        } yield result.code
      )
    } catch { case _: SecurityException => }

  val program3: ZIO[Console with Has[RedisConnection], Throwable, Unit] =
    for {
      i1 <- ZIO.accessM[Has[RedisConnection]](t => {
        Task.effect{
          val s = t.get.sync().get("test")
          println(s)
        }
      })
      _ <- putStrLn("program3 result:" +i1)
    } yield i1
//
//
  val program =  for {
    fiber1 <- program3.fork
    fiber2 <- program3.fork
    fiber3 <- program3.fork
    fiber4 <- program3.fork
    fiber5 <- program3.fork
    fiber6 <- program3.fork
    fiber7 <- program3.fork
    fiber8 <- program3.fork
    fiber9 <- program3.fork
    fiber10 <- program3.fork
    _     <- fiber1.join
    _     <- fiber2.join
    _     <- fiber3.join
    _     <- fiber4.join
    _     <- fiber5.join
    _     <- fiber6.join
    _     <- fiber7.join
    _     <- fiber8.join
    _     <- fiber9.join
    _     <- fiber10.join
  } yield ()


  def run(args: List[String]): ZIO[zio.ZEnv, Nothing, ExitCode] =
    ZIO(ConfigFactory.load.resolve)
      .flatMap(_ => program.provideCustomLayer(RedisManaged.layer))
      .exitCode
}

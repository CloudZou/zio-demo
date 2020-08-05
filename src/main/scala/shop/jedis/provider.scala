package shop.jedis

import cats.effect.{ Async, Blocker, ContextShift, Resource }
import com.typesafe.config.ConfigFactory
import org.apache.commons.pool2.impl.GenericObjectPoolConfig
import redis.clients.jedis.{ Jedis, JedisPool }
import shop.jedis.provider.JedisManaged
import zio.blocking.Blocking
import zio.{ ExitCode, Has, Managed, URIO, ZIO, ZLayer, ZManaged }
import zio._
import zio.interop.catz._

import scala.concurrent.ExecutionContext

object provider {

  object JedisManaged {

    val layer: ZLayer[Blocking, Throwable, Has[Jedis]] = ZLayer.fromManaged(
      ZIO.runtime[Blocking].toManaged_.flatMap { implicit rt =>
        for {
          transactEC    <- Managed.succeed(
                             rt.environment
                               .get[Blocking.Service]
                               .blockingExecutor
                               .asEC
                           )
          connectEC      = rt.platform.executor.asEC
          jedisResource <- jedisResource[Task](
                             new JedisPool,
                             connectEC,
                             Blocker.liftExecutionContext(transactEC)
                           ).toManaged
        } yield jedisResource
      }
    )
  }

  def jedisResource[M[_]](
    jedisPool: JedisPool,
    connectEC: ExecutionContext,
    blocker: Blocker
  )(implicit
    ev: Async[M],
    cs: ContextShift[M]
  ): Resource[M, Jedis] = {
    val acquire           = cs.evalOn(connectEC)(ev.delay(jedisPool.getResource))
    def release(c: Jedis) = blocker.blockOn(ev.delay(c.close()))
    Resource.make(acquire)(release)
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

  val program: ZIO[Console with Has[Jedis], Throwable, Unit] =
    for {
      i <- ZIO.accessM[Has[Jedis]] { p: Has[Jedis] =>
             val ret             = ZIO.effect {
               val xx = p.get.get("test")
               println(xx)
               xx
             }
             val list: List[Int] = (1 to 100000).toList
             val kk              = ZIO.foldLeft(list)("")((z, item) => ret)
             kk
           }
      _ <- putStrLn("xdd")
    } yield ()

  def run(args: List[String]): ZIO[zio.ZEnv, Nothing, ExitCode] =
    ZIO(ConfigFactory.load.resolve)
      .flatMap(rawConfig => program.provideCustomLayer(JedisManaged.layer))
      .exitCode
}

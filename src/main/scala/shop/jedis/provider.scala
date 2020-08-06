package shop.jedis

import cats.effect.{Async, Blocker, ContextShift, Resource}
import com.typesafe.config.ConfigFactory
import org.apache.commons.pool2.impl.GenericObjectPoolConfig
import redis.clients.jedis.{Jedis, JedisPool}
import shop.jedis.provider.{JedisManaged, JedisPoolManaged}
import zio.blocking.Blocking
import zio.{ExitCode, Has, Managed, URIO, ZIO, ZLayer, ZManaged}
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


  object JedisPoolManaged {
    val jedisPool = new JedisPool
    val acquire: Resource[Task, Jedis] = Resource.make(Task.effect( jedisPool.getResource))((jedis: Jedis) => Task.effect(jedis.close))
    val live: ZLayer[Any, Throwable, Has[Resource[Task, Jedis]]] = ZLayer.fromManaged{
      Managed.effectTotal(acquire)
    }
  }

  def jedisResource[M[_]](
    jedisPool: JedisPool,
    connectEC: ExecutionContext,
    blocker: Blocker
  )(implicit
    ev: Async[M],
    cs: ContextShift[M]
  ): Resource[M, Jedis] = {
    val acquire           = cs.evalOn(connectEC)(ev.delay {
      println("get Resource")
      jedisPool.getResource
    })
    def release(c: Jedis) = blocker.blockOn(ev.delay{
      println("close redis connection")
      c.close
    })
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

  val program1: ZIO[Console with Has[Jedis], Throwable, Unit] =
    for {
      i1 <- ZIO.accessM[Has[Jedis]](t => ZIO.effect{
        println("program1")
        val s1 = t.get.get("test")
        println("s1:"+ s1)
      })
      _ <- putStrLn("program1 result")
    } yield ()

  val program2: ZIO[Console with Has[Jedis], Throwable, Unit] =
    for {
      i1 <- ZIO.accessM[Has[Jedis]](t => ZIO.effect{
        println("program2")
        val s2 = t.get.get("test")
        println("s2:"+ s2)
      })
      _ <- putStrLn("program2 result")
    } yield ()

  val program3: ZIO[Console with Has[Resource[Task, Jedis]], Throwable, Unit] =
    for {
      i1 <- ZIO.accessM[Has[Resource[Task, Jedis]]](t => t.get.use{ r =>
        val s = r.get("test")
        println(s)
        Task.effect(s)
      })
      _ <- putStrLn("program3 result:" +i1)
    } yield i1

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
      .flatMap(rawConfig => program.provideCustomLayer(JedisPoolManaged.live))
      .exitCode
}

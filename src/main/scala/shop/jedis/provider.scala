package shop.jedis

import com.typesafe.config.ConfigFactory
import org.apache.commons.pool2.impl.GenericObjectPoolConfig
import redis.clients.jedis.{Jedis, JedisPool}
import shop.jedis.provider.{JedisConnectionProvider, JedisManaged, JedisPoolProvider}
import zio.blocking.Blocking
import zio.{ExitCode, Has, Managed, URIO, ZIO, ZLayer, ZManaged}
import zio._

object provider {
  type JedisPoolProvider = Has[JedisPool]

  object JedisPoolProvider {
    val live: ZLayer[Any, Throwable, JedisPoolProvider] =
      ZLayer.fromFunction(t => {
        val c = new GenericObjectPoolConfig()
        c.setMaxWaitMillis(1000)
        new JedisPool(c)
      })
  }

  type JedisManaged = Managed[Throwable, Jedis]
  object JedisConnectionProvider {
    val live: ZLayer[JedisPoolProvider, Throwable, Has[JedisManaged]] =
      ZLayer.fromManaged(
        ZManaged.fromFunction(r => ZManaged.effect(r.get.getResource))
      )
  }
}

object ProviderService extends BootstrapRuntime{
  import zio.console._

  final def main(args0: Array[String]): Unit =
    try sys.exit {
        unsafeRun(
          for {
            fiber <- run(args0.toList).fork
            _ <- IO.effectTotal(java.lang.Runtime.getRuntime.addShutdownHook(new Thread {
              override def run() = {
                val _ = unsafeRunSync(fiber.interrupt)
              }
            }))
            result <- fiber.join
            _ <- fiber.interrupt
          } yield result.code
        )
    }
    catch { case _: SecurityException => }

  val program: ZIO[Console with Has[JedisManaged], Throwable, Unit] =
    for {
      i <- ZIO.accessM[Has[JedisManaged]] { p: Has[JedisManaged] =>
        //在这里用ZManged的use 似乎当后面运行ZIO.foldLeft在副作用未生效之前就已经占用了resource资源，导致运行的时候，如果list长度大于pool的size后，直接导致从pool里面获取不到resource产生异常了。
        // 所以ZIO的pool 设计似乎不能这么用。。
        val s = p.get.use(jedis => ZIO.effect {
          val ret = jedis.get("test")
          ret
        })
        val list: List[Int] = (1 to 9).toList
        val kk = ZIO.foldLeft(list)("")( (z, item) => s)
        kk
      }
      _ <- putStrLn("xdd")
    } yield ()

  val layer: ZLayer[Blocking, Throwable, Has[JedisManaged]] =
    JedisPoolProvider.live >>> JedisConnectionProvider.live


   def run(args: List[String]): ZIO[zio.ZEnv, Nothing, ExitCode] = {
    ZIO(ConfigFactory.load.resolve).flatMap(rawConfig =>
      program.provideCustomLayer(layer)
    ).exitCode
  }
}
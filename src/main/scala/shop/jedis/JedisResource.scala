package shop.jedis

import cats.effect.Sync
import redis.clients.jedis.JedisPool
import cats.effect.Resource
import cats.effect.IO
import zio._
import redis.clients.jedis.Jedis

object JedisPoolResource {
  trait Service {
    def getJedisPool: Task[JedisPool]
  }
}

class DefaultJedisPoolService extends JedisPoolResource.Service {

  override def getJedisPool: Task[JedisPool] =
    Task.fromFunction(_ => new JedisPool)
}

object DefaultJedisPoolResource {

  val live: ZLayer[Has[Any], Throwable, JedisPoolService] =
    ZLayer.fromService { t: Any =>
      new DefaultJedisPoolService
    }
}

object JedisConnectionResource {

  trait Service {
    def getJedis: ZIO[JedisPoolService, Throwable, JedisAutoClosable]
  }
}

class DefaultJedisConnectionService extends JedisConnectionResource.Service {

  override def getJedis: ZIO[JedisPoolService, Throwable, JedisAutoClosable] =
    for {
      jedisPool <- ZIO.accessM[JedisPoolService](_.get.getJedisPool)
    } yield createJedisConnectionResource(jedisPool.getResource())

  private def createJedisConnectionResource(
    factory: => Jedis
  ): JedisAutoClosable = {
    val alloc = Sync[IO].delay(factory)
    val free  = (ds: Jedis) => Sync[IO].delay(ds.close())
    Resource.make(alloc)(free)
  }
}

object DefaultJedisConnectionResource {

  val live: ZLayer[JedisPoolService, Throwable, JedisConnectionService] =
    ZLayer.fromEffect {
      ZIO.effect(new DefaultJedisConnectionService)
    }
}

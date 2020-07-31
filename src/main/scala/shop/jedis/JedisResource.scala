package shop.jedis

import cats.effect.Sync
import redis.clients.jedis.JedisPool
import cats.effect.Resource
import cats.effect.IO
import zio._
import redis.clients.jedis.Jedis
import shop.jedis.JedisPoolResource.ServiceImpl

object JedisPoolResource {
  trait Service {
    def getJedisPool: Task[JedisPool]
  }

  class ServiceImpl extends Service {
    override def getJedisPool: Task[JedisPool] = Task.fromFunction(_ => new JedisPool)
  }
}

object DefaultJedisPoolResource {

  val live: ZLayer[Any, Throwable, JedisPoolService] =
    ZLayer.fromEffect(ZIO.effect(new ServiceImpl))
}

object JedisConnectionResource {

  trait Service {
    def getJedis(jedisPool: JedisPool): Task[JedisAutoClosable]
  }

  class ServiceImpl extends Service {
    override def getJedis(jedisPool: JedisPool): Task[shop.jedis.JedisAutoClosable] = Task.fromFunction(_ => createJedisConnectionResource(jedisPool.getResource()))

    private def createJedisConnectionResource(
    factory: => Jedis
  ): JedisAutoClosable = {
    val alloc = Sync[IO].delay(factory)
    val free  = (ds: Jedis) => Sync[IO].delay(ds.close())
    Resource.make(alloc)(free)
  }
  }
}

object DefaultJedisConnectionResource {

  val live: ZLayer[JedisPoolService, Throwable, JedisConnectionService] =
    ZLayer.fromEffect {
      ZIO.effect(new JedisConnectionResource.ServiceImpl)
    }
}

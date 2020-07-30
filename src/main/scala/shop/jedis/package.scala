package shop

import zio.Has
import cats.effect.Resource
import cats.effect.IO
import redis.clients.jedis.Jedis
import zio.ZLayer

package object jedis {
  type JedisPoolService       = Has[JedisPoolResource.Service]
  type JedisConnectionService = Has[JedisConnectionResource.Service]
  type JedisAutoClosable       = Resource[IO, Jedis]


  val jedisLayer: ZLayer[Any, Throwable, JedisAutoClosable] =
    DefaultJedisPoolResource.live ++ DefaultJedisConnectionResource.live 
}

package shop.jedis

import zio.ZIO
import zio.ZLayer

object jedisTestService {

  def test: ZIO[
    JedisPoolService with JedisConnectionService,
    Throwable,
    JedisAutoClosable
  ] =
    for {
      jedisPool <- ZIO.accessM[JedisPoolService](_.get.getJedisPool)
      jedis     <- ZIO.accessM[JedisConnectionService](_.get.getJedis(jedisPool))
    } yield jedis

  def main(): Unit = {}
}

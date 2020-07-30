package shop.jedis

import zio.ZIO

object jedisTestService {
    def test: ZIO[JedisConnectionService, Throwable, Int] = {
        for {
            jedis <- ZIO.accessM[JedisConnectionService](_.get.getJedis)
            i = 0
        } yield i
    }
}
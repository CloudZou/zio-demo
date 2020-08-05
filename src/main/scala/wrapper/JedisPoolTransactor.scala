package wrapper

import cats.effect._
import redis.clients.jedis.JedisPool

import scala.concurrent.ExecutionContext

object JedisPoolTransactor {
  type JedisPoolTransactor[M[F_]] = Transactor.Aux[M, JedisPool]

  private def createJedisPoolResource[M[_]: Sync](
    factory: => JedisPool
  ): Resource[M, JedisPool] = {
    val alloc = Sync[M].delay(factory)
    val free  = (jedisPool: JedisPool) => Sync[M].delay(jedisPool.close())
    Resource.make(alloc)(free)
  }

  def newJedisPoolTransactor[M[_]: Async: ContextShift](
    connectEC: ExecutionContext,
    blocker: Blocker
  ): Resource[M, JedisPoolTransactor[M]] =
    createJedisPoolResource(new JedisPool)
      .map(Transactor.fromJedisPool[M](_, connectEC, blocker))
}

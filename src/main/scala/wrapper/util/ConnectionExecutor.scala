package wrapper.util

import cats.data.Kleisli
import cats.effect.{ Async, Blocker, Bracket, ContextShift, Resource }
import cats.~>
import redis.clients.jedis.{ Jedis, JedisPool }
import wrapper.free.KleisliInterpreter
import wrapper.free.connection.ConnectionOp
import wrapper.util.ConnectionExecutor.Interpreter

import scala.concurrent.ExecutionContext

object ConnectionExecutor {
  type Interpreter[M[_]] = ConnectionOp ~> Kleisli[M, Jedis, *]

  type Aux[M[_], A0] = ConnectionExecutor[M] { type A = A0 }

  def apply[M[_], A0](
    kernel0: A0,
    connect0: A0 => Resource[M, Jedis],
    interpret0: Interpreter[M]
  ): ConnectionExecutor.Aux[M, A0] =
    new ConnectionExecutor[M] {
      type A = A0
      val kernel    = kernel0
      val connect   = connect0
      val interpret = interpret0
    }

  object fromPool {
    def apply[M[_]] = new FromPoolUnapplied[M]

    class FromPoolUnapplied[M[_]] {

      def apply(
        jedisPool: JedisPool,
        connecEC: ExecutionContext,
        blocker: Blocker
      )(implicit
        ev: Async[M],
        cs: ContextShift[M]
      ): ConnectionExecutor.Aux[M, JedisPool] = {
        val connect = (jedisPool: JedisPool) => {
          val acquire = cs.evalOn(connecEC)(ev.delay(jedisPool.getResource))
          val release = (jedis: Jedis) =>
            blocker.blockOn(ev.delay(jedis.close()))
          Resource.make(acquire)(release)
        }
        val interp  = KleisliInterpreter[M](blocker).ConnectionInterpreter
        ConnectionExecutor(jedisPool, connect, interp)
      }
    }
  }
}

sealed abstract class ConnectionExecutor[M[_]] { self =>
  import wrapper.free.connection._

  type A
  def kernel: A
  def connect: A => Resource[M, Jedis]
  def interpret: Interpreter[M]

  def exec(implicit ev: Bracket[M, Throwable]): ConnectionIO ~> M =
    λ[ConnectionIO ~> M] { f =>
      connect(kernel).use { conn =>
        f.foldMap(interpret).run(conn)
      }
    }
}

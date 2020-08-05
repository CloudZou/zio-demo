package wrapper

import cats.effect.Bracket
import wrapper.JedisConnection.JedisConnectionIO

class JedisConnectionIOOps[A](ma: JedisConnectionIO[A]) {

  def transact[M[_]](
    xa: Transactor[M]
  )(implicit
    ev: Bracket[M, Throwable]
  ): M[A] = xa.trans.apply(ma)
}

trait ToJedisConnectionIOOps {

  implicit def toJedisConnectionIOOps[A](
    ma: JedisConnectionIO[A]
  ): JedisConnectionIOOps[A] =
    new JedisConnectionIOOps(ma)
}

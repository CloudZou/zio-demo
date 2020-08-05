package wrapper.free

import cats.data.Kleisli
import cats.effect.{ Async, Blocker, ContextShift }
import cats.~>
import redis.clients.jedis.Jedis
import wrapper.free.connection.ConnectionOp

object KleisliInterpreter {

  @SuppressWarnings(Array("org.wartremover.warts.DefaultArguments"))
  def apply[M[_]](
    b: Blocker
  )(implicit
    am: Async[M],
    cs: ContextShift[M]
  ): KleisliInterpreter[M] =
    new KleisliInterpreter[M] {
      val asyncM        = am
      val contextShiftM = cs
      val blocker       = b
    }

}

trait KleisliInterpreter[M[_]] { outer =>
  implicit val asyncM: Async[M]

  val contextShiftM: ContextShift[M]
  val blocker: Blocker

  def primitive[J, A](f: J => A): Kleisli[M, J, A] =
    Kleisli { a =>
      blocker.blockOn[M, A](
        try asyncM.delay(f(a))
        catch {
          case scala.util.control.NonFatal(e) => asyncM.raiseError(e)
        }
      )(contextShiftM)
    }

  lazy val ConnectionInterpreter: ConnectionOp ~> Kleisli[M, Jedis, *] =
    new ConnectionInterpreter {}

  trait ConnectionInterpreter
      extends ConnectionOp.Visitor[Kleisli[M, Jedis, *]] {

    override def get(key: String): Kleisli[M, Jedis, String] =
      primitive(_.get(key))
  }
}

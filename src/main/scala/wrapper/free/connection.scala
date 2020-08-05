package wrapper.free

import cats.free.Free
import cats.~>
import wrapper.free.connection.ConnectionOp.Visitor

object connection {

  type ConnectionIO[A] = Free[ConnectionOp, A]

  sealed trait ConnectionOp[A] {
    def visit[F[_]](v: Visitor[F]): F[A]
  }

  object ConnectionOp {

    trait Visitor[F[_]] extends (ConnectionOp ~> F) {
      final def apply[A](fa: ConnectionOp[A]): F[A] = fa.visit(this)

      def get(key: String): F[String]
    }

    final case class GetValue(key: String) extends ConnectionOp[String] {
      def visit[F[_]](v: Visitor[F]) = v.get(key)
    }

    def get(key: String) = Free.liftF(GetValue(key))
  }
}

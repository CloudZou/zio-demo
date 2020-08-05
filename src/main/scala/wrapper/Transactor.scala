package wrapper

import cats.data.Kleisli
import cats.effect.{ Async, Blocker, Bracket, ContextShift, Resource }
import cats.free.Free
import cats.~>
import redis.clients.jedis
import redis.clients.jedis.{ Jedis, JedisPool }
import wrapper.JedisConnection.{ JedisConnectionIO, JedisConnectionOp }
import wrapper.Transactor.Interpreter
import cats.free.{ Free => FF }
import com.typesafe.config.ConfigFactory
import wrapper.JedisPoolTransactor.JedisPoolTransactor
import zio.blocking.Blocking
import zio.console.Console
import zio.{ Has, Managed, Task, ZIO, ZLayer, ZManaged }
import zio.interop.catz._
import zio.console._
import zio._

import scala.concurrent.ExecutionContext

object Test extends zio.App {

  def run(args: List[String]): ZIO[zio.ZEnv, Nothing, ExitCode] =
    ZIO(ConfigFactory.load.resolve)
      .flatMap(_ => program.provideCustomLayer(mkJedisPoolTransactor))
      .exitCode

  private val program
    : ZIO[Console with Has[JedisPoolTransactor[Task]], Throwable, Unit] = {
    for {
      t   <- ZIO.access[Has[JedisPoolTransactor[Task]]](_.get.trans)
      k1  <- t.apply(JedisConnection.getValue("test"))
      _   <- putStrLn(k1)
      k2  <- t.apply(JedisConnection.getValue("test"))
      _   <- putStrLn(k2)
      k3  <- t.apply(JedisConnection.getValue("test"))
      _   <- putStrLn(k3)
      k4  <- t.apply(JedisConnection.getValue("test"))
      _   <- putStrLn(k4)
      k5  <- t.apply(JedisConnection.getValue("test"))
      _   <- putStrLn(k5)
      k6  <- t.apply(JedisConnection.getValue("test"))
      _   <- putStrLn(k6)
      k7  <- t.apply(JedisConnection.getValue("test"))
      _   <- putStrLn(k7)
      k8  <- t.apply(JedisConnection.getValue("test"))
      _   <- putStrLn(k8)
      k9  <- t.apply(JedisConnection.getValue("test"))
      _   <- putStrLn(k9)
      k10 <- t.apply(JedisConnection.getValue("test"))
      _   <- putStrLn(k10)
    } yield ()
  }

  def mkJedisPoolTransactor
    : ZLayer[Blocking, Throwable, Has[JedisPoolTransactor[Task]]] =
    ZLayer.fromManaged(
      ZIO.runtime[Blocking].toManaged_.flatMap { implicit rt =>
        for {
          transactEC <- Managed.succeed(
                          rt.environment
                            .get[Blocking.Service]
                            .blockingExecutor
                            .asEC
                        )
          connectEC   = rt.platform.executor.asEC
          transactor <- JedisPoolTransactor
                          .newJedisPoolTransactor[Task](
                            connectEC,
                            Blocker.liftExecutionContext(transactEC)
                          )
                          .toManaged
        } yield transactor
      }
    )
}

object Transactor {
  type Interpreter[M[_]] = JedisConnectionOp ~> Kleisli[M, Jedis, *]

  type Aux[M[_], A0] = Transactor[M] { type A = A0 }

  def apply[M[_], A0](
    kernel0: A0,
    connect0: A0 => Resource[M, Jedis],
    interpret0: Interpreter[M]
  ): Transactor.Aux[M, A0] =
    new Transactor[M] {
      type A = A0
      val kernel    = kernel0
      val connect   = connect0
      val interpret = interpret0
    }

  object fromJedisPool {
    def apply[M[_]] = new FromJedisPoolUnapplied[M]

    class FromJedisPoolUnapplied[M[_]] {

      def apply(
        jedisPool: JedisPool,
        connectEC: ExecutionContext,
        blocker: Blocker
      )(implicit
        ev: Async[M],
        cs: ContextShift[M]
      ): Transactor.Aux[M, JedisPool] = {
        val connect = (jedisPool: JedisPool) => {
          val acquire           = cs.evalOn(connectEC)(ev.delay(jedisPool.getResource))
          def release(c: Jedis) = blocker.blockOn(ev.delay(c.close()))
          Resource.make(acquire)(release)
        }
        val interp  = KleisliInterpreter[M](blocker).ConnectionInterpreter
        Transactor(jedisPool, connect, interp)
      }
    }
  }
}

sealed abstract class Transactor[M[_]] { self =>
  import JedisConnection._

  type A

  def kernel: A

  def connect: A => Resource[M, Jedis]

  def interpret: Interpreter[M]

//  private def run(c: Jedis): JedisConnectionIO ~> M =
//    λ[JedisConnectionIO ~> M] { f =>
//      f.foldMap(interpret).run(c)
//    }

  def trans(implicit ev: Bracket[M, Throwable]): JedisConnectionIO ~> M =
    λ[JedisConnectionIO ~> M] { f =>
      connect(kernel).use { conn =>
        f.foldMap(interpret).run(conn)
      }
    }

}

object JedisConnection { module =>
  import JedisConnectionOp._
  type JedisConnectionIO[A] = FF[JedisConnectionOp, A]

  sealed trait JedisConnectionOp[A] {
    def visit[F[_]](v: Visitor[F]): F[A]
  }

  object JedisConnectionOp {

    trait Visitor[F[_]] extends (JedisConnectionOp ~> F) {
      final def apply[A](fa: JedisConnectionOp[A]): F[A] = fa.visit(this)

      def get(key: String): F[String]
    }

    final case class GetValue(key: String) extends JedisConnectionOp[String] {
      def visit[F[_]](v: Visitor[F]) = v.get(key)
    }
  }

  import JedisConnectionOp._

  def getValue(key: String) = FF.liftF(GetValue(key))
}

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

  lazy val ConnectionInterpreter: JedisConnectionOp ~> Kleisli[M, Jedis, *] =
    new ConnectionInterpreter {}

  trait ConnectionInterpreter
      extends JedisConnectionOp.Visitor[Kleisli[M, Jedis, *]] {

    override def get(key: String): Kleisli[M, Jedis, String] =
      primitive(_.get(key))
  }
}

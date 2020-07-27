package example.domain

import doobie.hikari._
import doobie.implicits._
import org.flywaydb.core.Flyway
import cats.effect.{Blocker, IO}
import doobie.util.transactor.Transactor
import example.layer.config.DBConfig
import zio.{Managed, Task, ZIO, ZManaged}
import zio.blocking.Blocking
import zio.interop.catz._

import scala.concurrent.ExecutionContext

trait DoobieTransactor {
  def initDb(cfg: DBConfig): Task[Unit] =
    Task {
      Flyway
        .configure()
        .dataSource(cfg.url, cfg.user, cfg.password)
        .load()
        .migrate()
    }.unit

  def mkTransactor(
                    cfg: DBConfig
                  ): ZManaged[Blocking, Throwable, HikariTransactor[Task]] =
    ZIO.runtime[Blocking].toManaged_.flatMap { implicit rt =>
      for {
        transactEC <- Managed.succeed(
          rt.environment
            .get[Blocking.Service]
            .blockingExecutor
            .asEC
        )
        connectEC = rt.platform.executor.asEC
        transactor <- HikariTransactor
          .newHikariTransactor[Task](
            cfg.driver,
            cfg.url,
            cfg.user,
            cfg.password,
            connectEC,
            Blocker.liftExecutionContext(transactEC)
          )
          .toManaged
      } yield transactor
    }
}

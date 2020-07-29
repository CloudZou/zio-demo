package example.domain

import doobie.hikari._
import org.flywaydb.core.Flyway
import cats.effect.{Blocker, IO}
import druid.DruidTransactor
import example.layer.config.DBConfig
import zio.{Managed, Task, ZIO, ZManaged}
import zio.blocking.Blocking
import zio.interop.catz._


trait DoobieTransactor {
  def initDb(cfg: DBConfig): Task[Unit] =
    Task {
      Flyway
        .configure()
        .dataSource(cfg.url, cfg.user, cfg.password)
        .load()
    }.unit

  def mkTransactor(
                    cfg: DBConfig
                  ): ZManaged[Blocking, Throwable, DruidTransactor[Task]] =
    ZIO.runtime[Blocking].toManaged_.flatMap { implicit rt =>
      for {
        transactEC <- Managed.succeed(
          rt.environment
            .get[Blocking.Service]
            .blockingExecutor
            .asEC
        )
        connectEC = rt.platform.executor.asEC
        transactor <- DruidTransactor
          .newDruidTransactor[Task](
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

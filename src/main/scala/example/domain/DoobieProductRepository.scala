package example.domain

import example.layer.config.DbConfigProvider
import zio.blocking.Blocking
import zio.{Queue, Ref, Task, UIO, ZIO, ZLayer}
import io.getquill.{idiom => _, _}
import doobie.util.transactor.Transactor
import doobie.implicits._
import doobie.quill.DoobieContext
import io.getquill._
import zio.interop.catz._

class DoobieProductRepository(xa: Transactor[Task]) extends ProductRepository.Service {
  val dc = new DoobieContext.Postgres(Literal)
  import dc._

  override def getAll: UIO[List[Product]] =
    run(query[Product]).transact(xa).orDie
}

object DoobieProductRepository extends DoobieTransactor {
  case class ItemId(id: Int)
  def layer
  : ZLayer[Blocking with DbConfigProvider, Throwable, ProductRepository] = ZLayer.fromFunctionM(env =>
    val s = ZLayer.fromManaged {
      for {
        cfg        <- ZIO.access[DbConfigProvider](_.get).toManaged_
        _          <- initDb(cfg).toManaged_
        transactor <- mkTransactor(cfg)
      } yield new DoobieProductRepository(transactor)
      Ref.make(List.empty[Queue[Int]]).map(new DoobieProductRepository())
    })
}
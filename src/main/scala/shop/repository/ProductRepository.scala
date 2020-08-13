package shop.repository

import io.getquill.{PostgresDialect, SnakeCase}
import zio._
import shop.model._
import BaseRepository._
import doobie.free.connection.ConnectionIO
import doobie.util.transactor.Transactor

object SkuRepository {
  type SkuRepository = Has[Service]

  trait Service extends PostgresRepository[Sku]{
    def getSkuInfo(id: Int): Task[Option[Sku]]
  }
}

object ProductRepository {

  type ProductRepository = Has[Service]

  trait Service extends PostgresRepository[ProductInfo]{
  }

  val productInfo = ProductInfo.apply

}

object ProductPictureRepository {
  type ProductPictureRepository = Has[Service]

  trait Service extends PostgresRepository[ProductPicture]{
  }
}

object ApplicationService {
  import doobie.implicits._
  import zio.interop.catz._

  type ZIOC[+A]     = ZIO[Any, Throwable, ConnectionIO[A]]
  implicit def connectionIO2ZIO[A](connA: ConnectionIO[A]): ZIOC[A] = Task.effect(connA)

  def xa: Transactor[Task]

  def getInfo(productId: Integer): ZIO[ProductRepository with SkuRepository with ProductPictureRepository, Throwable, (Option[ProductInfo], List[Sku], List[ProductPicture])] =
    for {
      ps <- ZIO.accessM[ProductRepository](_.get.select(product => product.id == productId))
      skus <- ZIO.accessM[SkuRepository](_.get.select(sku => sku.productId == productId))
      productPictures <- ZIO.accessM[ProductPictureRepository](_.get.select(pp => pp.proudctId == productId))
    } yield {
      val  s = for {
        p <- ps
        sku <- skus
        pp <- productPictures
      } yield {
        (p.headOption, sku, pp)
      }
      s.transact(xa)
    }
}

package shop.repository

import io.getquill.{PostgresDialect, SnakeCase}
import zio._
import shop.model._
import BaseRepository._

object SkuRepository {
  type SkuRepository = Has[Service]

  trait Service {
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

  trait Service extends PostgresRepository [ProductPicture]{
  }
}

object ApplicationService {
  def getInfo(productId: Integer): ZLayer[ProductRepository with SkuRepository with ProductPictureRepository, Throwable, (ProductInfo, List[Sku], List[ProductPicture])] =
    for {
      productInfos <- ZIO.accessM[ProductRepository](t => Task.effect(t.get.select(product => product.id == productId)))
      productInfo <- Task.effect(for {
        ps <- productInfos
      } yield ps.headOption )
      _ <- pro
    } yield (produ)
}

package shop.repository

import zio._
import shop.model._

object SkuRepository {

  trait Service {
    def getSkuInfo(id: Int): Task[Option[Sku]]
  }
}

object ProductRepository {

  trait Service {
    def getProduct(id: Int): Task[Option[ProductInfo]]
  }
}

object ProductPictureRepository {

  trait Service {
    def getProductPicture(productId: Int): Task[List[ProductPicture]]
  }
}

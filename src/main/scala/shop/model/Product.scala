package shop.model

import java.time.ZonedDateTime
import io.circe._
import io.circe.generic.semiauto.{ deriveDecoder, deriveEncoder }

case class ProductInfo(
  id: Int,
  userId: Int,
  name: String,
  nameDesc: String,
  description: String,
  productStatus: String,
  mainPic: String,
  productStage: String,
  status: Int,
  createTime: ZonedDateTime,
  updateTime: ZonedDateTime)

object ProductInfo {
  implicit val encoder: Encoder[ProductInfo] = deriveEncoder[ProductInfo]
  implicit val decoder: Decoder[ProductInfo] = deriveDecoder[ProductInfo]
}

case class ProductPicture(
  id: Int,
  proudctId: Int,
  pictureUrl: String,
  productPictureType: String,
  status: Int,
  createTime: ZonedDateTime,
  updateTime: ZonedDateTime)

object ProductPicture {
  implicit val encoder: Encoder[ProductPicture] = deriveEncoder[ProductPicture]
  implicit val decoder: Decoder[ProductPicture] = deriveDecoder[ProductPicture]
}

case class Sku(
  id: Int,
  productId: Int,
  skuDesc: String,
  storageNums: Int,
  sellNums: Int,
  tradeMaxNums: Int,
  marketPrice: Long,
  price: Long,
  costPrice: Long,
  postage: Long,
  defaultSku: Boolean,
  status: Int,
  createTime: ZonedDateTime,
  updateTime: ZonedDateTime) {

  def isGreaterThenTradeMaxNums(buyNums: Int): Boolean =
    return tradeMaxNums > 0 && buyNums > tradeMaxNums
}

object Sku {
  implicit val encoder: Encoder[Sku] = deriveEncoder[Sku]
  implicit val decoder: Decoder[Sku] = deriveDecoder[Sku]
}

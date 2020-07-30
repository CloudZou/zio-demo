package shop.repository

import zio._
import shop.model._

object OrderInfoRepository {

  trait Service {
    def createOrderInfo(orderInfo: OrderInfo): Task[Int]
  }
}

object OrderItemRepository {

  trait Service {
    def createOrderItem(orderItem: OrderItem): Task[Int]
  }
}

object TradeBillRepository {

  trait Service {
    def createTradeBill(tradeBill: TradeBill): Task[Int]
  }
}

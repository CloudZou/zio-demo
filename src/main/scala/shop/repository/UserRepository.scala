package shop.repository

import zio._
import shop.model._

object UserInfoRepository {

  trait Service {
    def getUserInfo(id: Int): Task[Option[UserInfo]]
  }
}

object UserAddressRepository {

  trait Service {
    def getUserAddressByUserId(userId: Int): Task[Option[UserAddress]]
  }
}

object UserSessionRepository {

  trait Service {
    def getCurrentUserInfo(): Task[Option[UserInfo]]
  }
}

package shop

import zio.Has
import shop.model.UserInfo

package object repository {
  type ProductRepository        = Has[ProductRepository.Service]
  type ProductPictureRepository = Has[ProductPictureRepository.Service]
  type SkuRepository            = Has[SkuRepository.Service]

  type UserInfoRepository    = Has[UserInfoRepository.Service]
  type UserAddressRepository = Has[UserAddressRepository.Service]
  type UserSessionRepository = Has[UserSessionRepository.Service]

  type ProductAllRepository = ProductRepository
    with ProductPictureRepository
    with SkuRepository

  type OrderAllRepository   = UserInfoRepository
    with UserAddressRepository
    with UserSessionRepository
}

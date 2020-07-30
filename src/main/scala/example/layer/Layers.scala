package example.layer

import example.domain.{ DoobieProductRepository, ProductRepository }
import example.layer.config.{
  AppConfigProvider,
  ConfigProvider,
  DbConfigProvider
}
import zio.ZLayer
import zio.blocking.Blocking
import zio.logging.Logging
import zio.logging.slf4j.Slf4jLogger

object Layers {

  type Layer0Env =
    ConfigProvider with Logging with Blocking

  type Layer1Env =
    Layer0Env with AppConfigProvider with DbConfigProvider

  type Layer2Env =
    Layer1Env with ProductRepository

  type AppEnv = Layer2Env

  object live {

    val layer0: ZLayer[Blocking, Throwable, Layer0Env] =
      Blocking.any ++ ConfigProvider.live ++ Slf4jLogger.make((_, msg) => msg)

    val layer1: ZLayer[Layer0Env, Throwable, Layer1Env] =
      AppConfigProvider.fromConfig ++ DbConfigProvider.fromConfig ++ ZLayer.identity

    val layer2: ZLayer[Layer1Env, Throwable, Layer2Env] =
      DoobieProductRepository.layer ++ ZLayer.identity

    val appLayer: ZLayer[Blocking, Throwable, AppEnv] =
      layer0 >>> layer1 >>> layer2
  }

}

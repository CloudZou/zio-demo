package example.application

import example.domain._
import example.layer.Layers.AppEnv
import zio.ZIO

import scala.concurrent.ExecutionContext

object ApplicationService {

  def addAsset(name: String, price: BigDecimal): ZIO[AssetRepository, DomainError, AssetId] =
    ZIO.accessM[AssetRepository](_.assetRepository.add(name, price))

  def updateAsset(assetId: AssetId, name: String, price: BigDecimal): ZIO[AssetRepository, DomainError, Unit] =
    for {
      _ <- ZIO.accessM[AssetRepository](_.assetRepository.update(assetId, name, price))
    } yield ()

  val getAssets: ZIO[AssetRepository, DomainError, List[Asset]] =
    ZIO.accessM[AssetRepository](_.assetRepository.getAll)

  def getProducts: ZIO[ProductRepository, Throwable, List[Product]] =
    ZIO.accessM[ProductRepository](_.get.getAll)

  def getPortfolio(portfolioId: PortfolioId): ZIO[AssetRepository with PortfolioAssetRepository, DomainError, PortfolioStatus] =
    PortfolioService.calculatePortfolioStatus(portfolioId)

  def updatePortfolio(portfolioId: PortfolioId, assetId: AssetId, amount: BigDecimal): ZIO[AssetRepository with PortfolioAssetRepository, DomainError, PortfolioStatus] =
    for {
      _ <- ZIO.accessM[PortfolioAssetRepository](_.portfolioAssetRepository.add(portfolioId, assetId, amount))
      p <- PortfolioService.calculatePortfolioStatus(portfolioId)
    } yield p

}

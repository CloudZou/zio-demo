package example.domain

case class AssetId(value: Long) extends AnyVal

case class Asset(id: Option[AssetId], name: String, price: BigDecimal)

case class PortfolioAsset(portfolioId: PortfolioId, assetId: AssetId, amount: BigDecimal)

case class PortfolioStatus(total: BigDecimal)

case class PortfolioId(value: Long) extends AnyVal


case class ProductId(value: Int) extends AnyVal
case class Product(id: Int, userId: Int, name: String, nameDesc: String, description: String, mainPic: String, status: Int)
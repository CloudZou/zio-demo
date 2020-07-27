package example.infrastructure.tables

import example.domain.Product
import slick.jdbc.PostgresProfile.api._


object ProductTable {
  case class LiftedProduct(id: Rep[Int], userId: Rep[Int], name: Rep[String], nameDesc: Rep[String], description: Rep[String], mainPic: Rep[String], status: Rep[Int])

  implicit object AssetShape extends CaseClassShape(LiftedProduct.tupled, Product.tupled)

  class Products(tag: Tag) extends Table[Product](tag, "product") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def userId = column[Int]("user_id")
    def name = column[String]("name")
    def nameDesc = column[String]("name_desc")
    def description = column[String]("description")
    def mainPic = column[String]("main_pic")
    def status = column[Int]("status")
    def * = LiftedProduct(id, userId, name, nameDesc, description, mainPic, status)
  }

}

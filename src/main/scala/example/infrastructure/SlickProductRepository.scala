package example.infrastructure

import example.domain._
import example.infrastructure.tables.ProductTable
import example.interop.slick.DatabaseProvider
import slick.lifted.TableQuery
import example.interop.slick.dbio._
import example.interop.slick.DatabaseProvider
import io.getquill.{PostgresAsyncContext, SnakeCase}
import slick.jdbc.PostgresProfile.api._
import zio.{ ZIO}

trait SlickProductRepository extends ProductRepository with DatabaseProvider {
  self =>
  lazy val ctx = new PostgresAsyncContext(SnakeCase, "quill")

  val products = TableQuery[ProductTable.Products]

  val productRepository = new ProductRepository.Service {
    override val getAll: ZIO[Any, RepositoryError, List[Product]] = {
      import ctx._
      implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global
      for {
        list <- ZIO.effect(ctx.run(query[Product].filter(t => t.id > lift(0)))).refineOrDie {
          case e: Exception => new RepositoryError(e)
        }
      } yield {
        
      }

    }
    //      ZIO.fromDBIO(products.result).provide(self).map(_.toList).refineOrDie {
//        case e: Exception => new RepositoryError(e)
//      }
  }
}

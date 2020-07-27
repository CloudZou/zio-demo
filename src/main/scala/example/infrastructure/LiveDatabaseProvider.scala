package example.infrastructure

import example.interop.slick.DatabaseProvider
import slick.basic.BasicBackend
import zio.{Managed, UIO, ZIO}
import slick.jdbc.H2Profile.backend._

trait LiveDatabaseProvider extends DatabaseProvider {
  override val databaseProvider = new DatabaseProvider.Service {

        override val db = ZIO.effectTotal(Database.forConfig("angrymiao"))
  }
}

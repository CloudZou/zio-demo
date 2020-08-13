package shop.repository

import doobie.quill.{DoobieContext, DoobieContextBase}
import doobie.free.connection.ConnectionIO
import io.getquill.{PostgresDialect, SnakeCase}

abstract class BaseRepository[T, Dialect, Naming](ctx: DoobieContextBase[Dialect, Naming]) {
  import ctx._

  private val selectQ  = (condition: T => Boolean) => quote(query[T].filter(condition))
  private val insertQ = (entry: T) => quote(query[T].insert(lift(entry)))
  private val updateQ = (condition: T => Boolean, entry: T) => quote(query[T].filter(condition).update(lift(entry)))
  private val deleteQ = (condition: T => Boolean) => quote(query[T].filter(condition).delete)

  def select(condition: T => Boolean): ConnectionIO[List[T]] = run(selectQ(condition))
  def insert(entry: T): ConnectionIO[Long] = run(insertQ(entry))
  def update(condition: T => Boolean, entry: T): ConnectionIO[Long] = run(updateQ(condition, entry))
  def delete(condition: T => Boolean): ConnectionIO[Long] = run(deleteQ(condition))
}

object BaseRepository {
  val doobiePostgreContext: DoobieContextBase[PostgresDialect, SnakeCase] = new DoobieContext.Postgres(SnakeCase)

  abstract class PostgresRepository[T]extends BaseRepository[T, PostgresDialect, SnakeCase](doobiePostgreContext){

  }
}
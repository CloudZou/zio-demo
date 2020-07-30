package druid

import cats.effect._
import com.alibaba.druid.pool.DruidDataSource
import doobie.util.transactor.Transactor

import scala.concurrent.ExecutionContext

object DruidTransactor {

  def apply[M[_]: Async: ContextShift](
    druidDataSource: DruidDataSource,
    connectEC: ExecutionContext,
    blocker: Blocker
  ): DruidTransactor[M] =
    Transactor.fromDataSource[M](druidDataSource, connectEC, blocker)

  private def createDataSourceResource[M[_]: Sync](
    factory: => DruidDataSource
  ): Resource[M, DruidDataSource] = {
    val alloc = Sync[M].delay(factory)
    val free  = (ds: DruidDataSource) => Sync[M].delay(ds.close())
    Resource.make(alloc)(free)
  }

  def initial[M[_]: Async: ContextShift](
    connectEC: ExecutionContext,
    blocker: Blocker
  ): Resource[M, DruidTransactor[M]] =
    createDataSourceResource(new DruidDataSource)
      .map(Transactor.fromDataSource[M](_, connectEC, blocker))

  def newDruidTransactor[M[_]: Async: ContextShift](
    driverClassName: String,
    url: String,
    user: String,
    pass: String,
    connectEC: ExecutionContext,
    blocker: Blocker
  ): Resource[M, DruidTransactor[M]] =
    for {
      _ <- Resource.liftF(Async[M].delay(Class.forName(driverClassName)))
      t <- initial[M](connectEC, blocker)
      _ <- Resource.liftF {
             t.configure { ds =>
               Async[M].delay {
                 ds.setUrl(url)
                 ds.setUsername(user)
                 ds.setPassword(pass)
                 ds.setMaxActive(100)
                 ds.setMinIdle(50)
                 ds.setInitialSize(50)
               }
             }
           }
    } yield t

}

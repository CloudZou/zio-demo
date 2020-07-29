import com.alibaba.druid.pool.DruidDataSource
import doobie.util.transactor.Transactor

package object druid {
  type DruidTransactor[M[F_]] = Transactor.Aux[M, DruidDataSource]
}

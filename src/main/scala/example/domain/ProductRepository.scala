package example.domain

import zio.IO

trait ProductRepository {
  def productRepository: ProductRepository.Service
}

object ProductRepository {
  trait Service {
    val getAll: IO[RepositoryError, List[Product]]
  }
}
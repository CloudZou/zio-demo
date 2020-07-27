package example

import zio.Has

package object domain {
  type ProductRepository = Has[ProductRepository.Service]
}

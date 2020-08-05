package movie

import movie.model.{
  IMDBID,
  MovieDetail,
  MovieRegistration,
  ReservationRequest,
  ReservationStatus,
  ScreenID
}
import zio.{ Has, UIO, ZIO }

package object domain {
  type MovieService = Has[MovieService.Service]

  def saveOrUpdate(
    registration: MovieRegistration
  ): ZIO[MovieService, Throwable, Boolean] =
    for {
      movieOption <-
        ZIO.accessM[MovieService](_.get.getIMDB(registration.imdbId))

    } yield true
}

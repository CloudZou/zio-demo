package movie

import movie.model.{IMDBID, MovieDetail, MovieRegistration, ReservationRequest, ReservationStatus, ScreenID}
import zio.{Has, UIO, ZIO}

package object domain {
  type MovieService = Has[MovieService.Service]

  def saveOrUpdate(registration: MovieRegistration): ZIO[MovieService, Throwable, Boolean] = {
    for {
      movieOption <- ZIO.accessM[MovieService](_.get.getIMDB(registration.imdbId))
      screeningScheduled <- ZIO.accessM[MovieService](_.get.contains(registration.screenId))
      registered <- if (movieOption.isDefined && screeningScheduled) {
        ZIO.accessM[MovieService](_.get.putReservation(ReservationRequest(registration.imdbId,
          registration.screenId),
          ReservationStatus(registration.imdbId,
            registration.screenId,
            movieOption.get.movieTitle,
            registration.availableSeats)))
      }else {
        ZIO.succeed(false)
      }
    } yield registered
  }
}

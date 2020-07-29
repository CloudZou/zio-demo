package movie.domain

import movie.model.{IMDBID, MovieDetail, ReservationRequest, ReservationStatus, ScreenID}
import zio.{Task, UIO}

object MovieService {
  trait Service {
    def getIMDB(imdbID: IMDBID): Task[Option[MovieDetail]]
    def contains(screenId: ScreenID): Task[Boolean]
    def getReservation(request: ReservationRequest): Task[Option[ReservationStatus]]
    def putReservation(request: ReservationRequest, status: ReservationStatus): Task[Boolean]
    def reserveSeat(request: ReservationRequest): Task[Boolean]
  }
}

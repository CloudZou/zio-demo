package movie

package object actors {

  object messages {
    import model._
    final case class GetIMDB(imdbId: IMDBID)

    final case class Contains(screenId: ScreenID)

    final case class GetReservation(request: ReservationRequest)

    final case class PutReservation(
      request: ReservationRequest,
      status: ReservationStatus)

    final case class ReserveSeat(request: ReservationRequest)
  }
}

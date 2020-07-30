package movie.actors

import akka.actor.Actor
import movie.actors.messages._
import movie.model._

import scala.collection.mutable

class ReservationActor extends Actor {

  private val demoStorage: mutable.Map[ReservationRequest, ReservationStatus] =
    mutable.Map.empty

  override def receive: Receive = {
    case GetReservation(request: ReservationRequest) =>
      sender ! demoStorage.get(request)
    case PutReservation(
          request: ReservationRequest,
          status: ReservationStatus
        ) =>
      sender ! {
        demoStorage.get(request) match {
          case Some(oldStatus) =>
            val updatedStatus = oldStatus.copy(
              movieTitle = status.movieTitle,
              availableSeats = status.availableSeats
            )
            demoStorage.put(request, updatedStatus)
            true
          case None            =>
            if (
              request.imdbId == "tt0111161" && request.screenId == "screen_123456"
            ) {
              demoStorage.put(request, status)
              true
            } else
              false
        }
      }
    case ReserveSeat(request: ReservationRequest)    =>
      sender ! {
        demoStorage.get(request) match {
          case Some(status) =>
            val newStaus =
              if (status.reservedSeats < status.availableSeats)
                status.copy(reservedSeats = status.reservedSeats + 1)
              else
                status
            demoStorage.put(request, newStaus)
            true
          case None         => false
        }
      }
  }
}

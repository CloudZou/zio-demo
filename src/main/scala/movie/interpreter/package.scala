package movie

import akka.actor.{ ActorRef, ActorSystem, Props }
import movie.domain.MovieService
import movie.model.{ IMDBID, ScreenID }
import zio.{ Has, Task, UIO, ZIO, ZLayer }
import akka.pattern.ask
import akka.util.Timeout
import movie.actors.{ IMDBActor, ReservationActor, ScreeningActor }
import movie.actors.messages._

import scala.concurrent.duration._
import movie.model._

package object interpreter {

  val live: ZLayer[Has[ActorSystem], Throwable, MovieService] =
    ZLayer.fromService { system: ActorSystem =>
      val imdbSource        = system.actorOf(Props[IMDBActor], "imdb")
      val screenInfo        = system.actorOf(Props[ScreeningActor], "screening")
      val reservationSource =
        system.actorOf(Props[ReservationActor], "reservation")
      new MovieServiceImpl(imdbSource, screenInfo, reservationSource)
    }
}

class MovieServiceImpl(
  imdb: ActorRef,
  screening: ActorRef,
  reservation: ActorRef)
    extends MovieService.Service {
  implicit val timeout = Timeout(5 seconds)
  import scala.concurrent.ExecutionContext.Implicits.global

  override def getIMDB(imdbID: IMDBID): Task[Option[model.MovieDetail]] =
    ZIO.fromFuture { ec =>
      (imdb ? GetIMDB(imdbID)).map(_.asInstanceOf[Option[model.MovieDetail]])
    }

  override def contains(screenId: ScreenID): Task[Boolean] =
    ZIO.fromFuture { ec =>
      (screening ? Contains(screenId)).map(_.asInstanceOf[Boolean])
    }

  override def getReservation(
    request: model.ReservationRequest
  ): Task[Option[model.ReservationStatus]] =
    ZIO.fromFuture { ec =>
      (reservation ? GetReservation(request)).map(
        _.asInstanceOf[Option[model.ReservationStatus]]
      )
    }

  override def putReservation(
    request: model.ReservationRequest,
    status: model.ReservationStatus
  ): Task[Boolean] =
    ZIO.fromFuture { ec =>
      (reservation ? PutReservation(request, status)).map(
        _.asInstanceOf[Boolean]
      )
    }

  override def reserveSeat(request: model.ReservationRequest): Task[Boolean] =
    ZIO.fromFuture { ec =>
      (reservation ? ReserveSeat(request)).map(_.asInstanceOf[Boolean])
    }
}

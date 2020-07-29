package movie.actors

import akka.actor.Actor
import movie.actors.messages._
import movie.model._

class IMDBActor extends Actor {

  def receive = {
    case GetIMDB(imdbId: IMDBID) => sender ! {
      if (imdbId == "tt0111161") Some(MovieDetail("tt0111161", "The Shawshank Redemption"))
      else None
    }
  }
}
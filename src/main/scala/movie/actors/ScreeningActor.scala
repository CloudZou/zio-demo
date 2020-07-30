package movie.actors

import akka.actor.Actor
import movie.actors.messages._
import movie.model._

class ScreeningActor extends Actor {

  override def receive: Receive = {
    case Contains(screenId: ScreenID) =>
      sender ! { screenId == "screen_123456" }
  }
}

package dibujakka

import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, Behavior}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.TextMessage
import akka.stream.scaladsl.Flow
import dibujakka.services.PlayerService


object RoomManager {
  trait Command
  final case class NewRoom(name: String) extends Command

  def apply(): Behavior[Command] =
    Behaviors.setup { context => {
        Behaviors.same
      }
    }

  def Draw(roomManager: ActorRef[Command], roomId: String) : Flow[String, String, Any] = {
    println("Room", roomId)
    println("Message (Draw)", _)
    _
    Flow.fromSinkAndSource(in, out)
  }
}

object HttpServerWithActorInteraction extends PlayerService {
  implicit val system: ActorSystem[RoomManager.Command] = ActorSystem(RoomManager(), "RoomManager")

  def main(args: Array[String]): Unit = {
    Http().newServerAt("0.0.0.0", 8080).bind(route)
  }
}

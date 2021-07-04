package dibujakka

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, Behavior}
import akka.http.scaladsl.Http
import dibujakka.HttpServerWithActorInteraction.sendMessageToClients
import dibujakka.services.PlayerService


object RoomManager {
  trait Command

  final case class PrintMsg(msg: String, roomId: String) extends Command

  def apply(): Behavior[PrintMsg] =
    Behaviors.setup { _ => {
      Behaviors.receiveMessage { message =>
        println("Room", message.roomId)
        println("Message", message.msg)
        sendMessageToClients(message.roomId, s"Response from roomManager: room $message.roomId, $message.msg")
        Behaviors.same
      }
    }
    }
}

object HttpServerWithActorInteraction extends PlayerService {
  implicit val system: ActorSystem[RoomManager.PrintMsg] = ActorSystem(RoomManager(), "RoomManager")

  def main(args: Array[String]): Unit = {
    Http().newServerAt("0.0.0.0", 8080).bind(route)
  }
}

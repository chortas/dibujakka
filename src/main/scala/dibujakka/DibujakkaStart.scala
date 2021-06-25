package dibujakka

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, Behavior}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._


object RoomManager {
  final case class NewRoom(name: String)

  def apply(): Behavior[NewRoom] =
    Behaviors.setup { context => {
        Behaviors.same
      }
    }
}

object HttpServerWithActorInteraction {
  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem[RoomManager.NewRoom] = ActorSystem(RoomManager(), "DibujakkaMain")

    val route = path("ws") {
      handleWebSocketMessages(ChatWebSocket.listen())
    }

    Http().newServerAt("127.0.0.1", 8080)
      .bind(route)

    readMessages()

    def readMessages(): Unit =
      for (ln <- io.Source.stdin.getLines) ln match {
        case "" =>
          system.terminate()
          return
        case other => ChatWebSocket.sendText(other)
      }
  }
}

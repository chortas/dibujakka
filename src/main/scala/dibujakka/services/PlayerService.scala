package dibujakka.services

import akka.actor.typed.ActorRef
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.http.scaladsl.server.Directives.{handleWebSocketMessages, parameter, path}
import akka.http.scaladsl.server.Route
import akka.stream.scaladsl.Flow
import dibujakka.RoomManager

abstract class PlayerCommand(command: String) {
  def process() : Unit
}

final case class DrawCommand(command: String) extends PlayerCommand(command) {
  def process(): Unit = println(command)
}

trait PlayerService {
  implicit val system: ActorRef[RoomManager.Command]

  val route: Route = path("ws") {
    parameter("roomId") {
      roomId => handleWebSocketMessages(webSocketHandlerForRoom(roomId))
    }
  }

  def webSocketHandlerForRoom(roomId: String): Flow[Message, Message, Any] = {
    val decodeCommand : Flow[String, PlayerCommand, Any] = Flow[String].map(msg => DrawCommand(msg))

    Flow[Message]
      .collect {
        case tm: TextMessage.Strict =>
          println("TextMessage received", tm)
          tm
        case bm: BinaryMessage.Strict =>
          // ignore binary messages but drain content to avoid the stream being clogged
          println("BinaryMessage received", bm)
          Nil
      }
      .via(_.toString)
      .via(decodeCommand)
      .via(RoomManager.Draw(system, roomId))
      .map()
  }
}

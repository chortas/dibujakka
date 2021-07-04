package dibujakka.services

import akka.NotUsed
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.http.scaladsl.server.Directives.{
  handleWebSocketMessages,
  parameter,
  path,
  _
}
import akka.http.scaladsl.server.Route
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{Flow, Sink, Source, SourceQueueWithComplete}
import dibujakka.Server.system
import dibujakka.communication.WebSocketChatMessageProtocol._
import dibujakka.communication.WebSocketMessageProtocol._
import dibujakka.communication._
import dibujakka.room.RoomMessages.ClientMessage
import spray.json._

trait PlayerService {

  val playersRoute: Route = path("ws") {
    parameter("roomId") { roomId =>
      handleWebSocketMessages(receiveMessageFromClients(roomId))
    }
  }

  private var clientConnections: Map[String, List[TextMessage => Unit]] =
    Map[String, List[TextMessage => Unit]]().withDefaultValue(List())

  def receiveMessageFromClients(
    roomId: String
  ): Flow[Message, Message, NotUsed] = {
    val inbound: Sink[Message, Any] = Sink.foreach({
      case tm: TextMessage =>
        val text = tm.getStrictText

        val webSocketMessage: WebSocketType =
          if (text.contains("chat"))
            text.parseJson.convertTo[WebSocketChatMessage]
          else text.parseJson.convertTo[WebSocketMessage]

        webSocketMessage.messageType match {
          case "draw" =>
            val newWebSocketMessage: WebSocketMessage =
              webSocketMessage.asInstanceOf[WebSocketMessage]
            system ! ClientMessage(
              roomId,
              DrawClientCommand(newWebSocketMessage.payload.toString)
            )
          case "chat" =>
            val newWebSocketChatMessage: WebSocketChatMessage =
              webSocketMessage.asInstanceOf[WebSocketChatMessage]

            system ! ClientMessage(
              roomId,
              ChatClientCommand(
                newWebSocketChatMessage.word,
                newWebSocketChatMessage.userName
              )
            )
          case "start" =>
            system ! ClientMessage(roomId, StartClientCommand())
          case "join" =>
            val newWebSocketMessage: WebSocketMessage =
              webSocketMessage.asInstanceOf[WebSocketMessage]
            system ! ClientMessage(
              roomId,
              JoinClientCommand(newWebSocketMessage.payload.toString)
            )
        }
        println("TextMessage received in room:", roomId)
      case bm: BinaryMessage =>
        // ignore binary messages but drain content to avoid the stream being clogged
        bm.dataStream.runWith(Sink.ignore)
        Nil
    })
    val outbound: Source[Message, SourceQueueWithComplete[Message]] =
      Source.queue[Message](16, OverflowStrategy.fail)

    Flow.fromSinkAndSourceMat(inbound, outbound)((_, outboundMat) => {
      clientConnections = clientConnections
        .updated(roomId, clientConnections(roomId).::(outboundMat.offer))
      NotUsed
    })
  }

  def sendMessageToClients(roomId: String, text: String): Unit = {
    for (connection <- clientConnections(roomId))
      connection(TextMessage.Strict(text))
  }
}

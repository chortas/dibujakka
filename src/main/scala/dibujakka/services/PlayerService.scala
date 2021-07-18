package dibujakka.services

import akka.NotUsed
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.http.scaladsl.server.Directives.{handleWebSocketMessages, parameter, path}
import akka.http.scaladsl.server.Route
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{Flow, Sink, Source, SourceQueue}
import dibujakka.RoomMessages.ClientMessage
import dibujakka.Server.system
import dibujakka._
import spray.json.DefaultJsonProtocol.{jsonFormat2, _}
import spray.json.{RootJsonFormat, _}

trait PlayerService {

  val playersRoute: Route = path("ws") {
    parameter("roomId") { roomId =>
      handleWebSocketMessages(receiveMessageFromClients(roomId))
    }
  }

  private var clientsQueuesByRoom = Map[String, List[SourceQueue[Message]]]().withDefaultValue(List())

  def receiveMessageFromClients(roomId: String): Flow[Message, Message, NotUsed] = {
    val inbound: Sink[Message, Any] = Sink.foreach({
      case tm: TextMessage =>
        implicit val wsFormat: RootJsonFormat[WebSocketMessage] = jsonFormat2(WebSocketMessage)

        val webSocketMessage : WebSocketMessage = tm.getStrictText.parseJson.convertTo[WebSocketMessage]

        webSocketMessage.messageType match {
          case "draw" =>
            system ! ClientMessage(roomId, DrawClientCommand(webSocketMessage.payload))
          case "chat" =>
            system ! ClientMessage(roomId, ChatClientCommand(webSocketMessage.payload))
          case "start" =>
            system ! ClientMessage(roomId, StartClientCommand())
          case "join" =>
            system ! ClientMessage(roomId, JoinClientCommand(webSocketMessage.payload))
        }

      case bm: BinaryMessage =>
        // ignore binary messages but drain content to avoid the stream being clogged
        bm.dataStream.runWith(Sink.ignore)
        Nil
    })

    val outbound: Source[Message, SourceQueue[Message]] =
      Source.queue[Message](1024, OverflowStrategy.backpressure)

    Flow.fromSinkAndSourceMat(inbound, outbound)((_, outboundMat) => {
      clientsQueuesByRoom = clientsQueuesByRoom.updated(roomId, clientsQueuesByRoom(roomId).::(outboundMat))
      NotUsed
    })
  }

  def sendMessageToClients(roomId: String, text: String): Unit = {
    clientsQueuesByRoom(roomId).foreach(_.offer(TextMessage.Strict(text)))
  }
}

package dibujakka.services

import akka.NotUsed
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.http.scaladsl.server.Directives.{handleWebSocketMessages, parameter, path}
import akka.http.scaladsl.server.Route
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{Flow, Sink, Source, SourceQueueWithComplete}
import dibujakka.RoomMessages.ClientMessage
import dibujakka.Server.system
import dibujakka.{ChatClientCommand, DrawClientCommand, WebSocketMessage}
import spray.json.DefaultJsonProtocol.{jsonFormat2, _}
import spray.json.{RootJsonFormat, _}

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
        implicit val wsFormat: RootJsonFormat[WebSocketMessage] =
          jsonFormat2(WebSocketMessage)

        val webSocketMessage =
          tm.getStrictText.parseJson.convertTo[WebSocketMessage]

        webSocketMessage.messageType match {
          case "draw" =>
            system ! ClientMessage(
              roomId,
              DrawClientCommand(webSocketMessage.payload)
            )
          case "chat" =>
            system ! ClientMessage(
              roomId,
              ChatClientCommand(webSocketMessage.payload)
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

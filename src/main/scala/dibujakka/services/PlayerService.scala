package dibujakka.services

import akka.NotUsed
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.http.scaladsl.server.Directives.{handleWebSocketMessages, parameter, path}
import akka.http.scaladsl.server.Route
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{Flow, Sink, Source, SourceQueueWithComplete}
import dibujakka.HttpServerWithActorInteraction.system
import dibujakka.RoomManager.PrintMsg

import scala.::

//abstract class PlayerCommand(command: String) {
//  def process() : Unit
//}
//
//final case class DrawCommand(command: String) extends PlayerCommand(command) {
//  def process(): Unit = println(command)
//}

trait PlayerService {

  val route: Route = path("ws") {
    parameter("roomId") {
      roomId => handleWebSocketMessages(receiveMessageFromClients(roomId))
    }
  }

  private var clientConnections: Map[String, List[TextMessage => Unit]] = Map[String, List[TextMessage => Unit]]().withDefaultValue(List())

  def receiveMessageFromClients(roomId: String): Flow[Message, Message, NotUsed] = {
    val inbound: Sink[Message, Any] = Sink.foreach({
      case tm: TextMessage =>
        println("TextMessage received in room:", roomId)
        system ! PrintMsg(tm.getStrictText, roomId)
      case bm: BinaryMessage =>
        // ignore binary messages but drain content to avoid the stream being clogged
        bm.dataStream.runWith(Sink.ignore)
        Nil
    })
    val outbound: Source[Message, SourceQueueWithComplete[Message]] = Source.queue[Message](16, OverflowStrategy.fail)

    Flow.fromSinkAndSourceMat(inbound, outbound)((_, outboundMat) => {
      clientConnections = clientConnections.updated(roomId, clientConnections(roomId).::(outboundMat.offer))
      NotUsed
    })
  }

  def sendMessageToClients(roomId: String, text: String): Unit = {
    for (connection <- clientConnections(roomId)) connection(TextMessage.Strict(text))
  }
}

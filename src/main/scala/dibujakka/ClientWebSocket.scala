package dibujakka

import akka.NotUsed
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{Flow, Sink, Source, SourceQueueWithComplete}
import dibujakka.HttpServerWithActorInteraction.system

object ClientWebSocket {
  private var browserConnections: List[TextMessage => Unit] = List()

  def greeter(): Flow[Message, Message, Any] =
    Flow[Message].mapConcat {
    case tm: TextMessage =>
      println("TextMessage received")
      TextMessage(Source.single("Hello ") ++ tm.textStream ++ Source.single("!")) :: Nil
    case bm: BinaryMessage =>
      // ignore binary messages but drain content to avoid the stream being clogged
      println("BinaryMessage received")
      //bm.dataStream.runWith(Sink.ignore)
      Nil
  }

  def listen(roomId: String): Flow[Message, Message, NotUsed] = {
    val inbound: Sink[Message, Any] = Sink.foreach({
      case tm: TextMessage =>
        println("TextMessage received in room:", roomId)
        TextMessage(Source.single("Hello ") ++ tm.textStream ++ Source.single("!")) :: Nil
      case bm: BinaryMessage =>
        // ignore binary messages but drain content to avoid the stream being clogged
        bm.dataStream.runWith(Sink.ignore)
        Nil
    })
    val outbound: Source[Message, SourceQueueWithComplete[Message]] = Source.queue[Message](16, OverflowStrategy.fail)

    Flow.fromSinkAndSourceMat(inbound, outbound)((_, outboundMat) => {
      browserConnections ::= outboundMat.offer
      NotUsed
    })
  }

  def sendText(text: String): Unit = {
    for (connection <- browserConnections) connection(TextMessage.Strict(text))
  }
}

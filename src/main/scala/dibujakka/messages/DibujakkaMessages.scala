package dibujakka.messages

import akka.actor.typed.ActorRef
import dibujakka.communication.{ClientCommand, ServerCommand}
import dibujakka.persistence.Word
import dibujakka.room.Room
import dibujakka.room.RoomManager.Rooms

object DibujakkaMessages {
  trait DibujakkaMessage

  case class NextRound(replyTo: ActorRef[SendToClients]) extends DibujakkaMessage

  case class AddPlayer() extends DibujakkaMessage

  case class GetRoom(replyTo: ActorRef[Room]) extends DibujakkaMessage

  case class GetRooms(replyTo: ActorRef[Rooms]) extends DibujakkaMessage

  case class CreateRoom(id: String,
                        name: String,
                        totalRounds: Int,
                        maxPlayers: Int,
                        language: String)
      extends DibujakkaMessage

  case class ClientMessage(roomId: String, command: ClientCommand)
      extends DibujakkaMessage

  case class SendToClients(roomId: String, serverCommand: ServerCommand)
      extends DibujakkaMessage

  case class DrawMessage(replyTo: ActorRef[SendToClients], message: String)
      extends DibujakkaMessage

  case class ChatMessage(replyTo: ActorRef[SendToClients],
                         word: String,
                         userName: String)
      extends DibujakkaMessage

  case class StartMessage(replyTo: ActorRef[SendToClients]) extends DibujakkaMessage

  case class JoinMessage(replyTo: ActorRef[SendToClients], name: String)
      extends DibujakkaMessage

  case class GetWord(replyTo: ActorRef[Option[Word]]) extends DibujakkaMessage

  case class UpdateWordMetrics(word: Word, wasGuessed: Boolean) extends DibujakkaMessage

  // case class GetWordE(replyTo: ActorRef[?], difficulty: Int) extends DbMessage

  // case class GetWordLE(replyTo: ActorRef[?], difficulty: Int) extends DbMessage

  // case class GetWordGE(replyTo: ActorRef[?], difficulty: Int) extends DbMessage
}

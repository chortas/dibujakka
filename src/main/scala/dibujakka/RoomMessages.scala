package dibujakka

import akka.actor.typed.ActorRef
import dibujakka.RoomManager.Rooms

object RoomMessages {
  sealed trait RoomMessage

  case class AddRound() extends RoomMessage
  case class AddPlayer() extends RoomMessage
  case class StartRoom() extends RoomMessage
  case class GetRoom(replyTo: ActorRef[Room]) extends RoomMessage
  case class GetRooms(replyTo: ActorRef[Rooms]) extends RoomMessage
  case class CreateRoom(id: Int,
                        name: String,
                        totalRounds: Int,
                        maxPlayers: Int,
                        language: String)
      extends RoomMessage
}

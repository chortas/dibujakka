package dibujakka

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import dibujakka.RoomMessages._

object RoomActor {
  def apply(): Behavior[RoomMessage] =
    Behaviors.setup(context => new RoomActor(context, None))

  def apply(room: Option[Room]): Behavior[RoomMessage] =
    Behaviors.setup(context => new RoomActor(context, room))
}

class RoomActor(context: ActorContext[RoomMessage], room: Option[Room])
  extends AbstractBehavior[RoomMessage](context) {

  import RoomActor._

  override def onMessage(msg: RoomMessage) =
    msg match {
      case GetRoom(replyTo) =>
        replyTo ! room.get
        Behaviors.same
      case CreateRoom(id, name, totalRounds, maxPlayers, language) =>
        apply(
          Some(
            Room(id, name, totalRounds, maxPlayers, language, 0, 0, "waiting", "word")
          )
        )
      case AddRound() =>
        room.foreach(room => room.copy(currentRound = room.currentRound + 1))
        Behaviors.same
      case AddPlayer() =>
        room.foreach(room => room.copy(playersCount = room.playersCount + 1))
        Behaviors.same
      case StartRoom() =>
        room.foreach(room => room.copy(status = "in progress"))
        Behaviors.same
      case DrawMessage(replyTo, message) =>
        val roomId = room.get.id
        replyTo ! SendToClients(roomId, DrawServerCommand(message))
        Behaviors.same
      case ChatMessage(replyTo, word) =>
        val roomId = room.get.id
        val currentWord = room.get.currentWord
        if (word.equalsIgnoreCase(currentWord)) {
          replyTo ! SendToClients(roomId, RoomServerCommand(room.get))
        } else {
          replyTo ! SendToClients(roomId, ChatServerCommand(word))
        }
        Behaviors.same
      case StartMessage(replyTo) =>
        val roomId = room.get.id
        replyTo ! SendToClients(roomId, RoomServerCommand(room.get))
        Behaviors.same
    }
}

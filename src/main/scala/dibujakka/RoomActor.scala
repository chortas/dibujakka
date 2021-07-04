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
            Room(
              id,
              name,
              totalRounds,
              maxPlayers,
              language,
              0,
              "waiting",
              "word",
              Map.empty
            )
          )
        )
      case AddRound() =>
        val newRoom = room.get.copy(currentRound = room.get.currentRound + 1)
        apply(Some(newRoom))
      case StartRoom() =>
        val newRoom = room.get.copy(status = "in progress")
        apply(Some(newRoom))
      case DrawMessage(replyTo, message) =>
        val roomId = room.get.id
        replyTo ! SendToClients(roomId, DrawServerCommand(message))
        Behaviors.same
      case ChatMessage(replyTo, word) =>
        // Must receive the username so if he guessed the word, the score goes up.
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
      case JoinMessage(replyTo, name) =>
        val newRoom = room.get.addPlayer(name)
        val roomId = newRoom.id
        replyTo ! SendToClients(roomId, RoomServerCommand(newRoom))
        apply(Some(newRoom))
    }
}

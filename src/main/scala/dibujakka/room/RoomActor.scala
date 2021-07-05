package dibujakka.room

import akka.actor.Cancellable
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import dibujakka.communication.{ChatServerCommand, DrawServerCommand, RoomServerCommand}
import dibujakka.room.RoomMessages._

import scala.concurrent.duration._
import dibujakka.Server.system._

object RoomActor {
  def apply(): Behavior[RoomMessage] =
    Behaviors.setup(context => new RoomActor(context, None, None))

  def apply(room: Option[Room], nextRoundScheduled: Option[Cancellable]): Behavior[RoomMessage] =
    Behaviors.setup(context => new RoomActor(context, room, nextRoundScheduled))
}

class RoomActor(context: ActorContext[RoomMessage], room: Option[Room], nextRoundScheduled: Option[Cancellable])
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
              "word", //TODO: change this harcoded word
              Map.empty
            )
          ),
          None
        )
      case NextRound(replyTo) =>
        val newRoom = room.get.copy(currentRound = room.get.currentRound + 1)
        replyTo ! SendToClients(newRoom.id, RoomServerCommand(newRoom))
        val newNextRoundScheduled: Option[Cancellable] = Some(context.system.scheduler.scheduleOnce(
          3.seconds,
          () => context.self ! NextRound(replyTo)
        ))
        apply(Some(newRoom), newNextRoundScheduled)
      case DrawMessage(replyTo, message) =>
        val roomId = room.get.id
        replyTo ! SendToClients(roomId, DrawServerCommand(message))
        Behaviors.same
      case ChatMessage(replyTo, word, userName) =>
        val roomId = room.get.id
        val currentWord = room.get.currentWord
        if (word.equalsIgnoreCase(currentWord)) {
          val newRoom = room.get.addScore(userName)
          replyTo ! SendToClients(roomId, RoomServerCommand(newRoom))
          //TODO: NTH IF EVERYONE GUESSES context.self ! NextRound(replyTo)
          //TODO: nextRoundScheduled.foreach(_.cancel())
          apply(Some(newRoom), nextRoundScheduled)
        } else {
          replyTo ! SendToClients(roomId, ChatServerCommand(word))
          Behaviors.same
        }
      case StartMessage(replyTo) =>
        context.self ! NextRound(replyTo)
        val newRoom = room.get.copy(status = "in progress")
        apply(Some(newRoom), nextRoundScheduled)
      case JoinMessage(replyTo, userName) =>
        val newRoom = room.get.addPlayer(userName)
        val roomId = newRoom.id
        replyTo ! SendToClients(roomId, RoomServerCommand(newRoom))
        apply(Some(newRoom), nextRoundScheduled)
    }
}

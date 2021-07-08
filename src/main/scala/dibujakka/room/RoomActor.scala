package dibujakka.room

import akka.actor.Cancellable
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import dibujakka.communication.{ChatServerCommand, DrawServerCommand, RoomServerCommand}
import dibujakka.room.RoomMessages._

import scala.concurrent.duration._
import dibujakka.Server.system._

import scala.util.Random.between

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
            )
          ),
          None
        )
      case NextRound(replyTo) =>
        nextRoundScheduled.foreach(_.cancel())
        //TODO: sumarle puntos a quien dibujó de acuerdo a cuántos adivinaron
        if (room.get.hasFinishedAllRounds) {
          val newRoom = room.get.copy(status = "finished")
          replyTo ! SendToClients(newRoom.id, RoomServerCommand(newRoom))
          apply(Some(newRoom), None)
        } else {
          val newRoom = room.get.copy(
            currentRound = room.get.currentRound + 1,
            whoIsDrawingIdx = (room.get.whoIsDrawingIdx + 1) % room.get.players.size,
            playersWhoGuessed = List.empty
          )
          val newNextRoundScheduled: Option[Cancellable] = Some(context.system.scheduler.scheduleOnce(
            10.seconds,
            () => context.self ! NextRound(replyTo)
          ))
          replyTo ! SendToClients(newRoom.id, RoomServerCommand(newRoom))
          apply(Some(newRoom), newNextRoundScheduled)
        }
      case DrawMessage(replyTo, message) =>
        val roomId = room.get.id
        replyTo ! SendToClients(roomId, DrawServerCommand(message))
        Behaviors.same
      case ChatMessage(replyTo, word, userName) =>
        val roomId = room.get.id
        val currentWord = room.get.currentWord
        if (word.equalsIgnoreCase(currentWord)) {
          if (room.get.playerHasGuessed(userName)) {
            Behaviors.same
          } else {
            val newRoom = room.get.updateScores(userName) //TODO: players.size - guessed
            if (newRoom.allPlayersGuessed) {
              context.self ! NextRound(replyTo)
            }
            replyTo ! SendToClients(roomId, RoomServerCommand(newRoom))
            apply(Some(newRoom), nextRoundScheduled)
          }
        } else {
          replyTo ! SendToClients(roomId, ChatServerCommand(word))
          Behaviors.same
        }
      case StartMessage(replyTo) =>
        context.self ! NextRound(replyTo)
        val newRoom = room.get.copy(
          currentRound = 0,
          status = "in progress",
          whoIsDrawingIdx = between(0, room.get.players.size),
          playersWhoGuessed = List.empty
        )
        apply(Some(newRoom), nextRoundScheduled)
      case JoinMessage(replyTo, userName) =>
        val newRoom = room.get.addPlayer(userName)
        val roomId = newRoom.id
        replyTo ! SendToClients(roomId, RoomServerCommand(newRoom))
        apply(Some(newRoom), nextRoundScheduled)
    }
}

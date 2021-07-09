package dibujakka.room

import akka.actor.Cancellable
import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.scaladsl.Behaviors._
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext}
import akka.actor.typed.{ActorRef, Behavior, Scheduler}
import akka.util.Timeout
import dibujakka.communication.{
  ChatServerCommand,
  DibujakkaServerCommand,
  DrawServerCommand
}
import dibujakka.messages.DibujakkaMessages._
import dibujakka.persistence.{DbActor, Word}

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.Random.between

object RoomActor {
  def apply(): Behavior[DibujakkaMessage] =
    setup(context => new RoomActor(context, None, None, None, None))

  def apply(
    room: Option[Room],
    nextRoundScheduled: Option[Cancellable],
    endRoundScheduled: Option[Cancellable],
    dbActorRef: Option[ActorRef[DibujakkaMessage]]
  ): Behavior[DibujakkaMessage] =
    setup(
      context =>
        new RoomActor(
          context,
          room,
          nextRoundScheduled,
          endRoundScheduled,
          dbActorRef
      )
    )
}

class RoomActor(context: ActorContext[DibujakkaMessage],
                room: Option[Room],
                nextRoundScheduled: Option[Cancellable],
                endRoundScheduled: Option[Cancellable],
                dbActorRef: Option[ActorRef[DibujakkaMessage]])
    extends AbstractBehavior[DibujakkaMessage](context) {
  implicit val scheduler: Scheduler = context.system.scheduler
  implicit val executionContext: ExecutionContext = context.executionContext
  implicit val timeout: Timeout = 10.seconds
  val LOGGER = context.system.log

  import RoomActor._

  override def onMessage(msg: DibujakkaMessage): Behavior[DibujakkaMessage] =
    msg match {
      case GetRoom(replyTo) =>
        replyTo ! room.get
        this
      case CreateRoom(id, name, totalRounds, maxPlayers, language) =>
        val newDbActorRef: ActorRef[DibujakkaMessage] =
          context.spawnAnonymous(DbActor())
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
              None,
            )
          ),
          None,
          None,
          Some(newDbActorRef)
        )
      case EndRound(replyTo) =>
        LOGGER.info("About to update word")
        room.get.currentWord.foreach(
          word =>
            dbActorRef.get ! UpdateWordMetrics(
              word,
              room.get.playersWhoGuessed.nonEmpty
          )
        )
        LOGGER.info("Word updated")

        endRoundScheduled.foreach(_.cancel())

        var newRoom = room.get.updateScores(room.get.getDrawer)
        newRoom = newRoom.copy(status = "interval")

        val newNextRoundScheduled = Some(
          context.system.scheduler
            .scheduleOnce(10.seconds, () => context.self ! NextRound(replyTo))
        )

        replyTo ! SendToClients(newRoom.id, DibujakkaServerCommand(newRoom))
        apply(
          Some(newRoom),
          newNextRoundScheduled,
          endRoundScheduled,
          dbActorRef
        )
      case NextRound(replyTo) =>
        nextRoundScheduled.foreach(_.cancel())

        val futureWord: Future[Option[Word]] = dbActorRef.get ? GetWord
        val word: Option[Word] = Await.result(futureWord, timeout.duration)
        LOGGER.info("New word from database")

        var newEndRoundScheduled: Option[Cancellable] = None
        var newRoom = room.get.copy(currentWord = word)

        if (newRoom.hasFinishedAllRounds) {
          LOGGER.info("All rounds have finished")
          newRoom = newRoom.copy(status = "finished")
          replyTo ! SendToClients(newRoom.id, DibujakkaServerCommand(newRoom))
        } else {
          LOGGER.info("Round in progress")
          newRoom = newRoom.copy(
            status = "in progress",
            currentRound = newRoom.currentRound + 1,
            whoIsDrawingIdx = newRoom.nextDrawingIndex,
            playersWhoGuessed = List.empty
          )
          newEndRoundScheduled = Some(
            context.system.scheduler
              .scheduleOnce(60.seconds, () => context.self ! EndRound(replyTo))
          )
          replyTo ! SendToClients(newRoom.id, DibujakkaServerCommand(newRoom))
        }
        LOGGER.info("End next round")
        apply(
          Some(newRoom),
          nextRoundScheduled,
          newEndRoundScheduled,
          dbActorRef
        )
      case DrawMessage(replyTo, message) =>
        val roomId = room.get.id
        replyTo ! SendToClients(roomId, DrawServerCommand(message))
        this
      case ChatMessage(replyTo, word, userName) =>
        var newRoom = room.get
        if (!newRoom.isDrawing(userName) && newRoom.playerIsInRoom(userName)) {
          val currentWord = newRoom.currentWord
          if (word.equalsIgnoreCase(currentWord.get.text)) {
            if (!newRoom.playerHasGuessed(userName)) {
              newRoom = newRoom.updateScores(userName)
              replyTo ! SendToClients(
                newRoom.id,
                DibujakkaServerCommand(newRoom)
              )
            }
          } else {
            replyTo ! SendToClients(newRoom.id, ChatServerCommand(word))
          }
        }
        if (newRoom.allPlayersGuessed) {
          context.self ! EndRound(replyTo)
        }
        apply(Some(newRoom), nextRoundScheduled, endRoundScheduled, dbActorRef)
      case StartMessage(replyTo) =>
        var newRoom = room.get
        if (room.get.canStart) {
          context.self ! NextRound(replyTo)
          newRoom = newRoom.copy(
            currentRound = 0,
            status = "in progress",
            whoIsDrawingIdx = between(0, room.get.players.size),
            playersWhoGuessed = List.empty
          )
        }
        apply(Some(newRoom), nextRoundScheduled, endRoundScheduled, dbActorRef)
      case JoinMessage(replyTo, userName) =>
        val newRoom = room.get.addPlayer(userName)
        val roomId = newRoom.id
        replyTo ! SendToClients(roomId, DibujakkaServerCommand(newRoom))
        apply(Some(newRoom), nextRoundScheduled, endRoundScheduled, dbActorRef)
    }
}

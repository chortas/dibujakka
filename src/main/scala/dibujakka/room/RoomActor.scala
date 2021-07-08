package dibujakka.room

import akka.actor.Cancellable
import akka.actor.typed.{ActorRef, Behavior, Scheduler}
import akka.actor.typed.scaladsl.Behaviors._
import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext}
import akka.util.Timeout
import dibujakka.communication.{ChatServerCommand, DibujakkaServerCommand, DrawServerCommand}
import dibujakka.persistence.{DbActor, Word}
import dibujakka.messages.DibujakkaMessages._

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._
import scala.util.Random.between

object RoomActor {
  def apply(): Behavior[DibujakkaMessage] =
    setup(context => new RoomActor(context, None, None, None))

  def apply(room: Option[Room], nextRoundScheduled: Option[Cancellable], dbActorRef: Option[ActorRef[DibujakkaMessage]]): Behavior[DibujakkaMessage] =
    setup(context => new RoomActor(context, room, nextRoundScheduled, dbActorRef))
}

class RoomActor(context: ActorContext[DibujakkaMessage], room: Option[Room],
                nextRoundScheduled: Option[Cancellable], dbActorRef: Option[ActorRef[DibujakkaMessage]])
  extends AbstractBehavior[DibujakkaMessage](context) {
  implicit val scheduler: Scheduler = context.system.scheduler
  implicit val executionContext: ExecutionContext = context.executionContext
  implicit val timeout: Timeout = 10.seconds

  import RoomActor._

  override def onMessage(msg: DibujakkaMessage): Behavior[DibujakkaMessage] =
    msg match {
      case GetRoom(replyTo) =>
        replyTo ! room.get
        this
      case CreateRoom(id, name, totalRounds, maxPlayers, language) =>
        val newDbActorRef: ActorRef[DibujakkaMessage] = context.spawnAnonymous(DbActor())
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
          Some(newDbActorRef)
        )
      case NextRound(replyTo) =>
        nextRoundScheduled.foreach(_.cancel())
        val futureWord: Future[Option[Word]] = dbActorRef.get ? GetWord
        val word: Option[Word] = Await.result(futureWord, timeout.duration)

        var newRoom = room.get.updateScores(room.get.getDrawer)
        var newNextRoundScheduled: Option[Cancellable] = None
        newRoom = newRoom.copy(currentWord = word)

        if (newRoom.hasFinishedAllRounds) {
          newRoom = newRoom.copy(status = "finished")
          replyTo ! SendToClients(newRoom.id, DibujakkaServerCommand(newRoom))
        } else {
          newRoom = newRoom.copy(
            currentRound = newRoom.currentRound + 1,
            whoIsDrawingIdx = newRoom.nextDrawingIndex,
            playersWhoGuessed = List.empty
          )
          newNextRoundScheduled = Some(context.system.scheduler.scheduleOnce(
            5.seconds,
            () => context.self ! NextRound(replyTo)
          ))
          replyTo ! SendToClients(newRoom.id, DibujakkaServerCommand(newRoom))
        }
        apply(Some(newRoom), newNextRoundScheduled, dbActorRef)
      case DrawMessage(replyTo, message) =>
        val roomId = room.get.id
        replyTo ! SendToClients(roomId, DrawServerCommand(message))
        this
      case ChatMessage(replyTo, word, userName) =>
        var newRoom = room.get
        if (!newRoom.isDrawing(userName)) {
          val currentWord = newRoom.currentWord
          if (word.equalsIgnoreCase(currentWord.get.text)) {
            if (!newRoom.playerHasGuessed(userName)) {
              newRoom = newRoom.updateScores(userName)
              replyTo ! SendToClients(newRoom.id, DibujakkaServerCommand(newRoom))
            }
          } else {
            replyTo ! SendToClients(newRoom.id, ChatServerCommand(word))
          }
        }
        if (newRoom.allPlayersGuessed) {
          context.self ! NextRound(replyTo)
        }
        apply(Some(newRoom), nextRoundScheduled, dbActorRef)
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
        apply(Some(newRoom), nextRoundScheduled, dbActorRef)
      case JoinMessage(replyTo, userName) =>
        val newRoom = room.get.addPlayer(userName)
        val roomId = newRoom.id
        replyTo ! SendToClients(roomId, DibujakkaServerCommand(newRoom))
        apply(Some(newRoom), nextRoundScheduled, dbActorRef)
    }
}

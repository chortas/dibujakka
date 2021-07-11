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
  val ROUND_TIME = 60
  val INTERVAL_TIME = 10

  import RoomActor._

  override def onMessage(msg: DibujakkaMessage): Behavior[DibujakkaMessage] =
    room match {
      case Some(room) => processMessageWithRoom(room, msg)
      case None       => processMessageWithoutRoom(msg)
    }

  def processMessageWithoutRoom(msg: DibujakkaMessage) = msg match {
    case CreateRoom(id, name, totalRounds, maxPlayers, language) =>
      processCreateRoom(id, name, totalRounds, maxPlayers, language)
    case _ => this
  }

  private def processMessageWithRoom(room: Room, msg: DibujakkaMessage) =
    msg match {
      case GetRoom(replyTo) =>
        processGetRoom(replyTo, room)
      case EndRound(replyTo) =>
        processEndRound(replyTo, room)
      case NextRound(replyTo) =>
        processNextRound(replyTo, room)
      case DrawMessage(replyTo, message) =>
        processDrawMessage(replyTo, message, room)
      case ChatMessage(replyTo, word, userName) =>
        processChatMessage(replyTo, word, userName, room)
      case StartMessage(replyTo) =>
        processStartMessage(replyTo, room)
      case JoinMessage(replyTo, userName) =>
        processJoinMessage(replyTo, userName, room)
      case _ => this
    }

  private def processGetRoom(replyTo: ActorRef[Room], room: Room) = {
    replyTo ! room
    this
  }

  private def processCreateRoom(id: String,
                                name: String,
                                totalRounds: Int,
                                maxPlayers: Int,
                                language: String) = {
    val newDbActorRef: ActorRef[DibujakkaMessage] =
      context.spawnAnonymous(DbActor())
    apply(
      Some(
        Room(id, name, totalRounds, maxPlayers, language, 0, "waiting", None)
      ),
      None,
      None,
      Some(newDbActorRef)
    )
  }

  private def processEndRound(replyTo: ActorRef[SendToClients], room: Room) = {
    updateMetricsForWord(room)

    endRoundScheduled.foreach(_.cancel())

    var newRoom = room.updateScores(room.getDrawer)
    newRoom = newRoom.copy(status = "interval")

    val newNextRoundScheduled = scheduleNextRound(replyTo)

    replyTo ! SendToClients(newRoom.id, DibujakkaServerCommand(newRoom))
    apply(Some(newRoom), newNextRoundScheduled, endRoundScheduled, dbActorRef)
  }

  private def processNextRound(replyTo: ActorRef[SendToClients],
                               room: Room): Behavior[DibujakkaMessage] = {
    nextRoundScheduled.foreach(_.cancel())
    var newRoom = room
    var newEndRoundScheduled: Option[Cancellable] = None

    dbActorRef.foreach(dbActorRef => {
      newRoom = getWordAndUpdateRoom(dbActorRef, room)

      if (newRoom.hasFinishedAllRounds) {
        newRoom = newRoom.copy(status = "finished")
      } else {
        val result = scheduleEndRound(replyTo, newRoom)
        newRoom = result._1
        newEndRoundScheduled = result._2
      }
      replyTo ! SendToClients(newRoom.id, DibujakkaServerCommand(newRoom))
    })
    apply(Some(newRoom), nextRoundScheduled, newEndRoundScheduled, dbActorRef)
  }

  private def processDrawMessage(replyTo: ActorRef[SendToClients],
                                 message: String,
                                 room: Room) = {
    val roomId = room.id
    replyTo ! SendToClients(roomId, DrawServerCommand(message))
    this
  }

  private def processChatMessage(replyTo: ActorRef[SendToClients],
                                 word: String,
                                 userName: String,
                                 room: Room) = {
    var newRoom = room
    if (!newRoom.isDrawing(userName) && newRoom.playerIsInRoom(userName)) {
      val currentWord = newRoom.currentWord
      currentWord.foreach(currentWord => {
        newRoom = sendChosenWord(word, currentWord, newRoom, userName, replyTo)
      })
    }
    if (newRoom.allPlayersGuessed) {
      context.self ! EndRound(replyTo)
    }
    apply(Some(newRoom), nextRoundScheduled, endRoundScheduled, dbActorRef)
  }

  private def processStartMessage(replyTo: ActorRef[SendToClients],
                                  room: Room): Behavior[DibujakkaMessage] = {
    var newRoom = room
    if (room.canStart) {
      context.self ! NextRound(replyTo)
      newRoom = newRoom.copy(
        currentRound = 0,
        status = "in progress",
        whoIsDrawingIdx = between(0, room.players.size),
        playersWhoGuessed = List.empty,
        scores = newRoom.players.map((_, 0)).toMap
      )
    }
    apply(Some(newRoom), nextRoundScheduled, endRoundScheduled, dbActorRef)
  }

  private def processJoinMessage(replyTo: ActorRef[SendToClients],
                                 userName: String,
                                 room: Room) = {
    val newRoom = room.addPlayer(userName)
    val roomId = newRoom.id
    replyTo ! SendToClients(roomId, DibujakkaServerCommand(newRoom))
    apply(Some(newRoom), nextRoundScheduled, endRoundScheduled, dbActorRef)
  }

  private def sendChosenWord(word: String,
                             currentWord: Word,
                             room: Room,
                             userName: String,
                             replyTo: ActorRef[SendToClients]) = {
    var newRoom = room
    if (word.equalsIgnoreCase(currentWord.text) && !room.playerHasGuessed(
          userName
        )) {
      newRoom = room.updateScores(userName)
      replyTo ! SendToClients(room.id, DibujakkaServerCommand(room))
    } else {
      replyTo ! SendToClients(room.id, ChatServerCommand(word))
    }
    newRoom
  }

  private def scheduleNextRound(replyTo: ActorRef[SendToClients]) = {
    val newNextRoundScheduled = Some(
      context.system.scheduler
        .scheduleOnce(
          ROUND_TIME.seconds,
          () => context.self ! NextRound(replyTo)
        )
    )
    newNextRoundScheduled
  }

  private def updateMetricsForWord(room: Room) = {
    room.currentWord.foreach(
      word =>
        dbActorRef.foreach(
          dbActorRef =>
            dbActorRef ! UpdateWordMetrics(
              word,
              room.playersWhoGuessed.nonEmpty
          )
      )
    )
  }

  private def getWordAndUpdateRoom(dbActorRef: ActorRef[DibujakkaMessage],
                                   room: Room) = {
    var word: Option[Word] = None
    do {
      val difficulty: Int = (room.currentRound / 3) + 1
      val futureWord: Future[Option[Word]] =
        dbActorRef.ask(actorRef => GetWordEqualDifficulty(actorRef, difficulty))
      word = Await.result(futureWord, timeout.duration)
    } while (room.hasBeenPlayed(word))
    room.addWord(word)
  }

  private def scheduleEndRound(replyTo: ActorRef[SendToClients], room: Room) = {
    val newRoom = room.copy(
      status = "in progress",
      currentRound = room.currentRound + 1,
      whoIsDrawingIdx = room.nextDrawingIndex,
      playersWhoGuessed = List.empty
    )
    val newEndRoundScheduled = Some(
      context.system.scheduler
        .scheduleOnce(
          INTERVAL_TIME.seconds,
          () => context.self ! EndRound(replyTo)
        )
    )
    (newRoom, newEndRoundScheduled)
  }
}

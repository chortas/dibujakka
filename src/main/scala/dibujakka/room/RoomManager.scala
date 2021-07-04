package dibujakka.room

import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.util.Timeout
import dibujakka.Server.sendMessageToClients
import dibujakka.communication.{
  ChatClientCommand,
  DrawClientCommand,
  JoinClientCommand,
  StartClientCommand
}
import dibujakka.room.RoomMessages._

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

object RoomManager {
  case class Rooms(rooms: List[Room])

  def apply(): Behavior[RoomMessage] =
    Behaviors.setup(context => new RoomManager(context, Map.empty))

  def apply(rooms: Map[String, ActorRef[RoomMessage]]): Behavior[RoomMessage] =
    Behaviors.setup(context => new RoomManager(context, rooms))
}

class RoomManager(context: ActorContext[RoomMessage],
                  rooms: Map[String, ActorRef[RoomMessage]])
    extends AbstractBehavior[RoomMessage](context) {

  implicit val system: ActorSystem[Nothing] = context.system
  implicit val executionContext: ExecutionContext = context.executionContext

  import RoomManager._

  override def onMessage(message: RoomMessage): Behavior[RoomMessage] =
    message match {
      case GetRooms(replyTo) =>
        implicit val timeout: Timeout = 10.seconds

        val mappedRooms: List[Future[Room]] = rooms.values
          .map(roomActor => (roomActor ? GetRoom).mapTo[Room])
          .toList

        Future.sequence(mappedRooms).foreach(rooms => replyTo ! Rooms(rooms))
        Behaviors.same
      case CreateRoom(id, name, totalRounds, maxPlayers, language) =>
        implicit val timeout: Timeout = 10.seconds

        val roomActor = context.spawnAnonymous(RoomActor())

        roomActor ! CreateRoom(id, name, totalRounds, maxPlayers, language)

        apply(rooms.updated(id, roomActor))
      case ClientMessage(roomId, command) =>
        command match {
          case DrawClientCommand(drawMessage) =>
            rooms
              .get(roomId)
              .foreach(roomActor => {
                roomActor ! DrawMessage(context.self, drawMessage)
              })
            Behaviors.same
          case ChatClientCommand(word, userName) =>
            rooms
              .get(roomId)
              .foreach(roomActor => {
                roomActor ! ChatMessage(context.self, word, userName)
              })
            Behaviors.same
          case StartClientCommand() =>
            rooms
              .get(roomId)
              .foreach(roomActor => {
                roomActor ! StartMessage(context.self)
              })
            Behaviors.same
          case JoinClientCommand(name) =>
            rooms
              .get(roomId)
              .foreach(roomActor => {
                roomActor ! JoinMessage(context.self, name)
              })
            Behaviors.same
        }
      case SendToClients(roomId, serverCommand) =>
        sendMessageToClients(roomId, serverCommand.toString)
        Behaviors.same
    }
}

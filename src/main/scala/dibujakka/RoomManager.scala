package dibujakka

import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.util.Timeout
import dibujakka.RoomMessages.{CreateRoom, GetRoom, GetRooms, RoomMessage}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

object RoomManager {
  case class Rooms(rooms: List[Room])

  implicit val system: ActorSystem[RoomMessage] =
    ActorSystem(RoomActor.apply(), "roomActor")
  implicit val executionContext: ExecutionContext = system.executionContext

  def apply(): Behavior[RoomMessage] =
    Behaviors.setup(context => new RoomManager(context, List.empty))

  def apply(rooms: List[ActorRef[RoomMessage]]): Behavior[RoomMessage] =
    Behaviors.setup(context => new RoomManager(context, rooms))
}

class RoomManager(context: ActorContext[RoomMessage],
                  rooms: List[ActorRef[RoomMessage]])
    extends AbstractBehavior[RoomMessage](context) {

  import RoomManager._

  override def onMessage(message: RoomMessage): Behavior[RoomMessage] =
    message match {
      // TODO: add functionality for otherMessages
      case RoomMessages.AddRound()  => Behaviors.same
      case RoomMessages.AddPlayer() => Behaviors.same
      case RoomMessages.StartRoom() => Behaviors.same
      case GetRoom(replyTo)         => Behaviors.same
      case GetRooms(replyTo) =>
        implicit val timeout: Timeout = 10.seconds

        val mappedRooms: List[Future[Room]] =
          rooms.map(roomActor => (roomActor ? GetRoom).mapTo[Room])

        Future.sequence(mappedRooms).foreach(rooms => replyTo ! Rooms(rooms))
        Behaviors.same
      case CreateRoom(id, name, totalRounds, maxPlayers, language) =>
        implicit val timeout: Timeout = 10.seconds

        val roomActor = context.spawnAnonymous(RoomActor())

        roomActor ! CreateRoom(id, name, totalRounds, maxPlayers, language)

        apply(rooms :+ roomActor)
    }

}

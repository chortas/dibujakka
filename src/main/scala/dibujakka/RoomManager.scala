package dibujakka

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.util.Timeout
import dibujakka.RoomMessages.{CreateRoom, GetRoom, GetRooms, RoomMessage}
import akka.actor.typed.scaladsl.AskPattern._

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._

object RoomManager {
  case class Rooms(rooms: List[Room])

  implicit val system: ActorSystem[RoomMessage] =
    ActorSystem(RoomActor.apply(), "roomActor")
  implicit val executionContext: ExecutionContext = system.executionContext

  def apply: Behaviors.Receive[RoomMessage] = apply(List.empty)

  def apply(
    rooms: List[ActorRef[RoomMessage]]
  ): Behaviors.Receive[RoomMessage] =
    Behaviors.receive {
      case (
          _,
          CreateRoom(
            id: Int,
            name: String,
            totalRounds: Int,
            maxPlayers: Int,
            language: String
          )
          ) =>
        Behaviors.setup { context =>
          implicit val timeout: Timeout = 10.seconds

          val roomActor = context.spawnAnonymous(RoomActor())

          roomActor ! CreateRoom(id, name, totalRounds, maxPlayers, language)

          apply(rooms :+ roomActor)
        }
      case (_, GetRooms(replyTo)) =>
        implicit val timeout: Timeout = 10.seconds

        val mappedRooms =
          rooms.map(roomActor => (roomActor ? GetRoom).mapTo[Room])

        Future.sequence(mappedRooms).foreach(rooms => replyTo ! Rooms(rooms))
        Behaviors.same
    }
}

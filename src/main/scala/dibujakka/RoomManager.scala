package dibujakka

import akka.actor.Status.Success
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.server.Directives.complete
import akka.util.Timeout
import dibujakka.RoomMessages.{CreateRoom, GetRoom, GetRooms, RoomMessage}
import dibujakka.RoomProtocol._

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._
import scala.util.Try

object RoomManager {
  case class Rooms(rooms: List[Room])

  implicit val system: ActorSystem[RoomMessage] =
    ActorSystem(RoomActor.apply(), "roomActor")
  implicit val executionContext: ExecutionContext = system.executionContext

  def apply: Behaviors.Receive[RoomMessage] = apply(List.empty)

  def apply(rooms: List[Room]): Behaviors.Receive[RoomMessage] =
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

          val room: Future[Room] =
            (roomActor ? GetRoom).mapTo[Room]

          room.onComplete(room => room.map(room => apply(rooms :+ room)))
          Behaviors.same
        }
      case (_, GetRooms(replyTo)) =>
        replyTo ! Rooms(rooms)
        Behaviors.same
    }
}

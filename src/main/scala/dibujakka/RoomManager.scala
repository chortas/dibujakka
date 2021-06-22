package dibujakka

import akka.actor.Status.Success
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.server.Directives.complete
import akka.util.Timeout
import dibujakka.RoomMessages.{CreateRoom, GetRoom, GetRooms, RoomMessage}
import dibujakka.RoomProtocol._

import dibujakka.RoomActor

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._
import scala.util.Try

object RoomManager {
  case class Rooms(rooms: List[Int])

  //implicit val system: ActorSystem[RoomMessage] =
  //  ActorSystem(RoomActor.apply(), "roomActor")
  //implicit val executionContext: ExecutionContext = system.executionContext

  def apply: Behaviors.Receive[RoomMessage] = apply(List.empty)

  def apply(rooms: List[Int]): Behaviors.Receive[RoomMessage] =
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

          //val roomActor = context.spawnAnonymous(RoomActor())
          val roomActor: ActorRef[RoomMessage] = context.spawn(RoomActor(id, name, totalRounds, maxPlayers, language), id.toString)

          context.log.info(s"$roomActor")

          //roomActor ! CreateRoom(id, name, totalRounds, maxPlayers, language)

          //val room: Future[Room] =
          //  (roomActor ? GetRoom).mapTo[Room]

          //room.onComplete(room => room.map(room => apply(rooms :+ room)))

          apply(rooms :+ id)
        }
      case (ctx, GetRooms(replyTo)) =>
        ctx.log.info(s"rooms: $rooms")
        replyTo ! Rooms(rooms)
        Behaviors.same
    }
}

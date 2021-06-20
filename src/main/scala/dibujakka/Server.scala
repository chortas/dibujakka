package dibujakka

import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.util.Timeout
import dibujakka.RoomManager.Rooms
import dibujakka.RoomMessages.{CreateRoom, GetRooms, RoomMessage}
import dibujakka.RoomProtocol._
import spray.json.RootJsonFormat

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.io.StdIn

object Server {
  // these are from spray-json
  implicit val roomsFormat: RootJsonFormat[Rooms] = jsonFormat1(Rooms)

  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem[RoomMessage] =
      ActorSystem(RoomManager.apply, "roomManager")
    // needed for the future flatMap/onComplete in the end
    implicit val executionContext: ExecutionContext = system.executionContext

    val roomManager: ActorRef[RoomMessage] = system

    val route =
      path("room") {
        concat(
          post {
            parameters(
              "id".as[Int],
              "name",
              "totalRounds".as[Int],
              "maxPlayers".as[Int],
              "language"
            ) { (id, name, totalRounds, maxPlayers, language) =>
              roomManager ! CreateRoom(
                id,
                name,
                totalRounds,
                maxPlayers,
                language
              )
              complete(StatusCodes.Accepted, "Room placed")
            }
          },
          get {
            implicit val timeout: Timeout = 5.seconds

            val rooms: Future[Rooms] =
              (roomManager ? GetRooms).mapTo[Rooms]
            complete(rooms)
          }
        )
      }

    val bindingFuture = Http().newServerAt("localhost", 8080).bind(route)
    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }
}

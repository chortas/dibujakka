package dibujakka.services

import akka.actor.typed.scaladsl.AskPattern._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import dibujakka.RoomJsonProtocol.RoomJsonFormat
import dibujakka.RoomManager.Rooms
import dibujakka.RoomMessages.{CreateRoom, GetRooms}
import dibujakka.Server.{roomManager, system}
import spray.json.DefaultJsonProtocol._
import spray.json.RootJsonFormat

import scala.concurrent.Future
import scala.concurrent.duration._

trait RoomService {
  // these are from spray-json
//  implicit val roomFormat: RootJsonFormat[Room] = jsonFormat8(Room)
  implicit val roomsFormat: RootJsonFormat[Rooms] = jsonFormat1(Rooms)

  val roomsRoute: Route =
    path("room") {
      concat(
        get {
          implicit val timeout: Timeout = 5.seconds

          val rooms: Future[Rooms] =
            (roomManager ? GetRooms).mapTo[Rooms]
          complete(StatusCodes.OK, rooms)
        },
        post {
          parameters(
            "id",
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
            complete(StatusCodes.Created, "Room placed")
          }
        }
      )
    }
}

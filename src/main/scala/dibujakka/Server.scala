package dibujakka

import akka.actor.typed.{ActorRef, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._

import scala.concurrent.ExecutionContext
import scala.io.StdIn

object Server {

  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem[Room.Message] =
      ActorSystem(Room.apply(), "room")
    // needed for the future flatMap/onComplete in the end
    implicit val executionContext: ExecutionContext = system.executionContext

    val room: ActorRef[Room.Message] = system

    val route =
      path("room") {
        concat(post {
          parameters(
            "id".as[Int],
            "name",
            "totalRounds".as[Int],
            "maxPlayers".as[Int],
            "language"
          ) { (id, name, totalRounds, maxPlayers, language) =>
            room ! Room.CreateRoom(id, name, totalRounds, maxPlayers, language)
            complete(StatusCodes.Accepted, "Room placed")
          }
        })
      }

    val bindingFuture = Http().newServerAt("localhost", 8080).bind(route)
    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }
}

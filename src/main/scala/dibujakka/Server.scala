package dibujakka

import akka.actor.typed.{ActorRef, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import dibujakka.RoomMessages.RoomMessage
import dibujakka.services.{PlayerService, RoomService}

import scala.concurrent.ExecutionContext
import scala.io.StdIn


object Server extends PlayerService with RoomService {
  implicit val system: ActorSystem[RoomMessage] =
    ActorSystem(RoomManager.apply, "roomManager")
  // needed for the future flatMap/onComplete in the end
  implicit val executionContext: ExecutionContext = system.executionContext
  implicit val roomManager: ActorRef[RoomMessage] = system

  def main(args: Array[String]): Unit = {

    val route = roomsRoute ~ playersRoute

    val bindingFuture = Http().newServerAt("localhost", 8080).bind(route)
    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }
}
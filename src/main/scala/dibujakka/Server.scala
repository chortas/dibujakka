package dibujakka

import akka.actor.typed.{ActorRef, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import dibujakka.RoomMessages.RoomMessage
import dibujakka.services.{PlayerService, RoomService}

import scala.concurrent.ExecutionContext
import scala.io.StdIn


import doobie._
import doobie.implicits._
import cats._
import cats.effect._
import cats.implicits._
import doobie.util.ExecutionContexts

object Server extends PlayerService with RoomService {
  implicit val system: ActorSystem[RoomMessage] =
    ActorSystem(RoomManager.apply, "roomManager")
  // needed for the future flatMap/onComplete in the end
  implicit val executionContext: ExecutionContext = system.executionContext
  implicit val roomManager: ActorRef[RoomMessage] = system

  def main(args: Array[String]): Unit = {

    implicit val cs = IO.contextShift(ExecutionContexts.synchronous)
    val xa = Transactor.fromDriverManager[IO](
      "org.postgresql.Driver",
      "jdbc:postgresql:dibujakka-pg",
      "postgres",
      "A1noko",
      Blocker.liftExecutionContext(ExecutionContexts.synchronous)
    )

    //val nameList: Unit = sql"select name from country"
    //  .query[String]    // Query0[String]
    //  .to[List]         // ConnectionIO[List[String]]
    //  .transact(xa)     // IO[List[String]]
    //  .unsafeRunSync()    // List[String]
    //  .take(5)          // List[String]
    //  .foreach(println) // Unit

    //sql"select id, word, difficulty, times_played, times_guessed from words_spa"
    //  .query[(Int, String, Int, Option[Int], Option[Int])]
    //  .to[List](...)
    //  .transact(xa)
    //  .take(5)
    //  .unsafeRunSync()
    //  .foreach(println)

    val route = roomsRoute ~ playersRoute

    val bindingFuture = Http().newServerAt("localhost", 8080).bind(route)
    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }
}

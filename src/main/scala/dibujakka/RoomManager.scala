package dibujakka

import akka.actor.typed.scaladsl.Behaviors
import dibujakka.Room.AddRound

object RoomManager {

  sealed trait Message

  case class CreateRoom(id: Int,
                        name: String,
                        totalRounds: Int,
                        maxPlayers: Int,
                        language: String)
      extends Message

  def apply() =
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
        Room.apply(id, name, totalRounds, maxPlayers, language)
      // add other cases to send message it is necessary a ref to the adctor
    }
}

// these are from spray-json
/*implicit val matchFormat = jsonFormat2(Auction.Game)
  implicit val matchesFormat = jsonFormat1(Auction.Games)

  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem[Auction.Message] =
      ActorSystem(Auction.apply, "auction")
    // needed for the future flatMap/onComplete in the end
    implicit val executionContext: ExecutionContext = system.executionContext

    val auction: ActorRef[Auction.Message] = system
    import Auction._

    val route =
      path("auction") {
        concat(
          put {
            parameters("game".as[Int], "user") { (game, user) =>
              // place a game, fire-and-forget
              auction ! new Game(user, game)
              complete(StatusCodes.Accepted, "game placed")
            }
          },
          get {
            implicit val timeout: Timeout = 5.seconds

            // query the actor for the current auction state
            val games: Future[Games] = (auction ? GetGames).mapTo[Games]
            complete(games)
          }
        )
      }

    val bindingFuture = Http().newServerAt("localhost", 8080).bind(route)
    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }*/

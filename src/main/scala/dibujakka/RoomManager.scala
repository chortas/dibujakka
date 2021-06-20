package dibujakka

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import dibujakka.Room.{CreateRoom, Message}

object RoomManager {

  def apply(): Behavior[Message] =
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
        Room.CreateRoom(id, name, totalRounds, maxPlayers, language)
        Behaviors.same
    }
}

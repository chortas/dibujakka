package dibujakka

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import dibujakka.Room.{AddPlayer, AddRound, Message, StartRoom}

object Room {
  sealed trait Message
  case class AddRound() extends Message
  case class AddPlayer() extends Message
  case class StartRoom() extends Message

  def apply(id: Int,
            name: String,
            totalRounds: Int,
            maxPlayers: Int,
            language: String): Behavior[Message] =
    Behaviors.setup(
      context => new Room(context, id, name, totalRounds, maxPlayers, language)
    )
}

class Room(context: ActorContext[Message],
           id: Int,
           name: String,
           totalRound: Int,
           maxPlayers: Int,
           language: String)
    extends AbstractBehavior[Message](context) {

  private var _currentRound = 0
  private var _playersCount = 0
  private var _status = "waiting"

  override def onMessage(msg: Message) =
    msg match {
      case AddRound() =>
        _currentRound += 1
        Behaviors.same
      case AddPlayer() =>
        _playersCount += 1
        Behaviors.same
      case StartRoom() =>
        _status = "In progress"
        Behaviors.same
    }
}

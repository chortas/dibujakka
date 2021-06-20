package dibujakka

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import dibujakka.Room.{AddPlayer, AddRound, CreateRoom, Message, StartRoom}

object Room {
  sealed trait Message
  case class CreateRoom(id: Int,
                        name: String,
                        totalRounds: Int,
                        maxPlayers: Int,
                        language: String)
      extends Message
  case class AddRound() extends Message
  case class AddPlayer() extends Message
  case class StartRoom() extends Message

  def apply(): Behavior[Message] =
    Behaviors.setup(context => new Room(context))
}

class Room(context: ActorContext[Message])
    extends AbstractBehavior[Message](context) {

  private var _currentRound = 0
  private var _playersCount = 0
  private var _status = "waiting"
  private var _id = 0
  private var _name = ""
  private var _totalRounds = 0
  private var _maxPlayers = 0
  private var _language = ""

  override def onMessage(msg: Message) =
    msg match {
      case CreateRoom(id, name, totalRounds, maxPlayers, language) =>
        _id = id
        _name = name
        _totalRounds = totalRounds
        _maxPlayers = maxPlayers
        _language = language
        Behaviors.same
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

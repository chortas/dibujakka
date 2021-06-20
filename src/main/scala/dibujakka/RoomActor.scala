package dibujakka

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import dibujakka.RoomMessages._

object RoomActor {
  def apply(): Behavior[RoomMessage] =
    Behaviors.setup(context => new RoomActor(context))
}

class RoomActor(context: ActorContext[RoomMessage])
    extends AbstractBehavior[RoomMessage](context) {

  private var _room = None: Option[Room]

  def room = _room

  override def onMessage(msg: RoomMessage) =
    msg match {
      case GetRoom(replyTo) =>
        replyTo ! _room.get
        Behaviors.same
      case CreateRoom(id, name, totalRounds, maxPlayers, language) =>
        _room = Some(
          new Room(id, name, totalRounds, maxPlayers, language, 0, 0, "waiting")
        )
        Behaviors.same
      case AddRound() =>
        _room.foreach(room => room.addRound())
        Behaviors.same
      case AddPlayer() =>
        _room.foreach(room => room.addPlayer())
        Behaviors.same
      case StartRoom() =>
        _room.foreach(room => room.start())
        Behaviors.same
    }
}

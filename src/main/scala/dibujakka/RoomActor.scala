package dibujakka

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import dibujakka.RoomMessages._

object RoomActor {
  def apply(): Behavior[RoomMessage] =
    Behaviors.setup(context => new RoomActor(context, None))

  def apply(room: Option[Room]): Behavior[RoomMessage] =
    Behaviors.setup(context => new RoomActor(context, room))
}

class RoomActor(context: ActorContext[RoomMessage], room: Option[Room])
    extends AbstractBehavior[RoomMessage](context) {
  import RoomActor._

  override def onMessage(msg: RoomMessage) =
    msg match {
      case GetRoom(replyTo) =>
        replyTo ! room.get
        Behaviors.same
      case CreateRoom(id, name, totalRounds, maxPlayers, language) =>
        apply(
          Some(
            new Room(
              id,
              name,
              totalRounds,
              maxPlayers,
              language,
              0,
              0,
              "waiting"
            )
          )
        )
      case AddRound() =>
        room.foreach(room => room.addRound())
        Behaviors.same
      case AddPlayer() =>
        room.foreach(room => room.addPlayer())
        Behaviors.same
      case StartRoom() =>
        room.foreach(room => room.start())
        Behaviors.same
    }
}

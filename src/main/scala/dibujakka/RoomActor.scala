package dibujakka

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import dibujakka.RoomMessages._

object RoomActor {
  def apply(id: Int, name: String, totalRounds: Int, maxPlayers: Int, language: String): Behavior[RoomMessage] =
    Behaviors.setup( context => new RoomActor(context,  id, name ,totalRounds, maxPlayers, language))
    //room(id, name, totalRounds, maxPlayers, language)

  /*private def room(id: Int, name: String, totalRounds: Int, maxPlayers: Int, language: String): Behavior[RoomMessage] =
    Behaviors.receive { (context, message) =>
      Behaviors.same
    }*/
  //def apply(): Behavior[RoomMessage] =
  //  Behaviors.setup(context => new RoomActor(context))
}


class RoomActor(context: ActorContext[RoomMessage], id: Int, name: String, totalRounds: Int, maxPlayers: Int, language: String)
    extends AbstractBehavior[RoomMessage](context) {


  override def onMessage(msg: RoomMessage) =
    msg match {
      case GetRoom(replyTo) =>
        //replyTo ! _room.get
        Behaviors.same

      case AddRound() =>
        //_room.foreach(room => room.addRound())
        Behaviors.same
      case AddPlayer() =>
        //_room.foreach(room => room.addPlayer())
        Behaviors.same
      case StartRoom() =>
        //_room.foreach(room => room.start())
        Behaviors.same
    }
}


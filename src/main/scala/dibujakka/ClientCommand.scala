package dibujakka

import akka.actor.typed.ActorRef
import dibujakka.RoomMessages.{RoomMessage, SendToClients}

sealed trait ClientCommand extends RoomMessage
case class DrawMessage(replyTo: ActorRef[SendToClients], message: String)
    extends ClientCommand
case class DrawClientCommand(message: String) extends ClientCommand

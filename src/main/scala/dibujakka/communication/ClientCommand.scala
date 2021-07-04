package dibujakka.communication

import dibujakka.room.RoomMessages.RoomMessage

sealed trait ClientCommand extends RoomMessage

case class DrawClientCommand(message: String) extends ClientCommand

case class ChatClientCommand(message: String, userName: String)
    extends ClientCommand

case class StartClientCommand() extends ClientCommand

case class JoinClientCommand(message: String) extends ClientCommand
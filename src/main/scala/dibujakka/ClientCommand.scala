package dibujakka

import dibujakka.RoomMessages.RoomMessage

sealed trait ClientCommand extends RoomMessage

case class DrawClientCommand(message: String) extends ClientCommand

case class ChatClientCommand(message: String) extends ClientCommand

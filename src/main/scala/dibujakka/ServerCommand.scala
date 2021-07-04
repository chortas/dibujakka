package dibujakka

import dibujakka.RoomMessages.RoomMessage
import spray.json._

sealed trait ServerCommand extends RoomMessage {
  def toString(): String
}

case class DrawServerCommand(drawing: String) extends ServerCommand {
  override def toString(): String =
    """{ "type": "draw", "payload": "%s" }"""
      .format(drawing)
      .parseJson
      .toString()
}

case class ChatServerCommand(word: String) extends ServerCommand {
  override def toString(): String =
    """{ "type": "chat", "payload": "%s" }"""
      .format(word)
      .parseJson
      .toString()
}

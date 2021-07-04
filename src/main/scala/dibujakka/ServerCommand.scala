package dibujakka

import dibujakka.RoomMessages.RoomMessage
import spray.json._

sealed trait ServerCommand extends RoomMessage {
  def toString(): String
}

case class DrawServerCommand(drawing: String) extends ServerCommand {
  override def toString(): String =
    """{ "messageType": "draw", "payload": "%s" }"""
      .format(drawing)
      .parseJson
      .toString()
}

case class ChatServerCommand(word: String) extends ServerCommand {
  override def toString(): String =
    """{ "messageType": "chat", "payload": "%s" }"""
      .format(word)
      .parseJson
      .toString()
}

case class RoomServerCommand(room: Room) extends ServerCommand {
  override def toString(): String =
    """{ "messageType": "room", "payload": { "status": "%s", "language": "%s", "scores": "%s", "totalTime": "%d", "remainingTime": "%d", "totalRounds": "%d", "currentRound": "%d", "word": "%s", "whoIsDrawing": "%s", "guess": "%s"} }"""
      .format(room.status, room.language, "", 60, 10, room.totalRounds, room.currentRound, room.currentWord, "", "")
      .parseJson
      .toString()
}

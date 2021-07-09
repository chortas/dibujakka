package dibujakka.communication

import dibujakka.room.Room
import dibujakka.messages.DibujakkaMessages.DibujakkaMessage
import spray.json.DefaultJsonProtocol._
import spray.json._

sealed trait ServerCommand extends DibujakkaMessage {
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

case class DibujakkaServerCommand(room: Room) extends ServerCommand {

  override def toString(): String = {
    val scores = room.scores.toJson
    val players = room.players.toJson
    val playersWhoGuessed = room.playersWhoGuessed.toJson
    """{ "messageType": "room", "payload": { "status": "%s", "language": "%s", "scores": %s, "totalTime": "%d", "remainingTime": "%d", "totalRounds": "%d", "currentRound": "%d", "word": "%s", "whoIsDrawing": "%s", "playersWhoGuessed": %s, "players": %s} }"""
      .format(
        room.status,
        room.language,
        scores,
        60,
        10,
        room.totalRounds,
        room.currentRound,
        room.currentWord match {
          case Some(word) => word.text
          case None => ""
        },
        room.getDrawer,
        playersWhoGuessed,
        players
      )
      .parseJson
      .toString()
  }

  implicit object AnyJsonFormat extends JsonFormat[Any] {
    def write(x: Any): JsValue with Serializable = x match {
      case n: Int           => JsNumber(n)
      case s: String        => JsString(s)
      case b: Boolean if b  => JsTrue
      case b: Boolean if !b => JsFalse
    }
    def read(value: JsValue): Any = value match {
      case JsNumber(n) => n.intValue
      case JsString(s) => s
      case JsTrue      => true
      case JsFalse     => false
      case _           => None
    }
  }
}

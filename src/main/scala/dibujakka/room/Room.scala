package dibujakka.room

import spray.json.{
  DefaultJsonProtocol,
  JsArray,
  JsNumber,
  JsObject,
  JsString,
  JsValue,
  RootJsonFormat,
  deserializationError
}

object RoomJsonProtocol extends DefaultJsonProtocol {
  implicit object RoomJsonFormat extends RootJsonFormat[Room] {
    def write(room: Room): JsObject =
      JsObject(
        "id" -> JsString(room.id),
        "name" -> JsString(room.name),
        "totalRounds" -> JsNumber(room.totalRounds),
        "maxPlayers" -> JsNumber(room.maxPlayers),
        "language" -> JsString(room.language),
        "currentRound" -> JsNumber(room.currentRound),
        "status" -> JsString(room.status),
        "currentWord" -> JsString(room.currentWord),
        "playersCount" -> JsNumber(room.players.size),
      )

    def read(value: JsValue): Room = value match {
      case JsArray(
          Vector(
            JsString(id),
            JsString(name),
            JsNumber(totalRounds),
            JsNumber(maxPlayers),
            JsString(language),
            JsNumber(currentRound),
            JsString(status),
            JsString(currentWord),
          )
          ) =>
        new Room(
          id,
          name,
          totalRounds.toInt,
          maxPlayers.toInt,
          language,
          currentRound.toInt,
          status,
          currentWord
        )
      case _ => deserializationError("Room expected")
    }
  }
}

object Room {
  def apply(id: String,
            name: String,
            totalRounds: Int,
            maxPlayers: Int,
            language: String,
            currentRound: Int,
            status: String,
            currentWord: String,
            players: Map[String, Int] = Map.empty,
            playersWhoGuessed: List[String] = List.empty) = {
    new Room(
      id,
      name,
      totalRounds,
      maxPlayers,
      language,
      currentRound,
      status,
      currentWord,
      players,
      playersWhoGuessed
    )
  }
}

case class Room(id: String,
                name: String,
                totalRounds: Int,
                maxPlayers: Int,
                language: String,
                currentRound: Int,
                status: String,
                currentWord: String,
                players: Map[String, Int] = Map.empty,
                playersWhoGuessed: List[String] = List.empty) {

  import Room._

  // NTH: use currying. See https://www.baeldung.com/scala/currying
  def addPlayer(name: String): Room = {
    val score = players.getOrElse(name, 0)
    apply(
      id,
      name,
      totalRounds,
      maxPlayers,
      language,
      currentRound,
      status,
      currentWord,
      players.updated(name, score)
    )
  }

  def updateScores(name: String): Room = {
    val newScore = players.getOrElse(name, 0) + 1
    apply(
      id,
      name,
      totalRounds,
      maxPlayers,
      language,
      currentRound,
      status,
      currentWord,
      players.updated(name, newScore),
      playersWhoGuessed.::(name)
    )
  }

  def allPlayersGuessed: Boolean = {
    players.size == playersWhoGuessed.size
  }

  def isInLastRound: Boolean = {
    currentRound == totalRounds
  }
}

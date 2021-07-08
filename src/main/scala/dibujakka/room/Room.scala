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
        "playersCount" -> JsNumber(room.scores.size),
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
          id = id,
          name = name,
          totalRounds = totalRounds.toInt,
          maxPlayers = maxPlayers.toInt,
          language = language,
          currentRound = currentRound.toInt,
          status = status,
          currentWord = currentWord,
          scores = Map.empty,
          players = List.empty,
          whoIsDrawingIdx = 0,
          playersWhoGuessed = List.empty
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
            scores: Map[String, Int] = Map.empty,
            players: List[String] = List.empty,
            whoIsDrawingIdx: Int = 0,
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
      scores,
      players,
      whoIsDrawingIdx,
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
                scores: Map[String, Int],
                players: List[String],
                whoIsDrawingIdx: Int,
                playersWhoGuessed: List[String]) {

  import Room._

  // NTH: use currying. See https://www.baeldung.com/scala/currying
  def addPlayer(name: String): Room = {
    val score = scores.getOrElse(name, 0)
    apply(
      id = id,
      name = name,
      totalRounds = totalRounds,
      maxPlayers = maxPlayers,
      language = language,
      currentRound = currentRound,
      status = status,
      currentWord = currentWord,
      scores = scores.updated(name, score),
      players = players.::(name),
      whoIsDrawingIdx = whoIsDrawingIdx,
      playersWhoGuessed = playersWhoGuessed
    )
  }

  def updateScores(name: String): Room = {
    val newScore = scores.getOrElse(name, 0) + 1
    apply(
      id = id,
      name = name,
      totalRounds = totalRounds,
      maxPlayers = maxPlayers,
      language = language,
      currentRound = currentRound,
      status = status,
      currentWord = currentWord,
      scores = scores.updated(name, newScore),
      players = players,
      whoIsDrawingIdx = whoIsDrawingIdx,
      playersWhoGuessed = playersWhoGuessed.::(name)
    )
  }

  def allPlayersGuessed: Boolean = {
    scores.size == playersWhoGuessed.size
  }

  def hasFinishedAllRounds: Boolean = {
    currentRound == totalRounds
  }

  def playerHasGuessed(userName: String): Boolean = {
    playersWhoGuessed contains userName
  }
}

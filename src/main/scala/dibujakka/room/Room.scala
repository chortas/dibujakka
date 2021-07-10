package dibujakka.room

import dibujakka.persistence.Word
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
          currentWord = None,
          scores = Map.empty,
          players = List.empty,
          whoIsDrawingIdx = 0,
          playersWhoGuessed = List.empty,
          wordsIdsPlayed = List.empty
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
            currentWord: Option[Word],
            scores: Map[String, Int] = Map.empty,
            players: List[String] = List.empty,
            whoIsDrawingIdx: Int = 0,
            playersWhoGuessed: List[String] = List.empty,
            wordsIdsPlayed: List[Int] = List.empty) = {
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
      playersWhoGuessed,
      wordsIdsPlayed
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
                currentWord: Option[Word],
                scores: Map[String, Int],
                players: List[String],
                whoIsDrawingIdx: Int,
                playersWhoGuessed: List[String],
                wordsIdsPlayed: List[Int]) {
  import Room._

  def addWord(wordToAdd: Option[Word]): Room = {
    wordToAdd match {
      case Some(word) =>
        apply(
          id = id,
          name = name,
          totalRounds = totalRounds,
          maxPlayers = maxPlayers,
          language = language,
          currentRound = currentRound,
          status = status,
          currentWord = Some(word),
          scores = scores,
          players = players,
          whoIsDrawingIdx = whoIsDrawingIdx,
          playersWhoGuessed = playersWhoGuessed,
          wordsIdsPlayed = wordsIdsPlayed.::(word.id)
        )
      case None =>
        apply(
          id = id,
          name = name,
          totalRounds = totalRounds,
          maxPlayers = maxPlayers,
          language = language,
          currentRound = currentRound,
          status = status,
          currentWord = currentWord,
          scores = scores,
          players = players,
          whoIsDrawingIdx = whoIsDrawingIdx,
          playersWhoGuessed = playersWhoGuessed,
          wordsIdsPlayed = wordsIdsPlayed
        )
    }
  }

  // NTH: use currying. See https://www.baeldung.com/scala/currying
  def addPlayer(userName: String): Room = {
    val score = scores.getOrElse(userName, 0)
    val newPlayers =
      if (playerIsInRoom(userName)) players else players.::(userName)
    apply(
      id = id,
      name = name,
      totalRounds = totalRounds,
      maxPlayers = maxPlayers,
      language = language,
      currentRound = currentRound,
      status = status,
      currentWord = currentWord,
      scores = scores.updated(userName, score),
      players = newPlayers,
      whoIsDrawingIdx = whoIsDrawingIdx,
      playersWhoGuessed = playersWhoGuessed,
      wordsIdsPlayed = wordsIdsPlayed
    )
  }

  def canStart: Boolean = {
    players.size > 1
  }

  def getDrawer: String = {
    players(whoIsDrawingIdx)
  }

  def isDrawing(userName: String): Boolean = {
    userName.equals(getDrawer)
  }

  def nextDrawingIndex: Int = {
    (whoIsDrawingIdx + 1) % players.size
  }

  def updateScores(userName: String): Room = {
    var newScore = scores.getOrElse(userName, 0)
    var newPlayersWhoGuessed = playersWhoGuessed
    if (isDrawing(userName)) {
      newScore += playersWhoGuessed.size * 2
    } else {
      // give more points to the player who guesses earlier
      newScore += players.size - playersWhoGuessed.size
      newPlayersWhoGuessed = newPlayersWhoGuessed.::(userName)
    }
    apply(
      id = id,
      name = name,
      totalRounds = totalRounds,
      maxPlayers = maxPlayers,
      language = language,
      currentRound = currentRound,
      status = status,
      currentWord = currentWord,
      scores = scores.updated(userName, newScore),
      players = players,
      whoIsDrawingIdx = whoIsDrawingIdx,
      playersWhoGuessed = newPlayersWhoGuessed,
      wordsIdsPlayed = wordsIdsPlayed
    )
  }

  def allPlayersGuessed: Boolean = {
    playersWhoGuessed.size == (players.size - 1) // the player that is drawing cant guess
  }

  def hasFinishedAllRounds: Boolean = {
    currentRound == totalRounds
  }

  def playerHasGuessed(userName: String): Boolean = {
    playersWhoGuessed.contains(userName)
  }

  def playerIsInRoom(userName: String): Boolean = {
    players.contains(userName)
  }

  def hasBeenPlayed(word: Option[Word]): Boolean = {
    word.isDefined && wordsIdsPlayed.contains(word.get.id)
  }
}

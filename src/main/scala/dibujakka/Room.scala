package dibujakka

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{
  DefaultJsonProtocol,
  JsArray,
  JsNumber,
  JsString,
  JsValue,
  RootJsonFormat
}

class Room(var id: Int,
           var name: String,
           var totalRounds: Int,
           var maxPlayers: Int,
           var language: String,
           var currentRound: Int,
           var playersCount: Int,
           var status: String) {

  def addRound() = currentRound += 1
  def addPlayer() = playersCount += 1
  def start() = status = "In progress"
}

object RoomProtocol extends DefaultJsonProtocol with SprayJsonSupport {
  implicit object RoomJsonFormat extends RootJsonFormat[Room] {
    def write(r: Room) =
      JsArray(
        JsNumber(r.id),
        JsString(r.name),
        JsNumber(r.totalRounds),
        JsNumber(r.maxPlayers),
        JsString(r.language),
        JsNumber(r.currentRound),
        JsNumber(r.playersCount),
        JsString(r.status)
      )

    def read(value: JsValue) = value match {
      case JsArray(
          Vector(
            JsNumber(id),
            JsString(name),
            JsNumber(totalRounds),
            JsNumber(maxPlayers),
            JsString(language),
            JsNumber(currentRound),
            JsNumber(playersCount),
            JsString(status)
          )
          ) =>
        new Room(
          id.intValue,
          name,
          totalRounds.intValue,
          maxPlayers.intValue,
          language,
          currentRound.intValue,
          playersCount.intValue,
          status
        )
    }
  }
}

package dibujakka.communication

import spray.json.{
  DefaultJsonProtocol,
  JsObject,
  JsString,
  JsValue,
  RootJsonFormat
}

abstract class WebSocketType(val messageType: String)
case class WebSocketMessage(override val messageType: String, payload: String)
    extends WebSocketType(messageType)
case class WebSocketChatMessage(override val messageType: String,
                                word: String,
                                userName: String)
    extends WebSocketType(messageType)

object WebSocketMessageProtocol extends DefaultJsonProtocol {
  implicit object WebSocketMessageJsonFormat
      extends RootJsonFormat[WebSocketMessage] {
    def write(webSocketMessage: WebSocketMessage): JsObject =
      JsObject(
        "messageType" -> JsString(webSocketMessage.messageType),
        "payload" -> JsString(webSocketMessage.payload),
      )

    def read(value: JsValue): WebSocketMessage = {
      val fields = value.asJsObject("Web Socket Message expected").fields
      WebSocketMessage(
        messageType = fields("messageType").convertTo[String],
        payload = fields("payload").convertTo[String]
      )
    }
  }
}

object WebSocketChatMessageProtocol extends DefaultJsonProtocol {

  implicit object WebSocketChatMessageJsonFormat
      extends RootJsonFormat[WebSocketChatMessage] {
    def write(webSocketChatMessage: WebSocketChatMessage): JsObject =
      JsObject(
        "messageType" -> JsString(webSocketChatMessage.messageType),
        "word" -> JsString(webSocketChatMessage.word),
        "userName" -> JsString(webSocketChatMessage.userName),
      )

    def read(value: JsValue): WebSocketChatMessage = {
      val fields = value.asJsObject("Web Socket Chat Message expected").fields
      WebSocketChatMessage(
        messageType = fields("messageType").convertTo[String],
        word = fields("word").convertTo[String],
        userName = fields("userName").convertTo[String],
      )
    }
  }
}

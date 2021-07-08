package dibujakka.communication

import dibujakka.messages.DibujakkaMessages.DibujakkaMessage

sealed trait ClientCommand extends DibujakkaMessage

case class DrawClientCommand(message: String) extends ClientCommand

case class ChatClientCommand(message: String, userName: String)
    extends ClientCommand

case class StartClientCommand() extends ClientCommand

case class JoinClientCommand(message: String) extends ClientCommand

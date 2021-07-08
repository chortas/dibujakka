package dibujakka.persistence

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors._
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext}
import dibujakka.Server.system._
import doobie._
import doobie.implicits._
import cats._
import cats.effect._
import cats.implicits._
import dibujakka.messages.DibujakkaMessages.{DibujakkaMessage, GetWord}
import doobie.util.transactor.Transactor.Aux

object DbActor {
  def apply(): Behavior[DibujakkaMessage] =
    setup(context => new DbActor(context))

}
// TODO try functional style
class DbActor(context: ActorContext[DibujakkaMessage]) extends AbstractBehavior[DibujakkaMessage](context) {
  import DbActor._

  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContexts.synchronous)
  val transaction: Aux[IO, Unit] = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver",
    "jdbc:postgresql:dibujakka-pg",
    "postgres",
    "A1noko",
    Blocker.liftExecutionContext(ExecutionContexts.synchronous)
  )

  override def onMessage(msg: DibujakkaMessage): Behavior[DibujakkaMessage] =
    msg match {
      case GetWord(replyTo) =>
        val word: Option[Word] = sql"select id, word, difficulty, times_played, times_guessed from words_spa order by random() limit 1"
          .query[Word]
          .option
          .transact(transaction)
          .unsafeRunSync()
        replyTo ! word
        this
      case _ =>
        this
    }
}

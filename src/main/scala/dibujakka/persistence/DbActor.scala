package dibujakka.persistence

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors._
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext}
import cats.effect._
import dibujakka.messages.DibujakkaMessages.{DibujakkaMessage, GetWord, UpdateWordMetrics}
import doobie._
import doobie.implicits._
import doobie.util.transactor.Transactor.Aux

object DbActor {
  implicit val cs: ContextShift[IO] =
    IO.contextShift(ExecutionContexts.synchronous)

  val transactor: Aux[IO, Unit] = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver",
    "jdbc:postgresql:dibujakka-pg",
    sys.env.getOrElse("DB_USER", "postgres"),
    sys.env.getOrElse("DB_PASS", ""),
    Blocker.liftExecutionContext(ExecutionContexts.synchronous)
  )

  def apply(): Behavior[DibujakkaMessage] =
    dbActor

  private def dbActor: Behavior[DibujakkaMessage] =
    receive({(_, message) => message match {
        case GetWord(replyTo) =>
          val word: Option[Word] =
          sql"select id, word, difficulty, times_played, times_guessed from words_spa order by random() limit 1"
            .query[Word]
            .option
            .transact(transactor)
            .unsafeRunSync()
          replyTo ! word
          same
        case UpdateWordMetrics(word, wasGuessed) =>
          val timesGuessed =
          if (wasGuessed) word.timesGuessed + 1 else word.timesGuessed
          val timesPlayed = word.timesPlayed + 1
          val id = word.id
          sql"update words_spa set times_guessed = $timesGuessed, times_played = $timesPlayed where id = $id".update.run
            .transact(transactor)
            .unsafeRunSync()
          same
        case _ =>
          same
      }})
}

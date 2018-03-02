package com.gifty

import com.gifty.model._
import slick.jdbc.PostgresProfile.api._

object AppStarter extends App {
  val connectionUrl = "jdbc:postgresql://localhost/gifty_database?user=gifty_admin&password=LeDtQw9m"
  val db = Database.forURL(connectionUrl, driver = "org.postgresql.Driver")
  val (gift, question, answer) = (TableQuery[GiftModel], TableQuery[QuestionModel], TableQuery[AnswerModel])
  val setup = DBIO.seq(
    (gift.schema ++ question.schema ++ answer.schema).create
  )
  val setupFuture = db.run(setup)

  GiftyBot.run()
}

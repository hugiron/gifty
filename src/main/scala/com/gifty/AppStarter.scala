package com.gifty

import com.gifty.model._
import slick.jdbc.PostgresProfile.api._
import slick.jdbc.meta.MTable

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global

object AppStarter extends App {
  val connectionUrl = "jdbc:postgresql://localhost/gifty_database?user=gifty_admin&password=LeDtQw9m"
  val db = Database.forURL(connectionUrl, driver = "org.postgresql.Driver")
  val (gift, question, answer) = (TableQuery[GiftModel], TableQuery[QuestionModel], TableQuery[AnswerModel])

  val tables = List(gift, question, answer)

  val existing = db.run(MTable.getTables)
  val setup    = existing.flatMap( v => {
    val names = v.map(mt => mt.name.name)
    val createIfNotExist = tables.filter( table =>
      (!names.contains(table.baseTableRow.tableName))).map(_.schema.create)
    db.run(DBIO.sequence(createIfNotExist))
  })

  Await.result(setup, Duration.Inf)

  GiftyBot.run()
}

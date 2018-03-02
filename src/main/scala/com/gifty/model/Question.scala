package com.gifty.model

import slick.jdbc.PostgresProfile.api._

case class Question(id: Int, question: String)

class QuestionModel(tag: Tag) extends Table[Question](tag, "Questions") {
  def id = column[Int]("id", O.AutoInc, O.PrimaryKey)
  def question = column[String]("question", O.Unique)

  def * = (id, question) <> (Question.tupled, Question.unapply)
}

object QuestionModel {
  val table = TableQuery[QuestionModel]
}

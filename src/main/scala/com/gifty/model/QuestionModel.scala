package com.gifty.model

import slick.jdbc.PostgresProfile.api._

class QuestionModel(tag: Tag) extends Table[(Int, String)](tag, "Questions") {
  def id = column[Int]("id", O.AutoInc, O.PrimaryKey)
  def question = column[String]("question", O.Unique)

  def * = (id, question)
}

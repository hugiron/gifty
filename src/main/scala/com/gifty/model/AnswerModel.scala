package com.gifty.model

import slick.jdbc.PostgresProfile.api._

//case class Answer(giftId: Int, questionId: Int, yesCount: Int, noCount: Int, idkCount: Int)

class AnswerModel(tag: Tag) extends Table[(Int, Int, Int, Int, Int)](tag, "Answers") {
  def giftId = column[Int]("gift_id")
  def questionId = column[Int]("question_id")
  def yesCount = column[Int]("yes_count")
  def noCount = column[Int]("no_count")
  def idkCount = column[Int]("idk_count")

  def * = (giftId, questionId, yesCount, noCount, idkCount)// <> (Answer.tupled, Answer.unapply _)
  def gift = foreignKey("gift_fk", giftId, TableQuery[GiftModel])(_.id, onDelete = ForeignKeyAction.Cascade)
  def question = foreignKey("question_fk", questionId, TableQuery[QuestionModel])(_.id, onDelete = ForeignKeyAction.Cascade)
}

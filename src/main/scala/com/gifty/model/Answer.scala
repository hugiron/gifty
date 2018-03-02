package com.gifty.model

import slick.jdbc.PostgresProfile.api._

case class Answer(giftId: Int, questionId: Int, yesCount: Int, noCount: Int, idkCount: Int)

class AnswerModel(tag: Tag) extends Table[Answer](tag, "Answers") {
  def giftId = column[Int]("gift_id")
  def questionId = column[Int]("question_id")
  def yesCount = column[Int]("yes_count")
  def noCount = column[Int]("no_count")
  def idkCount = column[Int]("idk_count")

  def * = (giftId, questionId, yesCount, noCount, idkCount) <> (Answer.tupled, Answer.unapply)
  def idx = index("answer_index", (giftId, questionId), unique = true)
  def gift = foreignKey("gift_fk", giftId, GiftModel.table)(_.id, onDelete = ForeignKeyAction.Cascade)
  def question = foreignKey("question_fk", questionId, QuestionModel.table)(_.id, onDelete = ForeignKeyAction.Cascade)
}

object AnswerModel {
  val table = TableQuery[AnswerModel]
}

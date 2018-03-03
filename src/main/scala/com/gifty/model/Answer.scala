package com.gifty.model

import slick.jdbc.PostgresProfile.api._

case class Answer(giftId: Int,
                  questionId: Int,
                  posYesCount: Int,
                  posNoCount: Int,
                  posIdkCount: Int,
                  negYesCount: Int,
                  negNoCount: Int,
                  negIdkCount: Int)

class AnswerModel(tag: Tag) extends Table[Answer](tag, "Answers") {
  def giftId = column[Int]("gift_id")
  def questionId = column[Int]("question_id")
  def posYesCount = column[Int]("pos_yes_count", O.Default(1))
  def posNoCount = column[Int]("pos_no_count", O.Default(1))
  def posIdkCount = column[Int]("pos_idk_count", O.Default(1))
  def negYesCount = column[Int]("neg_yes_count", O.Default(1))
  def negNoCount = column[Int]("neg_no_count", O.Default(1))
  def negIdkCount = column[Int]("neg_idk_count", O.Default(1))

  def * = (giftId, questionId, posYesCount, posNoCount, posIdkCount, negYesCount, negNoCount, negIdkCount) <> (Answer.tupled, Answer.unapply)
  def giftIdx = index("gift_index", giftId)
  def questionIdx = index("question_index", questionId)
  def gift = foreignKey("gift_fk", giftId, GiftModel.table)(_.id, onDelete = ForeignKeyAction.Cascade)
  def question = foreignKey("question_fk", questionId, QuestionModel.table)(_.id, onDelete = ForeignKeyAction.Cascade)
}

object AnswerModel {
  val table = TableQuery[AnswerModel]
}

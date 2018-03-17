package com.gifty

import com.gifty.Storage._
import com.gifty.Implicits._
import com.gifty.enum.AnswerType
import com.gifty.model.{GiftModel, History, QuestionModel}
import com.gifty.util.{NaiveBayes, Session}
import org.nd4s.Implicits._
import info.mukel.telegrambot4s.api.{Polling, TelegramBot}
import info.mukel.telegrambot4s.api.declarative.{Callbacks, Commands}
import info.mukel.telegrambot4s.methods._
import info.mukel.telegrambot4s.models.{CallbackQuery}
import org.nd4j.linalg.factory.Nd4j
import slick.jdbc.PostgresProfile.api._

import scala.util.Random

object GiftyBot extends TelegramBot with Polling with Commands with Callbacks {
  override def token: String = AppConfig.token

  onCommand(AppConfig.startCommand) { implicit msg =>
    postgres.run(GiftModel.table.length.result).map(count => {
      val posGifts = Nd4j.ones(count)
      val negGifts = Nd4j.ones(count)
      NaiveBayes.getNextQuestion(posGifts, negGifts, Set[Int]()).map(questionId => {
        val history = History()
        postgres.run(QuestionModel.table.filter(_.id === questionId).result).map(questions => {
          request(SendMessage(msg.source, questions.head.question, replyMarkup = Some(AppConfig.questionButtons))).map(message => {
            val sessionId = message.toSessionId
            Session.setPosGifts(sessionId, posGifts)
            Session.setNegGifts(sessionId, negGifts)
            Session.setLastQuestion(sessionId, questionId)
            Session.setQuestions(sessionId, Set[Int](questionId))
            Session.setHistory(sessionId, history)
          })
        })
      })
    })
  }

  onCallbackWithTag(AppConfig.yesButton._1) { implicit cbq =>
    questionButtonClick(cbq, AnswerType.Yes)
  }

  onCallbackWithTag(AppConfig.noButton._1) { implicit cbq =>
    questionButtonClick(cbq, AnswerType.No)
  }

  onCallbackWithTag(AppConfig.notKnowButton._1) { implicit cbq =>
    val sessionId = cbq.toSessionId.get
    Session.getNegGifts(sessionId).map { case Some(negGifts) =>
      Session.getPosGifts(sessionId).map { case Some(posGifts) =>
        Session.getQuestions(sessionId).map(questions => {
          NaiveBayes.getNextQuestion(posGifts, negGifts, questions.getOrElse(Set[Int]())).map(nextQuestion => {
            Session.setQuestions(sessionId, questions.getOrElse(Set[Int]()) + nextQuestion)
            Session.setLastQuestion(sessionId, nextQuestion)
            val query = QuestionModel.table.filter(_.id === nextQuestion)
            postgres.run(query.result).map(question => {
              request(EditMessageText(Some(cbq.message.get.source),
                Some(cbq.message.get.messageId),
                text = question.head.question,
                replyMarkup = Some(AppConfig.questionButtons)))
            })
          })
        })
      }
    }
  }

  onCallbackWithTag(AppConfig.acceptedButton._1) { implicit cbq =>
    val sessionId = cbq.toSessionId.get
    Session.getHistory(sessionId).map { case Some(history) =>
      Session.getLastGift(sessionId).map { case Some(giftId) =>
        for (item <- history) {
          val questionId = item._1
          val query = item._2 match {
            case AnswerType.Yes => sqlu"""UPDATE "Answers" SET pos_yes_count = pos_yes_count + 1 WHERE gift_id = ${giftId} AND question_id = ${questionId}"""
            case AnswerType.No => sqlu"""UPDATE "Answers" SET pos_no_count = pos_no_count + 1 WHERE gift_id = ${giftId} AND question_id = ${questionId}"""
            case AnswerType.NotKnow => sqlu"""UPDATE "Answers" SET pos_idk_count = pos_idk_count + 1 WHERE gift_id = ${giftId} AND question_id = ${questionId}"""
          }
          postgres.run(query)
        }
        val query = sqlu"""UPDATE "Gifts" SET likes = likes + 1 WHERE id = ${giftId}"""
        postgres.run(query)
      }
    }
    request(EditMessageReplyMarkup(Some(cbq.message.get.source), Some(cbq.message.get.messageId)))
    request(SendMessage(cbq.message.get.source, AppConfig.continuationBody, replyMarkup = Some(AppConfig.continuationButtons)))
      .map(message => {
        Session.copyTo(sessionId, message.toSessionId).onComplete { case _ =>
          Session.deleteSession(sessionId)
        }
    })
  }

  onCallbackWithTag(AppConfig.rejectedButton._1) { implicit cbq =>
    val sessionId = cbq.toSessionId.get
    Session.getLastGift(sessionId).map { case Some(giftId) =>
      Session.getHistory(sessionId).map { case Some(history) =>
        for (item <- history) {
          val questionId = item._1
          val query = item._2 match {
            case AnswerType.Yes => sqlu"""UPDATE "Answers" SET neg_yes_count = neg_yes_count + 1 WHERE gift_id = ${giftId} AND question_id = ${questionId}"""
            case AnswerType.No => sqlu"""UPDATE "Answers" SET neg_no_count = neg_no_count + 1 WHERE gift_id = ${giftId} AND question_id = ${questionId}"""
            case AnswerType.NotKnow => sqlu"""UPDATE "Answers" SET neg_idk_count = neg_idk_count + 1 WHERE gift_id = ${giftId} AND question_id = ${questionId}"""
          }
          postgres.run(query)
        }
        val query = sqlu"""UPDATE "Gifts" SET dislikes = dislikes + 1 WHERE id = ${giftId}"""
        postgres.run(query)
      }
      Session.getPosGifts(sessionId).map { case Some(posGifts) =>
        Session.getNegGifts(sessionId).map { case Some(negGifts) =>
          posGifts(giftId - 1) = 0
          negGifts(giftId - 1) = 0
          Session.setPosGifts(sessionId, posGifts)
          Session.setNegGifts(sessionId, negGifts)
          Session.getQuestions(sessionId).map(questions => {
            NaiveBayes.getNextQuestion(posGifts, negGifts, questions.getOrElse(Set[Int]())).map(nextQuestion => {
              Session.setQuestions(sessionId, questions.getOrElse(Set[Int]()) + nextQuestion)
              Session.setLastQuestion(sessionId, nextQuestion).onSuccess {
                case _ =>
                  val query = QuestionModel.table.filter(_.id === nextQuestion)
                  postgres.run(query.result).map(question => {
                    request(EditMessageText(Some(cbq.message.get.source),
                      Some(cbq.message.get.messageId),
                      text = question.head.question,
                      replyMarkup = Some(AppConfig.questionButtons)))
                  })
              }
            })
          })
        }
      }
    }
  }

  onCallbackWithTag(AppConfig.continueButton._1) { implicit cbq =>
    val sessionId = cbq.toSessionId.get
    Session.getPosGifts(sessionId).map { case Some(posGifts) =>
      Session.getNegGifts(sessionId).map { case Some(negGifts) =>
        Session.getLastGift(sessionId).map { case Some(lastGift) =>
          posGifts(lastGift - 1) = 0
          negGifts(lastGift - 1) = 0
          Session.setPosGifts(sessionId, posGifts)
          Session.setNegGifts(sessionId, negGifts)
          Session.getQuestions(sessionId).map(questions => {
            NaiveBayes.getNextQuestion(posGifts, negGifts, questions.getOrElse(Set[Int]())).map(questionId => {
              postgres.run(QuestionModel.table.filter(_.id === questionId).result).map(questions => {
                request(EditMessageText(Some(cbq.message.get.source), Some(cbq.message.get.messageId),
                  text = questions.head.question, replyMarkup = Some(AppConfig.questionButtons)))
              })
              Session.setQuestions(sessionId, questions.getOrElse(Set[Int]()) + questionId)
              Session.setLastQuestion(sessionId, questionId)
            })
          })
        }
      }
    }
  }

  onCallbackWithTag(AppConfig.stopButton._1) { implicit cbq =>
    val sessionId = cbq.toSessionId.get
    request(DeleteMessage(cbq.message.get.source, cbq.message.get.messageId))
    Session.deleteSession(sessionId)
  }

  private def questionButtonClick(cbq: CallbackQuery, answer: AnswerType.Value): Unit = {
    val sessionId = cbq.toSessionId.get
    Session.getHistory(sessionId).map { case Some(history) =>
      Session.getPosGifts(sessionId).map { case Some(cachePosGifts) =>
        Session.getNegGifts(sessionId).map { case Some(cacheNegGifts) =>
          Session.getLastQuestion(sessionId).map { case Some(lastQuestion) =>
            val gifts = NaiveBayes.getGifts(cachePosGifts, cacheNegGifts, lastQuestion, answer)
            gifts._1.map { case posGifts =>
              gifts._2.map { case negGifts =>
                val normPos = Nd4j.max(posGifts)
                val normNeg = Nd4j.max(negGifts)
                if (normPos > 0)
                  posGifts /= normPos
                if (normNeg > 0)
                  negGifts /= normNeg

                Session.setPosGifts(sessionId, posGifts)
                Session.setNegGifts(sessionId, negGifts)
                history.add(lastQuestion, answer)
                Session.setHistory(sessionId, history)

                val curGifts = posGifts - negGifts
                curGifts -= Nd4j.min(curGifts)
                val norm = Nd4j.max(curGifts)
                if (norm > 0)
                  curGifts /= norm
                val tmpGifts = Nd4j.create(curGifts.length)
                tmpGifts(0 -> tmpGifts.length) = curGifts(0 -> curGifts.length)
                val sortedGifts = Nd4j.sort(tmpGifts, false)
                if ((sortedGifts(0) - sortedGifts(1) >= AppConfig.threshold || history.buffer.length % AppConfig.maxStepCount == 0) &&
                  history.buffer.lengthCompare(AppConfig.minStepCount) > 0) {
                  val giftsWithIndex = curGifts.toArray.zipWithIndex
                  val giftsFilter = giftsWithIndex.filter(_._1 > (1 - AppConfig.threshold))
                  val giftsArray = giftsFilter.length match {
                    case 0 => giftsWithIndex.filter(_._1 == sortedGifts(0))
                    case _ => giftsFilter
                  }
                  val giftId = giftsArray(Random.nextInt(giftsArray.length))._2 + 1
                  postgres.run(GiftModel.table.filter(_.id === giftId).take(1).result).map(dbGifts => {
                    val gift = dbGifts.head
                    request(EditMessageText(Some(cbq.message.get.source),
                      Some(cbq.message.get.messageId),
                      text = AppConfig.giftBody.replace("{name}", gift.name).replace("{url}", gift.url),
                      replyMarkup = Some(AppConfig.giftButtons)))
                  })
                  Session.setLastGift(sessionId, giftId)
                } else {
                  Session.getQuestions(sessionId).map(questions => {
                    NaiveBayes.getNextQuestion(posGifts, negGifts, questions.getOrElse(Set[Int]())).map(nextQuestion => {
                      Session.setQuestions(sessionId, questions.getOrElse(Set[Int]()) + nextQuestion)
                      Session.setLastQuestion(sessionId, nextQuestion)
                      val query = QuestionModel.table.filter(_.id === nextQuestion)
                      postgres.run(query.take(1).result).map(question => {
                        request(EditMessageText(Some(cbq.message.get.source),
                          Some(cbq.message.get.messageId),
                          text = question.head.question,
                          replyMarkup = Some(AppConfig.questionButtons)))
                      })
                    })
                  })
                }
              }
            }
          }
        }
      }
    }
  }
}
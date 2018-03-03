package com.gifty

import com.gifty.Storage._
import com.gifty.Implicits._
import com.gifty.enum.AnswerType
import com.gifty.model.{AnswerModel, GiftModel, History, QuestionModel}
import com.gifty.util.{NaiveBayes, Session}
import org.nd4s.Implicits._
import info.mukel.telegrambot4s.api.{Polling, TelegramBot}
import info.mukel.telegrambot4s.api.declarative.{Callbacks, Commands}
import info.mukel.telegrambot4s.methods._
import info.mukel.telegrambot4s.models.CallbackQuery
import org.nd4j.linalg.factory.Nd4j
import slick.jdbc.PostgresProfile.api._

import scala.util.Random

object GiftyBot extends TelegramBot with Polling with Commands with Callbacks {
  override def token: String = AppConfig.token

  onCommand(AppConfig.startCommand) { implicit msg =>
    request(SendMessage(msg.source, AppConfig.greetingBody, replyMarkup = Some(AppConfig.startButtons)))
  }

  onCommand(AppConfig.helpCommand) { implicit msg =>
    request(SendMessage(msg.source, AppConfig.helpBody))
  }

  onCallbackWithTag(AppConfig.startButton._1) { implicit cbq =>
    val sessionId = cbq.toSessionId.get
    postgres.run(GiftModel.table.sortBy(_.id.asc).map(_.likes).result).map(likes => {
      val gifts = likes.toNDArray
      val sum = Nd4j.sum(gifts)
      gifts /= sum
      NaiveBayes.getNextQuestion(gifts).map(questionId => {
        val history = History()
        postgres.run(QuestionModel.table.filter(_.id === questionId).result).map(questions => {
          request(EditMessageText(Some(cbq.message.get.source), Some(cbq.message.get.messageId),
            text = questions.head.question, replyMarkup = Some(AppConfig.questionButtons)))
        })
        Session.setGifts(sessionId, gifts)
        Session.setLastQuestion(sessionId, questionId)
        Session.setHistory(sessionId, history)
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
    questionButtonClick(cbq, AnswerType.NotKnow)
  }

  onCallbackWithTag(AppConfig.acceptedButton._1) { implicit cbq =>
    val sessionId = cbq.toSessionId.get
    Session.getHistory(sessionId).map { case Some(history) =>
      Session.getLastGift(sessionId).map { case Some(giftId) =>
        for (item <- history) {
          val questionId = item._1
          val query = item._2 match {
            case AnswerType.Yes => sqlu"""UPDATE "Answers" SET yes_count = yes_count + 1 WHERE gift_id = ${giftId} AND question_id = ${questionId}"""
            case AnswerType.No => sqlu"""UPDATE "Answers" SET no_count = no_count + 1 WHERE gift_id = ${giftId} AND question_id = ${questionId}"""
            case AnswerType.NotKnow => sqlu"""UPDATE "Answers" SET idk_count = idk_count + 1 WHERE gift_id = ${giftId} AND question_id = ${questionId}"""
          }
          postgres.run(query)
        }
        val query = sqlu"""UPDATE "Gifts" SET likes = likes + 1 WHERE id = ${giftId}"""
        postgres.run(query)
        Session.deleteSession(sessionId)
      }
    }
    request(EditMessageReplyMarkup(Some(cbq.message.get.source), Some(cbq.message.get.messageId)))
  }

  onCallbackWithTag(AppConfig.rejectedButton._1) { implicit cbq =>
    val sessionId = cbq.toSessionId.get
    Session.getLastGift(sessionId).map { case Some(giftId) =>
      Session.getGifts(sessionId).map { case Some(gifts) =>
        gifts(giftId - 1) = 0
        val norm = Nd4j.max(gifts)
        if (norm > 0)
          gifts /= norm
        Session.setGifts(sessionId, gifts).onSuccess {
          case _ =>
            NaiveBayes.getNextQuestion(gifts).map(nextQuestion => {
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
        }
      }
    }
  }

  private def questionButtonClick(cbq: CallbackQuery, answer: AnswerType.Value): Unit = {
    val sessionId = cbq.toSessionId.get
    Session.getHistory(sessionId).map { case Some(history) =>
      Session.getGifts(sessionId).map { case Some(cacheGifts) =>
        Session.getLastQuestion(sessionId).map { case Some(lastQuestion) =>
          NaiveBayes.getGifts(cacheGifts, lastQuestion, answer).map(gifts => {
            Session.setGifts(sessionId, gifts)
            history.add(lastQuestion, answer)
            Session.setHistory(sessionId, history)

            val tmpGifts = Nd4j.create(gifts.length)
            tmpGifts(0 -> tmpGifts.length) = gifts(0 -> gifts.length)
            val sortedGifts = Nd4j.sort(tmpGifts, false)
            if ((sortedGifts(0) - sortedGifts(1) >= AppConfig.threshold || history.buffer.length % AppConfig.maxStepCount == 0) &&
              history.buffer.lengthCompare(AppConfig.minStepCount) > 0) {
              val giftsArray = gifts.toArray.zipWithIndex.filter(_._1 > (1 - AppConfig.threshold))
              val giftId = giftsArray(Random.nextInt(giftsArray.length))._2 + 1
              postgres.run(GiftModel.table.filter(_.id === giftId).result).map(dbGifts => {
                val gift = dbGifts.head
                // TODO: Реализовать полноценный ответ
                request(EditMessageText(Some(cbq.message.get.source),
                  Some(cbq.message.get.messageId),
                  text = gift.url,
                  replyMarkup = Some(AppConfig.giftButtons)))
              })
              Session.setLastGift(sessionId, giftId)
            } else {
              NaiveBayes.getNextQuestion(gifts).map(nextQuestion => {
                Session.setLastQuestion(sessionId, nextQuestion)
                val query = QuestionModel.table.filter(_.id === nextQuestion)
                postgres.run(query.result).map(question => {
                  request(EditMessageText(Some(cbq.message.get.source),
                    Some(cbq.message.get.messageId),
                    text = question.head.question,
                    replyMarkup = Some(AppConfig.questionButtons)))
                })
              })
            }
          })
        }
      }
    }
  }
}
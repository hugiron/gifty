package com.gifty

import com.gifty.Storage._
import com.gifty.Implicits._
import com.gifty.model.{AnswerModel, GiftModel, QuestionModel}
import com.gifty.util.{NaiveBayes, Session}
import org.nd4s.Implicits._
import info.mukel.telegrambot4s.api.{Polling, TelegramBot}
import info.mukel.telegrambot4s.api.declarative.{Callbacks, Commands}
import info.mukel.telegrambot4s.methods._
import org.nd4j.linalg.factory.Nd4j
import slick.jdbc.PostgresProfile.api._

object GiftyBot extends TelegramBot with Polling with Commands with Callbacks {
  override def token: String = AppConfig.token

  onCommand(AppConfig.startCommand) { implicit msg =>
    println("Hello World!")
    val text = "https://vsedrugoeshop.ru/catalog/dlya_rasteniy/rastenie_v_banke_novogodnyaya_sosna/"
    request(SendMessage(msg.source, text, replyMarkup = Some(AppConfig.giftButtons)))
  }

  onCommand(AppConfig.helpCommand) { implicit msg =>

  }

  onCallbackWithTag(AppConfig.yesButton._1) { implicit cbq =>

  }

  onCallbackWithTag(AppConfig.noButton._1) { implicit cbq =>

  }

  onCallbackWithTag(AppConfig.notKnowButton._1) { implicit cbq =>

  }

  onCallbackWithTag(AppConfig.acceptedButton._1) { implicit cbq =>
    val sessionId = cbq.toSessionId.get
    Session.getHistory(sessionId).map { case Some(history) =>
      Session.getLastGift(sessionId).map { case Some(giftId) =>
        for (item <- history) {
          val query =
            sqlu"""UPDATE "Answers"
                  SET ${item._2.toString.toLowerCase} = ${item._2.toString.toLowerCase} + 1
                  WHERE gift_id = ${giftId} AND question_id = ${item._1}"""
          postgres.run(query)
        }
        val query = sqlu"""UPDATE "Gifts" SET likes = likes + 1 WHERE id = ${giftId} """
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
            val nextQuestion = NaiveBayes.getNextQuestion(gifts)
            Session.setLastQuestion(sessionId, nextQuestion).onSuccess {
              case _ =>
                val query = QuestionModel.table.filter(_.id === nextQuestion).take(1)
                postgres.run(query.result).map(question => {
                  request(EditMessageText(Some(cbq.message.get.source),
                    Some(cbq.message.get.messageId),
                    text = question.head.question,
                    replyMarkup = Some(AppConfig.questionButtons)))
                })
            }
        }
      }
    }
  }
}
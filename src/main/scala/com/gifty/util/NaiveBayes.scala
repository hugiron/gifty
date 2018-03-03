package com.gifty.util

import com.gifty.enum.AnswerType
import com.gifty.model.AnswerModel
import com.gifty.Implicits._
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4s.Implicits._
import slick.jdbc.PostgresProfile.backend.DatabaseDef
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.Random

object NaiveBayes {
  def getGifts(posGifts: INDArray, negGifts: INDArray, questionId: Int, answer: AnswerType.Value)
              (implicit db: DatabaseDef, ex: ExecutionContext): (Future[INDArray], Future[INDArray]) = {
    val resPosGifts = {
      val answers = AnswerModel.table

      val q = answers.filter(_.questionId === questionId)
        .sortBy(_.giftId.asc)
        .map(x => (x.posYesCount, x.posNoCount, x.posIdkCount))

      val future = db.run(q.result)

      val res = future.map(_.map(x => {
        val (yes, no, idk) = x
        answer match {
          case AnswerType.Yes => yes.toDouble / (yes + no + idk)
          case AnswerType.No => no.toDouble / (yes + no + idk)
          case AnswerType.NotKnow => idk.toDouble / (yes + no + idk)
        }
      }
      ))

      res.map(_.toNDArray * posGifts)
    }

    val resNegGifts = {
      val answers = AnswerModel.table

      val q = answers.filter(_.questionId === questionId)
        .sortBy(_.giftId.asc)
        .map(x => (x.negYesCount, x.negNoCount, x.negIdkCount))

      val future = db.run(q.result)

      val res = future.map(_.map(x => {
        val (yes, no, idk) = x
        answer match {
          case AnswerType.Yes => yes.toDouble / (yes + no + idk)
          case AnswerType.No => no.toDouble / (yes + no + idk)
          case AnswerType.NotKnow => idk.toDouble / (yes + no + idk)
        }
      }
      ))

      res.map(_.toNDArray * negGifts)
    }

    resPosGifts -> resNegGifts
  }

  def getNextQuestion(implicit db: DatabaseDef, ex: ExecutionContext): Future[Int] = {
    val answers = AnswerModel.table

    db.run(answers.map(_.questionId).min.result).flatMap(xs => {
      val a = xs.head

      db.run(answers.map(_.questionId).max.result).map(xs => {
        val b = xs.head

        Random.nextInt(b - a + 1) + a
      })
    })
  }
}
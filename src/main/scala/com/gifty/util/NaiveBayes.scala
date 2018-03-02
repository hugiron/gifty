package com.gifty.util

import com.gifty.enum.AnswerType
import com.gifty.model.AnswerModel
import com.gifty.Implicits._
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.factory.Nd4j
import org.nd4j.linalg.factory.Nd4j
import org.nd4s.Implicits._
import slick.jdbc.JdbcBackend.DatabaseDef
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.{ExecutionContext, Future}

object NaiveBayes {
  def getGifts(gifts: INDArray, questionId: Int, answer: AnswerType.Value)(implicit db: DatabaseDef, ex: ExecutionContext): Future[INDArray] = {
    val answers = AnswerModel.table

    val q = answers.filter(_.questionId === questionId)
      .sortBy(_.giftId.asc)
      .map(x => (x.yesCount, x.noCount, x.idkCount))

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

    //TODO: Rename variables
    res.map(x => {
      val y = x.toNDArray
      val tmp = y * gifts

      y / Nd4j.max(tmp)
    })
  }

  def getNextQuestion(gifts: INDArray)(implicit db: DatabaseDef, ex: ExecutionContext): Int = {
    val answers = AnswerModel.table

    var bestId = 0
    var bestVal = Double.MaxValue

    val a = db.run(answers.map(_.questionId).min.result).value.get.get.get
    val b = db.run(answers.map(_.questionId).max.result).value.get.get.get

    for(qId <- a to b)
    {
      val vectorYes = db.run(answers.filter(_.questionId === qId)
        .map(x => (x.yesCount, x.noCount, x.idkCount))
        .result)
        .map(_.map(x => {
          val (yes, no, idk) = x
          yes.toDouble / (yes + no + idk)
        }))
        .map(_.toNDArray)

      val pYes = vectorYes.map(v => Nd4j.sum(v * gifts)).value.get.get

      val vectorNo = db.run(answers.filter(_.questionId === qId)
        .map(x => (x.yesCount, x.noCount, x.idkCount))
        .result)
        .map(_.map(x => {
          val (yes, no, idk) = x
          no.toDouble / (yes + no + idk)
        }))
        .map(_.toNDArray)

      val pNo = vectorNo.map(v => Nd4j.sum(v * gifts)).value.get.get

      val vectorIdk = db.run(answers.filter(_.questionId === qId)
        .map(x => (x.yesCount, x.noCount, x.idkCount))
        .result)
        .map(_.map(x => {
          val (yes, no, idk) = x
          idk.toDouble / (yes + no + idk)
        }))
        .map(_.toNDArray)

      val pIdk = vectorIdk.map(v => Nd4j.sum(v * gifts)).value.get.get

      val queryYes = answers.filter(_.questionId === qId)
        .sortBy(_.giftId.asc)
        .map(x => (x.yesCount, x.noCount, x.idkCount))
        .result

      val futureYes = db.run(queryYes).map(_.map(x => {
        val (yes, no, idk) = x
        yes.toDouble / (yes + no + idk)
      }))

      var hYesQ = gifts * futureYes.map(_.toNDArray).value.get.get
      hYesQ /= Nd4j.max(hYesQ)

      val queryNo = answers.filter(_.questionId === qId)
        .sortBy(_.giftId.asc)
        .map(x => (x.yesCount, x.noCount, x.idkCount))
        .result

      val futureNo = db.run(queryYes).map(_.map(x => {
        val (yes, no, idk) = x
        no.toDouble / (yes + no + idk)
      }))

      var hNoQ  = gifts * futureNo.map(_.toNDArray).value.get.get
      hNoQ /= Nd4j.max(hNoQ)

      val queryIdk = answers.filter(_.questionId === qId)
        .sortBy(_.giftId.asc)
        .map(x => (x.yesCount, x.noCount, x.idkCount))
        .result

      val futureIdk = db.run(queryYes).map(_.map(x => {
        val (yes, no, idk) = x
        idk.toDouble / (yes + no + idk)
      }))

      var hIdkQ = gifts * futureIdk.map(_.toNDArray).value.get.get
      hIdkQ /= Nd4j.max(hIdkQ)

      val hYes = Nd4j.sum(hYesQ.imap(p => -p * Math.log(p)))
      val hNo  = Nd4j.sum(hNoQ.imap(p => -p * Math.log(p)))
      val hIdk = Nd4j.sum(hIdkQ.imap(p => -p * Math.log(p)))

      val newVal = (hYes * pYes + hNo * pNo + hIdk * pIdk).getDouble(0)

      if(newVal < bestVal) {
        bestId = qId
        bestVal = newVal
      }
    }

    bestId
  }
}
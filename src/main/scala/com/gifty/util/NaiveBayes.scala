package com.gifty.util

import com.gifty.enum.AnswerType
import com.gifty.model.AnswerModel
import org.nd4j.linalg.api.ndarray.INDArray
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

  def getNextQuestion(gifts: INDArray)(implicit db: DatabaseDef): Int = {
    //TODO: Do this shit
    0
  }
}

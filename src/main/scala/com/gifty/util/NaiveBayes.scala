package com.gifty.util

import com.gifty.enum.AnswerType
import com.gifty.model.AnswerModel
import com.gifty.Implicits._
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.factory.Nd4j
import org.nd4s.Implicits._
import slick.jdbc.PostgresProfile.backend.DatabaseDef
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.Random

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

    res.map(x => {
      val array = x.toNDArray
      val weighted = array * gifts

      val normCoeff = Nd4j.max(weighted).getDouble(0)

      if (normCoeff != 0)
        weighted / normCoeff
      else
        weighted
    })
  }

  def getNextQuestion(gifts: INDArray)(implicit db: DatabaseDef, ex: ExecutionContext): Future[Int] = {
    return getRandomQuestion

    val answers = AnswerModel.table

    val future = db.run(answers.map(_.questionId).min.result).flatMap(xs => {
      val a = xs.head
      db.run(answers.map(_.questionId).max.result).map(xs => {
        val b = xs.head

        (a to b).map { qId =>
          val query = answers.filter(_.questionId === qId)
            .sortBy(_.giftId.asc)
            .map(x => (x.yesCount, x.noCount, x.idkCount))
            .result

          val futureResult = db.run(query)

          val futureVectorYes = futureResult
            .map(_.map(x => {
              val (yes, no, idk) = x
              yes.toDouble / (yes + no + idk)
            }))

          val vectorYes = futureVectorYes.map(x => {
            val arr = x.toNDArray
            arr /= Nd4j.max(arr)
          })

          val pYes = vectorYes.map(x => Nd4j.sum(x * gifts))

          val futureVectorNo = futureResult
            .map(_.map(x => {
              val (yes, no, idk) = x
              no.toDouble / (yes + no + idk)
            }))

          val vectorNo = futureVectorNo.map(x => {
            val arr = x.toNDArray
            arr /= Nd4j.max(arr)
          })

          val pNo = vectorNo.map(x => Nd4j.sum(x * gifts))

          val futureVectorIdk = futureResult
            .map(_.map(x => {
              val (yes, no, idk) = x
              idk.toDouble / (yes + no + idk)
            }))

          val vectorIdk = futureVectorIdk.map(x => {
            val arr = x.toNDArray
            arr /= Nd4j.max(arr)
          })

          val pIdk = vectorIdk.map(x => Nd4j.sum(x * gifts))

          val hYes = vectorYes.map(x => Nd4j.sum(x.imap(p => -p * Math.log(p))))
          val hNo = vectorNo.map(x => Nd4j.sum(x.imap(p => -p * Math.log(p))))
          val hIdk = vectorIdk.map(x => Nd4j.sum(x.imap(p => -p * Math.log(p))))

          val f: Future[Double] = pYes.flatMap(p_yes => {
            pNo.flatMap(p_no => {
              pIdk.flatMap(p_idk => {
                hYes.flatMap(h_yes => {
                  hNo.flatMap(h_no => {
                    hIdk.map(h_idk => {
                      (h_yes * p_yes + h_no * p_no + h_idk * p_idk).getDouble(0)
                    })
                  })
                })
              })
            })
          })

          f
        }
      })
    })

    future.flatMap((seq: Seq[Future[Double]]) => {
      val f = Future.sequence(seq)

      f.map((seqDoulbe: Seq[Double]) => {
        var best = Double.MaxValue
        var bestId = 0

        for (i <- seqDoulbe.indices) {
          if (seqDoulbe(i) < best) {
            best = seqDoulbe(i)
            bestId = i
          }
        }

        bestId
      })
    })
  }

  def getRandomQuestion(implicit db: DatabaseDef, ex: ExecutionContext): Future[Int] = {
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
package com.gifty.util

import com.gifty.enum.AnswerType
import com.gifty.model.AnswerModel
import com.gifty.Implicits._
import com.gifty.Storage
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
        .map(x => (x.posYesCount, x.posNoCount))

      val future = db.run(q.result)

      val res = future.map(_.map(x => {
        val (yes, no) = x
        answer match {
          case AnswerType.Yes => yes.toDouble / (yes + no)
          case AnswerType.No => no.toDouble / (yes + no)
          //case AnswerType.NotKnow => idk.toDouble / (yes + no + idk)
        }
      }
      ))

      res.map(_.toNDArray * posGifts)
    }

    val resNegGifts = {
      val answers = AnswerModel.table

      val q = answers.filter(_.questionId === questionId)
        .sortBy(_.giftId.asc)
        .map(x => (x.negYesCount, x.negNoCount))

      val future = db.run(q.result)

      val res = future.map(_.map(x => {
        val (yes, no) = x
        answer match {
          case AnswerType.Yes => yes.toDouble / (yes + no)
          case AnswerType.No => no.toDouble / (yes + no)
          //case AnswerType.NotKnow => idk.toDouble / (yes + no + idk)
        }
      }
      ))

      res.map(_.toNDArray * negGifts)
    }

    resPosGifts -> resNegGifts
  }

  def getNextQuestion(gifts: INDArray, questions: Set[Int])(implicit db: DatabaseDef, ex: ExecutionContext): Future[Int] = {
    val query = sql"""SELECT select_entropy(ARRAY [#${gifts.toArray.map(_.toFloat).mkString(", ")}]);""".as[String]
    Storage.postgres.run(query).map(res => {
      val entropy = res.head.substring(1, res.head.length - 1).split(',').map(_.toDouble).zipWithIndex
      val sortedEntropy = entropy.sortBy { case (entropy, index) => entropy } toSeq

      def searchQuestion(entropy: Seq[(Double, Int)]): Option[Int] = {
        if (entropy.isEmpty)
          None
        else {
          if (questions.contains(entropy.head._2 + 1))
            searchQuestion(entropy.tail)
          else
            Some(entropy.head._2 + 1)
        }
      }

      searchQuestion(sortedEntropy).getOrElse(Random.nextInt(entropy.length) + 1)
    })
  }
}
package com.gifty

import com.gifty.enum.AnswerType

import scala.collection.mutable
import com.gifty.model.History
import info.mukel.telegrambot4s.models.{CallbackQuery, Message}
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.factory.Nd4j
import org.nd4s.Implicits._

object Implicits {
  implicit val formats = DefaultFormats

  implicit class MessageExtension(msg: Message) {
    def toSessionId: String = s"${msg.source}:${msg.messageId}"
  }

  implicit class CallbackQueryExtension(cbq: CallbackQuery) {
    def toSessionId: Option[String] = cbq.message.map(_.toSessionId)
  }

  implicit class HistoryExtension(history: History) {
    def toRedis: String = {
      s"[${history.buffer.map(item => s"[${item._1}, ${item._2.id}]").mkString(", ")}]"
    }
  }

  implicit class StringExtension(str: String) {
    def toNDArray: INDArray = {
      parse(str).extract[Array[Double]].toNDArray
    }

    def toHistory: History = {
      History(parse(str).extract[Array[JValue]].map(_.extract[Array[Int]]).map(item => (item(0), AnswerType(item(1)))).toList)
    }
  }

  implicit class NDArrayExtension(array: INDArray) {
    def toRedis: String = {
      val builder = new mutable.StringBuilder("[")
      for (i <- 0 until array.length) {
        builder.append(array(i).toString)
        if (i < array.length - 1)
          builder.append(", ")
      }
      builder.append(']')
      builder.toString
    }

    def imap(f: Double => Double): INDArray = {
      val result = Nd4j.create(array.shape: _*)
      for (i <- 0 until result.length)
        result(i) = f(array(i))
      result
    }

    def ifilter(f: Double => Boolean): Option[INDArray] = {
      val result = Nd4j.create(array.length)
      var length = 0
      for (i <- 0 until array.length) {
        if (f(array(i))) {
          result(length) = array(i)
          length += 1
        }
      }
      if (length > 0) Some(result(0 -> length)) else None
    }

    def isearch(value: Double): Int = {
      var (left, right) = (0, array.length)
      while (left < right) {
        val mid: Int = (left + right) / 2
        if (value <= array(mid))
          right = mid
        else
          left = mid + 1
      }
      left
    }
  }
}

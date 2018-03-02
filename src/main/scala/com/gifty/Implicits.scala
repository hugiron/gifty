package com.gifty
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.factory.Nd4j
import org.nd4s.Implicits._

object Implicits {
  implicit class PairsExtension(pairs: List[(Int, Int)]) {
    def toRedis(): String = {
      pairs.map(x => s"${x._1}#${x._2}").mkString("|")
    }
  }

  implicit class StringExtension(str: String) {
    def toNDArray(): INDArray = {
      str.split('|').map(_.toDouble).toNDArray
    }

    def toPairs(): List[(Int, Int)] = {
      str.split('|').map(x => x.split('#')(0).toInt -> x.split('#')(1).toInt).toList
    }
  }

  implicit class NDArrayExtension(array: INDArray) {
    def toRedis(): String = {
      val result = new Array[Double](array.length)
      for (i <- 0 until array.length)
        result(i) = array.getDouble(i)
      result.map(_.toString).mkString("|")
    }

    /**
      * Создание новой последовательности посредством применения функции `f` к элементам исходной последовательности
      * @param f Функция, которую следует применить к элементам последовательности
      * @return Последовательность, полученная в результате применения функции `f` к исходной последовательности
      */
    def imap(f: Double => Double): INDArray = {
      val result = Nd4j.create(array.shape: _*)
      for (i <- 0 until result.length)
        result(i) = f(array(i))
      result
    }

    /**
      * Применение функции `f` к элементам последовательности
      * @param f Функция, которую следует применить к элементам последовательности
      */
    def iforall(f: Double => Any): Unit = {
      for (i <- 0 until array.length)
        f(array(i))
    }

    /**
      * Фильтрация исходной последовательности по предикату `f`
      * @param f Функция-предикат, определяющая, будет ли элемент входить в результирующую последовательность или нет
      * @return Последовательность, полученная в результате фильтрации
      */
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

    /**
      * Поиск элемента в отсортированной последовательности
      * @param value Значение элемента, который требуется найти в последовательности
      * @return Индекс элемента `value` в последовательности (или индекс позиции, на которую следует поместить `value`)
      */
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

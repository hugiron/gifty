package com.gifty.model

import com.gifty.enum.AnswerType

class History(var buffer: List[(Int, AnswerType.Value)] = Nil) extends Iterable[(Int, AnswerType.Value)] {
  def add(questionId: Int, answer: AnswerType.Value): Unit = {
    buffer ::= questionId -> answer
  }

  override def iterator: Iterator[(Int, AnswerType.Value)] = buffer.iterator
}

object History {
  def apply(buffer: List[(Int, AnswerType.Value)] = Nil): History = new History(buffer)
}
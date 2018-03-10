package com.gifty.util

import akka.util.Timeout
import com.redis.RedisClient
import org.nd4j.linalg.api.ndarray.INDArray
import com.gifty.Implicits._
import com.gifty.model.History

import scala.concurrent.{ExecutionContext, Future}

object Session {
  def getPosGifts(key: String)
              (implicit redis: RedisClient, context: ExecutionContext, timeout: Timeout): Future[Option[INDArray]] = {
    redis.hget(key, "pos_gifts").map(_.map(_.toString.toNDArray))
  }

  def setPosGifts(key: String, gifts: INDArray)
              (implicit redis: RedisClient, context: ExecutionContext, timeout: Timeout): Future[Boolean] = {
    redis.hset(key, "pos_gifts", gifts.toRedis)
  }

  def getNegGifts(key: String)
                 (implicit redis: RedisClient, context: ExecutionContext, timeout: Timeout): Future[Option[INDArray]] = {
    redis.hget(key, "neg_gifts").map(_.map(_.toString.toNDArray))
  }

  def setNegGifts(key: String, gifts: INDArray)
                 (implicit redis: RedisClient, context: ExecutionContext, timeout: Timeout): Future[Boolean] = {
    redis.hset(key, "neg_gifts", gifts.toRedis)
  }

  def getHistory(key: String)
                (implicit redis: RedisClient, context: ExecutionContext, timeout: Timeout): Future[Option[History]] = {
    redis.hget(key, "history").map(_.map(_.toString.toHistory))
  }

  def setHistory(key: String, history: History)
                (implicit redis: RedisClient, context: ExecutionContext, timeout: Timeout): Future[Boolean] = {
    redis.hset(key, "history", history.toRedis)
  }

  def getLastQuestion(key: String)
                     (implicit redis: RedisClient, context: ExecutionContext, timeout: Timeout): Future[Option[Int]] = {
    redis.hget(key, "last_question").map(_.map(_.toString.toInt))
  }

  def setLastQuestion(key: String, questionId: Int)
                     (implicit redis: RedisClient, context: ExecutionContext, timeout: Timeout): Future[Boolean] = {
    redis.hset(key, "last_question", questionId.toString)
  }

  def getLastGift(key: String)
                 (implicit redis: RedisClient, context: ExecutionContext, timeout: Timeout): Future[Option[Int]] = {
    redis.hget(key, "last_gift").map(_.map(_.toString.toInt))
  }

  def setLastGift(key: String, giftId: Int)
                     (implicit redis: RedisClient, context: ExecutionContext, timeout: Timeout): Future[Boolean] = {
    redis.hset(key, "last_gift", giftId.toString)
  }

  def getQuestions(key: String)
                  (implicit redis: RedisClient, context: ExecutionContext, timeout: Timeout): Future[Option[Set[Int]]] = {
    redis.hget(key, "questions").map(_.map(_.toString.toQuestions))
  }

  def setQuestions(key: String, questions: Set[Int])
                  (implicit redis: RedisClient, context: ExecutionContext, timeout: Timeout): Future[Boolean] = {
    redis.hset(key, "questions", questions.toRedis)
  }

  def deleteSession(key: String)
                   (implicit redis: RedisClient, context: ExecutionContext, timeout: Timeout): Future[Long] = {
    redis.hdel(key, "pos_gifts", "neg_gifts", "history", "last_question", "last_gift", "questions")
  }

  def copyTo(fromKey: String, toKey: String)
            (implicit redis: RedisClient, context: ExecutionContext, timeout: Timeout): Future[Unit] = {
    getPosGifts(fromKey).map(_.map(gifts => Session.setPosGifts(toKey, gifts)))
    getNegGifts(fromKey).map(_.map(gifts => Session.setNegGifts(toKey, gifts)))
    getHistory(fromKey).map(_.map(history => Session.setHistory(toKey, history)))
    getLastGift(fromKey).map(_.map(giftId => Session.setLastGift(toKey, giftId)))
    getLastQuestion(fromKey).map(_.map(questionId => Session.setLastQuestion(toKey, questionId)))
    getQuestions(fromKey).map(_.map(questions => Session.setQuestions(toKey, questions)))
  }
}
package com.gifty.util

import akka.util.Timeout
import com.redis.RedisClient
import org.nd4j.linalg.api.ndarray.INDArray
import com.gifty.Implicits._
import com.gifty.model.History

import scala.concurrent.{ExecutionContext, Future}

object Session {
  def getGifts(key: String)
              (implicit redis: RedisClient, context: ExecutionContext, timeout: Timeout): Future[Option[INDArray]] = {
    redis.hget(key, "gifts").map(_.map(_.toString.toNDArray))
  }

  def setGifts(key: String, gifts: INDArray)
              (implicit redis: RedisClient, context: ExecutionContext, timeout: Timeout): Future[Boolean] = {
    redis.hset(key, "gifts", gifts.toRedis)
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

  def deleteSession(key: String)
                   (implicit redis: RedisClient, context: ExecutionContext, timeout: Timeout): Future[Long] = {
    redis.hdel(key, "gifts", "history", "last_question", "last_gift")
  }
}
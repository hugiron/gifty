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
}

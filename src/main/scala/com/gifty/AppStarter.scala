package com.gifty

import akka.actor.ActorSystem
import akka.util.Timeout
import com.gifty.model._
import com.redis.RedisClient
import slick.jdbc.PostgresProfile.api._
import slick.jdbc.meta.MTable
import com.gifty.Implicits._
import com.gifty.Storage._
import com.gifty.util.Session
import com.typesafe.scalalogging.LazyLogging
import org.nd4j.linalg.factory.Nd4j
import org.nd4s.Implicits._

import scala.concurrent._
import scala.concurrent.duration._

object AppStarter extends App with LazyLogging {
  Await.result(createTables(), Duration.Inf)
  GiftyBot.run()
}

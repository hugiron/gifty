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

import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

object AppStarter extends App {
  Await.result(createTables(), Duration.Inf)
  GiftyBot.run()
}

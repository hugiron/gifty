package com.gifty

import ch.qos.logback.classic.{Level, Logger}
import com.gifty.Storage._
import com.typesafe.scalalogging.LazyLogging
import org.slf4j.LoggerFactory

import scala.concurrent._
import scala.concurrent.duration._

object AppStarter extends App with LazyLogging {
  LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME).asInstanceOf[Logger].setLevel(Level.WARN)
  Await.result(createTables(), Duration.Inf)
  GiftyBot.run()
}

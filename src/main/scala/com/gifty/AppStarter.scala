package com.gifty

import com.gifty.Storage._

import scala.concurrent._
import scala.concurrent.duration._

object AppStarter extends App {
  Await.result(createTables(), Duration.Inf)
  GiftyBot.run()
}

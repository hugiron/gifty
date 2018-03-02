package com.gifty

import info.mukel.telegrambot4s.api.{Polling, TelegramBot}
import info.mukel.telegrambot4s.api.declarative.{Callbacks, Commands}
import com.typesafe.config.ConfigFactory

object GiftyBot extends TelegramBot with Polling with Commands with Callbacks {
  lazy val token: String = ConfigFactory.load().getString("telegram.token")
}

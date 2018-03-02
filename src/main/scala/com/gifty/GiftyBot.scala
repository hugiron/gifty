package com.gifty

import info.mukel.telegrambot4s.api.{Polling, TelegramBot}
import info.mukel.telegrambot4s.api.declarative.{Callbacks, Commands}
import com.typesafe.config.{Config, ConfigFactory}

object GiftyBot extends TelegramBot with Polling with Commands with Callbacks {
  lazy val config: Config = ConfigFactory.load()
  lazy val token: String = config.getString("telegram.token")

  onCommand(config.getString("bot.command.start")) { implicit msg =>

  }

  onCommand(config.getString("bot.command.help")) { implicit msg =>

  }

  onCallbackWithTag(config.getString("bot.button.yes.tag")) { implicit cbq =>

  }

  onCallbackWithTag(config.getString("bot.button.no.tag")) { implicit cbq =>

  }

  onCallbackWithTag(config.getString("bot.button.not_know.tag")) { implicit cbq =>

  }

  onCallbackWithTag(config.getString("bot.button.accepted.tag")) { implicit cbq =>

  }

  onCallbackWithTag(config.getString("bot.button.rejected.tag")) { implicit cbq =>

  }
}

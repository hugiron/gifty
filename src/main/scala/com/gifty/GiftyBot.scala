package com.gifty

import com.gifty.Storage._
import com.gifty.model.GiftModel
import com.gifty.util.UI
import info.mukel.telegrambot4s.api.{Polling, TelegramBot}
import info.mukel.telegrambot4s.api.declarative.{Callbacks, Commands}
import com.typesafe.config.{Config, ConfigFactory}

object GiftyBot extends TelegramBot with Polling with Commands with Callbacks {
  val config: Config = ConfigFactory.load()
  val token: String = config.getString("telegram.token")

  onCommand(UI.startCommand) { implicit msg =>

  }

  onCommand(UI.helpCommand) { implicit msg =>

  }

  onCallbackWithTag(UI.yesButton._1) { implicit cbq =>

  }

  onCallbackWithTag(UI.noButton._1) { implicit cbq =>

  }

  onCallbackWithTag(UI.notKnowButton._1) { implicit cbq =>

  }

  onCallbackWithTag(UI.acceptedButton._1) { implicit cbq =>

  }

  onCallbackWithTag(UI.rejectedButton._1) { implicit cbq =>

  }
}
package com.gifty

import com.typesafe.config.{Config, ConfigFactory}
import info.mukel.telegrambot4s.models.{InlineKeyboardButton, InlineKeyboardMarkup}

object AppConfig {
  val config: Config = ConfigFactory.load()

  val token: String = config.getString("telegram.token")
  val threshold: Double = config.getDouble("model.threshold")
  val minStepCount: Int = config.getInt("model.min_step_count")
  val maxStepCount: Int = config.getInt("model.max_step_count")

  val startCommand: String = config.getString("bot.command.start")
  val helpCommand: String = config.getString("bot.command.help")

  val startButton: (String, String) = (config.getString("bot.button.start.tag"), config.getString("bot.button.start.text"))
  val yesButton: (String, String) = (config.getString("bot.button.yes.tag"), config.getString("bot.button.yes.text"))
  val noButton: (String, String) = (config.getString("bot.button.no.tag"), config.getString("bot.button.no.text"))
  val notKnowButton: (String, String) = (config.getString("bot.button.not_know.tag"), config.getString("bot.button.not_know.text"))
  val acceptedButton: (String, String) = (config.getString("bot.button.accepted.tag"), config.getString("bot.button.accepted.text"))
  val rejectedButton: (String, String) = (config.getString("bot.button.rejected.tag"), config.getString("bot.button.rejected.text"))

  val helpBody: String = config.getString("bot.body.help")
  val greetingBody: String = config.getString("bot.body.greeting")

  val startButtons: InlineKeyboardMarkup = InlineKeyboardMarkup.singleButton(
    InlineKeyboardButton.callbackData(startButton._2, startButton._1)
  )
  val questionButtons: InlineKeyboardMarkup = InlineKeyboardMarkup.singleRow(Seq(
    InlineKeyboardButton.callbackData(yesButton._2, yesButton._1),
    InlineKeyboardButton.callbackData(noButton._2, noButton._1),
    InlineKeyboardButton.callbackData(notKnowButton._2, notKnowButton._1)
  ))
  val giftButtons: InlineKeyboardMarkup = InlineKeyboardMarkup.singleRow(Seq(
    InlineKeyboardButton.callbackData(acceptedButton._2, acceptedButton._1),
    InlineKeyboardButton.callbackData(rejectedButton._2, rejectedButton._1)
  ))
}
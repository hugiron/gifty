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

  val yesButton: (String, String) = (config.getString("bot.button.yes.tag"), config.getString("bot.button.yes.text"))
  val noButton: (String, String) = (config.getString("bot.button.no.tag"), config.getString("bot.button.no.text"))
  val notKnowButton: (String, String) = (config.getString("bot.button.not_know.tag"), config.getString("bot.button.not_know.text"))
  val acceptedButton: (String, String) = (config.getString("bot.button.accepted.tag"), config.getString("bot.button.accepted.text"))
  val rejectedButton: (String, String) = (config.getString("bot.button.rejected.tag"), config.getString("bot.button.rejected.text"))
  val continueButton: (String, String) = (config.getString("bot.button.continue.tag"), config.getString("bot.button.continue.text"))
  val stopButton: (String, String) = (config.getString("bot.button.stop.tag"), config.getString("bot.button.stop.text"))

  val helpBody: String = config.getString("bot.body.help")
  val continuationBody: String = config.getString("bot.body.continuation")

  val questionButtons: InlineKeyboardMarkup = InlineKeyboardMarkup.singleRow(Seq(
    InlineKeyboardButton.callbackData(yesButton._2, yesButton._1),
    InlineKeyboardButton.callbackData(noButton._2, noButton._1),
    InlineKeyboardButton.callbackData(notKnowButton._2, notKnowButton._1)
  ))
  val giftButtons: InlineKeyboardMarkup = InlineKeyboardMarkup.singleRow(Seq(
    InlineKeyboardButton.callbackData(acceptedButton._2, acceptedButton._1),
    InlineKeyboardButton.callbackData(rejectedButton._2, rejectedButton._1)
  ))
  val continuationButtons: InlineKeyboardMarkup = InlineKeyboardMarkup.singleRow(Seq(
    InlineKeyboardButton.callbackData(continueButton._2, continueButton._1),
    InlineKeyboardButton.callbackData(stopButton._2, stopButton._1)
  ))
}
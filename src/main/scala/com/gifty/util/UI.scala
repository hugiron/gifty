package com.gifty.util

import com.typesafe.config.{Config, ConfigFactory}
import info.mukel.telegrambot4s.models.{InlineKeyboardButton, InlineKeyboardMarkup}

object UI {
  val config: Config = ConfigFactory.load()

  val startCommand: String = config.getString("bot.command.start")
  val helpCommand: String = config.getString("bot.command.help")

  val yesButton: (String, String) = (config.getString("bot.button.yes.tag"), config.getString("bot.button.yes.text"))
  val noButton: (String, String) = (config.getString("bot.button.no.tag"), config.getString("bot.button.no.text"))
  val notKnowButton: (String, String) = (config.getString("bot.button.not_know.tag"), config.getString("bot.button.not_know.text"))
  val acceptedButton: (String, String) = (config.getString("bot.button.accepted.tag"), config.getString("bot.button.accepted.text"))
  val rejectedButton: (String, String) = (config.getString("bot.button.rejected.tag"), config.getString("bot.button.rejected.text"))

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
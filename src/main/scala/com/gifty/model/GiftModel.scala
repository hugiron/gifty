package com.gifty.model

import slick.jdbc.PostgresProfile.api._

class GiftModel(tag: Tag) extends Table[(Int, String, String, Int)](tag, "Gifts") {
  def id = column[Int]("id", O.AutoInc, O.PrimaryKey)
  def name = column[String]("name")
  def url = column[String]("url")
  def likes = column[Int]("likes", O.Default(0))

  def * = (id, name, url, likes)
  def idx = index("likes_index", likes)
}

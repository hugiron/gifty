package com.gifty.model

import slick.jdbc.PostgresProfile.api._

case class Gift(id: Int, name: String, url: String, likes: Int)

class GiftModel(tag: Tag) extends Table[Gift](tag, "Gifts") {
  def id = column[Int]("id", O.AutoInc, O.PrimaryKey)
  def name = column[String]("name")
  def url = column[String]("url")
  def likes = column[Int]("likes", O.Default(1))

  def * = (id, name, url, likes) <> (Gift.tupled, Gift.unapply)
  def idx = index("likes_index", likes)
}

object GiftModel {
  val table = TableQuery[GiftModel]
}

package com.gifty

import akka.actor.ActorSystem
import akka.util.Timeout
import com.gifty.model.{AnswerModel, GiftModel, QuestionModel}
import com.redis.RedisClient
import slick.jdbc.PostgresProfile.api._
import com.typesafe.config.{Config, ConfigFactory}
import slick.jdbc.meta.MTable

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object Storage {
  val config: Config = ConfigFactory.load()

  private val postgresDriver: String = config.getString("database.postgres.driver")
  private val postgresHost: String = config.getString("database.postgres.host")
  private val postgresPort: String = config.getString("database.postgres.port")
  private val postgresUsername: String = config.getString("database.postgres.username")
  private val postgresPassword: String = config.getString("database.postgres.password")
  private val postgresDatabase: String = config.getString("database.postgres.database")
  private val postgresUrl = s"jdbc:postgresql://$postgresHost:$postgresPort/$postgresDatabase" +
    s"?user=$postgresUsername&password=$postgresPassword"

  private val redisHost: String = config.getString("database.redis.host")
  private val redisPort: String = config.getString("database.redis.port")

  implicit val system = ActorSystem("redis-client")
  implicit val timeout = Timeout(5 seconds)

  implicit val redis = RedisClient(redisHost, redisPort.toInt)
  implicit val postgres = Database.forURL(postgresUrl, driver = postgresDriver)

  def createTables(): Future[List[Unit]] = {
    val tables = List(GiftModel.table, QuestionModel.table, AnswerModel.table)
    val existing = postgres.run(MTable.getTables)

    existing.flatMap(v => {
      val names = v.map(mt => mt.name.name)
      val createIfNotExist = tables.filter( table =>
        (!names.contains(table.baseTableRow.tableName))).map(_.schema.create)
      postgres.run(DBIO.sequence(createIfNotExist))
    })
  }
}

package models

import java.util.UUID

import javax.inject.{ Inject, Singleton }
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class ReturnRepository @Inject() (val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  class ReturnTable(tag: Tag) extends Table[Return](tag, "return") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def userId = column[String]("userId")
    def productId = column[Long]("productId")
    def status = column[String]("status")
    def * = (id, userId, productId, status) <> ((Return.apply _).tupled, Return.unapply)
  }

  val return_table = TableQuery[ReturnTable]

  def create(userId: String, productId: Long, status: String): Future[Return] = db.run {
    (return_table.map(r => (r.userId, r.productId, r.status))
      returning return_table.map(_.id)
      into { case ((userId, productId, status), id) => Return(id, userId, productId, status) }) += (userId, productId, status)
  }

  def list(): Future[Seq[Return]] = db.run {
    return_table.result
  }

  def getById(id: Long): Future[Return] = db.run {
    return_table.filter(_.id === id).result.head
  }

  def getByIdOption(id: Long): Future[Option[Return]] = db.run {
    return_table.filter(_.id === id).result.headOption
  }

  def getByUserId(userId: String): Future[Seq[(Long, Long, String, String)]] = db.run {
    return_table.map { case (r) => (r.id, r.productId, r.userId, r.status) }.filter(_._3 === userId).result
  }

  def update(id: Long, new_return: Return): Future[Unit] = {
    val returnToUpdate: Return = new_return.copy(id)
    db.run(return_table.filter(_.id === id).update(returnToUpdate)).map(_ => ())
  }

  def delete(id: Long): Future[Unit] = db.run(return_table.filter(_.id === id).delete).map(_ => ())

}


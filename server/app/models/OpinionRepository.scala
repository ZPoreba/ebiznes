package models

import javax.inject.{Inject, Singleton}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class OpinionRepository @Inject() (val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  class OpinionTable(tag: Tag) extends Table[Opinion](tag, "opinion") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def userId = column[Long]("userId")
    def productId = column[Long]("productId")
    def content = column[String]("content")
    def * = (id, userId, productId, content) <> ((Opinion.apply _).tupled, Opinion.unapply)
  }

  val opinion = TableQuery[OpinionTable]

  def create(userId: Long, productId: Long, content: String): Future[Opinion] = db.run {
    (opinion.map(o => (o.userId, o.productId, o.content))
      returning opinion.map(_.id)
      into { case ((userId, productId, content), id) => Opinion(id, userId, productId, content)}
      ) += (userId, productId, content)
  }

  def list(): Future[Seq[Opinion]] = db.run {
    opinion.result
  }

  def getById(id: Long): Future[Opinion] = db.run {
    opinion.filter(_.id === id).result.head
  }

  def getByIdOption(id: Long): Future[Option[Opinion]] = db.run {
    opinion.filter(_.id === id).result.headOption
  }

  def update(id: Long, new_opinion: Opinion): Future[Unit] = {
    val opinionToUpdate: Opinion = new_opinion.copy(id)
    db.run(opinion.filter(_.id === id).update(opinionToUpdate)).map(_ => ())
  }

  def delete(id: Long): Future[Unit] = db.run(opinion.filter(_.id === id).delete).map(_ => ())
  def deleteProduct(id: Long): Future[Unit] = db.run(opinion.filter(_.productId === id).delete).map(_ => ())
  def deleteUser(id: Long): Future[Unit] = db.run(opinion.filter(_.userId === id).delete).map(_ => ())

}

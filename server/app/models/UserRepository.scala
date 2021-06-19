package models

import javax.inject.{Inject, Singleton}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UserRepository @Inject() (val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._
  import utils.UUIDHelper

  class UserTable(tag: Tag) extends Table[ApiUser](tag, "user") {
    def id = column[String]("id", O.PrimaryKey)
    def firstName = column[Option[String]]("firstName")
    def lastName = column[Option[String]]("lastName")
    def fullName = column[Option[String]]("fullName")
    def email = column[Option[String]]("email")
    def avatarURL = column[Option[String]]("avatarURL")
    def address = column[String]("address")

    def * = (id, firstName, lastName, fullName, email, avatarURL, address) <> ((ApiUser.apply _).tupled, ApiUser.unapply)
  }

  private val user = TableQuery[UserTable]

  def list(): Future[Seq[ApiUser]] = db.run {
    user.result
  }

  def getById(id: String): Future[ApiUser] = db.run {
    user.filter(_.id === id).result.head
  }

  def getByIdOption(id: String): Future[Option[ApiUser]] = db.run {
    user.filter(_.id === id).result.headOption
  }

  def update(id: String, new_user: ApiUser): Future[Unit] = {
    val userToUpdate: ApiUser = new_user.copy(id)
    db.run(user.filter(_.id === id).update(userToUpdate)).map(_ => ())
  }

  def delete(id: String): Future[Unit] = {
    db.run(user.filter(_.id === id).delete).map(_ => ())
  }

  def exists(id: String): Future[Boolean] =
    db.run(user.filter(i => i.id === id).exists.result)

}


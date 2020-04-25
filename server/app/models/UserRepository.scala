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

  class UserTable(tag: Tag) extends Table[User](tag, "user") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def firstName = column[String]("firstName")
    def secondName = column[String]("secondName")
    def email = column[String]("email")
    def password = column[String]("password")
    def address = column[String]("address")

    def * = (id, firstName, secondName, email, password, address) <> ((User.apply _).tupled, User.unapply)
  }

  private val user = TableQuery[UserTable]

  def create(firstName: String, secondName: String, email: String, password: String, address: String): Future[User] = db.run {
    (user.map(u => (u.firstName, u.secondName, u.email, u.password, u.address))
      returning user.map(_.id)
      into { case ((firstName, secondName, email, password, address), id) => User(id, firstName, secondName, email, password, address)}
      ) += (firstName, secondName, email, password, address)
  }

  def list(): Future[Seq[User]] = db.run {
    user.result
  }

  def getById(id: Long): Future[User] = db.run {
    user.filter(_.id === id).result.head
  }

  def getByIdOption(id: Long): Future[Option[User]] = db.run {
    user.filter(_.id === id).result.headOption
  }

  def update(id: Long, new_user: User): Future[Unit] = {
    val userToUpdate: User = new_user.copy(id)
    db.run(user.filter(_.id === id).update(userToUpdate)).map(_ => ())
  }

  def delete(id: Long): Future[Unit] = {
    db.run(user.filter(_.id === id).delete).map(_ => ())
  }

  def exists(id: Long): Future[Boolean] =
    db.run(user.filter(i => i.id === id).exists.result)

}


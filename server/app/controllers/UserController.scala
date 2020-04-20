package controllers

import javax.inject._
import models.{CartProductRepository, OpinionRepository, OrderRepository, User, UserRepository, WishListProductRepository}
import play.api.mvc._
import play.filters.csrf.CSRF

import scala.concurrent.{ExecutionContext, Future}


@Singleton
class UserController @Inject()(userRepository: UserRepository,
                               cartProductRepository: CartProductRepository,
                               wishListProductRepository: WishListProductRepository,
                               opinionRepository: OpinionRepository,
                               orderRepository: OrderRepository,
                               cc: MessagesControllerComponents) (implicit ec: ExecutionContext) extends MessagesAbstractController(cc) {

  def getToken = Action { implicit request =>
    val token = CSRF.getToken.get.value
    Ok(token)
  }

  def create = Action.async { implicit request =>
    val params = request.queryString.map { case (k,v) => k -> v.mkString }
    if (!params.contains("firstName")) {
      Future(Ok("No firstName parameter in query"))
    }
    else if (!params.contains("secondName")) {
      Future(Ok("No secondName parameter in query"))
    }
    else if (!params.contains("email")) {
      Future(Ok("No email parameter in query"))
    }
    else if (!params.contains("password")) {
      Future(Ok("No password parameter in query"))
    }
    else if (!params.contains("address")) {
      Future(Ok("No address parameter in query"))
    }
    else {
      userRepository.create(params("firstName"), params("secondName"), params("email"), params("password"), params("address"))
      Future(Ok("User created!"))
    }
  }

  def read: Action[AnyContent] = Action.async { implicit request =>
    val users = userRepository.list()
    users.map( user => Ok(user.toString()) )
  }

  def readById: Action[AnyContent] = Action.async { implicit request =>

    val params = request.queryString.map { case (k,v) => k -> v.mkString }
    if (!params.contains("id")) {
      Future(Ok("No id parameter in query"))
    }
    else {
      val users = userRepository.getById(params("id").toLong)
      users.map(user => user match {
        case Some(u) => Ok(u.toString())
        case None => Ok("No user with id")
      })
    }

  }

  def update = Action.async { implicit request =>
    val params = request.queryString.map { case (k,v) => k -> v.mkString }

    if (!params.contains("id")) {
      Future(Ok("No id parameter in query"))
    }
    else {

      try {
        val id = params("id").toLong
        val users = userRepository.getById(id)

        users.map(user => user match {
          case Some(u) => {
            val firstName = if (params.contains("firstName")) params("firstName") else u.firstName
            val secondName = if (params.contains("secondName")) params("secondName") else u.secondName
            val email = if (params.contains("email")) params("email") else u.email
            val password = if (params.contains("password")) params("password") else u.password
            val address = if (params.contains("address")) params("address") else u.address

            val newUser = User(id, firstName, secondName, email, password, address)
            userRepository.update(id, newUser)
            Ok("User updated!")
          }
          case None => Ok("No object with such id")
        })
      }
      catch {
        case e: NumberFormatException => Future(Ok("Id has to be integer"))
      }

    }
  }

  def delete = Action { implicit request =>
    val params = request.queryString.map { case (k,v) => k -> v.mkString }

    if (!params.contains("id")) {
      Ok("No id parameter in query")
    }
    else {
      try {
        cartProductRepository.deleteUser(params("id").toLong)
        wishListProductRepository.deleteUser(params("id").toLong)
        userRepository.delete(params("id").toLong)
        opinionRepository.deleteUser(params("id").toLong)
        orderRepository.deleteUser(params("id").toLong)
        Ok("User deleted!")
      }
      catch {
        case e: NumberFormatException => Ok("Id has to be integer")
      }
    }
  }

}

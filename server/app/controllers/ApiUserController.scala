package controllers

import javax.inject._
import models.{ ApiUser, CartProductRepository, OpinionRepository, OrderRepository, UserRepository, WishListProductRepository }
import play.api.libs.json._
import play.api.mvc._
import play.filters.csrf.CSRF

import scala.concurrent.duration.Duration
import scala.concurrent.{ Await, ExecutionContext, Future }

@Singleton
class ApiUserController @Inject() (
  userRepository: UserRepository,
  cartProductRepository: CartProductRepository,
  wishListProductRepository: WishListProductRepository,
  opinionRepository: OpinionRepository,
  orderRepository: OrderRepository,
  cc: MessagesControllerComponents)(implicit ec: ExecutionContext) extends MessagesAbstractController(cc) {

  def getToken = Action { implicit request =>
    val token = CSRF.getToken.get.value
    Ok(token)
  }

  def read: Action[AnyContent] = Action { implicit request =>
    val users = Await.result(userRepository.list(), Duration.Inf)
    Ok(Json.toJson(users))
  }

  def readById: Action[AnyContent] = Action.async { implicit request =>

    val params = request.queryString.map { case (k, v) => k -> v.mkString }
    if (!params.contains("id")) {
      Future(Ok("No id parameter in query"))
    } else {
      val users = userRepository.getByIdOption(params("id"))
      users.map(user => user match {
        case Some(u) => Ok(Json.toJson(u))
        case None => Ok("No user with id")
      })
    }

  }

  def update = Action.async { implicit request =>
    val params = request.queryString.map { case (k, v) => k -> v.mkString }

    if (!params.contains("id")) {
      Future(Ok("No id parameter in query"))
    } else {

      try {
        val id = params("id")
        val users = userRepository.getByIdOption(id)

        users.map(user => user match {
          case Some(u) => {
            val firstName = if (params.contains("firstName")) Option(params("firstName")) else u.firstName
            val lastName = if (params.contains("lastName")) Option(params("lastName")) else u.lastName
            val fullName = if (params.contains("fullName")) Option(params("fullName")) else u.fullName
            val email = if (params.contains("email")) Option(params("email")) else u.email
            val avatarURL = if (params.contains("avatarURL")) Option(params("avatarURL")) else u.avatarURL
            val address = if (params.contains("address")) params("address") else u.address

            var fullName_string = "";
            firstName match {
              case Some(fn) => fullName_string = fn + " "
              case None => Ok
            }

            lastName match {
              case Some(ln) => fullName_string += ln
              case None => Ok
            }

            val newUser = ApiUser(u.id, firstName, lastName, Option(fullName_string), email, avatarURL, address)
            userRepository.update(u.id, newUser)
            Ok("User updated!")
          }
          case None => Ok("No object with such id")
        })
      } catch {
        case e: NumberFormatException => Future(Ok("Id has to be integer"))
      }

    }
  }

  def delete = Action { implicit request =>
    val params = request.queryString.map { case (k, v) => k -> v.mkString }

    if (!params.contains("id")) {
      Ok("No id parameter in query")
    } else {
      try {
        cartProductRepository.deleteUser(params("id"))
        wishListProductRepository.deleteUser(params("id"))
        userRepository.delete(params("id"))
        opinionRepository.deleteUser(params("id"))
        orderRepository.deleteUser(params("id"))
        Ok("User deleted!")
      } catch {
        case e: NumberFormatException => Ok("Id has to be integer")
      }
    }
  }

}

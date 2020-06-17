package controllers

import javax.inject._
import models.{ ProductRepository, Return, ReturnRepository, UserRepository }
import play.api.libs.json._
import play.api.mvc._
import play.filters.csrf.CSRF

import scala.concurrent.duration.Duration
import scala.concurrent.{ Await, ExecutionContext, Future }

@Singleton
class ApiReturnController @Inject() (
  returnRepository: ReturnRepository,
  productRepository: ProductRepository,
  userRepository: UserRepository,
  cc: MessagesControllerComponents)(implicit ec: ExecutionContext) extends MessagesAbstractController(cc) {

  def getToken = Action { implicit request =>
    val token = CSRF.getToken.get.value
    Ok(token)
  }

  def create: Action[AnyContent] = Action.async { implicit request =>
    val params = request.queryString.map { case (k, v) => k -> v.mkString }
    if (!params.contains("userId")) {
      Future(Ok("No userId parameter in query"))
    } else if (!params.contains("productId")) {
      Future(Ok("No productId parameter in query"))
    } else if (!params.contains("status")) {
      Future(Ok("No status parameter in query"))
    } else {
      val userId = params("userId")
      val prodId = params("productId").toInt
      val status = params("status")

      userRepository.exists(userId).map(userExists => {
        productRepository.exists(prodId).map(productExists => {
          if (userExists && productExists) {
            returnRepository.create(userId, prodId, status)
          }
        })
      })

      Future(Ok("Return requested"))
    }
  }

  def read: Action[AnyContent] = Action { implicit request =>
    val returns = Await.result(returnRepository.list(), Duration.Inf)
    Ok(Json.toJson(returns))
  }

  def readById: Action[AnyContent] = Action { implicit request =>

    val params = request.queryString.map { case (k, v) => k -> v.mkString }
    if (!params.contains("id")) {
      Ok("No id parameter in query")
    } else {
      val returns = Await.result(returnRepository.getById(params("id").toLong), Duration.Inf)
      Ok(Json.toJson(returns))
    }

  }

  def readByUserId: Action[AnyContent] = Action { implicit request =>

    val params = request.queryString.map { case (k, v) => k -> v.mkString }
    if (!params.contains("userId")) {
      Ok("No userId parameter in query")
    } else {

      try {
        val returns = Await.result(returnRepository.getByUserId(params("userId")), Duration.Inf)

        val id = params("userId")
        val returnsArray = returns.map(c => (c._1, c._2, c._4))
        val res = JsObject(Seq(
          ("userId", Json.toJson(id)),
          ("returns", Json.toJson(returnsArray))))

        Ok(res)
      } catch {
        case e: NoSuchElementException => Ok("No element with such id")
      }

    }

  }

  def update = Action.async { implicit request =>
    val params = request.queryString.map { case (k, v) => k -> v.mkString }

    if (!params.contains("id")) {
      Future(Ok("No id parameter in query"))
    } else {

      try {
        val id = params("id").toLong
        val returns = returnRepository.getByIdOption(id)

        returns.map(return_elem => return_elem match {
          case Some(r) => {
            val userId = if (params.contains("userId")) params("userId") else r.userId
            val productId = if (params.contains("productId")) params("productId").toLong else r.productId
            val status = if (params.contains("status")) params("status") else r.status

            val newReturn = Return(id, userId, productId, status)
            returnRepository.update(id, newReturn)
            Ok("Return updated!")
          }
          case None => Ok("No object with such id")
        })
      } catch {
        case e: NumberFormatException => Future(Ok("Id, userId and productId have to be integer"))
      }

    }
  }

  def delete = Action { implicit request =>
    val params = request.queryString.map { case (k, v) => k -> v.mkString }

    if (!params.contains("id")) {
      Ok("No id parameter in query")
    } else {
      try {
        returnRepository.delete(params("id").toLong)
        Ok("Return canceled")
      } catch {
        case e: NumberFormatException => Ok("Id has to be integer")
      }
    }
  }

}

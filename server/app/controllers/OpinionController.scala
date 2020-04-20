package controllers

import javax.inject._
import models.{OpinionRepository, ProductRepository, Opinion, UserRepository}
import play.api.mvc._
import play.filters.csrf.CSRF

import scala.concurrent.{ExecutionContext, Future}


@Singleton
class OpinionController @Inject()(opinionRepository: OpinionRepository,
                                  productRepository: ProductRepository,
                                  userRepository: UserRepository,
                                  cc: MessagesControllerComponents)(implicit ec: ExecutionContext) extends MessagesAbstractController(cc) {

  def getToken = Action { implicit request =>
    val token = CSRF.getToken.get.value
    Ok(token)
  }

  def create:Action[AnyContent] = Action.async { implicit request =>
    val params = request.queryString.map { case (k,v) => k -> v.mkString }
    if (!params.contains("userId")) {
      Future(Ok("No userId parameter in query"))
    }
    else if (!params.contains("productId")) {
      Future(Ok("No productId parameter in query"))
    }
    else if (!params.contains("content")) {
      Future(Ok("No content parameter in query"))
    }
    else {
      val userId = params("userId").toInt
      val prodId = params("productId").toInt
      val content = params("content")

      userRepository.exists(userId).map(userExists => {
        productRepository.exists(prodId).map(productExists => {
          if (userExists && productExists) {
            opinionRepository.create(userId, prodId, content)
          }
        })
      })

      Future(Ok("Opinion created!"))
    }
  }

  def read: Action[AnyContent] = Action.async { implicit request =>
    val opinions = opinionRepository.list()
    opinions.map( opinion => Ok(opinion.toString()) )
  }

  def readById: Action[AnyContent] = Action.async { implicit request =>

    val params = request.queryString.map { case (k,v) => k -> v.mkString }
    if (!params.contains("id")) {
      Future(Ok("No id parameter in query"))
    }
    else {
      val opinions = opinionRepository.getById(params("id").toLong)
      opinions.map(opinion => Ok(opinion.toString()))
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
        val opinions = opinionRepository.getById(id)

        opinions.map(opinion => opinion match {
          case Some(o) => {
            val userId = if (params.contains("userId")) params("userId").toLong else o.userId
            val productId = if (params.contains("productId")) params("productId").toLong else o.productId
            val content = if (params.contains("content")) params("content") else o.content

            val newOpinion = Opinion(id, userId, productId, content)
            opinionRepository.update(id, newOpinion)
            Ok("Opinion updated!")
          }
          case None => Ok("No object with such id")
        })
      }
      catch {
        case e: NumberFormatException => Future(Ok("Id, userId and productId have to be integer"))
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
        opinionRepository.delete(params("id").toLong)
        Ok("Opinion deleted!")
      }
      catch {
        case e: NumberFormatException => Ok("Id has to be integer")
      }
    }
  }

}

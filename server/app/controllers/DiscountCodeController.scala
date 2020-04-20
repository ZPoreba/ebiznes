package controllers

import javax.inject._
import models.{DiscountCodeRepository, ProductRepository}
import play.api.mvc._
import play.filters.csrf.CSRF

import scala.concurrent.{ExecutionContext, Future}


@Singleton
class DiscountCodeController @Inject()(productRepository: ProductRepository,
                                       discountCodeRepository: DiscountCodeRepository,
                                       cc: MessagesControllerComponents)(implicit ec: ExecutionContext) extends MessagesAbstractController(cc) {

  def getToken = Action { implicit request =>
    val token = CSRF.getToken.get.value
    Ok(token)
  }

  def create:Action[AnyContent] = Action.async { implicit request =>
    val params = request.queryString.map { case (k,v) => k -> v.mkString }
    if (!params.contains("productId")) {
      Future(Ok("No productId parameter in query"))
    }
    else if (!params.contains("code")) {
      Future(Ok("No code parameter in query"))
    }
    else {

      val prodId = params("productId").toInt
      val code = params("code").toInt

      productRepository.exists(prodId).map(exists => {
        if (exists) {
          discountCodeRepository.create(prodId, code)
        }
      })

      Future(Ok("DiscountCode created!"))
    }
  }

  def read: Action[AnyContent] = Action.async { implicit request =>
    val codes = discountCodeRepository.list()
    codes.map( code => Ok(code.toString()) )
  }

  def readById: Action[AnyContent] = Action.async { implicit request =>

    val params = request.queryString.map { case (k,v) => k -> v.mkString }
    if (!params.contains("id")) {
      Future(Ok("No id parameter in query"))
    }
    else {
      val codes = discountCodeRepository.getById(params("id").toLong)
      codes.map(code => Ok(code.toString()))
    }

  }

  def update = Action { implicit request =>
    Ok("DicountCode update not needed if we assume possibility of existence" +
      "of many discount codes to single product. ")
  }

  def delete = Action { implicit request =>
    val params = request.queryString.map { case (k,v) => k -> v.mkString }

    if (!params.contains("id")) {
      Ok("No id parameter in query")
    }
    else {
      try {
        discountCodeRepository.delete(params("id").toLong)
        Ok("DiscountCode deleted!")
      }
      catch {
        case e: NumberFormatException => Ok("Id has to be integer")
      }
    }
  }

}

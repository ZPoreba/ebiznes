package controllers

import javax.inject._
import models.{ DiscountCodeRepository, ProductRepository }
import play.api.libs.json._
import play.api.mvc._
import play.filters.csrf.CSRF
import scala.concurrent.duration.Duration
import scala.concurrent.{ Await, ExecutionContext, Future }

@Singleton
class ApiDiscountCodeController @Inject() (
  scc: SilhouetteControllerComponents,
  productRepository: ProductRepository,
  discountCodeRepository: DiscountCodeRepository)(implicit ec: ExecutionContext) extends SilhouetteController(scc) {

  def getToken = SecuredAction { implicit request =>
    val token = CSRF.getToken.get.value
    Ok(token)
  }

  def create: Action[AnyContent] = SecuredAction.async { implicit request =>
    val params = request.queryString.map { case (k, v) => k -> v.mkString }
    if (!params.contains("productId")) {
      Future(Ok("No productId parameter in query"))
    } else if (!params.contains("code")) {
      Future(Ok("No code parameter in query"))
    } else {

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

  def read: Action[AnyContent] = SecuredAction { implicit request =>
    val codes = Await.result(discountCodeRepository.list(), Duration.Inf)
    Ok(Json.toJson(codes))
  }

  def readById: Action[AnyContent] = SecuredAction { implicit request =>

    val params = request.queryString.map { case (k, v) => k -> v.mkString }
    if (!params.contains("productId")) {
      Ok("No productId parameter in query")
    } else {
      val codes = Await.result(discountCodeRepository.getById(params("productId").toLong), Duration.Inf)
      Ok(Json.toJson(codes))
    }

  }

  def checkCodeForProducts: Action[AnyContent] = SecuredAction { implicit request =>

    val params = request.queryString.map { case (k, v) => k -> v.mkString }
    if (!params.contains("code")) {
      Ok("No code parameter in query")
    } else if (!params.contains("products")) {
      Ok("No products parameter in products")
    } else {
      val prodArray = params("products").replaceAll(" ", "").split(',')
      val longProdArray = prodArray.map(_.toLong)
      val code = params("code").toLong
      var exists = false;

      for (prodId <- longProdArray) {
        val discountCodeExists = Await.result(discountCodeRepository.exists(prodId, code), Duration.Inf);
        if (discountCodeExists) {
          exists = true;
        }
      }

      Ok(Json.toJson(exists))
    }
  }

  def update = SecuredAction { implicit request =>
    Ok("DicountCode update not needed if we assume possibility of existence" +
      "of many discount codes to single product. ")
  }

  def delete = SecuredAction { implicit request =>
    val params = request.queryString.map { case (k, v) => k -> v.mkString }

    if (!params.contains("id")) {
      Ok("No id parameter in query")
    } else {
      try {
        discountCodeRepository.delete(params("id").toLong)
        Ok("DiscountCode deleted!")
      } catch {
        case e: NumberFormatException => Ok("Id has to be integer")
      }
    }
  }

}
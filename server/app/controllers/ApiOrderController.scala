package controllers

import javax.inject._
import models.{ Order, OrderProductRepository, OrderRepository, Product, ProductRepository }
import play.api.libs.json._
import play.api.mvc._
import play.filters.csrf.CSRF
import scala.concurrent.duration.Duration
import scala.concurrent.{ Await, ExecutionContext, Future }

@Singleton
class ApiOrderController @Inject() (
  scc: SilhouetteControllerComponents,
  productRepository: ProductRepository,
  orderRepository: OrderRepository,
  orderProductRepository: OrderProductRepository)(implicit ec: ExecutionContext)
  extends SilhouetteController(scc) {

  def getToken = Action { implicit request =>
    val token = CSRF.getToken.get.value
    Ok(token)
  }

  def create: Action[AnyContent] = SecuredAction.async { implicit request =>
    val params = request.queryString.map { case (k, v) => k -> v.mkString }
    if (!params.contains("userId")) {
      Future(Ok("No userId parameter in query"))
    } else if (!params.contains("paymentId")) {
      Future(Ok("No paymentId parameter in query"))
    } else if (!params.contains("status")) {
      Future(Ok("No status parameter in query"))
    } else if (!params.contains("products")) {
      Future(Ok("No products parameter in query"))
    } else {
      val prodArray = params("products").replaceAll(" ", "").split(',')
      val longProdArray = prodArray.map(_.toLong)
      val prodId = orderRepository.create(params("userId"), params("paymentId").toLong, params("status"))
      prodId.map(id => {
        for (prodId <- longProdArray) {
          productRepository.exists(prodId).map(exists => {
            if (exists) {
              orderProductRepository.create(id, prodId)
            }
          })
        }

      })
      Future(Ok("Order created!"))
    }
  }

  def read: Action[AnyContent] = SecuredAction { implicit request =>
    val orders = Await.result(orderRepository.list(), Duration.Inf)
    Ok(Json.toJson(orders))
  }

  def readById: Action[AnyContent] = SecuredAction { implicit request =>

    val params = request.queryString.map { case (k, v) => k -> v.mkString }
    if (!params.contains("id")) {
      Ok("No id parameter in query")
    } else {

      try {
        val orderProducts = Await.result(orderProductRepository.getByOrderId(params("id").toLong), Duration.Inf)
        val orders = Await.result(orderRepository.getById(params("id").toLong), Duration.Inf)
        val products = orderProducts.map(p => p._5)
        val jsonOrders = Json.toJson(orders)

        val res = JsObject(Seq(
          ("order", jsonOrders),
          ("products", Json.toJson(products))))

        Ok(res)
      } catch {
        case e: NoSuchElementException => Ok("No element with such id")
      }

    }

  }

  def readByUserId: Action[AnyContent] = SecuredAction { implicit request =>

    val params = request.queryString.map { case (k, v) => k -> v.mkString }
    if (!params.contains("userId")) {
      Ok("No userId parameter in query")
    } else {

      try {
        val orderProducts = Await.result(orderProductRepository.getByUserId(params("userId")), Duration.Inf)

        val id = params("userId")
        val orders = orderProducts.map(c => c._1)
        val res = JsObject(Seq(
          ("userId", Json.toJson(id)),
          ("orders", Json.toJson(orders))))

        Ok(res)
      } catch {
        case e: NoSuchElementException => Ok("No element with such id")
      }

    }

  }

  def update = SecuredAction.async { implicit request =>
    val params = request.queryString.map { case (k, v) => k -> v.mkString }

    if (!params.contains("id")) {
      Future(Ok("No id parameter in query"))
    } else {

      try {
        val id = params("id").toLong
        val orders = orderRepository.getByIdOption(id)

        orders.map(order => order match {
          case Some(o) => {
            val userId = if (params.contains("userId")) params("userId") else o.userId
            val paymentId = if (params.contains("paymentId")) params("paymentId").toLong else o.paymentId
            val status = if (params.contains("status")) params("status") else o.status
            val products = if (params.contains("products")) params("products").replaceAll(" ", "")
              .split(',')
              .map(_.toLong)
            else Array[Long]()

            if (products.size != 0) orderProductRepository.deleteOrder(id) // if products are not empty, delete old one

            val newOrder = Order(id, userId, paymentId, status)
            orderRepository.update(id, newOrder)
            for (prodId <- products) {
              productRepository.exists(prodId).map(exists => {
                if (exists) {
                  orderProductRepository.create(id, prodId)
                }
              })
            }
            Ok("Order updated!")
          }
          case None => Ok("No object with such id")
        })
      } catch {
        case e: NumberFormatException => Future(Ok("Id has to be integer"))
      }

    }
  }

  def delete = SecuredAction { implicit request =>
    val params = request.queryString.map { case (k, v) => k -> v.mkString }

    if (!params.contains("id")) {
      Ok("No id parameter in query")
    } else {
      try {
        orderProductRepository.deleteOrder(params("id").toLong)
        orderRepository.delete(params("id").toLong)
        Ok("Order deleted!")
      } catch {
        case e: NumberFormatException => Ok("Id has to be integer")
      }
    }
  }

}

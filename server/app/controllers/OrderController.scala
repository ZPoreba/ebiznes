package controllers

import javax.inject._
import models.{OrderProductRepository, OrderRepository, Product, Order, ProductRepository}
import play.api.mvc._
import play.filters.csrf.CSRF

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class OrderController @Inject()(productRepository: ProductRepository,
                                orderRepository: OrderRepository,
                                orderProductRepository: OrderProductRepository,
                                cc: MessagesControllerComponents)(implicit ec: ExecutionContext)
  extends MessagesAbstractController(cc) {

  def getToken = Action { implicit request =>
    val token = CSRF.getToken.get.value
    Ok(token)
  }

  def create:Action[AnyContent] = Action.async { implicit request =>
    val params = request.queryString.map { case (k,v) => k -> v.mkString }
    if (!params.contains("userId")) {
      Future(Ok("No userId parameter in query"))
    }
    else if (!params.contains("paymentId")) {
      Future(Ok("No paymentId parameter in query"))
    }
    else if (!params.contains("status")) {
      Future(Ok("No status parameter in query"))
    }
    else if (!params.contains("products")) {
      Future(Ok("No products parameter in query"))
    }
    else {
      val prodArray = params("products").replaceAll(" ", "").split( ',' )
      val longProdArray = prodArray.map(_.toLong)
      val prodId = orderRepository.create(params("userId").toLong, params("paymentId").toLong, params("status"))
      prodId.map(id => {
        for (prodId <- longProdArray) {
             productRepository.exists(prodId).map(exists => {
            if (exists) {
              orderProductRepository.create(id, prodId)
            }
          })
        }

      })
      Future(Ok("Product created!"))
    }
  }

  def read: Action[AnyContent] = Action.async { implicit request =>
    val orders = orderProductRepository.list()
    orders.map( order => Ok(order.toString()) )
  }

  def readById: Action[AnyContent] = Action.async { implicit request =>

    val params = request.queryString.map { case (k,v) => k -> v.mkString }
    if (!params.contains("id")) {
      Future(Ok("No id parameter in query"))
    }
    else {
      val orders = orderProductRepository.getByOrderId(params("id").toLong)
      orders.map(order => Ok(order.toString()))
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
        val orders = orderRepository.getById(id)

        orders.map(order => order match {
          case Some(o) => {
            val userId = if (params.contains("userId")) params("userId").toLong else o.userId
            val paymentId = if (params.contains("paymentId")) params("paymentId").toLong else o.paymentId
            val status = if (params.contains("status")) params("status") else o.status
            val products = if (params.contains("products")) params("products").replaceAll(" ", "")
              .split(',')
              .map(_.toLong) else Array[Long]()

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
        orderProductRepository.deleteOrder(params("id").toLong)
        orderRepository.delete(params("id").toLong)
        Ok("Order deleted!")
      }
      catch {
        case e: NumberFormatException => Ok("Id has to be integer")
      }
    }
  }

}

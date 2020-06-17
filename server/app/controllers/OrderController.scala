package controllers

import javax.inject._
import models.{ ApiUser, Order, OrderProduct, OrderProductRepository, OrderRepository, Payment, PaymentRepository, Product, ProductRepository, User, UserRepository }
import play.api.data.Form
import play.api.data.Forms.mapping
import play.api.data.Forms._
import play.api.mvc._
import play.filters.csrf.CSRF

import scala.collection.mutable.ListBuffer
import scala.concurrent.duration.Duration
import scala.concurrent.{ Await, ExecutionContext, Future }
import scala.util.{ Failure, Success }

@Singleton
class OrderController @Inject() (
  productRepository: ProductRepository,
  orderRepository: OrderRepository,
  orderProductRepository: OrderProductRepository,
  userRepository: UserRepository,
  paymentRepository: PaymentRepository,
  cc: MessagesControllerComponents)(implicit ec: ExecutionContext)
  extends MessagesAbstractController(cc) {

  val createForm: Form[CreateOrderForm] = Form {
    mapping(
      "userId" -> nonEmptyText,
      "paymentId" -> longNumber,
      "status" -> nonEmptyText,
      "product" -> seq(longNumber))(CreateOrderForm.apply)(CreateOrderForm.unapply)
  }

  val updateForm: Form[UpdateOrderForm] = Form {
    mapping(
      "id" -> longNumber,
      "userId" -> nonEmptyText,
      "paymentId" -> longNumber,
      "status" -> nonEmptyText,
      "product" -> seq(longNumber))(UpdateOrderForm.apply)(UpdateOrderForm.unapply)
  }

  def create: Action[AnyContent] = Action { implicit request =>
    var prod = Await.result(productRepository.list(), Duration.Inf)
    var prod_list = new ListBuffer[(String, String)]()
    for (p <- prod) {
      prod_list.+=((p.id.toString, p.name))
    }
    val prod_list_seq = prod_list.toList

    val users = Await.result(userRepository.list(), Duration.Inf)
    val payments = Await.result(paymentRepository.list(), Duration.Inf)
    Ok(views.html.orderadd(createForm, users, payments, prod_list_seq))
  }

  def createHandle = Action.async { implicit request =>
    var usr = Await.result(userRepository.list(), Duration.Inf)
    var pay = Await.result(paymentRepository.list(), Duration.Inf)
    var prod = Await.result(productRepository.list(), Duration.Inf)

    var prod_list = new ListBuffer[(String, String)]()
    for (p <- prod) {
      prod_list.+=((p.id.toString, p.name))
    }
    val prod_list_seq = prod_list.toList

    createForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.orderadd(errorForm, usr, pay, prod_list_seq)))
      },
      order => {
        orderRepository.create(order.userId, order.paymentId, order.status).map { id =>
          for (p <- order.product) {
            orderProductRepository.create(id, p)
          }
          Redirect(routes.OrderController.create()).flashing("success" -> "order.created")
        }
      })

  }

  def read: Action[AnyContent] = Action { implicit request =>
    val orders = Await.result(orderRepository.list(), Duration.Inf)
    val order_products = new ListBuffer[Seq[(Long, String, Long, String, Long)]]()

    for (o <- orders) {
      val order_products_result = Await.result(orderProductRepository.getByOrderId(o.id), Duration.Inf)
      order_products.+=(order_products_result)
    }
    val ord_list_seq = order_products.toList

    Ok(views.html.ordersread(orders, ord_list_seq))
  }

  def readById(id: Long): Action[AnyContent] = Action.async { implicit request =>
    val order = orderRepository.getByIdOption(id)
    val order_product_result = Await.result(orderProductRepository.getByOrderId(id), Duration.Inf)

    order.map(ord => ord match {
      case Some(o) => Ok(views.html.orderread(o, order_product_result))
      case None => Redirect(routes.OrderController.read())
    })
  }

  def update(id: Long): Action[AnyContent] = Action.async { implicit request =>

    val products = productRepository.list()
    products.map(prod => {
      var prod_list = new ListBuffer[(String, String)]()
      for (p <- prod) {
        prod_list.+=((p.id.toString, p.name))
      }
      val prod_list_seq = prod_list.toList

      val order = orderRepository.getById(id)
      val order_result = Await.result(order, Duration.Inf)
      val users = Await.result(userRepository.list(), Duration.Inf)
      val payments = Await.result(paymentRepository.list(), Duration.Inf)

      val orderForm = updateForm.fill(UpdateOrderForm(order_result.id, order_result.userId, order_result.paymentId, order_result.status, Seq[Long]()))
      Ok(views.html.orderupdate(orderForm, users, payments, prod_list_seq))

    })

  }

  def updateHandle = Action.async { implicit request =>

    var usr: Seq[ApiUser] = Seq[ApiUser]()
    val users = userRepository.list().onComplete {
      case Success(u) => usr = u
      case Failure(_) => print("fail")
    }

    var pay: Seq[Payment] = Seq[Payment]()
    val payments = paymentRepository.list().onComplete {
      case Success(p) => pay = p
      case Failure(_) => print("fail")
    }

    var prod: Seq[Product] = Seq[Product]()
    val products = productRepository.list().onComplete {
      case Success(p) => prod = p
      case Failure(_) => print("fail")
    }

    var prod_list = new ListBuffer[(String, String)]()
    for (p <- prod) {
      prod_list.+=((p.id.toString, p.name))
    }
    val prod_list_seq = prod_list.toList
    val users_result = Await.result(userRepository.list(), Duration.Inf)
    val payments_result = Await.result(paymentRepository.list(), Duration.Inf)

    updateForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.orderupdate(errorForm, users_result, payments_result, prod_list_seq)))
      },
      order => {

        if (order.product.size > 0) {
          orderProductRepository.deleteOrder(order.id)
          for (p <- order.product) {
            orderProductRepository.create(order.id, p)
          }
        }

        orderRepository.update(order.id, Order(order.id, order.userId, order.paymentId, order.status)).map { _ =>
          Redirect(routes.OrderController.update(order.id)).flashing("success" -> "order updated")
        }
      })

  }

  def delete(id: Long): Action[AnyContent] = Action {
    orderProductRepository.deleteOrder(id)
    orderRepository.delete(id)
    Redirect("/readorders")
  }

}

case class CreateOrderForm(userId: String, paymentId: Long, status: String, product: Seq[Long])
case class UpdateOrderForm(id: Long, userId: String, paymentId: Long, status: String, product: Seq[Long])
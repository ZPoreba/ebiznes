package controllers

import java.text.SimpleDateFormat

import javax.inject._
import models.{Payment, PaymentRepository}
import play.api.mvc._
import play.filters.csrf.CSRF

import scala.concurrent.{ExecutionContext, Future}


@Singleton
class PaymentController @Inject()(paymentRepository: PaymentRepository, cc: MessagesControllerComponents) (implicit ec: ExecutionContext) extends MessagesAbstractController(cc) {

  def getToken = Action { implicit request =>
    val token = CSRF.getToken.get.value
    Ok(token)
  }

  def stringToDate(date:String, format:String = "yyyy-MM-dd"):java.sql.Date = {
    import java.text._
    val sdf = new SimpleDateFormat(format)
    val utilDate = sdf.parse(date)
    new java.sql.Date(utilDate.getTime)
  }

  def create = Action.async { implicit request =>
    val params = request.queryString.map { case (k,v) => k -> v.mkString }
    if (!params.contains("date")) {
      Future(Ok("No date parameter in query"))
    }
    else if (!params.contains("status")) {
      Future(Ok("No status parameter in query"))
    }
    else {
      val date = stringToDate(params("date"))
      paymentRepository.create(date, params("status"))
      Future(Ok("Payment created!"))
    }
  }

  def read: Action[AnyContent] = Action.async { implicit request =>
    val payments = paymentRepository.list()
    payments.map( payment => Ok(payment.toString()) )
  }

  def readById: Action[AnyContent] = Action.async { implicit request =>

    val params = request.queryString.map { case (k,v) => k -> v.mkString }
    if (!params.contains("id")) {
      Future(Ok("No id parameter in query"))
    }
    else {
      val payments = paymentRepository.getById(params("id").toLong)
      payments.map(payment => payment match {
        case Some(p) => Ok(p.toString())
        case None => Ok("No payment with id")
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
        val payments = paymentRepository.getById(id)

        payments.map(payment => payment match {
          case Some(p) => {
            val date = if (params.contains("date")) stringToDate(params("date")) else p.date
            val status = if (params.contains("status")) params("status") else p.status

            val newPayment = Payment(id, date, status)
            paymentRepository.update(id, newPayment)
            Ok("Payment updated!")
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
        paymentRepository.delete(params("id").toLong)
        Ok("Payment deleted!")
      }
      catch {
        case e: NumberFormatException => Ok("Id has to be integer")
      }
    }
  }

}

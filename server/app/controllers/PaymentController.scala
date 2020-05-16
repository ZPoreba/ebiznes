package controllers

import java.text.SimpleDateFormat

import javax.inject._
import models.{Payment, PaymentRepository}
import play.api.data.Form
import play.api.data.Forms.mapping
import play.api.data.Forms._
import play.api.mvc._
import play.filters.csrf.CSRF

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}


@Singleton
class PaymentController @Inject()(paymentRepository: PaymentRepository, cc: MessagesControllerComponents) (implicit ec: ExecutionContext) extends MessagesAbstractController(cc) {

  val createForm: Form[CreatePaymentForm] = Form {
    mapping(
      "date" -> nonEmptyText,
      "status" -> nonEmptyText
    )(CreatePaymentForm.apply)(CreatePaymentForm.unapply)
  }

  val updateForm: Form[UpdatePaymentForm] = Form {
    mapping(
      "id" -> longNumber,
      "date" -> nonEmptyText,
      "status" -> nonEmptyText
    )(UpdatePaymentForm.apply)(UpdatePaymentForm.unapply)
  }

  def stringToDate(date:String, format:String = "yyyy-MM-dd"):java.sql.Date = {
    import java.text._
    val sdf = new SimpleDateFormat(format)
    val utilDate = sdf.parse(date)
    new java.sql.Date(utilDate.getTime)
  }

  def create: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.paymentadd(createForm))
  }

  def createHandle = Action.async { implicit request =>

    createForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.paymentadd(errorForm))
        )
      },
      payment => {
        paymentRepository.create(stringToDate(payment.date), payment.status).map { _ =>
          Redirect(routes.PaymentController.create()).flashing("success" -> "payment.created")
        }
      }
    )
  }

  def read: Action[AnyContent] = Action { implicit request =>
    val payments = Await.result(paymentRepository.list(), Duration.Inf)
    Ok(views.html.paymentsread(payments))
  }

  def readById(id: Long): Action[AnyContent] = Action.async { implicit request =>
    val payment = paymentRepository.getByIdOption(id)

    payment.map(pay => pay match {
      case Some(p) => Ok(views.html.paymentread(p))
      case None => Redirect(routes.PaymentController.read())
    })
  }

  def update(id: Long): Action[AnyContent] = Action { implicit request =>
    val payment_result = Await.result(paymentRepository.getById(id), Duration.Inf)

    val payForm = updateForm.fill(UpdatePaymentForm(payment_result.id, payment_result.date.toString, payment_result.status))
    Ok(views.html.paymentupdate(payForm))
  }

  def updateHandle = Action.async { implicit request =>

    updateForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.paymentupdate(errorForm))
        )
      },
      payment => {
        paymentRepository.update(payment.id, Payment(payment.id, stringToDate(payment.date), payment.status)).map { _ =>
          Redirect(routes.PaymentController.update(payment.id)).flashing("success" -> "payment updated")
        }
      }
    )

  }

  def delete(id: Long): Action[AnyContent] = Action {
    paymentRepository.delete(id)
    Redirect("/readpayments")
  }

}

case class CreatePaymentForm(date: String, status: String)
case class UpdatePaymentForm(id: Long, date: String, status: String)
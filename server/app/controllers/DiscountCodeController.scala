package controllers

import javax.inject._
import models.{DiscountCodeRepository, ProductRepository}
import play.api.data.Form
import play.api.data.Forms.mapping
import play.api.data.Forms._
import play.api.mvc._
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext}


@Singleton
class DiscountCodeController @Inject()(productRepository: ProductRepository,
                                       discountCodeRepository: DiscountCodeRepository,
                                       cc: MessagesControllerComponents)(implicit ec: ExecutionContext) extends MessagesAbstractController(cc) {

  val createForm: Form[CreateDiscountCodeForm] = Form {
    mapping(
      "productId" -> longNumber,
      "code" -> longNumber,
    )(CreateDiscountCodeForm.apply)(CreateDiscountCodeForm.unapply)
  }

  val updateForm: Form[UpdateDiscountCodeForm] = Form {
    mapping(
      "productId" -> longNumber,
      "code" -> longNumber,
    )(UpdateDiscountCodeForm.apply)(UpdateDiscountCodeForm.unapply)
  }

  def create: Action[AnyContent] = Action { implicit request =>
    var prod = Await.result(productRepository.list(), Duration.Inf)
    Ok(views.html.discountcodeadd(createForm, prod))
  }

  def createHandle = Action { implicit request =>
    var prod = Await.result(productRepository.list(), Duration.Inf)

    createForm.bindFromRequest.fold(
      errorForm => {
        BadRequest(views.html.discountcodeadd(errorForm, prod))
      },
      code => {
        discountCodeRepository.create(code.productId, code.code)
        Redirect(routes.DiscountCodeController.create()).flashing("success" -> "discountcode.created")

      }
    )
  }

  def readById(id: Long): Action[AnyContent] = Action.async { implicit request =>
    val discountcode = discountCodeRepository.getById(id)

    discountcode.map(cat => cat match {
      case Some(d) => Ok(views.html.discountcoderead(d))
      case None => Redirect(routes.DiscountCodeController.read())
    })
  }

  def read: Action[AnyContent] = Action { implicit request =>
    val discountcodes = Await.result(discountCodeRepository.list(), Duration.Inf)
    Ok(views.html.discountcodesread(discountcodes))
  }

  def update = Action { implicit request =>
    Ok("DicountCode update not needed if we assume possibility of existence" +
      "of many discount codes to single product. ")
  }

  def delete(code: Long): Action[AnyContent] = Action {
    discountCodeRepository.delete(code)
    Redirect("/readdiscountcodes")
  }

}

case class CreateDiscountCodeForm(productId: Long, code: Long)
case class UpdateDiscountCodeForm(productId: Long, code: Long)
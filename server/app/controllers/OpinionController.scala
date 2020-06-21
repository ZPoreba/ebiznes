package controllers

import javax.inject._
import models.{ Opinion, OpinionRepository, ProductRepository, UserRepository }
import play.api.data.Form
import play.api.data.Forms.mapping
import play.api.mvc._
import play.api.data.Forms._

import scala.concurrent.duration.Duration
import scala.concurrent.{ Await, ExecutionContext, Future }

@Singleton
class OpinionController @Inject() (
  opinionRepository: OpinionRepository,
  cc: MessagesControllerComponents)(implicit ec: ExecutionContext) extends MessagesAbstractController(cc) {

  val createForm: Form[CreateOpinionForm] = Form {
    mapping(
      "userId" -> nonEmptyText,
      "productId" -> longNumber,
      "content" -> nonEmptyText)(CreateOpinionForm.apply)(CreateOpinionForm.unapply)
  }

  val updateForm: Form[UpdateOpinionForm] = Form {
    mapping(
      "id" -> longNumber,
      "userId" -> nonEmptyText,
      "productId" -> longNumber,
      "content" -> nonEmptyText)(UpdateOpinionForm.apply)(UpdateOpinionForm.unapply)
  }

  def create: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.opinionadd(createForm))
  }

  def createHandle = Action.async { implicit request =>

    createForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.opinionadd(errorForm)))
      },
      opinion => {
        opinionRepository.create(opinion.userId, opinion.productId, opinion.content).map { _ =>
          Redirect(routes.OpinionController.create()).flashing("success" -> "opinion.created")
        }
      })

  }

  def readById(id: Long): Action[AnyContent] = Action.async { implicit request =>
    val opinion = opinionRepository.getByIdOption(id)

    opinion.map(cat => cat match {
      case Some(o) => Ok(views.html.opinionread(o))
      case None => Redirect(routes.OpinionController.read())
    })
  }

  def read: Action[AnyContent] = Action { implicit request =>
    val opinions = Await.result(opinionRepository.list(), Duration.Inf)
    Ok(views.html.opinionsread(opinions))
  }

  def update(id: Long): Action[AnyContent] = Action { implicit request =>
    val opinion_result = Await.result(opinionRepository.getById(id), Duration.Inf)

    val opForm = updateForm.fill(UpdateOpinionForm(opinion_result.id, opinion_result.userId, opinion_result.productId, opinion_result.content))
    Ok(views.html.opinionupdate(opForm))
  }

  def updateHandle = Action.async { implicit request =>

    updateForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.opinionupdate(errorForm)))
      },
      opinion => {
        opinionRepository.update(opinion.id, Opinion(opinion.id, opinion.userId, opinion.productId, opinion.content)).map { _ =>
          Redirect(routes.OpinionController.update(opinion.id)).flashing("success" -> "opinion updated")
        }
      })

  }

  def delete(id: Long): Action[AnyContent] = Action {
    opinionRepository.delete(id)
    Redirect("/readopinions")
  }

}

case class CreateOpinionForm(userId: String, productId: Long, content: String)
case class UpdateOpinionForm(id: Long, userId: String, productId: Long, content: String)
package controllers

import javax.inject._
import models.{ Category, CategoryRepository, ProductCategoryRepository }
import play.api.data.Form
import play.api.data.Forms.mapping
import play.api.data.Forms._
import play.api.mvc._
import scala.concurrent.duration.Duration
import scala.concurrent.{ Await, ExecutionContext, Future }

@Singleton
class CategoryController @Inject() (categoryRepository: CategoryRepository, productCategoryRepository: ProductCategoryRepository, cc: MessagesControllerComponents)(implicit ec: ExecutionContext) extends MessagesAbstractController(cc) {

  val categoryForm: Form[CreateCategoryForm] = Form {
    mapping(
      "name" -> nonEmptyText)(CreateCategoryForm.apply)(CreateCategoryForm.unapply)
  }

  val updateForm: Form[UpdateCategoryForm] = Form {
    mapping(
      "id" -> longNumber,
      "name" -> nonEmptyText)(UpdateCategoryForm.apply)(UpdateCategoryForm.unapply)
  }

  def create: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.categoryadd(categoryForm))
  }

  def createHandle = Action.async { implicit request =>

    categoryForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.categoryadd(errorForm)))
      },
      product => {
        categoryRepository.create(product.name).map { _ =>
          Redirect(routes.CategoryController.create()).flashing("success" -> "category.created")
        }
      })
  }

  def readById(id: Long): Action[AnyContent] = Action.async { implicit request =>
    val category = categoryRepository.getByIdOption(id)

    category.map(cat => cat match {
      case Some(c) => Ok(views.html.categoryread(c))
      case None => Redirect(routes.CategoryController.read())
    })
  }

  def read: Action[AnyContent] = Action { implicit request =>
    val categories = Await.result(categoryRepository.list(), Duration.Inf)
    Ok(views.html.categoriesread(categories))
  }

  def update(id: Long): Action[AnyContent] = Action { implicit request =>
    val category_result = Await.result(categoryRepository.getById(id), Duration.Inf)

    val catForm = updateForm.fill(UpdateCategoryForm(category_result.id, category_result.name))
    Ok(views.html.categoryupdate(catForm))
  }

  def updateHandle = Action.async { implicit request =>

    updateForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.categoryupdate(errorForm)))
      },
      category => {
        categoryRepository.update(category.id, Category(category.id, category.name)).map { _ =>
          Redirect(routes.CategoryController.update(category.id)).flashing("success" -> "category updated")
        }
      })

  }

  def delete(id: Long): Action[AnyContent] = Action {
    categoryRepository.delete(id)
    productCategoryRepository.deleteCategory(id)
    Redirect("/readcategories")
  }
}

case class CreateCategoryForm(name: String)
case class UpdateCategoryForm(id: Long, name: String)
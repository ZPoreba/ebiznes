package controllers

import javax.inject._
import play.api.mvc._
import play.filters.csrf.CSRF


@Singleton
class CategoryController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

  def getToken = Action { implicit request =>
    val token = CSRF.getToken.get.value
    Ok(token)
  }

  def create = Action {
    Ok("Category created!")
  }

  def read = Action {
    Ok("Category read!")
  }

  def update = Action { implicit request =>
    Ok("Category updated!")
  }

  def delete = Action { implicit request =>
    Ok("Category deleted!")
  }

}

package controllers

import javax.inject._
import play.api.mvc._
import play.filters.csrf.CSRF


@Singleton
class ProductController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

  def getToken = Action { implicit request =>
    val token = CSRF.getToken.get.value
    Ok(token)
  }

  def create = Action {
    Ok("Product created!")
  }

  def read = Action {
    Ok("Product read!")
  }

  def update = Action { implicit request =>
    Ok("Product updated!")
  }

  def delete = Action { implicit request =>
    Ok("Product deleted!")
  }

}

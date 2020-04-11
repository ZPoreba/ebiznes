package controllers

import javax.inject._
import play.api.mvc._
import play.filters.csrf.CSRF


@Singleton
class ReturnController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

  def getToken = Action { implicit request =>
    val token = CSRF.getToken.get.value
    Ok(token)
  }

  def create = Action {
    Ok("Return created!")
  }

  def read = Action {
    Ok("Return read!")
  }

  def update = Action { implicit request =>
    Ok("Return updated!")
  }

  def delete = Action { implicit request =>
    Ok("Return deleted!")
  }

}

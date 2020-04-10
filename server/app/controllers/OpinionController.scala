package controllers

import javax.inject._
import play.api.mvc._
import play.filters.csrf.CSRF


@Singleton
class OpinionController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

  def getToken = Action { implicit request =>
    val token = CSRF.getToken.get.value
    Ok(token)
  }

  def create = Action {
    Ok("Opinion created!")
  }

  def read = Action {
    Ok("Opinion read!")
  }

  def update = Action { implicit request =>
    Ok("Opinion updated!")
  }

  def delete = Action { implicit request =>
    Ok("Opinion deleted!")
  }

}

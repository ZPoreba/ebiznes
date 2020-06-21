package controllers

import forms._

import scala.concurrent.Future
import com.mohiva.play.silhouette.api.LogoutEvent
import javax.inject.Inject
import play.filters.csrf.CSRF

import scala.concurrent.ExecutionContext

class ApplicationController @Inject() (scc: SilhouetteControllerComponents)(implicit ex: ExecutionContext) extends SilhouetteController(scc) {

  def getToken = Action { implicit request =>
    val token = CSRF.getToken.get.value
    Ok(token)
  }
  
  def index = Action { implicit request =>
    Ok(views.html.index())
  }

  def signOut = SecuredAction.async { implicit request =>
    val result = Ok("Signed out")
    eventBus.publish(LogoutEvent(request.identity, request))
    authenticatorService.discard(request.authenticator, result)
  }
}

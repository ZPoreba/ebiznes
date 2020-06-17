package controllers

import java.util.UUID

import javax.inject.Inject
import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.impl.providers._
import forms.SignUpForm
import models.User
import play.api.libs.json.Json

import scala.concurrent.Future
import scala.concurrent.ExecutionContext

class SignUpController @Inject() (
  scc: SilhouetteControllerComponents)(implicit ex: ExecutionContext) extends SilhouetteController(scc) {

  def signUp = Action.async { implicit request =>
    SignUpForm.form.bindFromRequest.fold(
      _ => Future.successful(BadRequest),
      data => {
        val loginInfo = LoginInfo(CredentialsProvider.ID, data.email)
        userService.retrieve(loginInfo).flatMap {
          case Some(user) =>
            Future.successful(Conflict)
          case None =>
            val authInfo = passwordHasherRegistry.current.hash(data.password)
            val user = User(
              id = UUID.randomUUID(),
              loginInfo = loginInfo,
              firstName = Some(data.firstName),
              lastName = Some(data.lastName),
              fullName = Some(data.firstName + " " + data.lastName),
              email = Some(data.email),
              avatarURL = None,
              address = "")
            for {
              avatar <- avatarService.retrieveURL(data.email)
              user <- userService.save(user.copy(avatarURL = avatar))
              authInfo <- authInfoRepository.add(loginInfo, authInfo)
              authenticator <- authenticatorService.create(loginInfo)
              value <- authenticatorService.init(authenticator)
              result <- authenticatorService.embed(value, Ok(Json.toJson(user)))
            } yield {
              eventBus.publish(SignUpEvent(user, request))
              eventBus.publish(LoginEvent(user, request))
              result
            }
        }
      })
  }
}

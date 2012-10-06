package controllers

import models._
import jp.t2v.lab.play20.auth._
import play.api._
import play.api.mvc.Results._
import play.api.mvc._
import play.api.data._

trait AuthConfigImpl extends AuthConfig {
  type Id = Long
  type User = models.User
  type Authority = String
  val idManifest: ClassManifest[Id] = classManifest[Id]
  val sessionTimeoutInSeconds: Int = 3600 * 2

  def resolveUser(id: Long): Option[User] = User.getByID(id)

  def loginSucceeded(request: RequestHeader): PlainResult = {
    val uri = request.session.get("redirect_after_auth").getOrElse(
      routes.Application.index.url.toString)
    request.session - "redirect_after_auth"
    Redirect(uri)
  }

  def logoutSucceeded(request: RequestHeader): PlainResult =
    Redirect(routes.Application.index)

  def authenticationFailed(request: RequestHeader): PlainResult =
    Redirect(routes.UserController.login).withSession(
      "redirect_after_auth" -> request.uri)

  def authorizationFailed(request: RequestHeader): PlainResult =
    Forbidden("Invalid Username/Password")

  /** Handle authorization based on the kind of [[User]].
    *
    * @todo Implement this.
    */
  def authorize(user: User, authority: Authority) = true
}

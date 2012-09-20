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
  def loginSucceeded[A](request: Request[A]): PlainResult =
    Redirect(routes.Application.index)
  def logoutSucceeded[A](request: Request[A]): PlainResult =
    Redirect(routes.Application.index)
  def authenticationFailed[A](request: Request[A]): PlainResult =
    Redirect(routes.UserController.login)
  def authorizationFailed[A](request: Request[A]): PlainResult =
    Forbidden("Invalid Username/Password")

  /** Handle authorization based on the kind of [[User]].
    *
    * @todo Implement this.
    */
  def authorize(user: User, authority: Authority) = true
}

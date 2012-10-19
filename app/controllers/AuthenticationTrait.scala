/** (c) 2012 Ricky Elrod <ricky@elrod.me>
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */

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
    Redirect(uri).flashing(
      "success" -> "Welcome back!"
    )
  }

  def logoutSucceeded(request: RequestHeader): PlainResult =
    Redirect(routes.Application.index).flashing(
      "success" -> "You have been logged out."
    )

  def authenticationFailed(request: RequestHeader): PlainResult =
    Redirect(routes.UserController.login).withSession(
      "redirect_after_auth" -> request.uri
    ).flashing(
      "error" -> "Please log in to view that resource."
    )

  def authorizationFailed(request: RequestHeader): PlainResult =
    Forbidden("Invalid permissions.")

  /** Handle authorization based on the kind of [[User]].
    *
    * @todo Implement this.
    */
  def authorize(user: User, authority: Authority) = true
}

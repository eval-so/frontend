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

import play.api._
import play.api.mvc._
import jp.t2v.lab.play20.auth._
import models.{Server}

/** General purpose controllers and methods.
  *
  * Controllers such as index() (the controller which renders /)
  * live here.
  */
object Application extends Controller with Auth with AuthConfigImpl {

  /** Generate a sha256 hash from a given string.
    *
    * @param s the String to hash.
    * @return a String which is the sha256sum result.
    */
   def sha256(s: String): String = {
     val m = java.security.MessageDigest.getInstance("SHA-256")
     val b = s.getBytes("UTF-8")
     m.update(b, 0, b.length)
     new java.math.BigInteger(1, m.digest).toString(16)
   }

  /** Generate an md5sum hash from a given string.
    *
    * @param s the String to hash.
    * @todo combine this with sha256 above and rename to something generalized.
    * @return a String which is the sha256sum result.
    */
   def md5(s: String): String = {
     val m = java.security.MessageDigest.getInstance("MD5")
     val b = s.getBytes("UTF-8")
     m.update(b, 0, b.length)
     new java.math.BigInteger(1, m.digest).toString(16)
   }

  /** The index controller.
    *
    * Renders / (the front page) of Frontend.
    */
  def index = optionalUserAction { user => implicit request =>
    user match {
      case None => Ok(views.html.index(user, UserController.registerForm))
      case Some(user) => {
        val existingData = Map(
          "name" -> user.name,
          "email" -> user.email,
          "new_password" -> "",
          "old_password" -> "")
        Ok(views.html.user.profile(user, UserController.profileForm(user).fill(existingData)))
      }
    }
  }

  /** The product controller.
    *
    * Tell people about what we do, why we're awesome, and why they should use
    * our product over our OH SO MANY competitors.
    */
  def product = optionalUserAction { user => implicit request =>
    Ok(views.html.product(user))
  }

  /** The status controller.
    *
    * Give a quick overview about the status of our servers.
    * Mostly, things like:
    *   - Current load
    *   - Number of enabled vs. disabled servers
    */
  def status = optionalUserAction { user => implicit request =>
    val servers = Server.getAllServers()
    Ok(views.html.status(user, servers))
  }  
}

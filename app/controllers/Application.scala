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
        val existingData = (user.name, user.email, "", "")
        Ok(views.html.user.profile(user, UserController.profileForm.fill(existingData)))
      }
    }
  }

  /** The product controller.
    *
    * Tell people about what we do, why we're awesome, and why they should use
    * our product over our OH SO MANY competitors.
    */
  def product = Action { implicit request =>
    Ok(views.html.product())
  }

  /** The status controller.
    *
    * Give a quick overview about the status of our servers.
    * Mostly, things like:
    *   - Current load
    *   - Number of enabled vs. disabled servers
    */
  def status = Action { implicit request =>
    val servers = Server.getAllServers()
    Ok(views.html.status(servers))
  }  
}

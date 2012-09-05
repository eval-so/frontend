package controllers

import play.api._
import play.api.mvc._

/** General purpose controllers and methods.
  *
  * Controllers such as index() (the controller which renders /)
  * live here.
  */
object Application extends Controller {

  /** Generate a sha256 hash from a given string
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

  /** The index controller.
    *
    * Renders / (the front page) of Frontend.
    */
  def index = Action { implicit request =>
    Ok(views.html.index(UserController.registerForm))
  }

  /** The product controller.
    *
    * Tell people about what we do, why we're awesome, and why they should use
    * our product over our OH SO MANY competitors.
    */
  def product = Action {
    Ok(views.html.product())
  }

  /** The status controller.
    *
    * Give a quick overview about the status of our servers.
    * Mostly, things like:
    *   - Current load
    *   - Number of enabled vs. disabled servers
    */
  def status = Action {
    Ok(views.html.status())
  }
  
}

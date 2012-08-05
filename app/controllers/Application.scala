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
  def index = Action {
    Ok(views.html.index())
  }
  
}

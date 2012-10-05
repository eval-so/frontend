package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json.Json._

/** Controllers to handle all calls to version 1 of the Breakpoint API. */
object APIv1Controller extends Controller {

  /** The evaluation controller.
    *
    * Lets users evaluate code by POSTing to here.
    * 
    * We take the request, do some sanity checking, then pass it down stream to
    * the AS.
    */
  def evaluate = Action(parse.json) { implicit request =>
    Ok(toJson(
      Map("this_works" -> true)
    ))
  }
}

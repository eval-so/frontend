package controllers

import models._
import play.api._
import play.api.mvc._
import play.api.data._
import play.api.Play.current
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import com.typesafe.plugin._
import jp.t2v.lab.play20.auth._


/** Controllers which deal with authentication. */
object BreakpointApplicationController extends Controller with Auth with AuthConfigImpl {
  def newApplication = authorizedAction("user") { user => implicit request =>
    Ok("Apps")
  }

  def myApplications = authorizedAction("user") { user => implicit request =>
    val applications = user.applications.toList
    Ok(views.html.applications.myApplications(applications))
  }
}

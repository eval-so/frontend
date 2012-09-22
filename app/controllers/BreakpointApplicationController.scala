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

/** Controllers which deal with application management. */
object BreakpointApplicationController extends Controller with Auth with AuthConfigImpl {
  /** A form to handle creating new apps and editing existing ones. */
  val applicationForm: Form[BreakpointApplication] = Form(
    mapping(
      "name" -> nonEmptyText,
      "allow_anonymous_auth" -> checked("Foo")
    )
    {
      (name, allow_anonymous_auth) => BreakpointApplication(
        None,
        name,
        java.util.UUID.randomUUID().toString,
        None,
        allow_anonymous_auth
      )
    }
    {
      breakpointApplication => Some(
        breakpointApplication.name,
        breakpointApplication.allowAnonymousEvals
      )
    }
  )

  def newApplication = authorizedAction("user") { user => implicit request =>
    Ok(views.html.applications.newApplication(applicationForm))
  }

  def myApplications = authorizedAction("user") { user => implicit request =>
    val applications = user.applications.toList
    Ok(views.html.applications.myApplications(applications))
  }
}

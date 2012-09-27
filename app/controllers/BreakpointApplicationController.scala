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
      "allow_anonymous_auth" -> checked("allow_anonymous_auth"),
      "description" -> nonEmptyText
    )
    {
      (name, allow_anonymous_auth, description) => BreakpointApplication(
        None,
        name,
        java.util.UUID.randomUUID().toString,
        None,
        allow_anonymous_auth,
        description
      )
    }
    {
      breakpointApplication => Some(
        breakpointApplication.name,
        breakpointApplication.allowAnonymousEvals,
        breakpointApplication.description
      )
    }
  )

  def newApplication = authorizedAction("user") { user => implicit request =>
    Ok(views.html.applications.newApplication(user, applicationForm))
  }

  def processNewApplication = authorizedAction("user") { user =>
    implicit request =>
      applicationForm.bindFromRequest.fold(
        formWithErrors => BadRequest(views.html.applications.newApplication(user, formWithErrors)),
        validApplication => {
          BreakpointApplication.add(validApplication) match {
            case Some(appID) => user.addApplication(appID, true)
            case _ =>
          }
          Redirect(routes.BreakpointApplicationController.myApplications).flashing(
            "success" -> "Your new application has been created.")
        }
      )
  }

  def myApplications = authorizedAction("user") { user => implicit request =>
    val applications = user.applications.toList
    Ok(views.html.applications.myApplications(user, applications))
  }
}

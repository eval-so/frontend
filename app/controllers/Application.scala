package controllers

import gd.eval.Router
import gd.eval.SandboxedLanguage.Result

import play.api._
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.functional.syntax._

object Application extends Controller {

  def index = Action {
    Ok(views.html.index())
  }

  def apiDocs(version: Int) = Action { Ok(views.html.apidocs.v1()) }

  def tryEvalGD = Action { Ok(views.html.tryEvalGD()) }

  case class Evaluation(language: String, code: String)

  implicit val rds = (
    (__ \ 'language).read[String] and
    (__ \ 'code).read[String]
  ) tupled

  implicit val evaluationWrites = Json.writes[Result]

    def sayHello = Action(parse.json) { request =>
      request.body.validate[(String, String)].map {
        case (language, code) => {
          val evaluation = Router.route(language, code)
          evaluation match {
            case Some(sandbox) => sandbox.evaluate.fold(
              left => BadRequest(Json.obj("error" -> "An error has occurred and evaluation has halted.")),
              right => Ok(Json.toJson(right)))
            case None => BadRequest(Json.obj("error" -> "No such language."))
          }
        }
      }.recoverTotal{
        e => BadRequest(Json.obj("error" -> JsError.toFlatJson(e)))
      }
    }
}

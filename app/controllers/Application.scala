package controllers

import so.eval.Router
import so.eval.SandboxedLanguage.Result

import play.api._
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.modules.statsd.api.Statsd

object Application extends Controller {

  def index = Action {
    Ok(views.html.index())
  }

  def apiDocs(version: Int) = Action { Ok(views.html.apidocs.v1()) }

  def tryEvalSO = Action { Ok(views.html.tryEvalSO()) }

  def languages = Action { Ok(Json.obj("languages" -> so.eval.Router.languages.keys)) }

  case class Evaluation(language: String, code: String)

  implicit val rds = (
    (__ \ 'language).read[String] and
    (__ \ 'code).read[String]
  ) tupled

  implicit val evaluationWrites = Json.writes[Result]

    def evaluate(version: Int) = Action(parse.json) { request =>
      request.body.validate[(String, String)].map {
        case (language, code) => {
          val evaluation = Router.route(language, code)
          evaluation match {
            case Some(sandbox) => sandbox.evaluate.fold(
              left => {
                Statsd.increment(s"evaluation.${language}.error", value = 1)
                BadRequest(Json.obj("error" -> "An error has occurred and evaluation has halted."))
              },
              right => {
                Statsd.increment(s"evaluation.${language}.ok", value = 1)
                Statsd.timing(s"evaluation.${language}.walltime", right.wallTime)
                Ok(Json.toJson(right))
              }
            )
            case None => BadRequest(Json.obj("error" -> "No such language."))
          }
        }
      }.recoverTotal{
        e => BadRequest(Json.obj("error" -> JsError.toFlatJson(e)))
      }
    }
}

package controllers

import so.eval.Router
import so.eval.SandboxedLanguage.Result

import play.api._
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.concurrent.Akka
import play.api.libs.functional.syntax._
import play.api.Play.current
import play.modules.statsd.api.Statsd

import scala.concurrent.ExecutionContext.Implicits.global

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
          case Some(sandbox) => {
            val evalPromise = Akka.future { sandbox.evaluate }
            Async {
              evalPromise.map {
                _.map { resultTry =>
                  resultTry.compilationResult match {
                    case Some(result) => Statsd.timing(s"evaluation.${language}.compilation.walltime", result.wallTime)
                    case _ =>
                  }
                  Statsd.timing(s"evaluation.${language}.execution.walltime", resultTry.wallTime)
                  Ok(Json.toJson(resultTry))
                }.getOrElse {
                  Statsd.increment(s"evaluation.${language}.error", value = 1)
                  BadRequest(Json.obj("error" -> "An error has occurred and evaluation has halted."))
                }
              }
            }
          }
          case None => BadRequest(Json.obj("error" -> "No such language."))
        }
      }
    }.recoverTotal{
      e => BadRequest(Json.obj("error" -> JsError.toFlatJson(e)))
    }
  }
}

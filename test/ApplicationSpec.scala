package test

import org.scalatest.{BeforeAndAfter, FunSpec, Inside, ParallelTestExecution}
import org.scalatest.matchers.ShouldMatchers

import play.api.test._
import play.api.test.Helpers._
import play.api.libs.json._

class ApplicationSpec
  extends FunSpec
  with ShouldMatchers
  with Inside
  with BeforeAndAfter
  with ParallelTestExecution {

  describe("Frontend") {
    it("should send 404 on a bad request") {
      running(FakeApplication()) {
        route(FakeRequest(GET, "/boum")) should be (None)
      }
    }

    it("should render the index page") {
      running(FakeApplication()) {
        val home = route(FakeRequest(GET, "/")).get
        status(home) should be (OK)
        contentType(home) should be (Some("text/html"))
        contentAsString(home) should include ("Welcome to eval.so")
      }
    }
  }

  describe("The languages list") {
    it("should return JSON containing the available languages") {
      running(FakeApplication()) {
        val languages = route(FakeRequest(GET, "/api/languages")).get
        status(languages) should be (OK)
        contentType(languages) should be (Some("application/json"))
        contentAsString(languages) should include (""""languages":""")
        contentAsString(languages) should include ("""ruby""")
      }
    }
  }

  describe("The code evaluation endpoint") {
    it("should return BadRequest when missing required arguments") {
      running(FakeApplication()) {
        val missingCode = Json.obj("language" -> "ruby")
        val missingCodeEval = route(FakeRequest(POST, "/api/evaluate").withJsonBody(missingCode)).get
        status(missingCodeEval) should be (BAD_REQUEST)
        contentType(missingCodeEval) should be (Some("application/json"))

        val missingLanguage = Json.obj("code" -> "puts 123")
        val missingLanguageEval = route(FakeRequest(POST, "/api/evaluate").withJsonBody(missingLanguage)).get
        status(missingLanguageEval) should be (BAD_REQUEST)
        contentType(missingLanguageEval) should be (Some("application/json"))
      }
    }
  }
}

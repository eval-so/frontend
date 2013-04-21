package test

import org.scalatest.{BeforeAndAfter, FunSpec, Inside, ParallelTestExecution}
import org.scalatest.matchers.ShouldMatchers

import play.api.test._
import play.api.test.Helpers._

class ApplicationSpec
  extends FunSpec
  with ShouldMatchers
  with Inside
  with BeforeAndAfter
  with ParallelTestExecution {

  describe("The Application controller") {
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
}

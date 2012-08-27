package test

import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._
import play.api.libs.ws.WS

class IntegrationTest extends Specification {
  "Frontend" should {
    "Show the front page" in {
      running(TestServer(3333)) {
        await(WS.url("http://localhost:3333/").get).status must equalTo(OK)
      }
    }
  }
}

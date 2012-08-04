package test

import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._

class IntegrationTest extends Specification {
  "Frontend" should {
    "work from within a browser" in {
      running(TestServer(3333), HTMLUNIT) { browser =>
        browser.goTo("http://localhost:3333/")

        // Able to load the front page
        browser.$("h1.name a").first.getText must equalTo("Breakpoint.")
      }
    }
  }
}

package test

import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._

class IntegrationTest extends Specification {
  "Frontend" should {
    "work from within a browser" in {
      running(TestServer(3333), HTMLUNIT) { browser =>
        browser.goTo("http://localhost:3333/")

        // Be able to load the front page
        browser.$("h1.name a").first.getText must equalTo("Breakpoint.")

        // See the registration form
        browser.goTo("http://localhost:3333/user/register")
        browser.$(".headercontent h1").first.getText must equalTo("Register for Breakpoint.")
        browser.pageSource must contain("username")
        browser.pageSource must contain("email")
        browser.pageSource must contain("password")
        browser.pageSource must contain("confirm")
        browser.pageSource must contain("accept")
      }
    }
  }
}

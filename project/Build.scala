import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

    val appName         = "frontend"
    val appVersion      = "1.0-SNAPSHOT"

    val appDependencies = Seq(
      "postgresql" % "postgresql" % "9.1-901.jdbc4",
      "joda-time" % "joda-time" % "2.1",
      "javax.mail" % "mail" % "1.4.5",
      "org.seleniumhq.selenium" % "selenium-firefox-driver" % "2.24.1" % "test"
    )

    val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA).settings(
      // Add your own project settings here      
    )

}

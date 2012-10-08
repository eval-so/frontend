import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

    val appName         = "frontend"
    val appVersion      = "1.0-SNAPSHOT"

    val appDependencies = Seq(
      "postgresql" % "postgresql" % "9.1-901.jdbc4",
      "joda-time" % "joda-time" % "2.1",
      "org.apache.commons" % "commons-email" % "1.2"
      //"com.typesafe" % "play-plugins-mailer_2.9.1" % "2.0.4",
    )

    val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA).settings(
    ).dependsOn(play21auth)

    lazy val play21auth = uri("https://github.com/CodeBlock/play20-auth.git#play21")

}

import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "frontend"
  val appVersion      = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    "com.typesafe" % "play-slick_2.10" % "0.3.0",
    "postgresql" % "postgresql" % "9.1-901.jdbc4",
    "securesocial" %% "securesocial" % "master-SNAPSHOT",
    "org.webjars" % "webjars-play" % "2.1.0",
    "org.webjars" % "jquery" % "1.9.1",
    "org.webjars" % "highlightjs" % "7.3",
    "org.webjars" % "font-awesome" % "3.0.2",
    "org.webjars" % "bootstrap" % "2.3.1",
    jdbc,
    anorm
  )

  val main = play.Project(appName, appVersion, appDependencies).settings(
    resolvers += Resolver.url("sbt-plugin-snapshots", new URL("http://repo.scala-sbt.org/scalasbt/sbt-plugin-snapshots/"))(Resolver.ivyStylePatterns),
    resolvers += Resolver.url("github repo for play-slick", url("http://loicdescotte.github.com/releases/"))(Resolver.ivyStylePatterns)
  )
}

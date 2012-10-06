// Comment to get more information during initialization
logLevel := Level.Warn

resolvers ++= Seq(
  Resolver.file("Local Repository", file("/home/ricky/devel/scala/play-head/repository/local"))(Resolver.ivyStylePatterns),
  "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"
)

// Use the Play sbt plugin for Play projects
addSbtPlugin("play" % "sbt-plugin" % "2.1-SNAPSHOT")

name := "ecount"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
    jdbc,
    anorm,
    cache,
    "postgresql" % "postgresql" % "9.1-901-1.jdbc4",
    "org.mybatis" % "mybatis" % "3.1.1",
  	"org.mybatis" % "mybatis-guice" % "3.3"
)

play.Project.playScalaSettings

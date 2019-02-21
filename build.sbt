import sbt.Resolver.bintrayRepo

organization in ThisBuild := "com.kps.equipes"
scalaVersion in ThisBuild := "2.11.8"

lazy val buildVersion = sys.props.getOrElse("buildVersion", "1.0.0-SNAPSHOT")

val macwire           = "com.softwaremill.macwire" %% "macros" % "2.2.5" % "provided"
val scalaTest         = "org.scalatest" %% "scalatest" % "3.0.1" % Test
val serviceLocatorDns = "com.lightbend" % "lagom13-scala-service-locator-dns_2.11" % "2.2.2"

lazy val `lagom-equipes` = (project in file(".")).aggregate(
  `country-api`, `country-impl`,
  `team-api`, `team-impl`
)

lazy val `country-api` = (project in file("country-api"))
  .settings(
    version := buildVersion,
    libraryDependencies ++= Seq(
      lagomScaladslApi
    )
  )

lazy val `country-impl` = (project in file("country-impl"))
  .enablePlugins(LagomScala)
  .settings(
    version := buildVersion,
    resolvers += bintrayRepo("hajile", "maven"),
    resolvers += bintrayRepo("hseeberger", "maven"),
    libraryDependencies ++= Seq(
      serviceLocatorDns,
      lagomScaladslPersistenceCassandra,
      lagomScaladslTestKit,
      macwire,
      scalaTest
    )
  )
  .settings(BuildTarget.additionalSettings)
  .settings(lagomForkedTestSettings: _*)
  .dependsOn(`country-api`)

lazy val `team-api` = (project in file("team-api"))
  .settings(
    version := buildVersion,
    libraryDependencies ++= Seq(
      lagomScaladslApi
    )
  )

lazy val `team-impl` = (project in file("team-impl"))
  .enablePlugins(LagomScala)
  .settings(
    version := buildVersion,
    resolvers += bintrayRepo("hajile", "maven"),
    resolvers += bintrayRepo("hseeberger", "maven"),
    libraryDependencies ++= Seq(
      serviceLocatorDns,
      lagomScaladslPersistenceCassandra,
      lagomScaladslTestKit,
      macwire,
      scalaTest
    )
  )
  .settings(BuildTarget.additionalSettings)
  .settings(lagomForkedTestSettings: _*)
  .dependsOn(`team-api`)

//lagomCassandraCleanOnStart in ThisBuild := true

// Kafka can be disabled until we need it
lagomKafkaEnabled in ThisBuild := false

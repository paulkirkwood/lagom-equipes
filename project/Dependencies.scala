import sbt._

object Version {
  val constructr = "0.17.0"
  val constructrZooKeeper = "0.4.0"
  val serviceLocatorDns = "2.2.2"
}

object Library {
  val constructr = "de.heikoseeberger"                  %% "constructr"                         % Version.constructr
  val constructrZooKeeper = "com.lightbend.constructr"  %% "constructr-coordination-zookeeper"  % Version.constructrZooKeeper
  val serviceLocatorDns = "com.lightbend"               %% "lagom13-scala-service-locator-dns"  % Version.serviceLocatorDns
}

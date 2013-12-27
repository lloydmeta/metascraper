name := "metascraper"

version := "0.2.4"

scalaVersion := "2.10.2"

crossScalaVersions := Seq("2.10.0", "2.10.1", "2.10.2")

crossVersion := CrossVersion.binary

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "1.9.1" % "test",
  "com.typesafe.akka" %% "akka-testkit" % "2.2.3" % "test",
  "org.codehaus.groovy" % "groovy-all" % "1.8.8" % "test",
  "co.freeside" % "betamax" % "1.1.2" % "test",
  "com.typesafe.akka" %% "akka-actor" % "2.2.3",
  "net.databinder.dispatch" %% "dispatch-core" % "0.11.0",
  "commons-validator" % "commons-validator" % "1.4.0",
  "org.jsoup" % "jsoup" % "1.7.2"
)

publishTo <<= version { v: String =>
  val nexus = "https://oss.sonatype.org/"
  if (v.trim.endsWith("SNAPSHOT"))
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

organization := "com.beachape.metascraper"

publishMavenStyle := true

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

pomExtra := (
  <url>https://github.com/lloydmeta/metascraper</url>
  <licenses>
    <license>
      <name>MIT</name>
      <url>http://opensource.org/licenses/MIT</url>
      <distribution>repo</distribution>
    </license>
  </licenses>
  <scm>
    <url>git@github.com:lloydmeta/metascraper.git</url>
    <connection>scm:git:git@github.com:lloydmeta/metascraper.git</connection>
  </scm>
  <developers>
    <developer>
      <id>lloydmeta</id>
      <name>Lloyd Chan</name>
      <url>http://beachape.com</url>
    </developer>
  </developers>
)
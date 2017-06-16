name := "metascraper"

version := "0.3.6"

scalaVersion := "2.11.11"

crossScalaVersions := Seq("2.10.6", "2.11.11", "2.12.2")

crossVersion := CrossVersion.binary

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.0.1" % Test,
  "com.typesafe.akka" %% "akka-testkit" % theAkkaVersion(scalaVersion.value) % Test,
  "com.typesafe.akka" %% "akka-actor" % theAkkaVersion(scalaVersion.value),
  "net.databinder.dispatch" %% "dispatch-core" % theDispatchVersion(scalaVersion.value),
  "com.ning" % "async-http-client" % "1.9.40",
  "commons-validator" % "commons-validator" % "1.6",
  "org.jsoup" % "jsoup" % "1.10.3"
)

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (version.value.trim.endsWith("SNAPSHOT"))
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

organization := "com.beachape.metascraper"

publishMavenStyle := true

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-Xlint", "-Xlog-free-terms")

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
      <url>https://beachape.com</url>
    </developer>
  </developers>
)

scalafmtOnCompile := true

scalafmtVersion := "1.0.0-RC3"

def theAkkaVersion(scalaVersion: String) =
  CrossVersion.partialVersion(scalaVersion) match {
    case Some((2, scalaMajor)) if scalaMajor >= 11 => "2.5.2"
    case Some((2, scalaMajor)) if scalaMajor == 10 => "2.3.16"
    case _ =>
      throw new IllegalArgumentException(s"Unsupported Scala version $scalaVersion")
  }

def theDispatchVersion(scalaVersion: String) =
  CrossVersion.partialVersion(scalaVersion) match {
    case Some((2, scalaMajor)) if scalaMajor >= 11 => "0.12.2"
    case Some((2, scalaMajor)) if scalaMajor == 10 => "0.11.3"
    case _ =>
      throw new IllegalArgumentException(s"Unsupported Scala version $scalaVersion")
  }

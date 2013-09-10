# Metascraper [![Build Status](https://travis-ci.org/lloydmeta/metascraper.png?branch=master)](https://travis-ci.org/lloydmeta/metascraper)

A Scala library built on Akka actors and [jsoup](http://jsoup.org/) that allows you to retrieve meta data from pages at specified URLs, prioritising [Open Graph tags](http://ogp.me/) over traditional tags if they exist.

Tested against Scala 2.10.x, OpenJDK 6 and 7, and Oracle JDK 7.

## Installation

Add the following to your `build.sbt`

```scala
libraryDependencies += "com.beachape.metascraper" %% "metascraper" % "0.0.7"
```

If the above does not work because it cannot be resolved, its likely because it hasn't been synced to Maven central yet.
In that case, download a SNAPSHOT release of the same version by adding this to `build.sbt`

```
resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

libraryDependencies += "com.beachape.metascraper" %% "metascraper" % "0.0.7-SNAPSHOT"
```

## Example usage
```scala
import akka.actor.ActorSystem
import com.beachape.metascraper.Messages._
import com.beachape.metascraper.ScraperActor
import scala.concurrent.Await
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._

implicit val timeout = Timeout(30 seconds)

implicit val system = ActorSystem("actorSystem")
implicit val dispatcher = system.dispatcher

val scraperActor = system.actorOf(ScraperActor())

for {
  future <- ask(scraperActor, ScrapeUrl("https://bbc.co.uk")).mapTo[Either[FailedToScrapeUrl,ScrapedData]]
} {
  future match {
    case Left(failed) => {
      println("Failed: ")
      println(failed.message)
    }
    case Right(data) => {
      println("Image urls")
      data.imageUrls.foreach(println)
    }
  }
}

/*
 #=>
  Image URLs:
  http://www.bbc.co.uk/img/iphone.png
  http://sa.bbc.co.uk/bbc/bbc/s?name=SET-COUNTER&pal_route=index&ml_name=barlesque&app_type=web&language=en-GB&ml_version=0.16.1&pal_webapp=wwhp&blq_s=3.5&blq_r=3.5&blq_v=default-worldwide
  http://static.bbci.co.uk/frameworks/barlesque/2.51.2/desktop/3.5/img/blq-blocks_grey_alpha.png
  http://static.bbci.co.uk/frameworks/barlesque/2.51.2/desktop/3.5/img/blq-search_grey_alpha.png
  http://news.bbcimg.co.uk/media/images/69612000/jpg/_69612953_69612952.jpg
*/
```

## Example application

[Metascraper-service](https://github.com/lloydmeta/metascraper-service): a RESTful Play2 service that uses this library to scrape data from a requested URL, taking advantage of the tight integration that Play2 has with Akka.

## Licence

The MIT License (MIT)

Copyright (c) 2013 by Lloyd Chan

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
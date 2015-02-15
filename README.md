# Metascraper [![Build Status](https://travis-ci.org/lloydmeta/metascraper.png?branch=master)](https://travis-ci.org/lloydmeta/metascraper) [![Coverage Status](https://coveralls.io/repos/lloydmeta/metascraper/badge.svg?branch=feature%2Fmultiple-schema-extractors)](https://coveralls.io/r/lloydmeta/metascraper?branch=feature%2Fmultiple-schema-extractors)

A non-blocking Scala library built on Akka actors and [jsoup](http://jsoup.org/) that allows you to retrieve meta data from pages at specified URLs, prioritising [Open Graph tags](http://ogp.me/) over traditional tags if they exist.

Tested against Scala 2.10.x, OpenJDK 6 and 7, and Oracle JDK 7.

## Installation

Add the following to your `build.sbt`

```scala
libraryDependencies += "com.beachape.metascraper" %% "metascraper" % "0.2.6"
```

If the above does not work because it cannot be resolved, its likely because it hasn't been synced to Maven central yet.
In that case, download a SNAPSHOT release of the same version by adding this to `build.sbt`

```
resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

libraryDependencies += "com.beachape.metascraper" %% "metascraper" % "0.2.5-SNAPSHOT"
```

## Example usage

### Scraper

```Scala
// You can customise Http here (it is simply Dispatcher's Http client)
val scraper = new Scraper(Http, Seq("http", "https"))

val fData: Future[ScrapedData] = scraper.fetch(ScrapeUrl("https://google.com"))

```

### ScraperActor

If you prefer to use a Scraper actor (e.g. to distribute load to remote systems), use ScraperActor:

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
  future <- ask(scraperActor, ScrapeUrl("https://bbc.co.uk")).mapTo[Either[Throwable,ScrapedData]]
} {
  future match {
    case Left(throwable) => {
      println("Failed: ")
      println(throwable.getMessage)
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

### Custom scraping schema

You can customise scraping by implementing `SchemaFactory` and `Schema`.

- A `SchemaFactory` needs to implement a method that takes a `Response` from AsyncHttpClient and returns a list of `Schema`
- A `Schema` is what is used to extract data from the response

This allows you to freely customise the way this lib extracts data from HTTP endpoints, for example as listed on [Schema.org](https://schema.org/).

That said, if all you want to do is implement a custom HTML data scraper, you just need to implement `HtmlSchema`
and pass it along to a `ScrapeUrl` message inside `HtmlSchemas`, as follows:

```scala

case class CustomHtmlSchema(doc: Document) extends HtmlSchema {
 // Implementations
}

val scraper = new Scraper(httpClient = Http, urlSchemas = Seq("http", "https"))
val scrapeMessage = ScrapeUrl("https://google.com", schemaFactories = Seq(HtmlSchemas(OpenGraph, CustomHtmlSchema, NormalPage)) )
val fData = scraper.fetch(scrapeMessage)
```

In the above case, `HtmlSchemas` is a `SchemaFactory` that creates a Jsoup `Document` and gives it to the `Schema`s that
it generates. That way, only one String to Document parse needs to happen.


## Example applications

1. [Metascraper-service](https://github.com/lloydmeta/metascraper-service): a RESTful Play2 service that uses this library to scrape data from a requested URL, taking advantage of the tight integration that Play2 has with Akka.

2. [Metascraper-scalatra](https://github.com/lloydmeta/metascraper-scalatra): same as 1. but built on Scalatra, which is more light-weight and Servlet 3.0 compliant.

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

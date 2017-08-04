# Metascraper [![Build Status](https://travis-ci.org/lloydmeta/metascraper.svg?branch=master)](https://travis-ci.org/lloydmeta/metascraper) [![Coverage Status](https://coveralls.io/repos/lloydmeta/metascraper/badge.svg?branch=master)](https://coveralls.io/r/lloydmeta/metascraper?branch=master) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.beachape.metascraper/metascraper_2.11/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.beachape.metascraper/metascraper_2.11)

A non-blocking Scala library that allows you to retrieve meta data from various URLs.

Out of the box, supports HTML content (works with OpenGraph as well as normal HTML meta/image tags), but can be easily extended to
support any kind of content (just implement `Schema` and `SchemaFactory`) so that you can work with any URL that returns an Array of Bytes.

Tested against Scala 2.10.x, 2.11.x, and 2.12.x.

## Installation

Add the following to your `build.sbt`

```scala
libraryDependencies += "com.beachape.metascraper" %% "metascraper" % version
```

If the above does not work because it cannot be resolved, its likely because it hasn't been synced to Maven central yet.
In that case, download a SNAPSHOT release of the same version by adding this to `build.sbt`

```scala
resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

libraryDependencies += "com.beachape.metascraper" %% "metascraper" % s"$version-SNAPSHOT"
```

## Example usage

### Scraper

```Scala
// You can customise Http here (it is simply Dispatcher's Http client)
val scraper = new Scraper(Http, Seq("http", "https"))

val fData: Future[ScrapedData] = scraper.fetch(ScrapeUrl("https://google.com"))

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

Copyright (c) 2015 by Lloyd Chan

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

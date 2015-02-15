package com.beachape.metascraper

import com.beachape.metascraper.Messages.ScrapeUrl
import com.beachape.metascraper.extractors.{ DocsSupport, Schema }

import dispatch.Http
import org.jsoup.nodes.Document
import org.scalatest._
import org.scalatest.concurrent.{ IntegrationPatience, PatienceConfiguration, ScalaFutures }

/**
 * Created by Lloyd on 2/15/15.
 */
class ScraperSpec
    extends FunSpec
    with Matchers
    with ScalaFutures
    with PatienceConfiguration
    with IntegrationPatience
    with DocsSupport {

  import scala.concurrent.ExecutionContext.Implicits.global

  val subject = new Scraper(Http, Seq("http", "https"))

  describe("#fetch") {

    it("should return a failed future if the url is invalide") {
      whenReady(subject.fetch(ScrapeUrl("arg")).failed) { _ shouldBe an[IllegalArgumentException] }
    }

    it("should return a simple ScrapedData if the url is an image url") {
      whenReady(subject.fetch(ScrapeUrl("http://google.com/image.jpg"))) { r =>
        r.mainImageUrl shouldBe "http://google.com/image.jpg"
      }
    }

  }

  describe("#extractData") {

    val scraper1 = new Schema {

      def extractUrl(doc: Document): Option[String] = Some("firstUrl")

      def extractTitle(doc: Document): Option[String] = Some("firstTitle")

      def extractImages(doc: Document): Seq[String] = Seq("first.jpeg")

      def extractDescription(doc: Document): Option[String] = Option("firstDescription")

      def extractMainImage(doc: Document): Option[String] = Some("firstMain.jpeg")

    }

    val scraper2 = new Schema {

      def extractUrl(doc: Document): Option[String] = Some("secondUrl")

      def extractTitle(doc: Document): Option[String] = Some("secondTitle")

      def extractImages(doc: Document): Seq[String] = Seq("second.jpeg")

      def extractDescription(doc: Document): Option[String] = Option("secondDescription")

      def extractMainImage(doc: Document): Option[String] = Some("second.jpeg")

    }

    it("should prioritise Schema scrapers at the beginning of the list") {
      val data = subject.extractData(withOgTagsSource.mkString, "hello", Seq(scraper1, scraper2), 5)
      data.url shouldBe "firstUrl"
      data.title shouldBe "firstTitle"
      data.description shouldBe "firstDescription"
      data.imageUrls shouldBe Seq("first.jpeg", "second.jpeg")
      data.mainImageUrl shouldBe "firstMain.jpeg"

      // check that images are prioritised
      val data2 = subject.extractData(withOgTagsSource.mkString, "hello", Seq(scraper1, scraper2), 1)
      data2.imageUrls shouldBe Seq("first.jpeg")
    }
  }

}

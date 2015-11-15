package com.beachape.metascraper

import java.io.InputStream
import java.nio.ByteBuffer
import java.util

import com.beachape.metascraper.Messages.ScrapeUrl
import com.beachape.metascraper.extractors.{ SchemaFactory, DocsSupport, Schema }
import com.ning.http.client.AsyncHandler.STATE
import com.ning.http.client.cookie.Cookie
import com.ning.http.client.{ HttpResponseHeaders, FluentCaseInsensitiveStringsMap, Response }

import dispatch.{ Uri, Http }
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

  describe("#handleSupportedTypes handler") {

    val htmlHandler = subject.handleSupportedTypes(Seq("text/html"))
    val htmlRespHeadersMap = {
      val m = new FluentCaseInsensitiveStringsMap()
      m.add("Content-Type", "text/html")
    }
    val nonHtmlRespHeadersMap = {
      val m = new FluentCaseInsensitiveStringsMap()
      m.add("Content-Type", "application/json")
    }

    it("should create a handler that continues on supported header types") {
      val r = htmlHandler.onHeadersReceived(new HttpResponseHeaders() {
        def getHeaders: FluentCaseInsensitiveStringsMap = htmlRespHeadersMap
      })
      r shouldBe STATE.CONTINUE
    }

    it("should create a handler that aborts on unsupported header types") {
      val r = htmlHandler.onHeadersReceived(new HttpResponseHeaders() {
        def getHeaders: FluentCaseInsensitiveStringsMap = nonHtmlRespHeadersMap
      })
      r shouldBe STATE.ABORT
    }

  }

  describe("#extractData") {

    val scraper1 = new Schema {

      def extractUrl: Option[String] = Some("firstUrl")

      def extractTitle: Option[String] = Some("firstTitle")

      def extractImages: Seq[String] = Seq("first.jpeg")

      def extractDescription: Option[String] = Option("firstDescription")

      def extractMainImage: Option[String] = Some("firstMain.jpeg")

    }

    val scraper2 = new Schema {

      def extractUrl: Option[String] = Some("secondUrl")

      def extractTitle: Option[String] = Some("secondTitle")

      def extractImages: Seq[String] = Seq("second.jpeg")

      def extractDescription: Option[String] = Option("secondDescription")

      def extractMainImage: Option[String] = Some("second.jpeg")

    }

    val factory = new SchemaFactory {
      val contentTypes = Seq("text/html")
      def apply(s: Response): Seq[Schema] = Seq(scraper1, scraper2)
    }

    def resp(s: String) = new Response {

      def getResponseBodyExcerpt(maxLength: Int, charset: String): String = s

      def getResponseBodyExcerpt(maxLength: Int): String = s

      def getResponseBodyAsByteBuffer: ByteBuffer = ???

      def getStatusCode: Int = 200

      def getResponseBodyAsBytes: Array[Byte] = ???

      def getResponseBodyAsStream: InputStream = ???

      def isRedirected: Boolean = ???

      def getCookies: util.List[Cookie] = ???

      def hasResponseBody: Boolean = ???

      def getStatusText: String = "200"

      def getHeaders(name: String): util.List[String] = ???

      def getHeaders: FluentCaseInsensitiveStringsMap = ???

      def hasResponseHeaders: Boolean = ???

      def getResponseBody(charset: String): String = s

      def getResponseBody: String = s

      def getContentType: String = ???

      def hasResponseStatus: Boolean = ???

      def getUri = ???

      def getHeader(name: String): String = ???
    }

    it("should prioritise Schema scrapers at the beginning of the list") {
      val data = subject.extractData(resp(withOgTagsSource.mkString), "hello", Seq(factory), 5)
      data.url shouldBe "firstUrl"
      data.title shouldBe "firstTitle"
      data.description shouldBe "firstDescription"
      data.imageUrls shouldBe Seq("first.jpeg", "second.jpeg")
      data.mainImageUrl shouldBe "firstMain.jpeg"

      // check that images are prioritised
      val data2 = subject.extractData(resp(withOgTagsSource.mkString), "hello", Seq(factory), 1)
      data2.imageUrls shouldBe Seq("first.jpeg")
    }
  }

}

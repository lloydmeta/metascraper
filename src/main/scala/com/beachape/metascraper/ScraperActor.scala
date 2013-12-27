package com.beachape.metascraper

import com.beachape.metascraper.Messages._
import akka.actor.{ActorLogging, ActorRef, Actor, Props}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import scala.collection.JavaConverters._
import scala.concurrent.Future
import dispatch._
import org.apache.commons.validator.routines.UrlValidator
import java.util.concurrent.{ExecutorService, Executors}
import com.ning.http.client.{AsyncHttpClientConfig, AsyncHttpClient}


/**
 * Companion object for instantiating ScaperActors
 */
object ScraperActor {

  /**
   * Factory method for the params required to instantiate a MonitorActor
   *
   * @param httpExecutorThreads Int number of threads to use for this actor's async HTTP executor service
   * @param maxConnectionsPerHost Int max connections at a time per host
   * @param connectionTimeoutInMs Int time in milliseconds before timing out when trying to make a connection to a host
   * @param requestTimeoutInMs Int time in milliseconds before timing out when waiting for a request to complete after connection
   * @return Props for instantiating a ScaperActor
   */
  def apply(httpExecutorThreads: Int = 10,
            maxConnectionsPerHost: Int = 30,
            connectionTimeoutInMs: Int = 10000,
            requestTimeoutInMs: Int = 15000) =
    Props(
      classOf[ScraperActor],
      httpExecutorThreads,
      maxConnectionsPerHost,
      connectionTimeoutInMs,
      requestTimeoutInMs)
}

/**
 * Actor for scraping metadata from websites at URLs
 *
 * Should be instantiated with Props provided via companion object factory
 * method
 */
class ScraperActor(
                    httpExecutorThreads: Int = 10,
                    maxConnectionsPerHost: Int = 30,
                    connectionTimeoutInMs: Int = 10000,
                    requestTimeoutInMs: Int = 15000)
  extends Actor with ActorLogging {

  import context.dispatcher

  lazy val compressionEnabled = true

  // Validator
  lazy val validSchemas = Seq("http", "https")
  lazy val urlValidator = new UrlValidator(validSchemas.toArray)

  // Http client
  lazy val followRedirects = true
  lazy val connectionPooling = true
  lazy val httpExecutorService: ExecutorService = Executors.newFixedThreadPool(httpExecutorThreads)
  lazy val config = new AsyncHttpClientConfig.Builder()
    .setExecutorService(httpExecutorService)
    .setMaximumConnectionsPerHost(maxConnectionsPerHost)
    .setAllowPoolingConnection(connectionPooling)
    .setAllowSslConnectionPool(connectionPooling)
    .setConnectionTimeoutInMs(connectionTimeoutInMs)
    .setRequestTimeoutInMs(requestTimeoutInMs)
    .setCompressionEnabled(compressionEnabled)
    .setFollowRedirects(followRedirects).build
  lazy val asyncHttpClient = new AsyncHttpClient(config)
  lazy val httpClient = new Http(asyncHttpClient)

  override def postStop() {
    httpClient.shutdown()
    httpExecutorService.shutdown()
  }

  def receive = {

    case message: ScrapeUrl => {
      if (urlValidator.isValid(message.url)) {
        val zender = sender
        for (futureEither <- getStringFromUrl(message)) {
          futureEither match {
            case Right(responseString) => self ! ScrapeString(responseString, message.url, zender)
            case Left(throwable) => logAndForwardErrorAsLeft(throwable, zender)
          }
        }
      } else {
        sender ! Left(new Throwable(s"Invalid url ${message.url}"))
      }
    }

    case message: ScrapeString => {
      try {
        val document = Jsoup.parse(message.string, message.url)
        val extractedData = extractScrapedData(document, message.url)
        message.zender ! Right(extractedData)
      } catch {
        case e: Exception => logAndForwardErrorAsLeft(e, message.zender)
      }
    }

    case _ => log.error("Scraper Actor received an unexpected message :( !")
  }

  /**
   * Returns a Future[Either[Throwable, String]] where the string is the response
   * string
   *
   * Does an HTTP Follows redirects in location headers.
   *
   * @param message ScrapeUrl message
   * @return document Future[Document]
   */
  def getStringFromUrl(message: ScrapeUrl): Future[Either[Throwable, String]] = {
    val requestHeaders = Map(
      "User-Agent" -> Seq(message.userAgent),
      "Accept-Language" -> Seq(message.acceptLanguageCode))
    val request = url(message.url).setHeaders(requestHeaders)
    val resp = httpClient(request OK as.String).either
    for (throwable <- resp.left) yield throwable
  }

  /**
   * Returns a ScrapedData object filled out using data extracted
   * from a JSoup document
   *
   * Prioritises Open Graph tags https://developers.facebook.com/docs/opengraph/
   * over conventional tags like <title>
   *
   * @param doc Document as parsed by JSoup
   * @return ScrapedData
   */
  def extractScrapedData(doc: Document, accessedUrl: String): ScrapedData = {
    ScrapedData(
      extractUrl(doc, accessedUrl),
      extractTitle(doc),
      extractDescription(doc),
      extractMainImage(doc),
      extractImages(doc)
    )
  }

  /**
   * Returns the url of a Jsoup document
   *
   * Prioritises <meta property="og:url" .. > tag over the url used
   * to access this document
   *
   * @param doc Document as parsed by JSoup
   * @return String url
   */
  def extractUrl(doc: Document, accessedUrl: String): String = {
    if (!doc.select("meta[property=og:url]").attr("content").isEmpty) {
      doc.select("meta[property=og:url]").attr("content")
    } else {
      accessedUrl
    }
  }

  /**
   * Returns the title of a Jsoup document
   *
   * Prioritises <meta property="og:title" .. > tag over <title> tag
   *
   * @param doc Document as parsed by JSoup
   * @return String title
   */
  def extractTitle(doc: Document): String = {
    if (!doc.select("meta[property=og:title]").attr("content").isEmpty) {
      doc.select("meta[property=og:title]").attr("content")
    } else {
      doc.title()
    }
  }

  /**
   * Returns the description of a Jsoup document
   *
   * Prioritises <meta property="og:description" .. > tag over <meta name="description" .. > tag
   *
   * @param doc Document as parsed by JSoup
   * @return String description
   */
  def extractDescription(doc: Document): String = {
    if (!doc.select("meta[property=og:description]").attr("content").isEmpty) {
      doc.select("meta[property=og:description]").attr("content")
    } else if (!doc.select("meta[name=description]").attr("content").isEmpty) {
      doc.select("meta[name=description]").attr("content")
    } else {
      val firstParagraph = doc.select("p").text
      if (firstParagraph.length > 300)
        s"${firstParagraph.take(300)}..."
      else
        firstParagraph
    }
  }

  /**
   * Returns a best guess for the url of the main Image of a Jsoup document
   *
   * Prioritises the first <meta property="og:image" .. > tag over the
   * first <img ..> tag
   *
   * @param doc Document as parsed by JSoup
   * @return String url of the main image
   */
  def extractMainImage(doc: Document): String = {
    extractImages(doc) match {
      case images: Seq[String] if !images.isEmpty => images.head
      case _ => ""
    }
  }

  /**
   * Returns a best guess for the best images of a Jsoup document
   *
   * * Prioritises <meta property="og:image" .. > tags over <img .. > tags
   *
   * @param doc Document as parsed by Jsoup
   * @param takeFirst Number of elements to take, defaults to 5
   * @return Seq[String] collection of image urls
   */
  def extractImages(doc: Document, takeFirst: Int = 5): Seq[String] = {
    if (!doc.select("meta[property=og:image]").attr("content").isEmpty) {
      val ogImageSrcs = doc.select("meta[property=og:image]").iterator().asScala.take(takeFirst).toSeq.map(_.attr("abs:content"))
      if (ogImageSrcs.size < takeFirst)
        ogImageSrcs ++ doc.select("img[src]").iterator().asScala.take(takeFirst - ogImageSrcs.size).toSeq.map(_.attr("abs:src"))
      else
        ogImageSrcs
    } else if (!doc.select("link[rel=image_src]").attr("href").isEmpty) {
      val imageRelSrc = doc.select("link[rel=image_src]").attr("abs:href")
      imageRelSrc +: doc.select("img[src]").iterator().asScala.take(takeFirst - 1).toSeq.map(_.attr("abs:src"))
    }
    else {
      doc.select("img[src]").iterator().asScala.take(takeFirst).toSeq.map(_.attr("abs:src"))
    }
  }

  /**
   * Helper function that logs an error and forwards the throwable
   *
   * @param throwable Throwable
   * @param sendToRef Actor to send the message to
   */
  def logAndForwardErrorAsLeft(throwable: Throwable, sendToRef: ActorRef) {
    log.error(throwable.getMessage)
    sendToRef ! Left(throwable)
  }

}

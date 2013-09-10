package com.beachape.metascraper

import com.beachape.metascraper.Messages._
import akka.actor.{Actor, Props}
import com.typesafe.scalalogging.slf4j.Logging
import java.io.IOException
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import scala.collection.JavaConverters._
import scala.annotation.tailrec

/**
 * Companion object for instantiating ScaperActors
 */
object ScraperActor {

  /**
   * Factory method for the params required to instantiate a MonitorActor
   *
   * @return Props for instantiating a ScaperActor
   */
  def apply() = Props[ScraperActor]
}

/**
 * Actor for scraping metadata from websites at URLs
 *
 * Should be instantiated with Props provided via companion object factory
 * method
 */
class ScraperActor extends Actor with Logging {

  def receive = {

    case message: ScrapeUrl => {
      try {
        val document = getDocument(message)
        val scrapedData = extractScrapedData(document, message.url)
        logger.info(s"Scraped data: ${scrapedData}")
        sender ! Right(scrapedData)
      } catch {
        case ioe: IOException => {
          val errorMessage = s"Failed to retrieve page at url: ${message.url}"
          logger.error(errorMessage)
          sender ! Left(FailedToScrapeUrl(errorMessage))
        }
        case e: Exception => {
          val errorMessage = e.getMessage
          logger.error(errorMessage)
          sender ! Left(FailedToScrapeUrl(errorMessage))
        }
      }
    }

    case _ => logger.error("Scraper Actor received an unexpected message :( !")
  }

  /**
   * Returns a Jsoup Document
   *
   * Follows redirects in location headers and in meta tags until
   * we get to the final document
   *
   * @param message ScrapeUrl message
   * @return document Jsoup document
   */
  @tailrec final def getDocument(message: ScrapeUrl): Document = {
    val document = Jsoup.connect(message.url).
      followRedirects(true).
      userAgent(message.userAgent).
      header("Accept-Language", message.acceptLanguageCode).
      get()
    val metaRefreshTags = document.select("meta[http-equiv=REFRESH]")
    if (metaRefreshTags.size > 0 && !metaRefreshTags.attr("content").isEmpty)
      getDocument(ScrapeUrl(metaRefreshTags.attr("abs:content").split("=")(1), userAgent = message.userAgent, acceptLanguageCode = message.acceptLanguageCode))
    else
      document
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
    if (!doc.select("meta[property=og:url]").attr("content").isEmpty()) {
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
    if (!doc.select("meta[property=og:title]").attr("content").isEmpty()) {
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
    if (!doc.select("meta[property=og:image]").attr("content").isEmpty()) {
      val ogImageSrcs = doc.select("meta[property=og:image]").iterator().asScala.take(takeFirst).toSeq.map(_.attr("abs:content"))
      if (ogImageSrcs.size < takeFirst)
        ogImageSrcs ++ doc.select("img[src]").iterator().asScala.take(takeFirst - ogImageSrcs.size).toSeq.map(_.attr("abs:src"))
      else
        ogImageSrcs
    } else {
      doc.select("img[src]").iterator().asScala.take(takeFirst).toSeq.map(_.attr("abs:src"))
    }
  }

}

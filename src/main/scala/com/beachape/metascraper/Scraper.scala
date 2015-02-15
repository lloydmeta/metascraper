package com.beachape.metascraper

import com.beachape.metascraper.Messages.{ ScrapedData, ScrapeUrl }
import com.beachape.metascraper.extractors.{ SchemaFactory, Schema }
import com.ning.http.client.Response
import dispatch._
import org.apache.commons.validator.routines.UrlValidator
import org.jsoup.Jsoup
import StringOps._

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Created by Lloyd on 2/15/15.
 */
class Scraper(httpClient: Http, urlSchemas: Seq[String])(implicit ec: ExecutionContext) {

  private val urlValidator = new UrlValidator(urlSchemas.toArray)

  /**
   * Returns [[ScrapedData]]
   *
   * - If the url is invalide, returns an illegal argument exception
   * - If the url is for an image, returns immediately with links to the image
   *
   * @param message ScrapeUrl message
   * @return document Future[Document]
   */
  def fetch(message: ScrapeUrl): Future[ScrapedData] = {
    val messageUrl = message.url
    if (!urlValidator.isValid(messageUrl))
      Future.failed(new IllegalArgumentException(s"Invalid url ${message.url}"))
    else if (messageUrl.hasImageExtension) {
      Future.successful(ScrapedData(messageUrl, messageUrl, messageUrl, messageUrl, Seq(messageUrl)))
    } else {
      val requestHeaders = Map(
        "User-Agent" -> Seq(message.userAgent),
        "Accept-Language" -> Seq(message.acceptLanguageCode))
      val request = url(messageUrl).setHeaders(requestHeaders)
      val resp = httpClient(request)
      resp map (s => extractData(s, messageUrl, message.schemaFactories, message.numberOfImages))
    }
  }

  /**
   * Returns [[ScrapedData]] based on the Schemas requested
   *
   * The list of [[Schema]] are processed from left to right, meaning the ones to the beginning of the
   * list get precedence over those towards the end
   */
  def extractData(resp: Response, url: String, schemaFactories: Seq[SchemaFactory], numberOfImages: Int): ScrapedData = {
    val schemas = schemaFactories.toStream.flatMap(_.apply(resp)) // Stream in case we have expensive factories
    val maybeUrl = schemas.foldLeft(None: Option[String]) { (acc, schema) => acc orElse schema.extractUrl }
    val maybeTitle = schemas.foldLeft(None: Option[String]) { (acc, schema) => acc orElse schema.extractTitle }
    val maybeDescription = schemas.foldLeft(None: Option[String]) { (acc, schema) => acc orElse schema.extractDescription }
    val imageUrls = schemas.foldLeft(Seq.empty[String]) { (acc, schema) => acc ++ schema.extractImages }
    val maybeMainImg = schemas.foldLeft(None: Option[String]) { (acc, schema) => acc orElse schema.extractMainImage }
    ScrapedData(
      url = maybeUrl.getOrElse(url),
      title = maybeTitle.getOrElse(""),
      description = maybeDescription.getOrElse(""),
      imageUrls = imageUrls.take(numberOfImages),
      mainImageUrl = maybeMainImg.getOrElse("")
    )
  }

}
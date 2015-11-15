package com.beachape.metascraper

import com.beachape.metascraper.Messages.{ ScrapedData, ScrapeUrl }
import com.beachape.metascraper.extractors.{ SchemaFactory, Schema }
import com.ning.http.client.Response
import dispatch._
import org.apache.commons.validator.routines.UrlValidator
import StringOps._

import scala.concurrent.{ ExecutionContext, Future }
import scala.util._

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
        "Accept-Language" -> Seq(message.acceptLanguageCode)
      )
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
    if (resp.getStatusCode / 100 == 2) {
      val schemas = schemaFactories.toStream.flatMap(f => Try(f.apply(resp)).getOrElse(Nil)) // Stream in case we have expensive factories
      val maybeUrl = schemas.flatMap(s => Try(s.extractUrl).toOption).find(_.isDefined).getOrElse(None)
      val maybeTitle = schemas.flatMap(s => Try(s.extractTitle).toOption).find(_.isDefined).getOrElse(None)
      val maybeDescription = schemas.flatMap(s => Try(s.extractDescription).toOption).find(_.isDefined).getOrElse(None)
      val imageUrls = schemas.foldLeft(Stream.empty[String]) { (acc, schema) => acc ++ Try(schema.extractImages).getOrElse(Nil) }
      val maybeMainImg = schemas.flatMap(s => Try(s.extractMainImage).toOption).find(_.isDefined).getOrElse(None)
      ScrapedData(
        url = maybeUrl.getOrElse(url),
        title = maybeTitle.getOrElse(""),
        description = maybeDescription.getOrElse(""),
        imageUrls = imageUrls.take(numberOfImages),
        mainImageUrl = maybeMainImg.getOrElse("")
      )
    } else {
      throw StatusCode(resp.getStatusCode)
    }
  }

}
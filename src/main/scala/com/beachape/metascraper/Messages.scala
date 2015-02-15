package com.beachape.metascraper

import com.beachape.metascraper.extractors.html.{ HtmlSchemas, OpenGraph, NormalPage }
import com.beachape.metascraper.extractors.{ SchemaFactory, Schema }

/**
 * Message case classes for actor
 */
object Messages {
  type Url = String

  /**
   * ScrapeUrl message
   *
   * @param url Url to scrape
   * @param acceptLanguageCode language to request for the page, defaults to 'en'
   * @param userAgent user agent to request as, defaults to 'Metascraper'
   * @param numberOfImages number of images links to return, defaults to 6
   * @param schemas Schemas to use for scraping, defaults to Seq(HtmlSchemas(OpenGraph, and NormalPage)),
   *                The schemas to the left of the list take precednece over those on the right
   */
  sealed case class ScrapeUrl(url: Url,
    acceptLanguageCode: String = "en",
    userAgent: String = "Metascraper",
    numberOfImages: Int = 5,
    schemas: Seq[SchemaFactory] = Seq(HtmlSchemas(OpenGraph, NormalPage)))

  sealed case class ScrapedData(url: Url,
    title: String,
    description: String,
    mainImageUrl: Url,
    imageUrls: Seq[Url])
}
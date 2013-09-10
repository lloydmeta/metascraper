package com.beachape.metascraper

/**
 * Message case classes for actor
 */
object Messages {
  type Url = String
  sealed case class ScrapeUrl(
                               url: Url,
                               acceptLanguageCode: String = "en",
                               userAgent: String = "Metascraper")
  sealed case class ScrapedData(
                                 url: Url,
                                 title: String,
                                 description: String,
                                 mainImageUrl: Url,
                                 imageUrls: Seq[Url])
  sealed case class FailedToScrapeUrl(message: String)
}
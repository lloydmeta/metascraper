package com.beachape.metascraper

/**
 * Message case classes for actor
 */
object Messages {
  type Url = String
  sealed case class ScrapeUrl(
                               url: Url,
                               acceptLanguageCode: String = "en",
                               userAgent: String = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/29.0.1547.65 Safari/537.36")
  sealed case class ScrapedData(
                                 url: Url,
                                 title: String,
                                 description: String,
                                 mainImageUrl: Url,
                                 imageUrls: Seq[Url])
  sealed case class FailedToScrapeUrl(message: String)
}
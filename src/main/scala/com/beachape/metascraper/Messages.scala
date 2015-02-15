package com.beachape.metascraper

import com.beachape.metascraper.extractors.{NormalPage, OpenGraph, Schema}

/**
 * Message case classes for actor
 */
object Messages {
  type Url = String

  sealed case class ScrapeUrl(url: Url,
                              acceptLanguageCode: String = "en",
                              userAgent: String = "Metascraper",
                              numberOfImages: Int = 5,
                              schemas: Seq[Schema] = Seq(OpenGraph, NormalPage))

  sealed case class ScrapedData(url: Url,
                                title: String,
                                description: String,
                                mainImageUrl: Url,
                                imageUrls: Seq[Url])
}
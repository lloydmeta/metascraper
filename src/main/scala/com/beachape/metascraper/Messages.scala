package com.beachape.metascraper

import org.jsoup.nodes.Document
import akka.actor.ActorRef

/**
 * Message case classes for actor
 */
object Messages {
  type Url = String
  sealed case class ScrapeUrl(
                               url: Url,
                               acceptLanguageCode: String = "en",
                               userAgent: String = "Metascraper")
  sealed case class ScrapeDocument(document: Document, url: Url, zender: ActorRef)
  sealed case class ScrapeString(string: String, url: Url, zender: ActorRef)
  sealed case class ScrapedData(
                                 url: Url,
                                 title: String,
                                 description: String,
                                 mainImageUrl: Url,
                                 imageUrls: Seq[Url])
}
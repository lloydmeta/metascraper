package com.beachape.metascraper.extractors

import org.jsoup.nodes.Document

import scala.util.Try
import scala.collection.JavaConverters._

/**
 * Created by Lloyd on 2/15/15.
 */
object OpenGraph extends Schema {

  def extractUrl(doc: Document): Option[String] = nonEmptyContent(doc, "meta[property=og:url]")

  def extractTitle(doc: Document): Option[String] = nonEmptyContent(doc, "meta[property=og:title]")

  def extractImages(doc: Document): Seq[String] = {
    Try {
      doc.select("meta[property=og:image]").iterator().asScala.toSeq.map(_.attr("abs:content"))
    }.getOrElse(Nil)
  }

  def extractDescription(doc: Document): Option[String] = nonEmptyContent(doc, "meta[property=og:description]")

  def extractMainImage(doc: Document): Option[String] = extractImages(doc).headOption
}
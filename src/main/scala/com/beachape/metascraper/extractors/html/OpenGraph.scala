package com.beachape.metascraper.extractors.html

import org.jsoup.nodes.Document

import scala.collection.JavaConverters._

/**
 * Created by Lloyd on 2/15/15.
 */
case class OpenGraph(doc: Document) extends HtmlSchema {

  def extractUrl: Option[String] = nonEmptyContent(doc, "meta[property=og:url]")

  def extractTitle: Option[String] = nonEmptyContent(doc, "meta[property=og:title]")

  lazy val extractImages: Seq[String] = {
    doc.select("meta[property=og:image]").iterator().asScala.toSeq.map(_.attr("abs:content"))
  }

  def extractDescription: Option[String] = nonEmptyContent(doc, "meta[property=og:description]")

  def extractMainImage: Option[String] = extractImages.headOption
}
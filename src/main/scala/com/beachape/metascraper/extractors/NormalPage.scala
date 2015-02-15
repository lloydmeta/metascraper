package com.beachape.metascraper.extractors

import org.jsoup.nodes.Document

import scala.util.Try
import scala.collection.JavaConverters._

/**
 * Created by Lloyd on 2/15/15.
 */

/**
 * Schema for just normal HTML, whatever that means
 */
object NormalPage extends Schema {

  def extractUrl(doc: Document): Option[String] = Try(doc.baseUri()).toOption.filter(_.nonEmpty)

  def extractTitle(doc: Document): Option[String] = Try(doc.title()).toOption.filter(_.nonEmpty)

  def extractImages(doc: Document): Seq[String] = Try {
    val imgSrc = doc.select("link[rel=image_src]").attr("abs:href")
    if (imgSrc.nonEmpty) {
      imgSrc +: doc.select("img[src]").iterator().asScala.toSeq.map(_.attr("abs:src"))
    } else {
      doc.select("img[src]").iterator().asScala.toSeq.map(_.attr("abs:src"))
    }
  }.getOrElse(Nil)

  def extractDescription(doc: Document): Option[String] = Try {
    val metaDesc = doc.select("meta[name=description]").attr("content")
    if (metaDesc.nonEmpty) {
      metaDesc
    } else {
      val firstParagraph = doc.select("p").text
      if (firstParagraph.length > 300)
        s"${firstParagraph.take(300)}..."
      else
        firstParagraph
    }
  }.toOption.filter(_.nonEmpty)

  def extractMainImage(doc: Document): Option[String] = extractImages(doc).headOption
}
package com.beachape.metascraper.extractors.html

import org.jsoup.nodes.Document

import scala.collection.JavaConverters._
import scala.util.Try

/**
 * Created by Lloyd on 2/15/15.
 */

/**
 * Schema for just normal HTML, whatever that means
 */
case class NormalPage(doc: Document) extends HtmlSchema {

  def extractUrl: Option[String] = Try(doc.baseUri()).toOption.filter(_.nonEmpty)

  def extractTitle: Option[String] = Try(doc.title()).toOption.filter(_.nonEmpty)

  lazy val extractImages: Seq[String] = Try {
    val imgSrc = doc.select("link[rel=image_src]").attr("abs:href")
    if (imgSrc.nonEmpty)
      imgSrc +: doc.select("img[src]").iterator().asScala.toSeq.map(_.attr("abs:src"))
    else
      doc.select("img[src]").iterator().asScala.toSeq.map(_.attr("abs:src"))
  }.getOrElse(Nil)

  def extractDescription: Option[String] = Try {
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

  def extractMainImage: Option[String] = extractImages.headOption
}
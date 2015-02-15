package com.beachape.metascraper.extractors.html

import com.beachape.metascraper.extractors.{ SchemaFactory, Schema }
import com.ning.http.client.Response
import dispatch.as.String
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

/**
 * Created by Lloyd on 2/15/15.
 */
trait HtmlSchema extends Schema {

  def doc: Document

  /**
   * Gets the non-empty content of a Document element.
   *
   * Returns None if it is empty
   */
  protected def nonEmptyContent(doc: Document, selector: String): Option[String] = Option {
    doc.select(selector).attr("content")
  }.filter(_.nonEmpty)

}

case class HtmlSchemas(schemas: (Document => HtmlSchema)*) extends SchemaFactory {

  def apply(resp: Response): Seq[HtmlSchema] = {
    val doc = Jsoup.parse(String(resp), resp.getUri.toString)
    schemas.map(_.apply(doc))
  }

}
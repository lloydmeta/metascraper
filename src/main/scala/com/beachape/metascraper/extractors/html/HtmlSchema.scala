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

object HtmlSchemas {

  private val ContentType = "text/html"

  private def supportedContentType(response: Response): Boolean = {
    Option(response.getContentType).exists(_.contains(ContentType))
  }
}

case class HtmlSchemas(schemas: (Document => HtmlSchema)*) extends SchemaFactory {

  def apply(resp: Response): Seq[HtmlSchema] = {
    if (HtmlSchemas.supportedContentType(resp)) {
      val doc = Jsoup.parse(String(resp), resp.getUri.toString)
      schemas.map(_.apply(doc))
    } else {
      Nil
    }
  }

}
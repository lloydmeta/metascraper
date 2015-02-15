package com.beachape.metascraper.extractors.html

import com.beachape.metascraper.extractors.{ SchemaFactory, Schema }
import com.ning.http.client.Response
import dispatch.StatusCode
import dispatch.as.String
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

import scala.util.Try

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
  protected def nonEmptyContent(doc: Document, selector: String): Option[String] = Try {
    doc.select(selector).attr("content")
  }.toOption.filter(_.nonEmpty)

}

case class HtmlSchemas(schemas: (Document => HtmlSchema)*) extends SchemaFactory {

  def apply(resp: Response): Seq[HtmlSchema] = {
    if (resp.getStatusCode / 100 == 2) {
      val doc = Jsoup.parse(String(resp), resp.getUri.toString)
      schemas.map(_.apply(doc))
    } else {
      throw StatusCode(resp.getStatusCode)
    }
  }

}
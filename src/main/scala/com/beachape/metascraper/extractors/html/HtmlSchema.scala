package com.beachape.metascraper.extractors.html

import java.nio.charset.Charset

import scala.collection.JavaConverters._
import com.beachape.metascraper.extractors.{ SchemaFactory, Schema }
import com.ning.http.client.Response
import com.ning.http.util.AsyncHttpProviderUtils
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
  val defaultCharset = String.utf8
  val contentTypes: Seq[String] = Seq("text/html")

  def apply(resp: Response): Seq[HtmlSchema] = {
    val doc = parse(resp)
    schemas.map(_.apply(doc))
  }

  protected def parse(resp: Response): Document = {
    def parseWith(charset: String.charset) = Jsoup.parse(charset(resp), resp.getUri.toString)

    val detectedFromResp = responseCharset(resp)
    val doc = parseWith(detectedFromResp.getOrElse(defaultCharset))

    detectedFromResp match {
      case Some(_) =>
        // HTTP response has Content-Type, it overrides the page settings, so use it
        doc

      case None =>
        // Read the page <meta> and re-parse if found
        docCharset(doc).filterNot(defaultCharset.==).map(parseWith).getOrElse(doc)
    }
  }

  protected def responseCharset(resp: Response): Option[String.charset] =
    tryFromContentType(resp.getContentType)

  protected def docCharset(doc: Document): Option[String.charset] = {
    val html5 = doc.select("meta[charset]").asScala.flatMap { e =>
      tryFromContentType(s"text/html; charset=${e.attr("charset")}")
    }
    val html = doc.select("meta[http-equiv=Content-Type]").asScala.collect {
      case e if e.hasAttr("content") => tryFromContentType(e.attr("content"))
    }.flatten

    (html5 ++ html).headOption
  }

  protected def tryFromContentType(contentType: String): Option[String.charset] =
    for {
      ct <- Option { contentType }
      charset <- Option { AsyncHttpProviderUtils.parseCharset(ct) }
      if Charset.isSupported(charset)
    } yield String.charset(Charset.forName(charset))
}
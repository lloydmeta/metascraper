package com.beachape.metascraper.extractors.html

import java.io.InputStream
import java.nio.ByteBuffer
import java.util
import com.ning.http.client.{ FluentCaseInsensitiveStringsMap, Response }
import com.ning.http.client.cookie.Cookie
import org.scalatest.{ FunSpec, Matchers }
import org.jsoup.nodes.Document

/**
 * Created by alaz on 04.01.17.
 */
class HtmlSchemasSpec extends FunSpec with Matchers {
  /*
    curl -i http://www.interfax.ru/russia/491014

Server: nginx/1.8.0
Date: Wed, 04 Jan 2017 14:20:25 GMT
Content-Type: text/html
Content-Length: 66222
Connection: keep-alive
Cache-Control: private
Pragma: no-cache
Expires: Wed, 04 Jan 2017 14:20:22 GMT
Last-Modified: Tue, 4 Jan 2017 14:20:22 GMT
Set-Cookie: IF=UseR=633724723; expires=Mon, 31-Dec-2029 21:00:00 GMT; path=/
Set-Cookie: ASPSESSIONIDSAQSQAAA=OKOMGMFCIHPGPBKPBBAKLPGP; path=/
X-Frame-Options: SAMEORIGIN


<meta http-equiv="Content-Type" content="text/html; charset=windows-1251" />

   */

  def resp(contentType: String, body: String) = new Response {
    def getResponseBodyExcerpt(maxLength: Int, charset: String): String = body.replace("CHARSET", charset)

    def getResponseBodyExcerpt(maxLength: Int): String = body

    def getResponseBodyAsByteBuffer: ByteBuffer = ???

    def getStatusCode: Int = 200

    def getResponseBodyAsBytes: Array[Byte] = ???

    def getResponseBodyAsStream: InputStream = ???

    def isRedirected: Boolean = ???

    def getCookies: util.List[Cookie] = ???

    def hasResponseBody: Boolean = ???

    def getStatusText: String = "200"

    def getHeaders(name: String): util.List[String] = ???

    def getHeaders: FluentCaseInsensitiveStringsMap = ???

    def hasResponseHeaders: Boolean = ???

    def getResponseBody(charset: String): String = body.replace("CHARSET", charset)

    def getResponseBody: String = body

    def getContentType: String = contentType

    def hasResponseStatus: Boolean = ???

    def getUri = com.ning.http.client.uri.Uri.create("http://www.interfax.ru/russia/491014")

    def getHeader(name: String): String = ???
  }

  val Html =
    """
      |<!DOCTYPE html>
      |<html lang="ru"><head>
      |<meta name="result" content="CHARSET" />
      |</head></html>
    """.stripMargin

  val Html_httpequiv =
    """
      |<!DOCTYPE html>
      |<html lang="ru"><head>
      |<meta http-equiv="Content-Type" content="text/html; charset=windows-1251" />
      |<meta name="result" content="CHARSET" />
      |</head></html>
    """.stripMargin

  val Html5 =
    """
      |<!DOCTYPE html>
      |<html lang="ru"><head>
      |<meta name="result" content="CHARSET" />
      |</head></html>
    """.stripMargin

  val Html5_httpequiv =
    """
      |<!DOCTYPE html>
      |<html lang="ru"><head>
      |<meta http-equiv="Content-Type" content="text/html; charset=windows-1251" />
      |<meta name="result" content="CHARSET" />
      |</head></html>
    """.stripMargin

  val Html5_charset =
    """
      |<!DOCTYPE html>
      |<html lang="ru"><head>
      |<meta charset="windows-1251" />
      |<meta name="result" content="CHARSET" />
      |</head></html>
    """.stripMargin

  val Html5_both =
    """
      |<!DOCTYPE html>
      |<html lang="ru"><head>
      |<meta charset="windows-1251" />
      |<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
      |<meta name="result" content="CHARSET" />
      |</head></html>
    """.stripMargin

  def resultFrom(doc: Document) =
    doc.select("meta[name=result]").attr("content")

  describe("HtmlSchema doc parser") {
    val factory = HtmlSchemas(NormalPage)

    it("should use UTF-8 as a default charset ") {
      resultFrom(factory(resp("text/html", Html)).head.doc) should equal("UTF-8")
      resultFrom(factory(resp("text/html", Html5)).head.doc) should equal("UTF-8")
    }
    it("should use HTTP Content-Type when not specified on page") {
      resultFrom(factory(resp("text/html; charset=windows-1251", Html)).head.doc) should equal("windows-1251")
      resultFrom(factory(resp("text/html; charset=windows-1251", Html5)).head.doc) should equal("windows-1251")
    }
    it("should use HTML `http-equiv`") {
      resultFrom(factory(resp("text/html", Html_httpequiv)).head.doc) should equal("windows-1251")
    }
    it("should use HTML5 `charset`") {
      resultFrom(factory(resp("text/html", Html5_charset)).head.doc) should equal("windows-1251")
    }
    it("should use HTML5 `http-equiv`") {
      resultFrom(factory(resp("text/html", Html5_httpequiv)).head.doc) should equal("windows-1251")
    }
    it("should use HTML5 `charset` over `http-equiv`") {
      resultFrom(factory(resp("text/html", Html5_both)).head.doc) should equal("windows-1251")
    }
    it("should use HTTP Content-Type over page <meta>") {
      resultFrom(factory(resp("text/html; charset=UTF-8", Html_httpequiv)).head.doc) should equal("UTF-8")
      resultFrom(factory(resp("text/html; charset=UTF-8", Html5_charset)).head.doc) should equal("UTF-8")
    }
  }
}

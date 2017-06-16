package com.beachape.metascraper.extractors

import com.ning.http.client.Response

/**
  * Created by Lloyd on 2/15/15.
  */
trait SchemaFactory extends (Response => Seq[Schema]) {

  /**
    * Supported Content-Types for this [[SchemaFactory]]
    */
  def contentTypes: Seq[String]

  /**
    * Based on a [[Response]], returns a list of [[Schema]]
    */
  def apply(s: Response): Seq[Schema]

}

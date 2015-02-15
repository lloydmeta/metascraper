package com.beachape.metascraper.extractors

import org.jsoup.nodes.Document

import scala.util.Try

/**
 * Created by Lloyd on 2/15/15.
 */

/**
 * Extractor based on whatever schema (e.g. OpenGraph, Twitter, etc)
 */
trait Schema {

  /**
   * Returns the url of a Jsoup document
   *
   * @param doc Document as parsed by JSoup
   * @return String url
   */
  def extractUrl(doc: Document): Option[String]

  /**
   * Returns the title of a Jsoup document
   *
   * @param doc Document as parsed by JSoup
   * @return String title
   */
  def extractTitle(doc: Document): Option[String]

  /**
   * Returns the description of a Jsoup document
   *
   * @param doc Document as parsed by JSoup
   * @return String description
   */
  def extractDescription(doc: Document): Option[String]

  /**
   * Returns a best guess for the url of the main Image of a Jsoup document
   *
   * @param doc Document as parsed by JSoup
   * @return String url of the main image
   */
  def extractMainImage(doc: Document): Option[String]

  /**
   * Returns a best guess for the best images of a Jsoup document
   *
   * @param doc Document as parsed by Jsoup
   * @return Seq[String] collection of image urls
   */
  def extractImages(doc: Document): Seq[String]

  /**
   * Gets the non-empty content of a Document element.
   *
   * Returns None if it is empty
   */
  protected def nonEmptyContent(doc: Document, selector: String): Option[String] = Try {
    doc.select(selector).attr("content")
  }.toOption.filter(_.nonEmpty)
}

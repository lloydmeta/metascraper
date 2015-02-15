package com.beachape.metascraper.extractors

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
   * @return String url
   */
  def extractUrl: Option[String]

  /**
   * Returns the title of a Jsoup document
   *
   * @return String title
   */
  def extractTitle: Option[String]

  /**
   * Returns the description of a Jsoup document
   *
   * @return String description
   */
  def extractDescription: Option[String]

  /**
   * Returns a best guess for the url of the main Image of a Jsoup document
   *
   * @return String url of the main image
   */
  def extractMainImage: Option[String]

  /**
   * Returns a best guess for the best images of a Jsoup document
   *
   * @return Seq[String] collection of image urls
   */
  def extractImages: Seq[String]

}

package com.beachape.metascraper

/**
 * Companion method containing the implicit conversion to RichString
 */
object RichString {
  val imageExtensions = Seq(".jpg", ".png", ".jpeg", ".gif", ".bmp")
  implicit def string2RichString(s: String): RichString = new RichString(s)
}

/**
 * Simple PimpMyLibrary class to see if a string has an image extension
 * @param string String to wrap
 */
case class RichString (string: String) extends AnyVal {
  import RichString._
  def hasImageExtension: Boolean = imageExtensions.find(string endsWith _).isDefined
}

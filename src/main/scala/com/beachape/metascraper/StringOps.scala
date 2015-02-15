package com.beachape.metascraper

/**
 * Companion method containing the implicit conversion to RichString
 */
object StringOps {
  val imageExtensions = Seq(".jpg", ".png", ".jpeg", ".gif", ".bmp")


  /**
   * Simple PimpMyLibrary class to see if a string has an image extension
   * @param string String to wrap
   */
  implicit class RichString (val string: String) extends AnyVal {
    def hasImageExtension: Boolean = imageExtensions.find(string endsWith _).isDefined
  }
}



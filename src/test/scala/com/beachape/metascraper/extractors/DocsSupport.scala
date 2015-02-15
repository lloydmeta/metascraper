package com.beachape.metascraper.extractors

import org.jsoup.Jsoup

import scala.io.Source

/**
 * Created by Lloyd on 2/15/15.
 */
trait DocsSupport {

  val withoutOgTagsSource = Source.fromURL(getClass.getResource("/withoutOgTags.html"))
  val withoutOgTagsDoc = Jsoup.parse(withoutOgTagsSource.mkString)

  val withOgTagsSource = Source.fromURL(getClass.getResource("/withOgTags.html"))
  val withOgTagsDoc = Jsoup.parse(withOgTagsSource.mkString)

  val withoutAnyTagsSource = Source.fromURL(getClass.getResource("/withoutAnyTags.html"))
  val withoutAnyTagsDoc = Jsoup.parse(withoutAnyTagsSource.mkString)

  val withOnlyPTag = Source.fromURL(getClass.getResource("/withOnlyPTag.html"))
  val withOnlyPTagDoc = Jsoup.parse(withOnlyPTag.mkString)

  val withOnlyImageRel = Source.fromURL(getClass.getResource("/withOnlyImageRel.html"))
  val withOnlyImageRelDoc = Jsoup.parse(withOnlyImageRel.mkString)

  val docs = Seq(
    withoutOgTagsDoc,
    withOgTagsDoc,
    withoutAnyTagsDoc,
    withOnlyPTagDoc,
    withOnlyImageRelDoc)

}

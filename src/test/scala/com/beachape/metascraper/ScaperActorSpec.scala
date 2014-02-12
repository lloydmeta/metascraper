package com.beachape.metascraper

import org.scalatest.BeforeAndAfter
import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers
import akka.testkit.{TestActorRef, TestKit, ImplicitSender}
import akka.actor.ActorSystem
import scala.io.Source
import org.jsoup.Jsoup
import com.beachape.metascraper.Messages.{ScrapedData, ScrapeUrl}
import com.beachape.support.Betamax
import co.freeside.betamax.TapeMode
import dispatch._, Defaults._
import scala.concurrent.duration._

class ScraperActorSpec extends TestKit(ActorSystem("testSystem"))
  with FunSpec
  with ShouldMatchers
  with BeforeAndAfter
  with ImplicitSender
  with Betamax {

  val scraperActorRef = TestActorRef(new ScraperActor)
  val scraperActor = scraperActorRef.underlyingActor

  lazy val withoutOgTagsSource = Source.fromURL(getClass.getResource("/withoutOgTags.html"))
  lazy val withoutOgTagsDoc = Jsoup.parse(withoutOgTagsSource.mkString)

  lazy val withOgTagsSource = Source.fromURL(getClass.getResource("/withOgTags.html"))
  lazy val withOgTagsDoc = Jsoup.parse(withOgTagsSource.mkString)

  lazy val withoutAnyTagsSource = Source.fromURL(getClass.getResource("/withoutAnyTags.html"))
  lazy val withoutAnyTagsDoc = Jsoup.parse(withoutAnyTagsSource.mkString)

  lazy val withOnlyPTag = Source.fromURL(getClass.getResource("/withOnlyPTag.html"))
  lazy val withOnlyPTagDoc = Jsoup.parse(withOnlyPTag.mkString)

  lazy val withOnlyImageRel = Source.fromURL(getClass.getResource("/withOnlyImageRel.html"))
  lazy val withOnlyImageRelDoc = Jsoup.parse(withOnlyImageRel.mkString)

  describe("#getStringFromUrl") {

    // Make sure to use a trailing slash at the end .. https://github.com/robfletcher/betamax/issues/61
    it("should return a string of the HTML at that page") _ using betamax("test-beachape.com", Some(TapeMode.READ_ONLY)) {
      val simpleDispatchResult = Http(url("http://www.beachape.com/about/") OK as.String)
      val futureEither = scraperActor.getStringFromUrl(ScrapeUrl("http://www.beachape.com/about/"))
      for{
        either <- futureEither
        returnedString <- either.right
      }{
        either should be('right)
        returnedString should be(simpleDispatchResult())
      }
    }

  }

  describe("#extractUrl") {

    describe("for a page without og:url") {

      it("should return the passed in accessedUrl") {
        scraperActor.extractUrl(withoutOgTagsDoc, "test") should be("test")
      }

    }

    describe("for a page with a meta og:url tag") {

      it("should return the content of the meta og:url tag") {
        scraperActor.extractUrl(withOgTagsDoc, "test") should be("https://ogtagsthingy.com/page.html")
      }

    }

  }

  describe("#extractTitle") {

    describe("for a page without og:title but with a title tag") {

      it("should return the contents of the <title> tag") {
        scraperActor.extractTitle(withoutOgTagsDoc) should be("Without Open Graph tags")
      }

    }

    describe("for a page with og:title") {

      it("should return the contents of the og:title tag") {
        scraperActor.extractTitle(withOgTagsDoc) should be("Title in Open Graph Tag")
      }

    }
  }

  describe("for a page without og:title and title tags") {

    it("should an empty string") {
      scraperActor.extractTitle(withoutAnyTagsDoc) should be("")
    }

  }

  describe("#extractDescription") {

    describe("for a page without og:description but with a meta description tag") {

      it("should return the contents of the <meta name='description' .. > tag") {
        scraperActor.extractDescription(withoutOgTagsDoc) should be("A Description in heeere")
      }

    }

    describe("for a page with a og:description tag") {

      it("should return the contents of the og:description tag") {
        scraperActor.extractDescription(withOgTagsDoc) should be("Description inside og:description tag")
      }

    }

    describe("for a page without og:description and meta description tags but has a paragraph tag") {
      it("should return an empty string") {
        scraperActor.extractDescription(withOnlyPTagDoc) should include("is an object-functional programming and scripting language for general software applications")
      }
    }

    describe("for a page without og:description and meta description tags") {

      it("should return an empty string") {
        scraperActor.extractDescription(withoutAnyTagsDoc) should be("")
      }

    }
  }

  describe("#extractImages") {

    describe("for a page without og:image tags") {

      it("should return the src contents of the first 5 image tags") {
        scraperActor.extractImages(withoutOgTagsDoc) should be(
          Seq(
            "http://lolol.com/thing1.gif",
            "http://lolol.com/thing2.jpg",
            "http://lolol.com/thing3.jpg",
            "http://lolol.com/thing4.png",
            "http://lolol.com/thing5.jpg"))
      }

    }

    describe("for a page with og:image tags") {

      it("should return the src contents of 5 image sources, prioritising og:image tag contents") {
        scraperActor.extractImages(withOgTagsDoc) should be(
          Seq(
            "http://lala.com/theMainImage.png",
            "http://lolol.com/thing1.gif",
            "http://lolol.com/thing2.jpg",
            "http://lolol.com/thing3.jpg",
            "http://lolol.com/thing4.png"))
      }

    }

    describe("for a page with an image rel tag") {

      it("should return a sequence with the image rel source url") {
        scraperActor.extractImages(withOnlyImageRelDoc) should be(
          Seq(
            "http://lala.com/theMainImage.png"))
      }

    }

    describe("for a page with neither og:image nor img tags") {

      it("should return an empty sequence") {
        scraperActor.extractImages(withoutAnyTagsDoc) should be('empty)
      }

    }
  }

  describe("#extractMainImage") {

    describe("for a page without og:image tags") {

      it("should return the src contents of the first image tag") {
        scraperActor.extractMainImage(withoutOgTagsDoc) should equal("http://lolol.com/thing1.gif")
      }

    }

    describe("for a page with og:image tags") {

      it("should return the src contents of the first og:image tag") {
        scraperActor.extractMainImage(withOgTagsDoc) should equal("http://lala.com/theMainImage.png")
      }

    }

    describe("for a page with neither og:image nor img tags") {

      it("should return an empty string") {
        scraperActor.extractMainImage(withoutAnyTagsDoc) should be("")
      }

    }

  }

  describe("#extractScrapedData") {

    describe("for a page without OG tags") {

      val scrappedData = scraperActor.extractScrapedData(withoutOgTagsDoc, "test")

      it("should return a ScrapedData message with proper attributes") {
        scrappedData.title should be("Without Open Graph tags")
        scrappedData.description should be("A Description in heeere")
        scrappedData.url should be("test")
        scrappedData.mainImageUrl should be("http://lolol.com/thing1.gif")
        scrappedData.imageUrls should be(
          Seq(
            "http://lolol.com/thing1.gif",
            "http://lolol.com/thing2.jpg",
            "http://lolol.com/thing3.jpg",
            "http://lolol.com/thing4.png",
            "http://lolol.com/thing5.jpg"))
      }

    }

    describe("for a page with og tags") {

      val scrappedData = scraperActor.extractScrapedData(withOgTagsDoc, "test")

      it("should return a ScrapedData message with proper attributes") {
        scrappedData.title should be("Title in Open Graph Tag")
        scrappedData.description should be("Description inside og:description tag")
        scrappedData.url should be("https://ogtagsthingy.com/page.html")
        scrappedData.mainImageUrl should be("http://lala.com/theMainImage.png")
        scrappedData.imageUrls should be(
          Seq(
            "http://lala.com/theMainImage.png",
            "http://lolol.com/thing1.gif",
            "http://lolol.com/thing2.jpg",
            "http://lolol.com/thing3.jpg",
            "http://lolol.com/thing4.png"))
      }

    }

    describe("for a page with no required tags") {

      val scrappedData = scraperActor.extractScrapedData(withoutAnyTagsDoc, "test")

      it("should return a ScrapedData message with proper attributes") {
        scrappedData.title should be("")
        scrappedData.description should be("")
        scrappedData.url should be("test")
        scrappedData.mainImageUrl should be("")
        scrappedData.imageUrls should be('empty)
      }

    }

  }

  describe("integration testing by sending ScrapeUrl messages") {

    it ("should return proper data for a non-redirecting URL") _ using betamax("test-beachape.com", Some(TapeMode.READ_ONLY)) {
      scraperActorRef ! ScrapeUrl("http://www.beachape.com/about/")
      val response = receiveOne(30 seconds).asInstanceOf[Either[Throwable, ScrapedData]]
      response should be('right)
      val Right(scrapedData) = response
      scrapedData.title should be("About me - BeachApe.")
      scrapedData.description should be("About Me Sep 5th, 2013 General stuff My name is Lloyd Chan (online, my handles are lloydmeta or meta_Lloyd) and I currently live in Tokyo, Japan. I …")
      scrapedData.url should be("http://www.beachape.com/about/")
      scrapedData.mainImageUrl should be('empty)
      scrapedData.imageUrls should be('empty)
    }

    it ("should return proper data for a redirecting URL") _ using betamax("test-youtu.be-redirecting", Some(TapeMode.READ_ONLY)) {
      scraperActorRef ! ScrapeUrl("http://youtu.be/G8CeP15EAS8/")
      val response = receiveOne(30 seconds).asInstanceOf[Either[Throwable, ScrapedData]]
      response should be('right)
      val Right(scrapedData) = response
      scrapedData.title should be("未来のライター Jii！Jii！Jii！")
      scrapedData.description should be("未来のライター【Jii】のテーマソング！ 詳細はこちら→http://jii-lighter.com/ USB充電式電熱線ライター【Jii】はガス不要！風に強い！USB充電で繰り返し使える！安心・安全で環境に優しい！")
      scrapedData.url should be("http://www.youtube.com/watch?v=G8CeP15EAS8")
      scrapedData.mainImageUrl should be("http://i1.ytimg.com/vi/G8CeP15EAS8/hqdefault.jpg")
      scrapedData.imageUrls should contain("http://i1.ytimg.com/vi/G8CeP15EAS8/hqdefault.jpg")
    }

    it ("should return proper data for a URL with a page that does not contain OG links") _ using betamax("test-imgur", Some(TapeMode.READ_ONLY)) {
      scraperActorRef ! ScrapeUrl("http://imgur.com/gallery/ndVA6qs")
      val response = receiveOne(30 seconds).asInstanceOf[Either[Throwable, ScrapedData]]
      response should be('right)
      val Right(scrapedData) = response
      scrapedData.title should be("What I imagine entering the job market in the 90s must have been like... - Imgur")
      scrapedData.description should be("Imgur is home to the web's most popular image content, curated in real time by a dedicated community through commenting, voting and sharing.")
      scrapedData.url should be("http://imgur.com/gallery/ndVA6qs")
      scrapedData.mainImageUrl should be("http://i.imgur.com/ndVA6qs.png")
      scrapedData.imageUrls should contain("http://i.imgur.com/ndVA6qs.png")
    }

    it("should return Left for an invalid URL") {
      scraperActorRef ! ScrapeUrl("omgwtfbbq")
      val response = receiveOne(30 seconds).asInstanceOf[Either[Throwable, ScrapedData]]
      response should be('left)
    }

    it ("should return Left for a broken URL") _ using betamax("test-beachape.com-broken", Some(TapeMode.READ_ONLY)) {
      scraperActorRef ! ScrapeUrl("http://beachape.com/asdfadgadskgjhagkjas/")
      val response = receiveOne(30 seconds).asInstanceOf[Either[Throwable, ScrapedData]]
      response should be('left)
    }

    it ("should return proper mostly Empty data for a URL that does not point to HTML") _ using betamax("test-beachape.com-non-HTML", Some(TapeMode.READ_ONLY)) {
      scraperActorRef ! ScrapeUrl("http://www.beachape.com/downloads/code/scala/schwatcher_example.scala/")
      val response = receiveOne(30 seconds).asInstanceOf[Either[Throwable, ScrapedData]]
      response should be('right)
      val Right(scrapedData) = response
      scrapedData.title should be('empty)
      scrapedData.description should be('empty)
      scrapedData.url should be("http://www.beachape.com/downloads/code/scala/schwatcher_example.scala/")
      scrapedData.mainImageUrl should be('empty)
      scrapedData.imageUrls should be('empty)
    }

    it ("should return the URL if it ends in any of the accepted image extenions") {
      val imgUrl = "http://nonexistentsite.com/somethingnonexistent.jpg"
      scraperActorRef ! ScrapeUrl(imgUrl)
      val response = receiveOne(30 seconds).asInstanceOf[Either[Throwable, ScrapedData]]
      response should be('right)
      val Right(scrapedData) = response
      scrapedData.title should be(imgUrl)
      scrapedData.description should be(imgUrl)
      scrapedData.url should be(imgUrl)
      scrapedData.mainImageUrl should be(imgUrl)
      scrapedData.imageUrls should be(Seq(imgUrl))
    }

  }

}

package com.beachape.metascraper.extractors

import org.jsoup.Jsoup
import org.scalatest.{ Matchers, FunSpec }

/**
 * Created by Lloyd on 2/15/15.
 */
class NormalPageSpec extends FunSpec with Matchers with DocsSupport {

  val subject = NormalPage

  describe("#extractUrl") {

    describe("for docs without a base Url") {

      it("should return None") {
        docs.map(subject.extractUrl).forall(_ == None) shouldBe true
      }

    }

    describe("for a doc with base Url") {

      it("should return the base Uri") {
        val doc = Jsoup.parse(withoutOgTagsSource.mkString)
        doc.setBaseUri("hello")
        subject.extractUrl(doc) shouldBe Some("hello")
      }

    }

  }

  describe("#extractTitle") {

    describe("for a page with a title tag") {

      it("should return the contents of the <title> tag") {
        subject.extractTitle(withOgTagsDoc) shouldBe Some("With Open Graph tags")
        subject.extractTitle(withoutOgTagsDoc) shouldBe Some("Without Open Graph tags")
      }

    }

    describe("for a page without title tags") {

      it("should return None") {
        subject.extractTitle(withoutAnyTagsDoc) shouldBe None
      }

    }
  }

  describe("#extractDescription") {

    describe("for a page without description tags") {

      it("should return None") {
        subject.extractDescription(withoutAnyTagsDoc) shouldBe None
      }

    }

    describe("for a page with description tags") {

      it("should return the contents of the description tag") {
        subject.extractDescription(withOgTagsDoc) shouldBe Some("A Description in heeere")

      }

    }

    describe("for a page with just p tags") {

      it("should return the contents of the first p") {
        subject.extractDescription(withOnlyPTagDoc).get should include("is an object-functional programming and scripting language for general software applications")

      }
    }
  }

  describe("#extractImages") {

    describe("for a page without image tags") {

      it("should return an empty sequence") {
        subject.extractImages(withoutAnyTagsDoc) shouldBe 'empty
      }

    }

    describe("for a page with image tags") {

      it("should return the src contents of the image sources") {
        subject.extractImages(withoutOgTagsDoc) shouldBe
          Seq("http://lolol.com/thing1.gif",
            "http://lolol.com/thing2.jpg",
            "http://lolol.com/thing3.jpg",
            "http://lolol.com/thing4.png",
            "http://lolol.com/thing5.jpg",
            "http://lolol.com/thing6.jpg")

      }

    }

    describe("for a page with an image rel tag") {

      it("should return the src contents of the image sources, including the image_rel") {
        subject.extractImages(withOnlyImageRelDoc) shouldBe
          Seq("http://lala.com/theMainImage.png")
      }

    }

  }

  describe("#extractMainImage") {

    describe("for a page without image tags") {

      it("should return None") {
        subject.extractMainImage(withoutAnyTagsDoc) shouldBe None
      }

    }

    describe("for a page with image tags") {

      it("should return the src contents of the first image tag") {
        subject.extractMainImage(withoutOgTagsDoc) shouldBe Some("http://lolol.com/thing1.gif")
      }

    }

    describe("for a page with an image rel tag") {

      it("should return the src content of the image_rel") {
        subject.extractMainImage(withOnlyImageRelDoc) shouldBe Some("http://lala.com/theMainImage.png")
      }

    }

  }

}
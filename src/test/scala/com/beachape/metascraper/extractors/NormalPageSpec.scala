package com.beachape.metascraper.extractors

import com.beachape.metascraper.extractors.html.NormalPage
import org.jsoup.Jsoup
import org.scalatest.{Matchers, FunSpec}

/**
  * Created by Lloyd on 2/15/15.
  */
class NormalPageSpec extends FunSpec with Matchers with DocsSupport {

  describe("#extractUrl") {

    describe("for docs without a base Url") {

      it("should return None") {
        docs
          .map { doc =>
            val subject = NormalPage(doc)
            subject.extractUrl
          }
          .forall(_ == None) shouldBe true
      }

    }

    describe("for a doc with base Url") {

      it("should return the base Uri") {
        val doc = Jsoup.parse(withoutOgTagsSource.mkString)
        doc.setBaseUri("hello")
        NormalPage(doc).extractUrl shouldBe Some("hello")
      }

    }

  }

  describe("#extractTitle") {

    describe("for a page with a title tag") {

      it("should return the contents of the <title> tag") {
        NormalPage(withOgTagsDoc).extractTitle shouldBe Some("With Open Graph tags")
        NormalPage(withoutOgTagsDoc).extractTitle shouldBe Some("Without Open Graph tags")
      }

    }

    describe("for a page without title tags") {

      it("should return None") {
        NormalPage(withoutAnyTagsDoc).extractTitle shouldBe None
      }

    }
  }

  describe("#extractDescription") {

    describe("for a page without description tags") {

      it("should return None") {
        NormalPage(withoutAnyTagsDoc).extractDescription shouldBe None
      }

    }

    describe("for a page with description tags") {

      it("should return the contents of the description tag") {
        NormalPage(withOgTagsDoc).extractDescription shouldBe Some("A Description in heeere")

      }

    }

    describe("for a page with just p tags") {

      it("should return the contents of the first p") {
        NormalPage(withOnlyPTagDoc).extractDescription.get should include(
          "is an object-functional programming and scripting language for general software applications")
      }
    }
  }

  describe("#extractImages") {

    describe("for a page without image tags") {

      it("should return an empty sequence") {
        NormalPage(withoutAnyTagsDoc).extractImages shouldBe 'empty
      }

    }

    describe("for a page with image tags") {

      it("should return the src contents of the image sources") {
        NormalPage(withoutOgTagsDoc).extractImages shouldBe
          Seq(
            "http://lolol.com/thing1.gif",
            "http://lolol.com/thing2.jpg",
            "http://lolol.com/thing3.jpg",
            "http://lolol.com/thing4.png",
            "http://lolol.com/thing5.jpg",
            "http://lolol.com/thing6.jpg"
          )

      }

    }

    describe("for a page with an image rel tag") {

      it("should return the src contents of the image sources, including the image_rel") {
        NormalPage(withOnlyImageRelDoc).extractImages shouldBe
          Seq("http://lala.com/theMainImage.png")
      }

    }

  }

  describe("#extractMainImage") {

    describe("for a page without image tags") {

      it("should return None") {
        NormalPage(withoutAnyTagsDoc).extractMainImage shouldBe None
      }

    }

    describe("for a page with image tags") {

      it("should return the src contents of the first image tag") {
        NormalPage(withoutOgTagsDoc).extractMainImage shouldBe Some("http://lolol.com/thing1.gif")
      }

    }

    describe("for a page with an image rel tag") {

      it("should return the src content of the image_rel") {
        NormalPage(withOnlyImageRelDoc).extractMainImage shouldBe Some(
          "http://lala.com/theMainImage.png")
      }

    }

  }

}

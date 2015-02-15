package com.beachape.metascraper.extractors

import org.scalatest._

/**
 * Created by Lloyd on 2/15/15.
 */
class OpenGraphSpec extends FunSpec with Matchers with DocsSupport {

  val subject = OpenGraph

  describe("#extractUrl") {

    describe("for a page without og:url") {

      it("should return None") {
        subject.extractUrl(withoutOgTagsDoc) shouldBe None
      }

    }

    describe("for a page with a meta og:url tag") {

      it("should return the content of the meta og:url tag") {
        subject.extractUrl(withOgTagsDoc) shouldBe Some("https://ogtagsthingy.com/page.html")
      }

    }
  }

  describe("#extractTitle") {

    describe("for a page without og:title but with a title tag") {

      it("should return None") {
        subject.extractTitle(withoutOgTagsDoc) shouldBe None
      }

    }

    describe("for a page with og:title") {

      it("should return the contents of the og:title tag") {
        subject.extractTitle(withOgTagsDoc) shouldBe Some("Title in Open Graph Tag")
      }

    }
  }

  describe("#extractDescription") {

    describe("for a page without og:description") {

      it("should return None") {
        subject.extractDescription(withoutOgTagsDoc) shouldBe None
      }

    }

    describe("for a page with a og:description tag") {

      it("should return the contents of the og:description tag") {
        subject.extractDescription(withOgTagsDoc) shouldBe Some("Description inside og:description tag")
      }

    }
  }

  describe("#extractImages") {

    describe("for a page without og:image tags") {

      it("should return an empty sequence") {
        subject.extractImages(withoutOgTagsDoc) shouldBe 'empty
      }

    }

    describe("for a page with og:image tags") {

      it("should return the src contents of 5 pg:image sources") {
        subject.extractImages(withOgTagsDoc) shouldBe
          Seq(
            "http://lala.com/theMainImage.png",
            "http://lolol.com/thing1.gif",
            "http://lolol.com/thing2.jpg",
            "http://lolol.com/thing3.jpg",
            "http://lolol.com/thing4.png",
            "http://lolol.com/thing5.jpg")

      }

    }

  }

  describe("#extractMainImage") {

    describe("for a page without og:image tags") {

      it("should return None") {
        subject.extractMainImage(withoutOgTagsDoc) shouldBe None
      }

    }

    describe("for a page with og:image tags") {

      it("should return the src contents of the first og:image tag") {
        subject.extractMainImage(withOgTagsDoc) shouldBe Some("http://lala.com/theMainImage.png")
      }

    }


  }

}
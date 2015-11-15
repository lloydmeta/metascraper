package com.beachape.metascraper

import org.scalatest.BeforeAndAfter
import org.scalatest._
import akka.testkit.{ TestActorRef, TestKit, ImplicitSender }
import akka.actor.ActorSystem
import com.beachape.metascraper.Messages.{ ScrapedData, ScrapeUrl }
import scala.language.postfixOps
import scala.concurrent.duration._

class ScraperActorSpec extends TestKit(ActorSystem("testSystem"))
    with FunSpecLike
    with Matchers
    with BeforeAndAfter
    with ImplicitSender {

  val scraperActorRef = TestActorRef(new ScraperActor)

  describe("integration testing by sending ScrapeUrl messages") {

    it("should return proper data for a non-redirecting URL") {
      scraperActorRef ! ScrapeUrl("https://beachape.com/about/")
      val response = receiveOne(30 seconds).asInstanceOf[Either[Throwable, ScrapedData]]
      response should be('right)
      val Right(scrapedData) = response
      scrapedData.title should be("About Me - BeachApe.")
      scrapedData.description should startWith("Sep 5th, 2013 My name is Lloyd Chan")
      scrapedData.url should be("https://beachape.com/about/")
      scrapedData.mainImageUrl shouldBe "https://beachape.com/images/rss.png"
      scrapedData.imageUrls should contain("https://beachape.com/images/rss.png")
    }

    it("should return proper data for a redirecting URL") {
      scraperActorRef ! ScrapeUrl("http://youtu.be/G8CeP15EAS8/")
      val response = receiveOne(30 seconds).asInstanceOf[Either[Throwable, ScrapedData]]
      response should be('right)
      val Right(scrapedData) = response
      scrapedData.title should be("未来のライター Jii！Jii！Jii！")
      scrapedData.description should be("未来のライター【Jii】のテーマソング！ 詳細はこちら→http://jii-lighter.com/ USB充電式電熱線ライター【Jii】はガス不要！風に強い！USB充電で繰り返し使える！安心・安全で環境に優しい！")
      scrapedData.url should be("https://www.youtube.com/watch?v=G8CeP15EAS8")
      scrapedData.mainImageUrl should (be("https://i.ytimg.com/vi/G8CeP15EAS8/hqdefault.jpg") or be("http://i.ytimg.com/vi/G8CeP15EAS8/hqdefault.jpg"))
      scrapedData.imageUrls should (contain("https://i.ytimg.com/vi/G8CeP15EAS8/hqdefault.jpg") or contain("http://i.ytimg.com/vi/G8CeP15EAS8/hqdefault.jpg"))
    }

    it("should return proper data for a 303-redirecting URL") {
      scraperActorRef ! ScrapeUrl("http://www.nytimes.com/2015/09/23/business/media/the-plot-twist-e-book-sales-slip-and-print-is-far-from-dead.html")
      val response = receiveOne(30 seconds).asInstanceOf[Either[Throwable, ScrapedData]]
      response should be('right)
      val Right(scrapedData) = response
      scrapedData.title should be("The Plot Twist: E-Book Sales Slip, and Print Is Far From Dead")
      scrapedData.description should be("With readers on a reverse migration to print, the “e-book terror has kind of subsided” for bookstores and publishers.")
      scrapedData.url should be("http://www.nytimes.com/2015/09/23/business/media/the-plot-twist-e-book-sales-slip-and-print-is-far-from-dead.html")
      scrapedData.mainImageUrl shouldBe "http://static01.nyt.com/images/2015/09/23/business/23jpEBOOKS1/23jpEBOOKS1-facebookJumbo.jpg"
      scrapedData.imageUrls shouldBe Seq("http://static01.nyt.com/images/2015/09/23/business/23jpEBOOKS1/23jpEBOOKS1-facebookJumbo.jpg", "http://static01.nyt.com/images/2015/09/23/business/23jpEBOOKS1/23jpEBOOKS1-master675.jpg", "http://static01.nyt.com/images/2015/09/23/business/23jpEBOOKS2/23jpEBOOKS2-articleLarge.jpg")
    }

    it("should return proper data for a URL with a page that does not contain OG links") {
      scraperActorRef ! ScrapeUrl("http://imgur.com/gallery/ndVA6qs")
      val response = receiveOne(30 seconds).asInstanceOf[Either[Throwable, ScrapedData]]
      response should be('right)
      val Right(scrapedData) = response
      scrapedData.title should startWith("What I imagine entering the job")
      scrapedData.description should startWith("Imgur")
      scrapedData.url should be("http://imgur.com/gallery/ndVA6qs")
      scrapedData.mainImageUrl should be("http://i.imgur.com/ndVA6qs.png?fb")
      scrapedData.imageUrls should contain("http://i.imgur.com/ndVA6qs.png?fb")
    }

    it("should return Left for an invalid URL") {
      scraperActorRef ! ScrapeUrl("omgwtfbbq")
      val response = receiveOne(30 seconds).asInstanceOf[Either[Throwable, ScrapedData]]
      response should be('left)
    }

    it("should return Left for a broken URL") {
      scraperActorRef ! ScrapeUrl("http://beachape.com/asdfadgadskgjhagkjas")
      val response = receiveOne(30 seconds).asInstanceOf[Either[Throwable, ScrapedData]]
      response should be('left)
    }

    it("should return proper mostly Empty data for a URL that does not point to HTML") {
      scraperActorRef ! ScrapeUrl("http://www.beachape.com/downloads/code/scala/schwatcher_example.scala")
      val response = receiveOne(30 seconds).asInstanceOf[Either[Throwable, ScrapedData]]
      response should be('right)
      val Right(scrapedData) = response
      scrapedData.title should be('empty)
      scrapedData.description should be('empty)
      scrapedData.url should be("http://www.beachape.com/downloads/code/scala/schwatcher_example.scala")
      scrapedData.mainImageUrl should be('empty)
      scrapedData.imageUrls should be('empty)
    }

    it("should return the URL if it ends in any of the accepted image extenions") {
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

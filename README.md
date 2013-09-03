# Metascraper

## Example
```scala
import akka.actor.ActorSystem
import com.beachape.metascraper.Messages._
import com.beachape.metascraper.ScraperActor
import scala.concurrent.Await
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._

implicit val timeout = Timeout(5 seconds)

implicit val system = ActorSystem("actorSystem")
implicit val dispatcher = system.dispatcher

val scraperActor = system.actorOf(ScraperActor())

val future = scraperActor ? ScrapeUrl("http://bbc.co.uk")
val result = Await.result(future, timeout.duration).asInstanceOf[ScrapedData]

result.imageUrls.foreach(println)
```
package com.beachape.metascraper

import com.beachape.metascraper.Messages._
import akka.actor.{ ActorLogging, ActorRef, Actor, Props }
import dispatch._
import java.util.concurrent.Executors
import com.ning.http.client.{ AsyncHttpClientConfig, AsyncHttpClient }

import scala.util.{ Failure, Success }

/**
 * Companion object for instantiating ScaperActors
 */
object ScraperActor {

  /**
   * Factory method for the params required to instantiate a MonitorActor
   *
   * @param httpExecutorThreads Int number of threads to use for this actor's async HTTP executor service
   * @param maxConnectionsPerHost Int max connections at a time per host
   * @param connectionTimeoutInMs Int time in milliseconds before timing out when trying to make a connection to a host
   * @param requestTimeoutInMs Int time in milliseconds before timing out when waiting for a request to complete after connection
   * @return Props for instantiating a ScaperActor
   */
  def apply(
    httpExecutorThreads: Int = 10,
    maxConnectionsPerHost: Int = 30,
    connectionTimeoutInMs: Int = 10000,
    requestTimeoutInMs: Int = 15000
  ) =
    Props(
      classOf[ScraperActor],
      httpExecutorThreads,
      maxConnectionsPerHost,
      connectionTimeoutInMs,
      requestTimeoutInMs
    )
}

/**
 * Actor for scraping metadata from websites at URLs
 *
 * Should be instantiated with Props provided via companion object factory
 * method
 */
class ScraperActor(
  httpExecutorThreads: Int = 10,
  maxConnectionsPerHost: Int = 30,
  connectionTimeoutInMs: Int = 10000,
  requestTimeoutInMs: Int = 15000
)
    extends Actor with ActorLogging {

  import context.dispatcher

  // Validator
  val validSchemas = Seq("http", "https")
  // Http client config
  val followRedirects = true
  val connectionPooling = true

  private val executorService = Executors.newFixedThreadPool(httpExecutorThreads)
  private val config = new AsyncHttpClientConfig.Builder()
    .setExecutorService(executorService)
    .setIOThreadMultiplier(1) // otherwise we might not have enough threads
    .setMaxConnectionsPerHost(maxConnectionsPerHost)
    .setAllowPoolingConnections(connectionPooling)
    .setAllowPoolingSslConnections(connectionPooling)
    .setConnectTimeout(connectionTimeoutInMs)
    .setRequestTimeout(requestTimeoutInMs)
    .setFollowRedirect(followRedirects).build
  private val asyncHttpClient = new AsyncHttpClient(config)
  private val httpClient = new Http(asyncHttpClient)

  private val scraper = new Scraper(httpClient, validSchemas)

  override def postStop() {
    httpClient.shutdown()
    executorService.shutdown()
  }

  def receive = {

    case message: ScrapeUrl => {
      val zender = sender()
      val fScrapedData = scraper.fetch(message)
      fScrapedData onComplete {
        case Success(data) => zender ! Right(data)
        case Failure(e) => logAndForwardErrorAsLeft(e, zender)
      }
    }

    case _ => log.error("Scraper Actor received an unexpected message :( !")
  }

  /**
   * Helper function that logs an error and forwards the throwable
   *
   * @param throwable Throwable
   * @param sendToRef Actor to send the message to
   */
  def logAndForwardErrorAsLeft(throwable: Throwable, sendToRef: ActorRef) {
    log.error(throwable.getMessage)
    sendToRef ! Left(throwable)
  }

}

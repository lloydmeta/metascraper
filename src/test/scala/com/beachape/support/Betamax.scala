package com.beachape.support

import co.freeside.betamax.TapeMode
import co.freeside.betamax.Recorder
import co.freeside.betamax.proxy.jetty.ProxyServer

// Shamelessly cargo-culted/stolen from http://www.scottlogic.com/blog/2013/07/18/betamax-in-scala.html
trait Betamax extends Wrapped {

  def betamax(tape: String, mode: Option[TapeMode] = None)(testFun: => Unit) = {
    val recorder = new Recorder
    val proxyServer = new ProxyServer(recorder)
    recorder.insertTape(tape)
    recorder.getTape.setMode(mode.getOrElse(recorder.getDefaultMode()))
    proxyServer.start()
    try {
      testFun
    } finally {
      recorder.ejectTape()
      proxyServer.stop()
    }
  }

}
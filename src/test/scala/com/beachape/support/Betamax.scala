package com.beachape.support

import co.freeside.betamax.TapeMode
import co.freeside.betamax.Recorder
import co.freeside.betamax.proxy.jetty.ProxyServer

/**
 * Trait that allows us to run a block of code under the "context" of a Betamax
 * recorded tape.
 *
 * Extends Wrapped to allow "_ using betamax("tapeName") { .. }" syntax
 */
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
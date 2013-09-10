package com.beachape.support

// Shamelessly cargo-culted/stolen from http://www.scottlogic.com/blog/2013/07/18/betamax-in-scala.html
trait Wrapped {
  implicit def wrapPartialFunction(f: (=> Unit) => Unit) = new wrapped(f)

  class wrapped(f: (=> Unit) => Unit) {
    def using(f1: => Unit) = f {
      f1
    }
  }
}

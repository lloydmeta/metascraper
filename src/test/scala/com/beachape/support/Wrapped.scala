package com.beachape.support

/**
 * Trait that allows us to "wrap" a multi-parameter list function that takes
 * a function-block as it's last argument
 *
 * By causing the original function to become a partially-applied function
 * via "_" then calling using, we can pass another function to be run "in the
 * context" of the original
 *
 * Shamelessly cargo-culted/stolen from http://www.scottlogic.com/blog/2013/07/18/betamax-in-scala.html
 */
trait Wrapped {
  implicit def wrapPartialFunction(f: (=> Unit) => Unit) = new wrapped(f)

  class wrapped(f: (=> Unit) => Unit) {
    def using(f1: => Unit) = f {
      f1
    }
  }
}

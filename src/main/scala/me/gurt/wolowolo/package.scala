package me.gurt

import cats.Monoid
import cats.data._
import me.gurt.wolowolo.dsl._
import me.gurt.wolowolo.plugins._

package object wolowolo {
  private[wolowolo] type Handler = ((Source, Target, Received)) => Option[Sendable]

  def handleFirst(hs: Iterable[Handler]): Handler =
    hs.map(_.unlift)
      .foldLeft(PartialFunction.empty[(Source, Target, Received), Sendable])(_ orElse _)
      .lift

  // catMaybe style
  val handleAll: Monoid[Handler] = new Monoid[Handler] {
    def empty: Handler = _ => None

    def combine(h1: Handler, h2: Handler): Handler =
      x => NonEmptyChain.fromSeq(Vector(h1, h2).flatMap(h => h(x))).map(identity[Sendable](_))
  }

  val handleFirst: Monoid[Handler] = new Monoid[Handler] {
    def empty: Handler = _ => None

    def combine(h1: Handler, h2: Handler): Handler =
      x => h1(x) orElse h2(x)
  }

  val allPlugins = Vector(
    classOf[Nyaa],
    classOf[Relay],
    classOf[Useless],
    classOf[VideoMeta],
  )

}

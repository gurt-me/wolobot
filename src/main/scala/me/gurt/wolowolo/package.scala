package me.gurt

import me.gurt.wolowolo.dsl._
import me.gurt.wolowolo.plugins._

package object wolowolo {
  private[wolowolo] type Handler = ((Source, Target, Received)) => Option[Sendable]

  // combine handlers catMaybe style
  def handleAll(hs: Iterable[Handler]): Handler =
    t => Some(IterSendable(hs.flatMap(_(t)))).filter(_.it.nonEmpty)

  def handleFirst(hs: Iterable[Handler]): Handler =
    hs.map(_.unlift)
      .foldLeft(PartialFunction.empty[(Source, Target, Received), Sendable])(_ orElse _)
      .lift

  val allPlugins = Vector(
    classOf[Nyaa],
    classOf[Relay],
    classOf[Useless],
    classOf[VideoMeta],
  )

}

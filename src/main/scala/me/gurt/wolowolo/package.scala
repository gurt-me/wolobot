package me.gurt

import me.gurt.wolowolo.dsl.Target.Channel
import me.gurt.wolowolo.dsl._

import scala.concurrent.Future

package object wolowolo {
  private[wolowolo] type Handler = ((Source, Target, Received)) => Option[Sendable]

  // combine handlers catMaybe style
  def handleAll(hs: Iterable[Handler]): Handler =
    t => Some(IterSendable(hs.flatMap(_(t)))).filter(_.it.nonEmpty)

  def handleFirst(hs: Iterable[Handler]): Handler =
    hs.map(_.unlift)
      .foldLeft(PartialFunction.empty[(Source, Target, Received), Sendable])(_ orElse _)
      .lift

}

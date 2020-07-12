package me.gurt

import cats._
import cats.data._
import cats.implicits._
import me.gurt.wolowolo.dsl._

import scala.language.implicitConversions

package object wolowolo {
  private[wolowolo] type Handler = ((Source, Target, Received)) => Option[Sendable]

  // handle first
  implicit val sendableSemigroupEvidence: Semigroup[Sendable] = (s1, _) => s1

  implicit private[wolowolo] val handleFirst: Monoid[Handler] = Monoid.instance(
    _ => None,
    (h1, h2) => x => Chain(h1, h2).map(h => h(x)).combineAll,
  )

  implicit def chainHandler2Handler[T[_]: Foldable](ch: T[Handler]): Handler =
    ch.combineAll

}

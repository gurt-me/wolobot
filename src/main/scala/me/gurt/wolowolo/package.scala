package me.gurt

import cats._
import cats.data._
import cats.implicits._
import me.gurt.wolowolo.dsl._

import scala.language.implicitConversions

package object wolowolo {
  private[wolowolo] type Handler = ((Source, Target, Received)) => Option[Sendable]

  // catMaybe style only used explicitly
  val handleAll: Monoid[Handler] = new Monoid[Handler] {
    def empty: Handler = _ => None

    def combine(h1: Handler, h2: Handler): Handler =
      x => NonEmptyChain.fromSeq(Vector(h1, h2).flatMap(h => h(x))).map(identity[Sendable](_))
  }

  // handle first
  implicit val sendableSemigroupEvidence: Semigroup[Sendable] = (s1, _) => s1

  implicit private[wolowolo] val handleFirst: Monoid[Handler] = new Monoid[Handler] {
    def empty: Handler = _ => None

    def combine(h1: Handler, h2: Handler): Handler =
      x => Chain(h1, h2).map(h => h(x)).combineAll
  }

  implicit def chainHandler2Handler[T[_]: Foldable](ch: T[Handler]): Handler =
    ch.combineAll

}

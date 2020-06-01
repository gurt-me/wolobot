package me.gurt.wolowolo

import me.gurt.wolowolo.dsl._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.language.implicitConversions

package object plugin {
  // for command:      input message trailing the command  vvvvvv
  private[plugin] type MessageResponder = (Source, Target, String) => Option[Sendable]
  // for pattern:      full input message that matched it  ^^^^^^
  // it's a happy coincidence that we can share types

  case class Simply(sr: String => Option[SimpleResponse]) extends AnyVal

  implicit def simpleResponder2MessageResponder(sr: Simply): MessageResponder = {
    case (_, t: Target, m: String) =>
      sr.sr(m) map {
        case ssr: SimpleStringResponse => t msg ssr.s
        case sfr: SimpleFutureResponse => sfr.f map (t msg _)
      }
  }

  sealed trait SimpleResponse                                extends Any
  implicit class SimpleStringResponse(val s: String)         extends AnyVal with SimpleResponse
  implicit class SimpleFutureResponse(val f: Future[String]) extends AnyVal with SimpleResponse

}

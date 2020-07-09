package me.gurt.wolowolo

import cats.effect.IO
import me.gurt.wolowolo.config.BaseConfig
import me.gurt.wolowolo.dsl._
import net.ceedubs.ficus.Ficus._

import scala.language.implicitConversions
import scala.util.matching.Regex
import scala.util.matching.Regex.MatchIterator

package object plugins {

  case class Resp[T](resp: (Source, Target, T) => Option[Sendable])

  object Resp {
    def apply[T](sr: T => Option[SimpleResponse]): Resp[T] =
      Resp(liftSimpleResponder(sr)(_, _, _))

    private def liftSimpleResponder[T](
        sr: T => Option[SimpleResponse],
    )(s: Source, t: Target, m: T): Option[Sendable] =
      sr(m) map {
        case ssr: SimpleStringResponse => t msg ssr.s
        case sfr: SimpleEffectResponse => sfr.f map (t msg _)
      }
  }

  sealed trait SimpleResponse                            extends Any
  implicit class SimpleStringResponse(val s: String)     extends AnyVal with SimpleResponse
  implicit class SimpleEffectResponse(val f: IO[String]) extends AnyVal with SimpleResponse

  object Hook extends BaseConfig {

    val prefixes = config.as[Set[String]]("command.prefixes")

    def command(cmd: String, h: Resp[String], aliases: String*): Handler = {
      case (s: Source, t: Target, PrivMsg(_, message))
          if message
            .split(" ", 2)
            .headOption
            .exists(prefixes.flatMap(p => (cmd +: aliases).map(p + _)).contains(_)) =>
        h.resp(s, t, message.split(" ", 2).lift(1) getOrElse "")

      case _ => None
    }

    def regex(regex: Regex, h: Resp[MatchIterator]): Handler = {
      case (s: Source, t: Target, PrivMsg(_, message)) =>
        val matches = regex.unanchored findAllIn message
        h.resp(s, t, matches)

      case _ => None
    }

  }
}

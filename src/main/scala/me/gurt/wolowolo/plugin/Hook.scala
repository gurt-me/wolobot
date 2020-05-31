package me.gurt.wolowolo.plugin

import me.gurt.wolowolo.Handler
import me.gurt.wolowolo.config.BaseConfig
import me.gurt.wolowolo.dsl._
import net.ceedubs.ficus.Ficus._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.language.implicitConversions
import scala.util.matching.Regex

object Hook extends BaseConfig {

  val prefixes = config.as[Set[String]]("command.prefixes")

  def command(cmd: String, h: MessageResponder, aliases: String*): Handler = {
    case (s: Source, t: Target, PrivMsg(_, message))
        if message
          .split(" ", 2)
          .headOption
          .exists(prefixes.flatMap(p => (cmd +: aliases).map(p + _)).contains(_)) =>
      h(s, t, message.split(" ", 2).lift(1) getOrElse "")

    case _ => None
  }

  def regex(regex: Regex, h: MessageResponder): Handler = {
    case (s: Source, t: Target, PrivMsg(_, message)) if regex matches message =>
      h(s, t, message)

    case _ => None
  }

  case class Simply(val sr: String => Option[SimpleResponse]) extends AnyVal

  implicit def simpleResponder2MessageResponder(sr: Simply): MessageResponder = {
    case (_, t: Target, m: String) =>
      sr.sr(m) map {
        case ssr: SimpleStringResponse => t msg ssr.s
        case sfr: SimpleFutureResponse => sfr.f map (t msg)
      }
  }

  sealed trait SimpleResponse                                extends Any
  implicit class SimpleStringResponse(val s: String)         extends AnyVal with SimpleResponse
  implicit class SimpleFutureResponse(val f: Future[String]) extends AnyVal with SimpleResponse
}

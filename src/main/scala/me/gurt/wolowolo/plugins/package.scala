package me.gurt.wolowolo

import cats.effect.IO
import me.gurt.wolowolo.config.BaseConfig
import me.gurt.wolowolo.dsl._
import net.ceedubs.ficus.Ficus._

import scala.language.implicitConversions
import scala.util.matching.Regex
import scala.util.matching.Regex.MatchIterator

package object plugins extends BaseConfig {

  val allPlugins = Vector(
    classOf[Nyaa],
    classOf[Relay],
    classOf[Sed],
    classOf[Useless],
    classOf[VideoMeta],
  )

  implicit class Resp[T](val resp: (Source, Target, T) => Option[Sendable]) extends AnyVal

  object Resp {
    def liftSimpleResponder[T](
        sr: T => Option[IO[String]],
    )(s: Source, t: Target, m: T): Option[Sendable] =
      sr(m).map(_.map(t.msg))
  }

  // tfw brain too small
  implicit def autoReply[T](simpleReplier: T => Option[IO[String]]): Resp[T] =
    Resp.liftSimpleResponder(simpleReplier) _

  implicit def autoReply2[T](simpleReplier: T => IO[String]): Resp[T] =
    autoReply(simpleReplier andThen (Some(_)))

  implicit def autoReply3[T](simpleReplier: T => Option[String]): Resp[T] =
    autoReply(simpleReplier.andThen(_.map(IO.pure)))

  implicit def autoReply4[T](simpleReplier: T => String): Resp[T] =
    autoReply3(simpleReplier andThen (Some(_)))

  /** Define a usage to have it shown when handler.resp returns None. */
  case class Command(names: Seq[String], handler: Resp[String], usage: Option[String])
  object Command {
    // just unrolling some default arguments
    def apply(name: String, handler: Resp[String]): Command =
      Command(Seq(name), handler, None)
    def apply(name: String, handler: Resp[String], usage: Option[String]): Command =
      Command(Seq(name), handler, usage)
    def apply(names: Seq[String], handler: Resp[String]): Command =
      Command(names, handler, None)
  }

  val prefixes = config.as[Set[String]]("command.prefixes")

  implicit def command2Handler(ch: Command): Handler = {
    case (s: Source, t: Target, PrivMsg(_, message)) =>
      val split = message.split(" ", 2)
      if (split.headOption.exists(prefixes.flatMap(p => (ch.names).map(p + _)).contains(_)))
        ch.handler
          .resp(s, t, split.lift(1) getOrElse "")
          .orElse(ch.usage.map(usageText => t.msg(s"Usage: ${split(0)} $usageText")))
      else None

    case _ => None
  }

  object Hook {

    def regex(regex: Regex, h: Resp[MatchIterator]): Handler = {
      case (s: Source, t: Target, PrivMsg(_, message)) =>
        val matches = regex.unanchored findAllIn message
        if (matches.isEmpty)
          None
        else
          h.resp(s, t, matches)

      case _ => None
    }

  }
}

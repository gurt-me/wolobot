package me.gurt.wolowolo

import cats.effect.IO
import me.gurt.wolowolo.config.BaseConfig
import me.gurt.wolowolo.dsl._
import net.ceedubs.ficus.Ficus._

import scala.language.implicitConversions
import scala.util.matching.Regex
import scala.util.matching.Regex.MatchIterator

package object plugins {

  val allPlugins = Vector(
    classOf[Nyaa],
    classOf[Relay],
    classOf[Useless],
    classOf[VideoMeta],
  )

  case class Resp[T](resp: (Source, Target, T) => Option[Sendable])

  object Resp {
    def liftSimpleResponder[T](
        sr: T => Option[IO[String]],
    )(s: Source, t: Target, m: T): Option[Sendable] =
      sr(m).map(_.map(t.msg))
  }

  // tfw brain too small
  implicit def autoReply[T](simpleReplier: T => Option[IO[String]]): Resp[T] =
    Resp(Resp.liftSimpleResponder(simpleReplier))

  implicit def autoReply2[T](simpleReplier: T => IO[String]): Resp[T] =
    autoReply(simpleReplier andThen (Some(_)))

  implicit def autoReply3[T](simpleReplier: T => Option[String]): Resp[T] =
    autoReply(simpleReplier.andThen(_.map(IO.pure)))

  implicit def autoReply4[T](simpleReplier: T => String): Resp[T] =
    autoReply3(simpleReplier andThen (Some(_)))

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
        if (matches.isEmpty)
          None
        else
          h.resp(s, t, matches)

      case _ => None
    }

  }
}

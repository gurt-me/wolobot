package me.gurt.wolowolo.plugin

import me.gurt.wolowolo.Handler
import me.gurt.wolowolo.config.BaseConfig
import me.gurt.wolowolo.dsl._
import net.ceedubs.ficus.Ficus._

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
    case (s: Source, t: Target, PrivMsg(_, message)) if regex.unanchored matches message =>
      h(s, t, message)

    case _ => None
  }

}

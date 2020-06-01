package me.gurt.wolowolo

import org.jibble.pircbot

import scala.concurrent.Future
import scala.language.implicitConversions

package object dsl {

  sealed trait Received extends Any
  sealed trait Sendable extends Any

  case class PrivMsg(target: Target, message: String)              extends Received with Sendable
  case class Notice(target: Target, notice: String)                extends Received with Sendable
  case class Action(target: Target, action: String)                extends Received with Sendable
  case class Ctcp(target: Target, payload: String)                 extends Sendable
  case class Mode(target: Target, payload: String)                 extends Received with Sendable
  case class Numeric(code: Int, response: String)                  extends Received
  case class Join(channel: Channel, key: Option[String] = None)    extends Received with Sendable
  case class Part(channel: Channel, reason: Option[String] = None) extends Received with Sendable
  case class Raw(line: String, skipQueue: Boolean = false)         extends Sendable
  implicit class FutureSendable(val future: Future[Sendable])      extends AnyVal with Sendable
  implicit class IterSendable(val it: Iterable[Sendable])          extends AnyVal with Sendable

  sealed trait Source                                           extends Any
  case object Server                                            extends Source
  case class UserInfo(nick: String, user: String, host: String) extends Source

  sealed trait Target extends Any {
    def target: String
    override def toString = target
  }
  case class Channel(target: String) extends AnyVal with Target
  case class User(target: String)    extends AnyVal with Target
  object Target {
    private val CHANNEL_PREFIXES = "#&+!"
    def apply(target: String): Target =
      if (CHANNEL_PREFIXES.contains(target.charAt(0))) Channel(target) else User(target)
  }
  implicit def pircBotUser2Target(user: pircbot.User) = Target(user.getNick)
  implicit def string2Target(s: String) = Target(s)

  implicit class TargetOps(val target: Target) extends AnyVal {
    // Sugar
    def msg    = PrivMsg(target, _)
    def me     = Action(target, _)
    def notice = Notice(target, _)
  }

}

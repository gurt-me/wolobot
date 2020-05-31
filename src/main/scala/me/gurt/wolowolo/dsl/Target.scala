package me.gurt.wolowolo.dsl

import org.jibble.pircbot

import scala.language.implicitConversions

sealed trait Target extends Any {
  def target: String
  override def toString = target

  // Sugar
  def msg    = PrivMsg(this, _)
  def me     = Action(this, _)
  def notice = Notice(this, _)
}

object Target {
  // we use this to pattern match on incoming messages
  case class Channel(target: String) extends AnyVal with Target
  case class User(target: String)    extends AnyVal with Target

  def apply(target: String): Target =
    if (CHANNEL_PREFIXES.contains(target.charAt(0))) Channel(target) else User(target)

  private val CHANNEL_PREFIXES = "#&+!"

  implicit def pircBotUser2Target(user: pircbot.User) = Target(user.getNick)
  implicit def string2Target(s: String)               = Target(s)

  // Sugar: $"string" = Target("string")
  /*
  implicit class TargetHelper(val sc: StringContext) extends AnyVal {
    def $(args: Any*): Target = {
      val strings     = sc.parts.iterator
      val expressions = args.iterator
      val buf         = new StringBuffer(strings.next)
      while (strings.hasNext) {
        buf append expressions.next
        buf append strings.next
      }
      Target(buf.toString)
    }
  } */
}

package me.gurt.wolowolo.plugins

import me.gurt.wolowolo.Handler
import me.gurt.wolowolo.dsl._
import org.jibble.pircbot.Colors.BOLD
import shapeless._

import scala.collection.immutable.{Queue, SortedMap}
import scala.util.matching.Regex

class Sed extends Plugin {
  import Sed._
  private var lines: LineStorage =
    SortedMap.empty(Ordering.comparatorToOrdering(String.CASE_INSENSITIVE_ORDER))

  private def unshiftLine(key: String, line: String): Unit =
    synchronized {
      lines = lines.updatedWith(key) {
        case Some(queue) => Some(queue.finPrepended(line))
        case None        => Some(Queue(line))
      }
    }

  private def findAndReplaceLine(
      key: String,
      search: String,
      replacement: String,
      flags: String,
  ): Option[(String, String)] = {
    val insensitiveFlag = if (flags.contains('i')) """(?i)""" else ""
    val regex           = s"$insensitiveFlag$search".r.unanchored
    val replaceWith: String => String => String = repl =>
      src =>
        if (flags.contains('g')) regex.replaceAllIn(src, repl)
        else regex.replaceFirstIn(src, repl)

    lines(key) find (regex.matches) map { line =>
      (replaceWith(replacement)(line), replaceWith(s"$BOLD$replacement$BOLD")(line))
    }
  }

  def handle(net: String): Handler = {
    case (UserInfo(nick, _, _), t @ Channel(chan), PrivMsg(_, msg)) =>
      // importing shapeless just for this
      // ahahah fuck you type erasure
      val (toStore, toSend) = Generic[(Option[String], Option[Sendable])].from(msg match {
        case sedMyself(_, search, replacement, flags) =>
          findAndReplaceLine(s"$net.$chan.$nick", search, replacement, Option(flags) getOrElse "")
            .map {
              case (toStore, toReply) => HList(Some(toStore), Some(t msg s"<$nick> $toReply"))
            } getOrElse HList(None, None)

        case sedOther(who, _, search, replacement, flags) =>
          findAndReplaceLine(s"$net.$chan.$who", search, replacement, Option(flags) getOrElse "")
            .map {
              case (_, toReply) => HList(None, Some(t msg s"$nick thinks $who meant: $toReply"))
            } getOrElse HList(None, None)

        case _ =>
          HList(Some(msg), None)
      })
      toStore.foreach(unshiftLine(s"$net.$chan.$nick", _))
      toSend
    case _ =>
      None
  }

}

object Sed {
  type LineStorage = SortedMap[String, Queue[String]]

  val sedMyself: Regex = """s([^a-zA-Z0-9 ])(.+?)\1(.*?)(?:\1(.*))?\s*""".r
  val sedOther: Regex =
    """([-\\a-zA-Z0-9_\[\]{}^`|]+)[:,] s([^a-zA-Z0-9 ])(.+?)\2(.*?)(?:\2(.*))?\s*""".r

  private val maxLinesToRemember = 4

  implicit class FiniteQueue[T](q: Queue[T]) {
    def finPrepended(t: T): Queue[T] = {
      q.prepended(t).take(maxLinesToRemember)
    }
  }

}

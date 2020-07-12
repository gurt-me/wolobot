package me.gurt.wolowolo.plugins

import me.gurt.wolowolo.Handler
import me.gurt.wolowolo.dsl._
import org.jibble.pircbot.Colors.BOLD
import shapeless._

import scala.collection.immutable.{Queue, SortedMap}
import scala.util.matching.Regex

class Sed extends Plugin {
  import Sed._

  private val ls = LineStorage(4)

  def handle(net: String): Handler = {
    case (UserInfo(nick, _, _), t @ Channel(chan), PrivMsg(_, msg)) =>
      // importing shapeless just for this
      // ahahah fuck you type erasure
      val (toStore, toSend) = Generic[(Option[String], Option[Sendable])].from(msg match {
        case sedMyself(_, search, replacement, flags) =>
          ls.findAndReplaceLine(
              s"$net.$chan.$nick",
              search,
              replacement,
              Option(flags) getOrElse "",
            )
            .map {
              case (toStore, toReply) => HList(Some(toStore), Some(t msg s"<$nick> $toReply"))
            } getOrElse HList(None, None)

        case sedOther(who, _, search, replacement, flags) =>
          ls.findAndReplaceLine(s"$net.$chan.$who", search, replacement, Option(flags) getOrElse "")
            .map {
              case (_, toReply) => HList(None, Some(t msg s"$nick thinks $who meant: $toReply"))
            } getOrElse HList(None, None)

        case _ =>
          HList(Some(msg), None)
      })
      toStore.foreach(ls.unshiftLine(s"$net.$chan.$nick", _))
      toSend
    case _ =>
      None
  }

}

object Sed {
  val sedMyself: Regex = """s([^a-zA-Z0-9 ])(.+?)\1(.*?)(?:\1(.*))?\s*""".r
  val sedOther: Regex =
    """([-\\a-zA-Z0-9_\[\]{}^`|]+)[:,] s([^a-zA-Z0-9 ])(.+?)\2(.*?)(?:\2(.*))?\s*""".r

  class LineStorage(maxLinesToRemember: Int) {
    private var lines: SortedMap[String, Queue[String]] =
      SortedMap.empty(Ordering.comparatorToOrdering(String.CASE_INSENSITIVE_ORDER))

    def unshiftLine(key: String, line: String): Unit =
      synchronized {
        lines = lines.updatedWith(key) {
          case Some(queue) => Some(queue.prepended(line).take(maxLinesToRemember))
          case None        => Some(Queue(line))
        }
      }

    def findAndReplaceLine(
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

      lines.get(key) flatMap (_.find(regex.matches)) map { line =>
        (replaceWith(replacement)(line), replaceWith(s"$BOLD$replacement$BOLD")(line))
      }
    }
  }

  object LineStorage {
    def apply(maxLinesToRemember: Int) =
      if (maxLinesToRemember < 1)
        throw new IllegalArgumentException("wtf bro how do you expect me to keep 0 or less lines??")
      else
        new LineStorage(maxLinesToRemember)
  }
}

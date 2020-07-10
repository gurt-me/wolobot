package me.gurt.wolowolo.plugins

import java.util.concurrent.TimeUnit.SECONDS

import cats.data.EitherT
import cats.effect.IO
import io.circe.generic.auto._
import io.circe.parser._
import me.gurt.wolowolo.{Handler, Plugin}

import scala.concurrent.duration.Duration
import scala.util.matching.Regex

class VideoMeta extends Plugin {
  import VideoMeta._

  val urlPattern: Regex =
    "https?://([\\w_-]+(?:(?:\\.[\\w_-]+)+))([\\w.,@?^=%&:/~+#-]*[\\w@?^=%&/~+#-])?".r

  def handle(networkName: String): Handler =
    Hook.regex(
      urlPattern,
      Resp { matches => matches.toSeq.headOption.map(lookupMetadata(_).map(_.toString)) },
    )

}

object VideoMeta {
  case class YtdlDump(
      extractor_key: String,
      title: String,
      duration: Double,
      uploader: Option[String],
      upload_date: String,
      view_count: Option[Int],
      like_count: Option[Int],
      dislike_count: Option[Int],
  ) {
    def length: Duration = Duration(duration, SECONDS)

    override def toString: String = {
      import org.jibble.pircbot.Colors._
      f"""[${extractor_key}] $BOLD$title$BOLD (${length.toMinutes}:${length.toSeconds % 60}%02d)
         |- ${uploader.map(up => s"$BOLD$up$BOLD").!} on $upload_date -
         | ${view_count.map(n => s"$n views").!}
         | ${like_count.map(n => s"$n likes").!}
         | ${dislike_count.map(n => s"$n dislikes").!}
         |""".stripMargin.split("\\s*\\n").mkString(" ")

    }
  }

  def lookupMetadata(url: String): IO[YtdlDump] = {
    import scala.sys.process._
    EitherT(IO {
      val outBuffer = new StringBuilder
      val errBuffer = new StringBuilder
      val rc = Seq("youtube-dl", "--skip-download", "--dump-json", url) ! ProcessLogger(
        outBuffer.addAll,
        errBuffer.addAll,
      )
      if (rc != 0) Left(new YtdlUnsupportedException(errBuffer.toString))
      else decode[YtdlDump](outBuffer.toString)
    }).foldF(IO.raiseError, IO.pure[YtdlDump])
  }

  implicit class OptionStrOps(os: Option[String]) {
    def `!` : String = os getOrElse ""
  }

  class YtdlUnsupportedException(message: String) extends UnsupportedOperationException(message)
}

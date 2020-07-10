package me.gurt.wolowolo.bot

import cats.Parallel
import cats.data.EitherT
import cats.effect.{ContextShift, IO}
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import me.gurt.wolowolo.Handler
import me.gurt.wolowolo.config.WoloBotConfig
import me.gurt.wolowolo.dsl._
import me.gurt.wolowolo.plugins.VideoMeta.YtdlUnsupportedException

import scala.concurrent.ExecutionContext

class WoloBotImpl(val connectionConfig: Config)
    extends WoloBot
    with WoloBotConfig
    with LazyLogging {
  var handler: Handler = _

  setVerbose(verbose)
  setAutoNickChange(true)

  setName(connectionSettings.nick)
  connectionSettings.user.foreach(setLogin)
  connectionSettings.realname.foreach(setVersion)

  def connect(): Unit = {
    require(handler != null, "handler not set")
    connect(
      connectionSettings.server,
      connectionSettings.port.getOrElse(6667),
      connectionSettings.password.orNull,
    )
  }

  override def onConnect(): Unit = {
    connectionSettings.umode.foreach(setMode(getNick, _))
    connectionSettings.perform.foreach(sendRawLineViaQueue)
    Thread.sleep(1000)
    connectionSettings.channels.foreach(joinChannel)
  }

  override def onDisconnect(): Unit = {
    Thread.sleep(10000)
    reconnect()
  }

  // Everything below this line is big cancer but I rather talk to a >10 year old java library than write actual code.
  override def onAction(
      sender: String,
      login: String,
      hostname: String,
      target: String,
      action: String,
  ): Unit = {
    val t = Target(target)
    handler(UserInfo(sender, login, hostname), resolveReply(sender, t), Action(t, action))
      .foreach(send)
  }

  override def onMessage(
      channel: String,
      sender: String,
      login: String,
      hostname: String,
      message: String,
  ): Unit =
    handler(UserInfo(sender, login, hostname), Channel(channel), PrivMsg(Channel(channel), message))
      .foreach(send)

  override def onNotice(
      sourceNick: String,
      sourceLogin: String,
      sourceHostname: String,
      target: String,
      notice: String,
  ): Unit = {
    val t = Target(target)
    handler(
      UserInfo(sourceNick, sourceLogin, sourceHostname),
      resolveReply(sourceNick, t),
      Notice(t, notice),
    )
  }

  override def onJoin(channel: String, sender: String, login: String, hostname: String): Unit =
    handler(UserInfo(sender, login, hostname), Channel(channel), Join(Channel(channel)))

  override def onPart(channel: String, sender: String, login: String, hostname: String): Unit =
    handler(UserInfo(sender, login, hostname), Channel(channel), Part(Channel(channel)))

  // XXX: nickchange quit topic all the modes

  override def onPrivateMessage(
      sender: String,
      login: String,
      hostname: String,
      message: String,
  ): Unit =
    handler(UserInfo(sender, login, hostname), User(sender), PrivMsg(User(getNick), message))
      .foreach(send)

  override def onServerResponse(code: Int, response: String): Unit =
    handler(Server, User(getNick), Numeric(code, response))

  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  def send(s: Sendable): Unit =
    IO.shift *> evalSendable(s) unsafeRunAsync {
      case Left(t: Throwable) => logger.warn("failed to execute effect", t)
      case Right(_: Unit)     => ()
    }

  def evalSendable(s: Sendable): IO[Unit] = {
    // Needed for IO.start to do a logical thread fork
    implicit val p: Parallel[IO] = IO.ioParallel(cs)
    import cats.implicits._
    s match {
      case PrivMsg(target, m) => IO { sendMessage(target.toString, m.take(450)) }
      case Notice(target, m)  => IO { sendNotice(target.toString, m.take(450)) }
      case Action(target, m)  => IO { sendAction(target.toString, m.take(450)) }
      case Ctcp(target, m)    => IO { sendCTCPCommand(target.toString, m.take(450)) }
      case Mode(target, m)    => IO { setMode(target.toString, m.take(450)) }
      case Join(channel, key) =>
        IO { key.fold(joinChannel(channel.toString))(joinChannel(channel.toString, _)) }
      case Part(channel, rs) =>
        IO { rs.fold(partChannel(channel.toString))(partChannel(channel.toString, _)) }
      case Raw(line, skipQueue) =>
        IO {
          if (skipQueue)
            sendRawLine(line)
          else sendRawLineViaQueue(line)
        }
      case is: IOSendable    => is.io.flatMap(evalSendable)
      case cs: ChainSendable => cs.nc.parTraverse_(evalSendable)
    }
  }

  def resolveReply(senderNick: String, target: Target): Target =
    target match {
      case User(_)    => Target(senderNick)
      case Channel(_) => target
    }
}

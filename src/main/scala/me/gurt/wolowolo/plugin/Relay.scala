package me.gurt.wolowolo.plugin

import com.typesafe.config.Config
import me.gurt.wolowolo._
import me.gurt.wolowolo.config.BaseConfig
import me.gurt.wolowolo.dsl.Target.Channel
import me.gurt.wolowolo.dsl._
import net.ceedubs.ficus.Ficus._

class Relay extends Plugin with BaseConfig {
  private val relayConfig = config.as[Config]("relay")
  // cbf write custom config readers for pairs so lets just make 2 parallel lists
  private val relay =
    relayConfig.as[Seq[String]]("nets") zip relayConfig.as[Seq[String]]("chans")

  //require(relay.map(_._1).forall(Main.bots.contains))

  def handle(fromNet: String): Handler = {

    case (UserInfo(nick, _, _), _, Action(Channel(c), message)) if relay contains (fromNet, c) =>
      relay.filter(_ != (fromNet, c)) foreach {
        case (toNet, toChan) =>
          Main.bots(toNet).send(PrivMsg(toChan, s"[$fromNet] * $nick $message"))
      }
      None

    case (UserInfo(nick, _, _), _, PrivMsg(Channel(c), message)) if relay contains (fromNet, c) =>
      relay.filter(_ != (fromNet, c)) foreach {
        case (toNet, toChan) =>
          Main.bots(toNet).send(PrivMsg(toChan, s"[$fromNet] <$nick> $message"))
      }
      None

    case _ => None
  }
}

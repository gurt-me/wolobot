package me.gurt.wolowolo

import cats.data._
import com.typesafe.config.Config
import me.gurt.wolowolo.bot.WoloBot
import me.gurt.wolowolo.config.BaseConfig
import me.gurt.wolowolo.dsl._
import me.gurt.wolowolo.plugins.allPlugins
import net.ceedubs.ficus.Ficus._

object Main extends BaseConfig {
  private val connections = config.as[Map[String, Config]]("connections")

  val bots: Map[String, WoloBot] = connections.view.mapValues(WoloBot(_)).toMap

  def main(args: Array[String]): Unit = {
    val disabled = config.getAs[Set[String]]("plugin.disabled") getOrElse Set.empty
    val plugins = allPlugins
      .filterNot(clazz => disabled(clazz.getSimpleName))
      .map(clazz => clazz.getDeclaredConstructor().newInstance())

    // attach handlers
    bots foreachEntry { (net, bot) =>
      bot.handler =
        x => NonEmptyChain.fromSeq(plugins.map(_.handle(net)).flatMap(h => h(x))).map(ChainSendable)
    }
    bots.values.foreach(_.connect())
  }
}

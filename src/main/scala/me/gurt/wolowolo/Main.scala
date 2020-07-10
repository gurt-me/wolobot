package me.gurt.wolowolo

import com.typesafe.config.Config
import me.gurt.wolowolo.bot.WoloBot
import me.gurt.wolowolo.config.BaseConfig
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
      bot.handler = handleAll.combineAll(plugins.map(_.handle(net)))
    }
    bots.values.foreach(_.connect())
  }
}

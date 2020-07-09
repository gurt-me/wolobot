package me.gurt.wolowolo

import com.typesafe.config.Config
import me.gurt.wolowolo.bot.WoloBot
import me.gurt.wolowolo.config.BaseConfig
import net.ceedubs.ficus.Ficus._
import org.clapper.classutil.ClassFinder

object Main extends BaseConfig {
  private val connections = config.as[Map[String, Config]]("connections")

  val bots: Map[String, WoloBot] = connections.view.mapValues(WoloBot(_)).toMap

  def main(args: Array[String]): Unit = {
    val disabled = config.getAs[Set[String]]("plugin.disabled") getOrElse Set.empty
    // make a sacrifice to evil reflection gods to enumerate all plugins
    val plugins = ClassFinder
      .concreteSubclasses(classOf[Plugin], ClassFinder().getClasses())
      .map(_.name)
      .filterNot(className => disabled("""^.*\.""".r.replaceFirstIn(className, "")))
      .map(Class.forName(_).getDeclaredConstructor().newInstance().asInstanceOf[Plugin])
      .toVector

    // attach handlers
    bots foreachEntry { (net, bot) =>
      bot.handler = handleAll(plugins.map(_.handle(net)))
    }
    bots.values.foreach(_.connect())
  }
}

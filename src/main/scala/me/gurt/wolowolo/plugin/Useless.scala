package me.gurt.wolowolo.plugin
import me.gurt.wolowolo.Handler
import me.gurt.wolowolo.plugin.Hook._

class Useless extends Plugin {

  def handle(net: String): Handler =
    Hook.command("bots", Simply { _ => Some("Reporting in!!! [Scala]") })

}

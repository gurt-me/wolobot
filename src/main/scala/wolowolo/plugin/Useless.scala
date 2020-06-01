package me.gurt.wolowolo.plugin

import me.gurt.wolowolo.Handler

class Useless extends Plugin {

  def handle(net: String): Handler =
    Hook.command("bots", Simply { _ => Some("Reporting in!!! [Scala]") })

}

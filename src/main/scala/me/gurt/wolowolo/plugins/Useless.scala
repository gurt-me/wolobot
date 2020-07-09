package me.gurt.wolowolo.plugins

import me.gurt.wolowolo.{Handler, Plugin}

class Useless extends Plugin {

  def handle(net: String): Handler =
    Hook.command("bots", Resp { _ => Some("Reporting in!!! [Scala]") })

}

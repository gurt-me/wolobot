package me.gurt.wolowolo.plugins

import cats.data.Chain
import me.gurt.wolowolo.{Handler, Plugin}

class Useless extends Plugin {

  def handle(net: String): Handler =
    Chain(
      Hook.command("bots", Resp { _ => Some("Reporting in!!! [Scala]") }),
      Hook.command(
        "source",
        Resp { _ => Some("Wolobot is a piece of shit see https://github.com/gurt-me/wolobot") },
      ),
    )

}

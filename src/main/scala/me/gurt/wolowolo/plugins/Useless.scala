package me.gurt.wolowolo.plugins

import cats.data.Chain
import me.gurt.wolowolo.{Handler, Plugin}

class Useless extends Plugin {

  def handle(net: String): Handler =
    Chain(
      Hook.command("bots", (_: String) => "Reporting in!!! [Scala]"),
      Hook.command(
        "source",
        (_: String) => "Wolobot is a piece of shit see https://github.com/gurt-me/wolobot",
      ),
    )

}

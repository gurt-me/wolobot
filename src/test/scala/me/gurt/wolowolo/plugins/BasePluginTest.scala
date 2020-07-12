package me.gurt.wolowolo.plugins

import me.gurt.wolowolo.dsl._

trait BasePluginTest {
  var plugin: Plugin = _

  def handlePrivMsg(
      nick: String,
      msg: String,
      channel: String = "#dummy",
      network: String = "dummy",
  ): Option[Sendable] =
    plugin.handle(network)(
      UserInfo(nick, "aaa", "bbb"),
      Channel(channel),
      PrivMsg(Channel(channel), msg),
    )
}

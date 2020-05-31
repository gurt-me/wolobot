package me.gurt.wolowolo.plugin

import me.gurt.wolowolo.Handler

trait Plugin {
  def handle(networkName: String): Handler
}

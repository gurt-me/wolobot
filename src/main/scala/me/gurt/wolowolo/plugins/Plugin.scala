package me.gurt.wolowolo.plugins

import me.gurt.wolowolo.Handler

trait Plugin {
  def handle(networkName: String): Handler
}

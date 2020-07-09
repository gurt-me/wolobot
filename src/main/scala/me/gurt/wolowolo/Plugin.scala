package me.gurt.wolowolo

trait Plugin {
  def handle(networkName: String): Handler
}

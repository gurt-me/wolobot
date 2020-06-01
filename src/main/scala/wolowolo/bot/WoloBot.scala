package me.gurt.wolowolo.bot

import com.typesafe.config.Config
import me.gurt.wolowolo.Handler
import me.gurt.wolowolo.dsl.Sendable
import org.jibble.pircbot.PircBot

trait WoloBot extends PircBot {
  def connect(): Unit
  var handler: Handler
  def send(s: Sendable): Unit
}

object WoloBot {
  def apply(connectionConfig: Config): WoloBot = new WoloBotImpl(connectionConfig)
}

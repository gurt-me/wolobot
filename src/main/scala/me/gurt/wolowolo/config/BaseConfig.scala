package me.gurt.wolowolo.config

import com.typesafe.config.ConfigFactory

trait BaseConfig {
  lazy val config = ConfigFactory.load()
}

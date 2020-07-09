package me.gurt.wolowolo.config

import com.typesafe.config.Config
import me.gurt.wolowolo.config.WoloBotConfig.ConnectionSettings
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._

trait WoloBotConfig extends BaseConfig {
  def connectionConfig: Config
  val connectionSettings: ConnectionSettings = connectionConfig.as[ConnectionSettings]

  val verbose = config.getAs[Boolean]("verbose") getOrElse false
}

object WoloBotConfig {

  case class ConnectionSettings(
      server: String,
      port: Option[Int],
      nick: String,
      user: Option[String],
      password: Option[String],
      realname: Option[String],
      channels: Option[String],
      umode: Option[String],
      perform: Seq[String],
      //owner: String, TODO
  )

}

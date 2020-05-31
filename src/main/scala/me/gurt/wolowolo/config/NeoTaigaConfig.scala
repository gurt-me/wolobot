package me.gurt.wolowolo.config

import com.typesafe.config.Config
import me.gurt.wolowolo.config.NeoTaigaConfig.ConnectionSettings
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._

trait NeoTaigaConfig extends BaseConfig {
  def connectionConfig: Config
  val connectionSettings: ConnectionSettings = connectionConfig.as[ConnectionSettings]

  val verbose = config.getAs[Boolean]("verbose") getOrElse false
}

object NeoTaigaConfig {

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

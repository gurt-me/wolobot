package me.gurt.wolowolo.plugins

import cats.data.Chain
import cats.effect.IO
import de.kaysubs.tracker.nyaasi.NyaaSiApi
import de.kaysubs.tracker.nyaasi.model.SearchRequest
import de.kaysubs.tracker.nyaasi.model.SearchRequest._
import me.gurt.wolowolo._
import org.jibble.pircbot.Colors.BOLD

import scala.util.matching.Regex

class Nyaa extends Plugin {
  import Nyaa._

  def handle(networkName: String): Handler =
    Chain(
      Hook.command("nyaa", nyaa),
      Hook.command("sukebei", sukebei),
      Hook.regex(
        movieCode,
        (matches: Regex.MatchIterator) => matches.toSeq.headOption.map(sukebei(_)),
      ),
    )

}

object Nyaa {
  val movieCode = "[A-Z]{3,5}-[0-9]{3}".r

  val nyaa: String => IO[String]    = search(NyaaSiApi.getNyaa, "https://nyaa.si")
  val sukebei: String => IO[String] = search(NyaaSiApi.getSukebei, "https://sukebei.nyaa.si")

  def search(api: NyaaSiApi, baseUrl: String)(term: String): IO[String] = {
    val searchRequest = new SearchRequest()
      .setOrdering(Ordering.DESCENDING)
      .setSortedBy(Sort.SEEDERS)
      .setTerm(term)
    IO {
      val topResult = api.search(searchRequest)(0)
      s"[${topResult.getCategory.getName}] $BOLD${topResult.getTitle}$BOLD " ++
        s"$baseUrl/view/${topResult.getId}"
    }
  }
}

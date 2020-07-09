package me.gurt.wolowolo.plugin

import de.kaysubs.tracker.nyaasi.NyaaSiApi
import de.kaysubs.tracker.nyaasi.model.SearchRequest
import de.kaysubs.tracker.nyaasi.model.SearchRequest._
import me.gurt.wolowolo.plugin.Nyaa._
import me.gurt.wolowolo._
import org.jibble.pircbot.Colors.BOLD

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class Nyaa extends Plugin {

  def handle(networkName: String): Handler =
    handleFirst(
      Seq(
        Hook.command("nyaa", Simply { args => Some(nyaa(args)) }),
        Hook.command("sukebei", Simply { args => Some(sukebei(args)) }),
        Hook.regex(movieCode, Simply { movieCode.findFirstIn(_).map(sukebei(_)) }),
      ),
    )

}

object Nyaa {

  val movieCode = "[A-Z]{3,4}-[0-9]{3}".r

  val nyaa: String => Future[String]    = search(NyaaSiApi.getNyaa, "https://nyaa.si")
  val sukebei: String => Future[String] = search(NyaaSiApi.getSukebei, "https://sukebei.nyaa.si")

  private def search(api: NyaaSiApi, baseUrl: String)(term: String): Future[String] = {
    val searchRequest = new SearchRequest()
      .setOrdering(Ordering.DESCENDING)
      .setSortedBy(Sort.SEEDERS)
      .setTerm(term)
    Future {
      val topResult = api.search(searchRequest)(0)
      s"[${topResult.getCategory.getName}] $BOLD${topResult.getTitle}$BOLD " ++
        s"$baseUrl/view/${topResult.getId}"
    }
  }

}

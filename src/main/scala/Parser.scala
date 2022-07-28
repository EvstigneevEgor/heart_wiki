import java.net.URLDecoder
import java.util.concurrent.Executors

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.xml._

object Parser extends App {
  implicit val exc =
    ExecutionContext.fromExecutor(Executors.newFixedThreadPool(12))
  implicit class futureUtils[T](future: Future[T]) {
    def await(duration: Duration): T = {
      Await.result(future, duration)
    }

  }
  private val urls: Set[String] = netParse(
    "https://ru.wikipedia.org/wiki/%D0%9F%D0%BE%D1%81%D1%91%D0%BB%D0%BE%D0%BA_%D0%93%D0%B0%D0%B7%D0%BE%D0%BF%D1%80%D0%BE%D0%B2%D0%BE%D0%B4%D0%B0"
  )
  urls.foreach(println)

  def urlsFromFile(filePath: String) = {
    val bufferedSource = scala.io.Source.fromFile(filePath)
    val subUrls = bufferedSource.getLines()
    mapUrlsWithCount(subUrls)
  }

  private def mapUrlsWithCount(subUrls: Iterator[String]) = {
    val future: Future[Iterator[Set[String]]] = Future.traverse(subUrls) {
      subUrls =>
        Future(netParse(s"https://ru.wikipedia.org/wiki/$subUrls"))
    }
    val function: List[Set[String]] => List[(String, Long)] = _.flatten.toList
      .foldLeft(Map((String, 0L)).empty) { (a, x) =>
        val count = a.getOrElse(x, 0) + 1
        a ++ Map(x -> count)
      }
      .toList

    val function2: List[Set[String]] => Future[List[(String, Long)]] = it => {
      val list = it.flatten
      Future.traverse(list) { url =>
        Future(url, list.count(_ == url))
      }
    }

    val urlsFromPages = future
      .await(Duration.Inf)
      .toList

  }

  def netParse(sUrl: String): Set[String] = {
    val pattern = "wiki/.+?((?=\")|(?=&amp))".r
    val urlDecoder: String => String = URLDecoder.decode(_, "UTF-8").trim
    pattern
      .findAllIn(getHtmlPageFromUrl(sUrl))
      .map(urlDecoder)
      .toSet
  }

  private def getHtmlPageFromUrl(sUrl: String): String = {
    val source = scala.io.Source
      .fromURL(sUrl)("UTF-8")
    val html = source
      .getLines()
      .mkString
    html
  }
}

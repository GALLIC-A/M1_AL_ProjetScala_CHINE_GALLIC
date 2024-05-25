import scalaj.http.Http
import scopt.OParser
import play.api.libs.json.{JsArray, Json}

case class Config(limit: Int = 10, keyword: String = "")

case class WikiPage(title: String, words: Int)

object Main extends App {
  parseArguments(args) match {
    case Some(config) => run(config)
    case _            => println("Unable to parse arguments")
  }

  def parseArguments(args: Array[String]): Option[Config] = {
    val builder = OParser.builder[Config]
    val parser = {
      import builder._
      OParser.sequence(
        programName("WikiStats"),
        head("WikiStats", "1.0"),
        opt[Int]('l', "limit")
          .withFallback(() => 10) // Valeur par défaut : 10
          .action((l, c) => c.copy(limit = l)),
        opt[String]('k', "keyword")
          .required()
          .withFallback(() => "Scala") // Valeur par défaut : "Scala"
          .action((k, c) => c.copy(keyword = k))
      )
    }

    OParser.parse(parser, args, Config())
  }

  def run(config: Config): Unit = {
    // println(config)

    val url = formatUrl(config.keyword, config.limit)
    getPages(url) match {
      case Left(errorCode) => println(s"Une erreur est survenue : $errorCode")
      case Right(body) =>
        val pages = parseJson(body)
        println(s"Nombre de pages trouvées : ${pages.length}")
        pages.foreach(page =>
          println(s"Titre : ${page.title}, Nombre de mots : ${page.words}")
        )
    }
  }

  def formatUrl(keyword: String, limit: Int): String = {
    s"https://en.wikipedia.org/w/api.php?action=query&format=json&prop=&sroffset=0&list=search&srsearch=${keyword}&srlimit=${limit}"
  }

  def getPages(url: String): Either[Int, String] = {
    val result = Http(url).asString
    if (result.code != 200) {
      Left(result.code)
    } else {
      Right(result.body)
    }
  }

  def parseJson(jsonString: String): Seq[WikiPage] = {
    val jsonDatas = Json.parse(jsonString)
    val searchResults = (jsonDatas \ "query" \ "search").as[JsArray]

    searchResults.value.map { result =>
      val title = (result \ "title").as[String]
      val words = (result \ "wordcount").as[Int]
      WikiPage(title, words)
    }
  }
}

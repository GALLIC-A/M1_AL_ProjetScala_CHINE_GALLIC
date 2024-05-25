import scalaj.http.Http
import scopt.OParser

case class Config(limit: Int = 10, keyword: String = "")

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
      case Right(body)     => println(body)
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
}

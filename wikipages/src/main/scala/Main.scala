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
          .action((k, c) => c.copy(keyword = k)),
      )
    }

    OParser.parse(parser, args, Config())
  }

  def run(config: Config): Unit = {
    println(config)
  }
}

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
        programName("WikiStats")
      )
    }

    OParser.parse(parser, args, Config())
  }

  def run(config: Config): Unit = {
    println(config)
  }
}

import org.scalatest.flatspec.AnyFlatSpec

class MainSpec extends AnyFlatSpec {
  "formatUrl" should "return the correct URL" in {
    val keyword = "scalatest"
    val limit = 5
    val expectedUrl =
      s"https://en.wikipedia.org/w/api.php?action=query&format=json&prop=&sroffset=0&list=search&srsearch=$keyword&srlimit=$limit"

    val actualUrl = Main.formatUrl(keyword, limit)

    assert(actualUrl == expectedUrl)
  }

  "parseJson" should "return a list of WikiPage objects" in {
    val fakeDatas =
      """{"query":{"search":[{"title":"Scala","wordcount":100},{"title":"Java","wordcount":200}]}}"""
    val expectedPages = Seq(
      WikiPage("Scala", 100),
      WikiPage("Java", 200)
    )

    val actualPages = Main.parseJson(fakeDatas)

    assert(actualPages == expectedPages)
  }
}

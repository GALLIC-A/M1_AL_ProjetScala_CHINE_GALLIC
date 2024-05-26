import MockHttpUtils.mock
import http.HttpUtils
import org.scalatest.flatspec.AnyFlatSpec
import org.mockito.scalatest.MockitoSugar
import scalaj.http.{HttpRequest, HttpResponse}
import org.mockito.ArgumentMatchersSugar._
import org.mockito.Mockito._

class MainSpec extends AnyFlatSpec {

  "Main.run" should "call the necessary methods and print correct output" in {
    val config = Config(limit = 10, keyword = "Scala")
    val httpUtils = MockHttpUtils

    val outContent = new java.io.ByteArrayOutputStream()
    Console.withOut(new java.io.PrintStream(outContent)) {
      Main.run(config, httpUtils)
    }

    val output = outContent.toString
    assert(output.contains("Nombre de pages trouvees : 2"))
    assert(output.contains("Titre : Scala, Nombre de mots : 100"))
    assert(output.contains("Titre : Java, Nombre de mots : 200"))
    assert(output.contains("Nombre total de mots : 300"))
    assert(output.contains("Nombre de mots moyen par page : 150"))
  }

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

  "totalWords" should "return 0 for an empty list of pages" in {
    val emptyPages = Seq.empty[WikiPage]

    val total = Main.totalWords(emptyPages)

    assert(total == 0)
  }

  it should "return the correct total number of words for a non-empty list of pages" in {
    val pages = Seq(
      WikiPage("Page 1", 100),
      WikiPage("Page 2", 150),
      WikiPage("Page 3", 200)
    )

    val expectedTotal = 450 // 100 + 200 + 150

    val total = Main.totalWords(pages)

    assert(total == expectedTotal)
  }

  "parseArguments" should "return None for unparsable arguments" in {
    val args = Array("--invalid-arg")
    val result = Main.parseArguments(args)
    assert(result.isEmpty)
  }

  it should "return Config with keyword and default limit for keyword-only argument" in {
    val testKeyword = "scala"
    val args = Array("-k", testKeyword)

    val result = Main.parseArguments(args)

    assert(result.contains(Config(keyword = testKeyword)))
  }

  it should "return Config with keyword and limit for keyword and limit arguments" in {
    val testKeyword = "scala"
    val testLimit = 5
    val args = Array("-k", testKeyword, "-l", testLimit.toString)

    val result = Main.parseArguments(args)

    assert(result.contains(Config(keyword = testKeyword, limit = testLimit)))
  }

  "getPages" should "return Right with body if response code is 200" in {
    val url =
      "https://en.wikipedia.org/w/api.php?action=query&format=json&prop=&sroffset=0&list=search&srsearch=Scala&srlimit=10"
    val result = Main.getPages(MockHttpUtils, url)
    result.contains(
      Right(
        """{"query":{"search":[{"title":"Scala","wordcount":100},{"title":"Java","wordcount":200}]}}"""
      )
    )
  }

  it should "return Left with code if response code is 404 for non-Wikipedia URLs" in {
    val url = "https://example.com/api"
    val result = Main.getPages(MockHttpUtils, url)
    assert(result.equals(Left(404)))
  }

  it should "return different results for different Wikipedia search queries" in {
    val urlScala =
      "https://en.wikipedia.org/w/api.php?action=query&format=json&prop=&sroffset=0&list=search&srsearch=Scala&srlimit=10"
    val resultScala = Main.getPages(MockHttpUtils, urlScala)
    /* SHOULD BE -> Scala et Java */
    resultScala.contains(
      Right(
        """{"query":{"search":[{"title":"Scala","wordcount":100},{"title":"Java","wordcount":200}]}}"""
      )
    )

    /* SHOULD BE -> Java et Kotlin */
    val urlJava =
      "https://en.wikipedia.org/w/api.php?action=query&format=json&prop=&sroffset=0&list=search&srsearch=Java&srlimit=10"
    val resultJava = Main.getPages(MockHttpUtils, urlJava)
    resultJava.contains(
      Right(
        """{"query":{"search":[{"title":"Java","wordcount":200},{"title":"Kotlin","wordcount":150}]}}"""
      )
    )

    /* SHOULD BE -> Python et Django */
    val urlPython =
      "https://en.wikipedia.org/w/api.php?action=query&format=json&prop=&sroffset=0&list=search&srsearch=Python&srlimit=10"
    val resultPython = Main.getPages(MockHttpUtils, urlPython)
    resultPython.contains(
      Right(
        """{"query":{"search":[{"title":"Python","wordcount":250},{"title":"Django","wordcount":180}]}}"""
      )
    )
  }
}

object MockHttpUtils extends HttpUtils with MockitoSugar {

  override def parse(url: String): HttpRequest = {
    val mockRequest = mock[HttpRequest]

    if (!url.contains("wikipedia.org")) {
      val mockHttpResponse = new HttpResponse[String](
        "Error",
        404,
        Map.empty[String, IndexedSeq[String]]
      )
      when(mockRequest.asString).thenReturn(mockHttpResponse)
    } else if (url.contains("srsearch=Scala")) {
      val mockHttpResponse = new HttpResponse[String](
        """{"query":{"search":[{"title":"Scala","wordcount":100},{"title":"Java","wordcount":200}]}}""",
        200,
        Map.empty[String, IndexedSeq[String]]
      )
      when(mockRequest.asString).thenReturn(mockHttpResponse)
    } else if (url.contains("srsearch=Java")) {
      val mockHttpResponse = new HttpResponse[String](
        """{"query":{"search":[{"title":"Java","wordcount":200},{"title":"Kotlin","wordcount":150}]}}""",
        200,
        Map.empty[String, IndexedSeq[String]]
      )
      when(mockRequest.asString).thenReturn(mockHttpResponse)
    } else if (url.contains("srsearch=Python")) {
      val mockHttpResponse = new HttpResponse[String](
        """{"query":{"search":[{"title":"Python","wordcount":250},{"title":"Django","wordcount":180}]}}""",
        200,
        Map.empty[String, IndexedSeq[String]]
      )
      when(mockRequest.asString).thenReturn(mockHttpResponse)
    }
    mockRequest
  }
}

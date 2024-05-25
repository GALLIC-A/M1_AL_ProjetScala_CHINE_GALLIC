package http

import scalaj.http.{Http, HttpRequest}

object HttpUtilsImpl extends HttpUtils {

  override def parse(url: String): HttpRequest = {
    Http(url)
  }

}

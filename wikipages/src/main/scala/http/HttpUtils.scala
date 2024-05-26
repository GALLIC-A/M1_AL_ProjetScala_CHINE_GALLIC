package http

import scalaj.http.HttpRequest

trait HttpUtils {
  def parse(url: String): HttpRequest
}

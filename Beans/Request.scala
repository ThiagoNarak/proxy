package Beans

class Request(val method: String, val host: String, val requisicao: String, var porta: Int) {

  def this() = this("", "", "", 0)


}
package Utils

import java.awt.Color

object Alerts {

  def print(text:String,cor:Color): Unit ={
    var prefixo = "LOG: "
    cor match {
      case Color.RED => println(prefixo+"\u001B[31m"+ text+ "\u001B[m")
      case Color.BLUE => println(prefixo+"\u001B[34m"+ text+ "\u001B[m")
      case Color.GREEN => println(prefixo+"\u001b[32m"+ text + "\u001b[m")
      case Color.YELLOW => println(prefixo+"\u001b[33m"+ text + "\u001b[m")
      case whoa => println(prefixo+text)
    }

  }
}

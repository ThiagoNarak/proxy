import java.awt.Color
import java.io.{BufferedReader, FileWriter, InputStream, InputStreamReader}
import java.net.{ServerSocket, Socket, SocketException, SocketTimeoutException, URLEncoder}
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.util
import java.util.concurrent.Executors

import Beans.Request
import Utils.{Alerts, ByteUtils}

class Server {

  def startServer(): Unit = {
    var serverSocketFactory = new ServerSocket(3000)
    for (i <- 1 to 1) {

      Alerts.print("**********************************", Color.GREEN)
      Alerts.print("-Aguardando requisição do browser-", Color.GREEN)
      Alerts.print("**********************************", Color.GREEN)

      var socket = serverSocketFactory.accept()
      Alerts.print("Requisição Recebida, encaminhando para Thread", Color.GREEN)
      val thread = new Thread {
        Alerts.print("Server : Thread iniciada", Color.GREEN)

        override def run {
          var bytes = new Array[Byte](4096)
          var servidor = socket
          var is = servidor.getInputStream.read(bytes);
          var req = getHost(bytes)
          if (req != null)
            if (req.method.contains("GET")) {
              Alerts.print("METODO GET THREAD", Color.RED)
              Alerts.print(s"Server: Conectando ao host ${req.host}", Color.YELLOW)


              var cliente = new Socket(req.host, 80)

              //              cliente.setSoTimeout(2000)
              Alerts.print("Server: escrevendo requisição", Color.GREEN)

              cliente.getOutputStream.write(bytes)
              //              cliente.setSoTimeout(2000)
              //              servidor.setSoTimeout(2000)

              Thread.sleep(400)
              try {


                while (true) {
                  var buffer = Array[Byte]()
                  var bytes2 = Array[Byte]()
                  Alerts.print("Cliente: lendo retorno ", Color.GREEN)
                  try {

                    Thread.sleep(300)
                    var contentLength = cliente.getInputStream.available()
                    var rodou  =false;
                    var saveContent=0
                    do {
                      println("rodou")
                      bytes2 = new Array[Byte](cliente.getInputStream.available())
                      cliente.getInputStream.read(bytes2)

                      if(rodou==false){
                        contentLength = Integer.parseInt(new String(bytes2).split("\n").apply(2).split(":").apply(1).trim)
                        Alerts.print(new String(bytes2).split("\n").apply(2),Color.GREEN)
                        Alerts.print("VALOR DO CONTENT LENGTH :   "+contentLength,Color.RED)
                        rodou = true
                        var string = new String(bytes2)
                        contentLength = contentLength + string.split("\r\n\r\n").apply(0).length
                        saveContent = contentLength
                      }
                      buffer = ByteUtils.concat(buffer, bytes2)

                      contentLength = contentLength - bytes2.length
                      println("contentlength == "+contentLength)
                    } while (contentLength > 0)



                    Alerts.print(""+saveContent,Color.YELLOW)


                    servidor.getOutputStream.write(buffer,0,saveContent)
                    Alerts.print("tamanho do buffer" + buffer.length, Color.RED)


                    Alerts.print("Server: encaminhando  retorno para o servidor ", Color.GREEN)

                    Thread.sleep(300)
                  }
                  catch {
                    case i: SocketTimeoutException => println("timeout")
                    case j: SocketException => Alerts.print("erro ao escrever no socket", Color.RED)

                  }
                  Thread.sleep(300)

                  try {

                    Alerts.print("Server: lendo retorno para o servidor", Color.GREEN)

                    servidor.getInputStream.read(bytes2)
                    for (i <- bytes2) print(i.toChar)
                    Alerts.print("Cliente: encaminhando retorno para o cliente", Color.GREEN)
                    cliente.getOutputStream.write(bytes2)
                    cliente.getOutputStream.flush()
                  } catch {
                    case i: SocketTimeoutException => println("timeout")
                    case j: SocketException => println("erro ao escrever no socket")

                  }

                }


              }
              catch {
                case e: IllegalThreadStateException => println("problema da thread encerrada")
                case i: SocketTimeoutException => println("tempo de expera expirado")
              }
              //              } while (!x.contains("\r\n"));
              Alerts.print("finalizando host", Color.RED)
              println("fim")

              servidor.close()
              cliente.close()
            }

            else if (req.method.contains("POST")) {
              Alerts.print("METODO POST THREAD", Color.RED)
              var cliente = new Socket(req.host.substring(0, req.host.length - 1), 443)
              var b1 = new Array[Byte](4096)

              cliente.getOutputStream.write(bytes)
              cliente.getInputStream.read(b1)
              servidor.getOutputStream.write(b1)
              servidor.getInputStream.read(b1)
              cliente.getOutputStream.write(b1)

            } else {

              Alerts.print("METODO CONNECT THREAD", Color.RED)
              Alerts.print("req HOst: " + req.host, Color.GREEN)
              var cliente = new Socket(req.host, 443)
              Alerts.print("realizando handshake", Color.GREEN)
              servidor.getOutputStream.write(URLEncoder.encode("HTTP/1.0 200 Connection established\r\n" +
                "Proxy-Agent: ProxyServer/1.0\r\n" +
                "\r\n", StandardCharsets.UTF_8.toString()).getBytes)
              var b1 = new Array[Byte](4096)


              var x = ""
              do {

                servidor.getInputStream.read(b1)
                Alerts.print("servidor leu inputstream", Color.GREEN)
                cliente.getOutputStream.write(b1)
                Alerts.print("cliente escreveu outputstream", Color.GREEN)
                cliente.getInputStream.read(b1)
                Alerts.print("cliente leu inputstream", Color.GREEN)
                servidor.getOutputStream.write(b1)
                Alerts.print("servidor escreve no Outputstream", Color.GREEN)

                x = new String(b1)
              } while (true)

              println("fim")

            }
        }
      }
      thread.start
      Thread.sleep(1000) // slow the loop down a bit


    }


  }

  def getHost(bytes: Array[Byte]): Request = {
    for (i <- bytes)
      print(i.toChar)

    var frase = new String(bytes)
    var frases = frase.split("\n")
    Alerts.print(frases.apply(0), Color.RED)
    var req = new Request()
    if (frases.apply(0).substring(0, 4).contains("GET")) {

      Alerts.print("Server Proxy: metodo detectado: GET", Color.GREEN)

      if (frases.apply(1).contains("www.")) {
        Alerts.print(frases.apply(1).substring(10), Color.GREEN)
        return new Request("GET", frases.apply(1).substring(10, frases.apply(1).length - 1), new String(frases.toString), 80)

      } else {
        Alerts.print(frases.apply(1).substring(6), Color.GREEN)

        return new Request("GET", frases.apply(1).substring(6, frases.apply(1).length - 1), new String(frases.toString), 80)

      }

    } else if (frases.apply(0).contains("CONNECT")) {
      Alerts.print("Server Proxy: metodo detectado: CONNECT", Color.GREEN)
      Alerts.print(frases.apply(0).split(" ").apply(1), Color.GREEN)
      Alerts.print(frases.apply(0), Color.GREEN)
      if (frases.apply(0).split(" ").apply(1).contains(":")) {
        var x = frases.apply(0).split(" ").apply(1).split(":")

        return new Request("CONNECT", x.apply(0), frases.apply(0), 443)
      } else {
        return new Request("CONNECT", frases.apply(0).split(" ").apply(1), frases.apply(0), 443)

      }
    } else {
      return null
    }


  }
}



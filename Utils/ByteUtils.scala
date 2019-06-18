package Utils

object ByteUtils {

  def concat(b1: Array[Byte], b2: Array[Byte]): Array[Byte] = {
    var breturt = new Array[Byte](b1.length + b2.length)
    var cont = 0;
    for (i <- 0 to b1.length + b2.length -1) {
      if (i < b1.length) {
        breturt.update(i, b1.apply(i))
      } else {
        breturt.update(i, b2.apply(cont))
        cont = cont + 1
      }

    }

    return breturt
  }
}

object test {
  def main(args: Array[String]): Unit = {
    val line ="    abcdef   sfsdf''  ' fdsthe the fsdffasdf   "
    println(line.trim().replaceAll(" +", " "))
    for(i <- 0 to line.size -3){
      val token = line.substring(i,i+3)
      println(token)
    }
  }
}

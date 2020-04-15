import java.io.IOException
import java.util._
import org.apache.hadoop.fs.Path
import org.apache.hadoop.conf._
import org.apache.hadoop.io._
import org.apache.hadoop.mapred._
import org.apache.hadoop.util._
import scala.io.Source

object KmerCounting {

  class Map extends MapReduceBase with Mapper[LongWritable, Text, Text, IntWritable] {
    private final val one = new IntWritable(1)
    private val word = new Text()

    @throws[IOException]
    def map(key: LongWritable, value: Text, output: OutputCollector[Text, IntWritable], reporter: Reporter) {
      val line: String = value.toString
      if (line.size<3) return
      for(i <- 0 to line.size -3){
        val token = line.substring(i,i+3)
        word.set(token)
        output.collect(word, one)
      }
    }
  }

  class Reduce extends MapReduceBase with Reducer[Text, IntWritable, Text, IntWritable] {
    @throws[IOException]
    def reduce(key: Text, values: Iterator[IntWritable], output: OutputCollector[Text, IntWritable], reporter: Reporter) {
      import scala.collection.JavaConversions._
      val sum = values.toList.reduce((valueOne, valueTwo) => new IntWritable(valueOne.get() + valueTwo.get()))
      output.collect(key, new IntWritable(sum.get()))
    }
  }

  @throws[Exception]
  def main(args: Array[String]) {
    try {
      val conf: JobConf = new JobConf(this.getClass)
      conf.setJobName("WordCountScala")
      conf.set("fs.file.impl", "com.conga.services.hadoop.patch.HADOOP_7682.WinLocalFileSystem")
      conf.setOutputKeyClass(classOf[Text])
      conf.setOutputValueClass(classOf[IntWritable])
      conf.setMapperClass(classOf[Map])
      conf.setCombinerClass(classOf[Reduce])
      conf.setReducerClass(classOf[Reduce])
      conf.setInputFormat(classOf[TextInputFormat])
      conf.setOutputFormat(classOf[TextOutputFormat[Text, IntWritable]])
      FileInputFormat.setInputPaths(conf, new Path("input/3mer"))
      FileOutputFormat.setOutputPath(conf, new Path("output/3mer/scala"))

      JobClient.runJob(conf)
    } catch {
      case _ => print("da thuc hien 3-mer cout")
    }

    class Count(val word:String,val count: Int)
    val wordCount = scala.collection.mutable.ArrayBuffer.empty[Count]
    val filename = "output/3mer/scala/part-00000"
    for (line <- Source.fromFile(filename).getLines) {
      val row = line.toString() split "\t"
      wordCount+= new Count(row(0),row(1).toInt)
    }
    wordCount.sortWith((a,b)=> a.count >b.count)
      .take(10).foreach(t => println(t.word," - ",t.count))

  }
}

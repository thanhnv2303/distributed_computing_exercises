import java.io.IOException
import java.util._

import org.apache.hadoop.fs.Path
import org.apache.hadoop.conf._
import org.apache.hadoop.io._
import org.apache.hadoop.mapred._
import org.apache.hadoop.mapreduce.Job
import org.apache.hadoop.util._

import scala.io.Source

object WordCount {

  class Map extends MapReduceBase with Mapper[LongWritable, Text, Text, IntWritable] {
    private final val one = new IntWritable(1)
    private val word = new Text()

    @throws[IOException]
    def map(key: LongWritable, value: Text, output: OutputCollector[Text, IntWritable], reporter: Reporter) {
//      val line: String = value.toString()
//        .trim().replaceAll(" +", " ")
//      line.split(" ").foreach { token =>
//        word.set(token)
//        output.collect(word, one)
//      }
      val itr = new StringTokenizer(value.toString)
      while (itr.hasMoreTokens()) {
        word.set(itr.nextToken())
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
      FileInputFormat.setInputPaths(conf, new Path("input/wordcount"))
      FileOutputFormat.setOutputPath(conf, new Path("output/wordcount/scala"))

      JobClient.runJob(conf)


    } catch {
      case _ => println("da thuc hien word cout")
    }

    class Count(val word:String,val count: Int)
    val wordCount = scala.collection.mutable.ArrayBuffer.empty[Count]
    val filename = "output/wordcount/scala/part-00000"
    for (line <- Source.fromFile(filename).getLines) {
      val row = line.toString() split "\t"
      wordCount+= new Count(row(0),row(1).toInt)
    }
    println("top 10 most frequently used words in the corpus")
    wordCount.sortWith((a,b)=> a.count >b.count)
      .take(10).foreach(t => println(t.word + " "+ t.count+" times"))

  }
}
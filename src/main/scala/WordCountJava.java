import java.io.IOException;
import java.util.*;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import javax.swing.*;
import java.io.File;  // Import the File class
import java.io.FileNotFoundException;  // Import this class to handle errors


public class WordCountJava {

    public static class TokenizerMapper
            extends Mapper<Object, Text, Text, IntWritable> {

        private final static IntWritable one = new IntWritable(1);
        private Text word = new Text();

        public void map(Object key, Text value, Context context
        ) throws IOException, InterruptedException {
            StringTokenizer itr = new StringTokenizer(value.toString());
            while (itr.hasMoreTokens()) {
                word.set(itr.nextToken());
                context.write(word, one);
            }
        }
    }

    public static class IntSumReducer
            extends Reducer<Text, IntWritable, Text, IntWritable> {
        private IntWritable result = new IntWritable();

        public void reduce(Text key, Iterable<IntWritable> values,
                           Context context
        ) throws IOException, InterruptedException {
            int sum = 0;
            for (IntWritable val : values) {
                sum += val.get();
            }
            result.set(sum);
            context.write(key, result);
        }
    }

    public static class Count implements Comparable {
        public String word;
        public int count;

        public Count(String word, int count) {
            this.word = word;
            this.count = count;
        }

        @Override
        public int compareTo(Object other) {
            if (other instanceof Count) {
                int otherCount = ((Count) other).count;
                return otherCount - this.count;
            }
            return 0;

        }


        @Override
        public String toString() {
            return " (word:" + word + " - " + count + " times)";
        }


    }

    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "word count");
        //This setup for window
        job.getConfiguration().set("fs.file.impl", "com.conga.services.hadoop.patch.HADOOP_7682.WinLocalFileSystem");
        //
        job.setJarByClass(WordCount.class);
        job.setMapperClass(TokenizerMapper.class);
        job.setCombinerClass(IntSumReducer.class);
        job.setReducerClass(IntSumReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);
        FileInputFormat.addInputPath(job, new Path("input/wordcount"));
        FileOutputFormat.setOutputPath(job, new Path("output/wordcount/java"));
        try {

            job.waitForCompletion(true);
        } catch (Exception e) {
            System.out.println(" WordCount existed !");
        }


        List<Count> listWordCounts = new ArrayList<Count>();
        try {
            File myObj = new File("output/wordcount/java/part-r-00000");
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                String[] temp = data.split("\t");
                listWordCounts.add(new Count(temp[0], Integer.parseInt(temp[1])));
            }
            myReader.close();
            Collections.sort(listWordCounts);
            System.out.println("Top 10 most frequently used words in the corpus");
            for (int i = 0; i < 10; i++) {
                System.out.println(listWordCounts.get(i));
            }

        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

    }
}
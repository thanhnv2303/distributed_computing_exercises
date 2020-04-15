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


public class KmerCountingJava {

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

    public static class Kmer implements Comparable {
        public String kmer;
        public int count;

        public Kmer(String kmer, int count) {
            this.kmer = kmer;
            this.count = count;
        }

        @Override
        public int compareTo(Object other) {
            if (other instanceof Kmer) {
                int otherCount = ((Kmer) other).count;
                return otherCount - this.count;
            }
            return 0;

        }


        @Override
        public String toString() {
            return " (word:" + kmer + " - " + count + " times)";
        }
    }

    public static class KmerMapper extends Mapper<Object, Text, Text, IntWritable> {

        private final static IntWritable one = new IntWritable(1);
        private Text word = new Text();

        public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
            String line = value.toString();
            if(line.length()>2){
                for (int i = 0; i <line.length()-3 ; i++) {
                    String token =line.substring(i,i+3);
                    word.set(token);
                    context.write(word,one);
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "Kmer count");
        //This setup for window
        job.getConfiguration().set("fs.file.impl", "com.conga.services.hadoop.patch.HADOOP_7682.WinLocalFileSystem");
        //
        job.setJarByClass(KmerCountingJava.class);
        job.setMapperClass(KmerCountingJava.KmerMapper.class);
        job.setCombinerClass(KmerCountingJava.IntSumReducer.class);
        job.setReducerClass(KmerCountingJava.IntSumReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);
        FileInputFormat.addInputPath(job, new Path("input/3mer"));
        FileOutputFormat.setOutputPath(job, new Path("output/3mer/java"));
        try {

           job.waitForCompletion(true);
            System.out.println(" cc");
        } catch (Exception e) {
            System.out.println(" WordCount existed !");
        }


        List<WordCountJava.Count> list3mer = new ArrayList<WordCountJava.Count>();
        try {
            File myObj = new File("output/3mer/java/part-r-00000");
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                String[] temp = data.split("\t");
                list3mer.add(new WordCountJava.Count(temp[0], Integer.parseInt(temp[1])));
            }
            myReader.close();
            Collections.sort(list3mer);
            System.out.println("Top 10 most frequently used words in the corpus");
            for (int i = 0; i < 10; i++) {
                System.out.println(list3mer.get(i));
            }

        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

    }
}

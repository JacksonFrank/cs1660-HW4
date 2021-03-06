import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class MaxTemperature {

  public static class MaxTempMapper
       extends Mapper<LongWritable, Text, Text, IntWritable>{

    private final static int MISSING = 9999;

    public void map(LongWritable key, Text value, Context context
                    ) throws IOException, InterruptedException {
      String line = value.toString();
      String year = line.substring(15, 19);
      int temperature;
      if (line.charAt(87) == '+') {
          temperature = Integer.parseInt(line.substring(88, 92));
      } else {
          temperature = Integer.parseInt(line.substring(87, 92));
      }
      String quality = line.substring(92, 93);
      if (temperature != MISSING && quality.matches("[01459]")) {
          context.write(new Text(year), new IntWritable(temperature));
      }
    }
  }

  public static class MaxTempReducer
       extends Reducer<Text,IntWritable,Text,IntWritable> {
    private IntWritable result = new IntWritable();

    public void reduce(Text key, Iterable<IntWritable> values,
                       Context context
                       ) throws IOException, InterruptedException {
      int max_val = Integer.MIN_VALUE;
      for (IntWritable val : values) {
          max_val = Math.max(max_val, val.get());
      }
      context.write(key, new IntWritable(max_val));
    }
  }

  public static void main(String[] args) throws Exception {
    if (args.length != 2) {
        System.err.println("Usage: MaxTemperature <input-path> <output-path.");
        System.exit(-1);
    }
    Job job = new Job();
    job.setJarByClass(MaxTemperature.class);
    job.setJobName("Max Temperature");

    FileInputFormat.addInputPath(job, new Path(args[0]));
    FileOutputFormat.setOutputPath(job, new Path(args[1]));

    job.setMapperClass(MaxTempMapper.class);
    job.setReducerClass(MaxTempReducer.class);

    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(IntWritable.class);

    System.exit(job.waitForCompletion(true) ? 0 : 1);
  }
}
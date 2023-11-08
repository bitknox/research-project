package edu.ucr.cs.bdlab.beastExamples;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.spark.SparkConf;
import org.apache.spark.sql.SparkSession;

import edu.ucr.cs.bdlab.beast.JavaSpatialSparkContext;

public class BeastJava {
  public static void main(String[] args) throws IOException {

    SparkConf conf = new SparkConf().setAppName("Beast Example");

    // Set Spark master to local if not already set
    if (!conf.contains("spark.master"))
      conf.setMaster("local[*]");
    BufferedWriter writer = new BufferedWriter(new FileWriter("results.txt"));
    SparkSession sparkSession = SparkSession.builder().config(conf).getOrCreate();
    JavaSpatialSparkContext sparkContext = new JavaSpatialSparkContext(sparkSession.sparkContext());

    String[] vectorSets = {"/data/test/ne_10m_admin_0_countries.zip"};
    String[] rasterSets = {"/data/test/glc2000"};
    BenchRunner runner = new BenchRunner();
    // Generate the combinations we want to run benchmarks for
    List<ABenchmark> benchmarks = new ArrayList<>();
    try {
      for (String vectorSet : vectorSets)
      for (String rasterSet : rasterSets
      ) {
        String vectorName = vectorSet.substring(vectorSet.lastIndexOf('/') + 1);
        String rasterName = rasterSet.substring(rasterSet.lastIndexOf('/') + 1);
        // Run the benchmark
        ABenchmark benchmark = new SingleBenchmark(vectorName+rasterName, vectorSet,rasterSet,sparkContext);
        benchmarks.add(benchmark);
      }
      benchmarks.forEach(benchmark -> benchmark.setup());
      List<BenchmarkResult> results = runner.runBenchmarks(benchmarks.toArray(new ABenchmark[benchmarks.size()]));
      
      
      results.forEach(r -> {
        try {
          writer.write(r.toString());
        } catch (IOException e) {
          System.out.println("Failed to write result " + r.toString()); 
          e.printStackTrace();
        }
      });
    } finally {
      writer.close();
      // Clean up Spark session
      sparkSession.stop();
    }
  }
}
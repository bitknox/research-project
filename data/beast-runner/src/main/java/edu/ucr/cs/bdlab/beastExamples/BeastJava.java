package edu.ucr.cs.bdlab.beastExamples;

/*
 * Copyright 2021 University of California, Riverside
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import edu.ucr.cs.bdlab.beast.JavaSpatialRDDHelper;
import edu.ucr.cs.bdlab.beast.JavaSpatialSparkContext;
import edu.ucr.cs.bdlab.beast.common.BeastOptions;
import edu.ucr.cs.bdlab.beast.geolite.IFeature;
import edu.ucr.cs.bdlab.beast.geolite.ITile;
import edu.ucr.cs.bdlab.raptor.RaptorJoinFeature;
import edu.ucr.cs.bdlab.raptor.RasterFileRDD;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.SparkSession;
import scala.Tuple2;
import scala.Tuple5;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BeastJava {
  public static void main(String[] args) throws IOException {

    SparkConf conf = new SparkConf().setAppName("Beast Example");

    // Set Spark master to local if not already set
    if (!conf.contains("spark.master"))
      conf.setMaster("local[*]");
    BufferedWriter writer = new BufferedWriter(new FileWriter("results.txt"));
    SparkSession sparkSession = SparkSession.builder().config(conf).getOrCreate();
    JavaSpatialSparkContext sparkContext = new JavaSpatialSparkContext(sparkSession.sparkContext());

    String[] vectorSets = {"/data/test/treecover", "/data/test/ne_10m_admin_0_countries.zip"};
    String[] rasterSets = {"/data/test/treecover", "/data/test/ne_10m_admin_0_countries.zip"};
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
      //write results somehow :)
    } finally {
      writer.close();
      // Clean up Spark session
      sparkSession.stop();
    }
  }
}
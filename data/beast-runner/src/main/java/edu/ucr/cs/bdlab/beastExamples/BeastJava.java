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

import java.util.Map;

public class BeastJava {
  public static void main(String[] args) {
    // Initialize Spark
    SparkConf conf = new SparkConf().setAppName("Beast Example");

    // Set Spark master to local if not already set
    if (!conf.contains("spark.master"))
      conf.setMaster("local[*]");

    // Create Spark session (for Dataframe API) and Spark context (for RDD API)
    SparkSession sparkSession = SparkSession.builder().config(conf).getOrCreate();
    JavaSpatialSparkContext sparkContext = new JavaSpatialSparkContext(sparkSession.sparkContext());

    try {
      // 1- Load raster and vector data
  
      JavaRDD<ITile<Float>> treecover = sparkContext.geoTiff("treecover");
      JavaRDD<IFeature> countries = sparkContext.shapefile("ne_10m_admin_0_countries.zip");

      // 2- Run the Raptor join operation
      JavaRDD<RaptorJoinFeature<Float>> join =
          JavaSpatialRDDHelper.<Float>raptorJoin(countries, treecover, new BeastOptions())
              .filter(v -> v.m() >= 0 && v.m() <= 100.0);
      // 3- Aggregate the result
      JavaPairRDD<String, Float> countries_treecover = join.mapToPair(v -> new Tuple2<>(v.feature(), v.m()))
          .reduceByKey(Float::sum)
          .mapToPair(fv -> {
            String name = fv._1.getAs("NAME");
            float treeCover = fv._2;
            return new Tuple2<>(name, treeCover);
          });
      // 4- Write the output
      System.out.println("State\tTreeCover");
      for (Map.Entry<String, Float> result : countries_treecover.collectAsMap().entrySet())
        System.out.printf("%s\t%r\n", result.getKey(), result.getValue());
    } finally {
      // Clean up Spark session
      sparkSession.stop();
    }
  }
}
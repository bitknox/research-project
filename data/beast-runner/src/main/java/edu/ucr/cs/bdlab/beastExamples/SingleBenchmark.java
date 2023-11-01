package edu.ucr.cs.bdlab.beastExamples;

import java.util.Map;

import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import edu.ucr.cs.bdlab.beast.JavaSpatialSparkContext;
import edu.ucr.cs.bdlab.beast.JavaSpatialRDDHelper;
import edu.ucr.cs.bdlab.beast.common.BeastOptions;
import edu.ucr.cs.bdlab.beast.geolite.IFeature;
import edu.ucr.cs.bdlab.beast.geolite.ITile;
import edu.ucr.cs.bdlab.raptor.RaptorJoinFeature;
import scala.Tuple2;

public class SingleBenchmark extends ABenchmark {


	public SingleBenchmark(String name, String vectorSet, String rasterSet, JavaSpatialSparkContext sparkContext) {
		super(name, vectorSet, rasterSet, sparkContext);
	}

  @Override
  public double applyAsDouble(int i) {

    JavaRDD<ITile<Integer>> treecover = sparkContext.geoTiff("/data/test/treecover");
      JavaRDD<IFeature> countries = sparkContext.shapefile("/data/test/ne_10m_admin_0_countries.zip");

      // 2- Run the Raptor join operation
      JavaRDD<RaptorJoinFeature<Integer>> join =
          JavaSpatialRDDHelper.<Integer>raptorJoin(countries, treecover, new BeastOptions());
      // 3- Aggregate the result
      JavaPairRDD<String, Float> countries_treecover = join.mapToPair(v -> new Tuple2<>(v.feature(), v.m()))
          .reduceByKey(Integer::sum)
          .mapToPair(fv -> {
            String name = fv._1.getAs("NAME");
            float treeCover = fv._2;
            return new Tuple2<>(name, treeCover);
          });
   return i * countries_treecover.count();
  }
	
}

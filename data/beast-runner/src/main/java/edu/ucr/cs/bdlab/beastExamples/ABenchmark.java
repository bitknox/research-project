package edu.ucr.cs.bdlab.beastExamples;

import edu.ucr.cs.bdlab.beast.JavaSpatialSparkContext;

public abstract class ABenchmark extends Benchmarkable {
	
	JavaSpatialSparkContext sparkContext;
	String vectorSet;
	String rasterSet;
	String name;
	public ABenchmark(String name, String vectorSet, String rasterSet, JavaSpatialSparkContext sparkContext) {
		this.sparkContext = sparkContext;
		this.rasterSet = rasterSet;
		this.vectorSet = vectorSet;
		this.name = name;
	}
}

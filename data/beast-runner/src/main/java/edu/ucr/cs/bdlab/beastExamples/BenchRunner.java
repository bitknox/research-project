package edu.ucr.cs.bdlab.beastExamples;

import java.util.ArrayList;
import java.util.List;

public class BenchRunner {

	List<BenchmarkResult> results = new ArrayList<BenchmarkResult>();

	public List<BenchmarkResult> runBenchmarks(ABenchmark[] benchmarks) {
		for (ABenchmark benchmark : benchmarks) {
			benchmark.setup();
			System.out.println("Running benchmark " + benchmark.name);
			BenchmarkResult benchResult = BenchmarkUtil.Mark8(benchmark.name, "info", benchmark, 1, 1 * 60);
			System.out.println(benchResult.toString());
			results.add(benchResult);
		}
		return results;
	}
}

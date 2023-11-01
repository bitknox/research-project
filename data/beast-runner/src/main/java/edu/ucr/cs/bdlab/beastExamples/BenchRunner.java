package edu.ucr.cs.bdlab.beastExamples;

import java.util.ArrayList;
import java.util.List;

public class BenchRunner {

	List<BenchmarkResult> results = new ArrayList<BenchmarkResult>();

	public List<BenchmarkResult> runBenchmarks(ABenchmark[] benchmarks) {
		for (ABenchmark benchmark : benchmarks) {
			benchmark.setup();

			double benchResult = BenchmarkUtil.Mark7(benchmark.name, benchmark);
			results.add(new BenchmarkResult(benchmark.name, benchResult));
		}
		return results;
	}
}

package edu.ucr.cs.bdlab.beastExamples;

public class BenchmarkResult {
	public String name;
	public double time;
	public BenchmarkResult(String name, double time) {
		this.name = name;
		this.time = time;
	}
	public String toString() {
		return String.format("%s: %f ms", name, time);
	}
}

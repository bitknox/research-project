package dk.itu.raven.visualizer;

import java.awt.Color;

public class VisualizerOptions {
	public String outputPath, outputFormat;
	public boolean useRandomColor, useOutput;
	public Color color;

	public VisualizerOptions() {
		this.color = Color.black;
		this.outputPath = "./output.tif";
		this.outputFormat = "tif";
		this.useRandomColor = false;
		this.useOutput = true;
	}

	public VisualizerOptions(String outputPath, boolean useRandomColor, boolean useOutput, Color color) {
		this.color = color;
		this.outputPath = outputPath;
		this.outputFormat = outputPath.substring(outputPath.lastIndexOf('.') + 1);
		this.useRandomColor = useRandomColor;
		this.useOutput = useOutput;
	}

	public VisualizerOptions(String outputPath, boolean useRandomColor, boolean useOutput) {
		this.color = Color.black;
		this.outputPath = outputPath;
		this.outputFormat = outputPath.substring(outputPath.lastIndexOf('.') + 1);
		this.useRandomColor = useRandomColor;
		this.useOutput = useOutput;
	}

}

package dk.itu.raven.io;

import java.io.IOException;

import dk.itu.raven.util.Pair;
import dk.itu.raven.util.matrix.Matrix;
import dk.itu.raven.util.matrix.RandomMatrix;

public class GeneratorRasterReader extends RasterReader {
	private int width;
	private int height;
	private int maxValue;
	private long seed;
	private TFWFormat transform;

	public GeneratorRasterReader(int width, int height, long seed, int maxValue, TFWFormat transform) {
		this.width = width;
		this.height = height;
		this.transform = transform;
		this.maxValue = maxValue;
	}

	@Override
	public Pair<Matrix, TFWFormat> readRasters() throws IOException {
		Matrix randomMatrix = new RandomMatrix(seed, width, height, maxValue);
		return new Pair<Matrix, TFWFormat>(randomMatrix, transform);
	}
}

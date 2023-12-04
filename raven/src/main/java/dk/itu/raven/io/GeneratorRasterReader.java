package dk.itu.raven.io;

import java.io.IOException;

import com.github.davidmoten.rtree2.geometry.Rectangle;

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
	public Matrix readRasters(Rectangle rect) throws IOException {
		Matrix randomMatrix = new RandomMatrix(seed, width, height, maxValue);
		return randomMatrix;
	}

	@Override
	public TFWFormat getTransform() throws IOException {
		return transform;
	}
}

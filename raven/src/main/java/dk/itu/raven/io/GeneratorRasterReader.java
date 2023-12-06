package dk.itu.raven.io;

import java.io.IOException;

import com.github.davidmoten.rtree2.geometry.Rectangle;

import dk.itu.raven.util.Pair;
import dk.itu.raven.util.matrix.ArrayMatrix;
import dk.itu.raven.util.matrix.Matrix;
import dk.itu.raven.util.matrix.RandomMatrix;

public class GeneratorRasterReader implements RasterReader {
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
		int width = (int) rect.x2() - (int) rect.x1();
		int height = (int) rect.y2() - (int) rect.y1();

		int[][] m = new int[width][height];

		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				m[i][j] = randomMatrix.get(i, j);
			}
		}
		return new ArrayMatrix(m, width, height);

		// return randomMatrix;
	}

	@Override
	public TFWFormat getTransform() throws IOException {
		return transform;
	}
}

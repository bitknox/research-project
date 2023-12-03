package dk.itu.raven.io;

import java.io.IOException;

import dk.itu.raven.util.Pair;
import dk.itu.raven.util.matrix.Matrix;

public abstract class RasterReader {

	public RasterReader() {

	}

	public abstract Pair<Matrix, TFWFormat> readRasters() throws IOException;

}

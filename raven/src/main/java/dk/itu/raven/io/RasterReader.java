package dk.itu.raven.io;

import java.io.IOException;

import com.github.davidmoten.rtree2.geometry.Rectangle;

import dk.itu.raven.util.Pair;
import dk.itu.raven.util.matrix.Matrix;

public abstract class RasterReader {

	public RasterReader() {

	}

	public abstract Matrix readRasters(Rectangle rect) throws IOException;

	public abstract TFWFormat getTransform() throws IOException;

}

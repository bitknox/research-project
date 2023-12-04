package dk.itu.raven.io;

import java.io.File;
import java.io.IOException;

import com.github.davidmoten.rtree2.geometry.Rectangle;

import dk.itu.raven.util.Pair;
import dk.itu.raven.util.matrix.RastersMatrix;
import dk.itu.raven.util.matrix.Matrix;
import mil.nga.tiff.FileDirectory;
import mil.nga.tiff.Rasters;
import mil.nga.tiff.TIFFImage;
import mil.nga.tiff.TiffReader;
import mil.nga.tiff.ImageWindow;

public class FileRasterReader extends RasterReader {

	File tiff;
	File tfw;

	TFWFormat transform;

	public FileRasterReader(File directory) throws IOException {
		for (File file : directory.listFiles()) {

			if (file.getName().endsWith(".tif") ||
					file.getName().endsWith(".tiff")) {
				tiff = file;
			}
			if (file.getName().endsWith(".tfw")) {
				tfw = file;
			}
		}
		if (tiff == null || tfw == null) {
			throw new IOException("Missing tiff or tfw file");
		}
	}

	@Override
	public Matrix readRasters(Rectangle rect) throws IOException {
		TIFFImage image = TiffReader.readTiff(tiff);
		FileDirectory directory = image.getFileDirectory();
		ImageWindow window = new ImageWindow((int) rect.x1(), (int) rect.y1(), (int) Math.ceil(rect.x2()),
				(int) Math.ceil(rect.y2()));
		Rasters rasters = directory.readRasters(window);
		Matrix matrix = new RastersMatrix(rasters);

		return matrix;
	}

	public TFWFormat getTransform() throws IOException {
		if (transform == null) {
			transform = TFWFormat.read(tfw);
		}
		return transform;
	}
}

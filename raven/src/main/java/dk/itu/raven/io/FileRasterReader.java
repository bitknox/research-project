package dk.itu.raven.io;

import java.io.File;
import java.io.IOException;
import dk.itu.raven.util.Pair;
import dk.itu.raven.util.matrix.RastersMatrix;
import dk.itu.raven.util.matrix.Matrix;
import mil.nga.tiff.FileDirectory;
import mil.nga.tiff.Rasters;
import mil.nga.tiff.TIFFImage;
import mil.nga.tiff.TiffReader;

public class FileRasterReader extends RasterReader {

	File tiff;
	File tfw;

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
	public Pair<Matrix, TFWFormat> readRasters() throws IOException {
		TIFFImage image = TiffReader.readTiff(tiff);
		FileDirectory directory = image.getFileDirectory();
		Rasters rasters = directory.readRasters();
		Matrix matrix = new RastersMatrix(rasters);
		TFWFormat tfwFormat = TFWFormat.read(tfw);

		return new Pair<>(matrix, tfwFormat);
	}

}

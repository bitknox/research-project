package dk.itu.raven.io;

import java.io.File;
import java.io.IOException;

public abstract class FileRasterReader implements RasterReader {
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

	public TFWFormat getTransform() throws IOException {
		if (transform == null) {
			transform = TFWFormat.read(tfw);
		}
		return transform;
	}
}

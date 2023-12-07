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

public class MilRasterReader extends FileRasterReader {

	public MilRasterReader(File directory) throws IOException {
		super(directory);
	}

	@Override
	public Matrix readRasters(Rectangle rect) throws IOException {
		TIFFImage image = TiffReader.readTiff(tiff);
		FileDirectory directory = image.getFileDirectory();
		int imageWidth = directory.getImageWidth().intValue();
		int imageHeight = directory.getImageHeight().intValue();
		Rasters rasters;

		ImageWindow window = new ImageWindow((int) rect.x1(), (int) rect.y1(), (int) Math.ceil(rect.x2()),
				(int) Math.ceil(rect.y2()));

		if (window.getMaxX() - window.getMinX() > imageWidth || window.getMaxY() - window.getMinY() > imageHeight) {
			rasters = directory.readRasters();
		} else {
			rasters = directory.readRasters(window);
		}

		Matrix matrix = new RastersMatrix(rasters);

		return matrix;
	}

}

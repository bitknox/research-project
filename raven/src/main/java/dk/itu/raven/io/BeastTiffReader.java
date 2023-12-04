package dk.itu.raven.io;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.conf.Configuration;

import java.io.IOException;

import edu.ucr.cs.bdlab.beast.io.tiff.*;

class BeastTiffReader {

	public BeastTiffReader(String path) throws IOException {
		Path pt = new Path(path);
		FileSystem fs = pt.getFileSystem(new Configuration());
		ITiffReader reader = ITiffReader.openFile(fs, pt);
		int layers = reader.getNumLayers();
		TiffRaster tiffRaster = new TiffRaster(reader, 0);
		long pixel = tiffRaster.getPixel(0, 0);
	}
}
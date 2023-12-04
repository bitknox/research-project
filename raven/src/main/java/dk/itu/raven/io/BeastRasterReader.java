package dk.itu.raven.io;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import com.github.davidmoten.rtree2.geometry.Rectangle;

import dk.itu.raven.util.matrix.BeastMatrix;
import dk.itu.raven.util.matrix.Matrix;

import org.apache.hadoop.conf.Configuration;

import java.io.File;
import java.io.IOException;

import edu.ucr.cs.bdlab.beast.io.tiff.*;

public class BeastRasterReader extends FileRasterReader {


	public BeastRasterReader(File directory) throws IOException {
		super(directory);
	}
	
	@Override
	public Matrix readRasters(Rectangle rect) throws IOException {
		// TODO Auto-generated method stub
		Path pt = new Path(tiff.getAbsolutePath());
		FileSystem fs = pt.getFileSystem(new Configuration());
		ITiffReader reader = ITiffReader.openFile(fs, pt);
		TiffRaster tiffRaster = new TiffRaster(reader, 0);
		return new BeastMatrix(tiffRaster, rect);
	}
}
package dk.itu.raven.io;

import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.fs.Path;
import org.locationtech.jts.geom.Geometry;

import dk.itu.raven.geometry.Polygon;
import edu.ucr.cs.bdlab.beast.common.BeastOptions;
import edu.ucr.cs.bdlab.beast.geolite.IFeature;
import edu.ucr.cs.bdlab.beast.io.shapefile.ShapefileFeatureReader;

public class ShapfileReader {

	private TFWFormat transform;

	public ShapfileReader(TFWFormat transform) {
		this.transform = transform;
	}

	public Iterable<com.github.davidmoten.rtree2.geometry.Geometry> readShapefile(String path) {
		List<com.github.davidmoten.rtree2.geometry.Geometry> features = new ArrayList<>();
		try (ShapefileFeatureReader featureReader = new ShapefileFeatureReader()) {
			featureReader.initialize(new Path(
					path),
					new BeastOptions());
			for (IFeature feature : featureReader) {
				extractGeometries(feature.getGeometry(), features);
			}
			return features;
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
			return null;
		}
	}

	private void extractGeometries(Geometry geometry, List<com.github.davidmoten.rtree2.geometry.Geometry> features) {
		if (geometry.getNumGeometries() > 1) {
			for (int i = 0; i < geometry.getNumGeometries(); i++) {
				Geometry geom = geometry.getGeometryN(i);
				extractGeometries(geom, features);
			}
		} else {
			Polygon poly = new Polygon(geometry.getCoordinates(), transform);
			features.add(poly);
		}
	}
}

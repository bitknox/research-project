package dk.itu.raven.io;

import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.fs.Path;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Coordinate;

import com.github.davidmoten.rtree2.geometry.Point;
import com.github.davidmoten.rtree2.geometry.Geometries;

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
			createPolygons(geometry.getCoordinates(), features);
		}
	}

	private void createPolygons(Coordinate[] coordinates, List<com.github.davidmoten.rtree2.geometry.Geometry> features) {
		List<Point> points = new ArrayList<>();
		double minx = Double.MAX_VALUE, miny = Double.MAX_VALUE;
		double maxx = Double.MIN_VALUE, maxy = Double.MIN_VALUE;
		Coordinate start = coordinates[0];
		Point p;
		for (int i = 0; i < coordinates.length; i++) {
			Coordinate coord = coordinates[i];
			if (start.x == coord.x && start.y == coord.y && points.size() > 0) {
				features.add(new Polygon(points, Geometries.rectangle(minx, miny, maxx, maxy)));
				points = new ArrayList<>();
				minx = Double.MAX_VALUE;
				miny = Double.MAX_VALUE;
				maxx = Double.MIN_VALUE;
				maxy = Double.MIN_VALUE;
				if (i + 1 < coordinates.length) {
					start = coordinates[i + 1];
				}
			} else {
				p = transform.transFromCoordinateToPixel(coord.x, coord.y);
				minx = Math.min(minx, p.x());
				maxx = Math.max(maxx, p.x());
				miny = Math.min(miny, p.y());
				maxy = Math.max(maxy, p.y());
				points.add(p);
			}
		}
	}
}

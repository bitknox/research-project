package dk.itu.raven.join;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Test;

import com.github.davidmoten.rtree2.geometry.Geometries;
import com.github.davidmoten.rtree2.geometry.Point;

import dk.itu.raven.util.PixelRange;
import dk.itu.raven.util.Polygon;

public class RavenJoinTest {
    @Test
	public void testExtractCellsPolygon() {
        List<Point> points = new ArrayList<>();
		points.add(Geometries.point(0, 0));
		points.add(Geometries.point(10, 10));
		points.add(Geometries.point(20, 0));
		points.add(Geometries.point(20, 30));
		points.add(Geometries.point(10, 20));
		points.add(Geometries.point(0, 30));
		Polygon poly = new Polygon(points);

		Square square = new Square(0, 0, 30);
		RavenJoin join = new RavenJoin(null, null);
		Collection<PixelRange> ranges = join.ExtractCellsPolygon(poly, 0, square);
        assertTrue(ranges.stream().anyMatch(pr -> pr.row == 2 && pr.x1 == 0 && pr.x2 == 2));
        assertFalse(ranges.stream().anyMatch(pr -> pr.row == 2 && pr.x1 == 2));
        assertTrue(ranges.stream().anyMatch(pr -> pr.row == 3 && pr.x1 == 0 && pr.x2 == 3));
        assertTrue(ranges.stream().anyMatch(pr -> pr.row == 2 && pr.x1 == 18 && pr.x2 == 20));
	}
    
    @Test
    public void testExtractCellsPolygonWithLine() {
        List<Point> points = new ArrayList<>();
		points.add(Geometries.point(0, 0));
		points.add(Geometries.point(10, 10));
        Polygon poly = new Polygon(points);
        Square square = new Square(0, 0, 11);
		RavenJoin join = new RavenJoin(null, null);
        Collection<PixelRange> ranges = join.ExtractCellsPolygon(poly, 0, square);

        assertTrue(ranges.size() == 11);
        for (int i = 0; i <= 10; i++) {
            final int j = i;
            assertTrue(ranges.stream().anyMatch(pr -> pr.row == j && pr.x1 == j && pr.x2 == j));
        }
        assertFalse(ranges.stream().anyMatch(pr -> pr.row == 10 && pr.x1 == 10 && pr.x2 == 9));
    }
}

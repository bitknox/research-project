package dk.itu.raven.join;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.HashSet;
import java.util.Random;

import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.junit.jupiter.api.RepeatedTest;

import com.github.davidmoten.rtree2.geometry.Geometry;
import com.github.davidmoten.rtree2.RTree;
import com.github.davidmoten.rtree2.geometry.Geometries;
import com.github.davidmoten.rtree2.geometry.Point;

import dk.itu.raven.geometry.PixelRange;
import dk.itu.raven.geometry.Polygon;
import dk.itu.raven.ksquared.K2Raster;
import dk.itu.raven.util.matrix.ArrayMatrix;
import dk.itu.raven.util.matrix.Matrix;
import dk.itu.raven.util.matrix.RandomMatrix;
import dk.itu.raven.util.Pair;

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
        assertTrue(ranges.stream().anyMatch(pr -> pr.row == 2 && pr.x1 == 0 && (pr.x2 == 2 || pr.x2 == 3)));
        assertFalse(ranges.stream().anyMatch(pr -> pr.row == 2 && pr.x1 == 2));
        assertTrue(ranges.stream().anyMatch(pr -> pr.row == 3 && pr.x1 == 0 && (pr.x2 == 3 || pr.x2 == 4)));
        assertTrue(ranges.stream().anyMatch(pr -> pr.row == 2 && (pr.x1 == 18 || pr.x1 == 17) && pr.x2 == 20));
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

        assertEquals(ranges.size(), 10);
        assertTrue(ranges.stream().anyMatch(pr -> pr.row == 1));
        
        int i = 0;
        for (PixelRange range : ranges) {
            assertEquals(new PixelRange(i,i,i),range);
            i++;
        }

        for (i = 0; i < ranges.size(); i++) {
            final int j = i;
            assertTrue(ranges.stream().anyMatch(pr -> pr.row == j && pr.x1 == j && pr.x2 == j));
        }
        assertFalse(ranges.stream().anyMatch(pr -> pr.row == 10 && pr.x1 == 10 && pr.x2 == 9));
    }

    @RepeatedTest(100)
    public void testCombineLists() {
        Random r = new Random();
        Matrix matrix = new RandomMatrix(100, 100, 100);
        int lo = 25;
        int hi = 75;
        K2Raster k2Raster = new K2Raster(matrix);
        RavenJoin join = new RavenJoin(k2Raster, null);
        List<Pair<Geometry, Collection<PixelRange>>> def = new ArrayList<>();
        List<Pair<Geometry, Collection<PixelRange>>> prob = new ArrayList<>();
        List<PixelRange> initialDef = new ArrayList<>();
        def.add(new Pair<Geometry, Collection<PixelRange>>(null, new ArrayList<>()));
        prob.add(new Pair<Geometry, Collection<PixelRange>>(null, new ArrayList<>()));
        for (int i = 0; i < matrix.getHeight(); i++) {
            int start = r.nextInt(25);
            int end = 75 + r.nextInt(25);
            PixelRange range = new PixelRange(i, start, end);

            if (i % 2 == 0) {
                def.get(0).second.add(range);
                initialDef.add(range);
            } else {
                prob.get(0).second.add(range);
            }
        }

        join.combineLists(def, prob, lo, hi);

        for (PixelRange range : initialDef) {
            assertTrue(def.get(0).second.contains(range));
        }

        HashSet<Long> seen = new HashSet<>();
        // check that all ranges in def are within the range of lo and hi
        for (Pair<Geometry, Collection<PixelRange>> pair : def.subList(1, def.size())) {
            for (PixelRange range : pair.second) {
                for (int i = range.x1; i <= range.x2; i++) {
                    long hash = range.row;
                    hash <<= 32;
                    hash += i;
                    seen.add(hash);
                    int val = matrix.get(range.row, i);
                    assertTrue(val >= lo && val <= hi);
                }
            }
        }

        // check that all pixels in prob with a value in the range are present in def
        for (PixelRange range : prob.get(0).second) {
            for (int i = range.x1; i <= range.x2; i++) {
                int val = matrix.get(range.row, i);
                if (val <= hi && val >= lo) {
                    long hash = range.row;
                    hash <<= 32;
                    hash += i;
                    assertTrue(seen.contains(hash));
                }
            }
        }

    }

    @Test
    public void testRavenJoin() {
        int[][] matrix = new int[16][16];
        int fillValue = 42; // You can change this to any integer value
        for (int i = 0; i < 16; i++)
            for (int j = 0; j < 16; j++)
                matrix[i][j] = fillValue;
        matrix[6][6] = 0;
        
        K2Raster k2 = new K2Raster(new ArrayMatrix(matrix, 16, 16));
        
        RTree<String, Geometry> rtree = RTree.star().maxChildren(6).create();
        Polygon p = new Polygon(new Coordinate[] {new Coordinate(1,1),new Coordinate(3,1),new Coordinate(3,3),new Coordinate(1,3)});
        Polygon p2 = new Polygon(new Coordinate[] {new Coordinate(5,5),new Coordinate(10,5),new Coordinate(10,10),new Coordinate(5,10)});
        PixelRange[] expected = new PixelRange[] {new PixelRange(1, 1, 3),new PixelRange(2, 1, 3)};
        PixelRange[] expected2 = new PixelRange[] {new PixelRange(5, 5, 10),new PixelRange(6, 5, 5),new PixelRange(6, 7, 10),new PixelRange(7, 5, 10),new PixelRange(8, 5, 10),new PixelRange(9, 5, 10)};
        rtree = rtree.add(null,p);
        rtree = rtree.add(null,p2);


        RavenJoin join = new RavenJoin(k2, rtree);
        List<Pair<Geometry,Collection<PixelRange>>> res = join.join(42,42);

        assertEquals(res.get(0).first, p);
        int idx = 0;
        assertEquals(res.get(0).second.size(), expected.length);
        assertEquals(res.get(1).second.size(), expected2.length);
        for (PixelRange range : res.get(0).second) {
            assertEquals(expected[idx++],range);
        }
        idx = 0;
        for (PixelRange range : res.get(1).second) {
            assertEquals(expected2[idx++],range);
        }
    }
    
}

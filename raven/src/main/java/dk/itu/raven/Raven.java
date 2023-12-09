package dk.itu.raven;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.Stack;

import org.apache.hadoop.fs.Path;
import org.locationtech.jts.geom.Coordinate;

import com.github.davidmoten.rtree2.geometry.Geometries;
import com.github.davidmoten.rtree2.geometry.Point;

import dk.itu.raven.geometry.Polygon;

import dk.itu.raven.join.RavenJoin;
import dk.itu.raven.join.Square;

public class Raven {

    public static void main(String[] args) throws IOException {
        ArrayList<Long> times = new ArrayList<>();
        int[] numPoints = { 2000, 4000, 8000, 16000, 32000, 64000, 128000, 256000 };
        for (int numP : numPoints) {

            for (int j = 0; j < 1000; j++) {
                List<Point> points = new ArrayList<>();
                double angle = 0;
                double length = 2.0;
                double size = 0;
                points.add(Geometries.point(0, 0));
                for (int i = 0; i < numP; i++) {
                    angle += 2 * Math.PI / numP;
                    Point last = points.get(points.size() - 1);
                    Point next = Geometries.point(last.x() + Math.cos(angle) * length,
                            last.y() + Math.sin(angle) * length);
                    points.add(next);
                    size = Math.max(size, next.y());
                    size = Math.max(size, next.x());
                }
                Polygon poly = new Polygon(points);
                RavenJoin join = new RavenJoin(null, null);
                Square square = new Square(0, 0, (int) size + 10);
                long start = System.nanoTime();
                join.ExtractCellsPolygonBeast(poly, 0, square, square.getSize());
                times.add(System.nanoTime() - start);

            }
            double avg = calculateAverage(times);
            System.out.println("Average time: " + avg);
            times.clear();

        }

    }

    private static double calculateAverage(List<Long> marks) {
        return marks.stream()
                .mapToDouble(d -> d)
                .average()
                .orElse(0.0);
    }
}
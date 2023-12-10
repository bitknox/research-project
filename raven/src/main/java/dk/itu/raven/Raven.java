package dk.itu.raven;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.Stack;
import java.util.stream.Collectors;

import org.apache.hadoop.fs.Path;
import org.locationtech.jts.geom.Coordinate;

import com.github.davidmoten.rtree2.geometry.Geometries;
import com.github.davidmoten.rtree2.geometry.Point;

import dk.itu.raven.geometry.Polygon;

import dk.itu.raven.join.RavenJoin;
import dk.itu.raven.join.Square;
import dk.itu.raven.util.Pair;

public class Raven {

    public static void main(String[] args) throws IOException {
        ArrayList<Long> times = new ArrayList<>();
        int[] numPoints = { 2000, 2000, 4000, 8000, 16000, 32000, 64000, 128000, 256000 };
        for (int numP : numPoints) {

            for (int j = 0; j < 1000; j++) {
                Polygon poly = generatePolygon(numP);
                RavenJoin join = new RavenJoin(null, null);
                Square square = new Square(-2000, -2000, 4000);
                long start = System.nanoTime();
                join.ExtractCellsPolygon(poly, 0, square, square.getSize());
                times.add(System.nanoTime() - start);

            }
            double avg = calculateAverage(times);
            System.out.print(avg + ",");
            times.clear();

        }

    }

    private static Polygon generatePolygon(int numPoints) {
        Random r = new Random();
        final double innerDist = 700;
        final double outerDist = 2000;
        double oldDist = (innerDist + outerDist) / 2;
        double maxJump = 30.0;

        List<Pair<Double, Point>> points = new ArrayList<>();
        for (int i = 0; i < numPoints; i++) {
            double angle = 2 * Math.PI * i / numPoints;
            double dist = Math.max(Math.min((oldDist + maxJump * (r.nextDouble() - 0.5)), outerDist), innerDist);
            Point p = Geometries.point(Math.cos(angle) * dist, Math.sin(angle) * dist);
            points.add(new Pair<Double, Point>(angle, p));
            oldDist = dist;
        }

        Collections.sort(points, new Comparator<Pair<Double, Point>>() {
            @Override
            public int compare(Pair<Double, Point> o1, Pair<Double, Point> o2) {
                return (int) Math.signum(o1.first - o2.first);
            }
        });

        Polygon poly = new Polygon(points.stream()
                .map(pr -> Geometries.point(pr.second.x() + 2000, pr.second.y() + 2000)).collect(Collectors.toList()));
        return poly;
    }

    private static double calculateAverage(List<Long> marks) {
        return marks.stream()
                .mapToDouble(d -> d)
                .average()
                .orElse(0.0);
    }
}
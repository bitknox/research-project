package dk.itu.raven.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;

import com.github.davidmoten.rtree2.geometry.Geometries;
import com.github.davidmoten.rtree2.geometry.Geometry;
import com.github.davidmoten.rtree2.geometry.Point;
import com.github.davidmoten.rtree2.geometry.Rectangle;

public class Polygon implements Geometry, Iterator<Point>, Iterable<Point> {
    private Rectangle mbr;
    private List<Point> points;
    private int currentIteratorIndex;

    public Polygon(List<Point> points) {
        this.points = points;
        double minx = Double.MAX_VALUE, miny = Double.MAX_VALUE;
        double maxx = Double.MIN_VALUE, maxy = Double.MIN_VALUE;
        for (Point p : points) {
            minx = Math.min(minx, p.x());
            maxx = Math.max(maxx, p.x());
            miny = Math.min(miny, p.y());
            maxy = Math.max(maxy, p.y());
        }
        this.mbr = Geometries.rectangle(minx, miny, maxx, maxy);
    }

    public Polygon(Coordinate[] coordinates) {
        this.points = new ArrayList<>();
        double minx = Double.MAX_VALUE, miny = Double.MAX_VALUE;
        double maxx = Double.MIN_VALUE, maxy = Double.MIN_VALUE;
        for (Coordinate coord : coordinates) {
            Point p = Geometries.point(coord.x, coord.y);
            minx = Math.min(minx, p.x());
            maxx = Math.max(maxx, p.x());
            miny = Math.min(miny, p.y());
            maxy = Math.max(maxy, p.y());
            this.points.add(p);
        }
        this.mbr = Geometries.rectangle(minx, miny, maxx, maxy);
    }

    public Polygon(List<Point> points, Rectangle mbr) {
        this.points = points;
        this.mbr = mbr;
    }

    // FIXME: this method is not correct
    private double distanceSimple(Rectangle r) {
        return mbr().distance(r);
    }

    @Override
    public double distance(Rectangle r) {
        return distanceSimple(r);
    }

    @Override
    public Rectangle mbr() {
        return this.mbr;
    }

    // FIXME: this method is not correct
    private boolean intersectsSimple(Rectangle r) {
        return mbr().intersects(r);
    }

    @Override
    public boolean intersects(Rectangle r) {
        return intersectsSimple(r);
    }

    @Override
    public boolean isDoublePrecision() {
        return true;
    }

    @Override
    public Iterator<Point> iterator() {
        this.currentIteratorIndex = 1;
        return this;
    }

    @Override
    public boolean hasNext() {
        return currentIteratorIndex <= this.points.size();
    }

    @Override
    public Point next() {
        return this.points.get((currentIteratorIndex++) % this.points.size());
    }

    public Point getFirst() {
        return points.get(0);
    }
}

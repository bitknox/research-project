package dk.itu.raven.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.github.davidmoten.rtree2.geometry.Geometries;
import com.github.davidmoten.rtree2.geometry.Geometry;
import com.github.davidmoten.rtree2.geometry.Point;
import com.github.davidmoten.rtree2.geometry.Rectangle;

public class Polygon implements Geometry, Iterator<Point>, Iterable<Point> {
    private Rectangle mbr;
    private List<Point> points;
    private int currentIteratorIndex;


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

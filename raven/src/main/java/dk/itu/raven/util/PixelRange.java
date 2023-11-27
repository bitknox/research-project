package dk.itu.raven.util;

import com.github.davidmoten.rtree2.geometry.Geometry;

public class PixelRange {
    public int row;
    public int x1,x2;
    public Geometry geometry;

    public PixelRange(int row, int x1, int x2, Geometry geometry) {
        this.row = row;
        this.x1 = x1;
        this.x2 = x2;
        this.geometry = geometry;
    }
}

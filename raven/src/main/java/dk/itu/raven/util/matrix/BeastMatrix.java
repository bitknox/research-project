package dk.itu.raven.util.matrix;

import java.io.IOException;

import edu.ucr.cs.bdlab.beast.io.tiff.TiffRaster;
import com.github.davidmoten.rtree2.geometry.Rectangle;

public class BeastMatrix extends Matrix {
    TiffRaster raster;
    int xoffset;
    int yoffset;

    public BeastMatrix(TiffRaster raster, Rectangle rect) {
        super((int)Math.ceil(rect.x2()-rect.x1()), (int) Math.ceil(rect.y2()-rect.y1()));
        this.raster = raster;
        this.xoffset = (int) rect.x1();
        this.yoffset = (int) rect.y1();
    }

    @Override
    protected int getWithinRange(int r, int c) throws IOException {
        return (int) raster.getPixel(c + xoffset, yoffset + r);
    }
    
}

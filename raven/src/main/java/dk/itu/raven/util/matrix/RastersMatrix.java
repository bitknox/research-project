package dk.itu.raven.util.matrix;

import mil.nga.tiff.Rasters;

public class RastersMatrix extends Matrix {
    private Rasters rasters;

    public RastersMatrix(Rasters rasters) {
        super(rasters.getWidth(), rasters.getHeight());
        this.rasters = rasters;
    }

    @Override
    public int getWithinRange(int r, int c) {
        // Logger.log(rasters.getPixel(r, c)[0]);
        return rasters.getPixelSample(0, c, r).intValue();
    }

}

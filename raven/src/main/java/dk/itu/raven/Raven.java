package dk.itu.raven;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.hadoop.fs.Path;

import com.github.davidmoten.rtree2.RTree;
import com.github.davidmoten.rtree2.geometry.Geometry;

import dk.itu.raven.geometry.PixelRange;
import dk.itu.raven.geometry.Polygon;
import dk.itu.raven.io.GeneratorRasterReader;
import dk.itu.raven.io.RasterReader;
import dk.itu.raven.io.ShapfileReader;
import dk.itu.raven.io.TFWFormat;
import dk.itu.raven.join.RavenJoin;
import dk.itu.raven.ksquared.K2Raster;
import dk.itu.raven.util.Pair;
import dk.itu.raven.util.matrix.Matrix;
import dk.itu.raven.visualizer.Visualizer;
import dk.itu.raven.visualizer.VisualizerOptions;

public class Raven {

    public static void main(String[] args) throws IOException {
        Visualizer visualizer = new Visualizer(4000, 4000);
        RasterReader rasterReader = new GeneratorRasterReader(4000, 4000, 129384129, 10,
                new TFWFormat(0.09, 0, 0, -0.09, -180, 90));
        Pair<Matrix, TFWFormat> rasterData = rasterReader.readRasters();

        K2Raster k2Raster = new K2Raster(rasterData.first);
        System.out.println("Done Building Raster");
        System.out.println(k2Raster.Tree.size());

        RTree<String, Geometry> rtree = RTree.star().maxChildren(6).create();
        ShapfileReader featureReader = new ShapfileReader(rasterData.second);
        Iterable<Geometry> geometries = featureReader.readShapefile(
                "C:\\Users\\Johan\\Documents\\Research Project\\research-project\\data\\testdata\\vector\\boundaries.zip");

        for (Geometry geom : geometries) {
            rtree = rtree.add(null, geom);
        }
        visualizer.drawShapefile(geometries, rasterData.second);

        System.out.println("Done Building rtree");

        RavenJoin join = new RavenJoin(k2Raster, rtree);
        List<Pair<Geometry, Collection<PixelRange>>> result = join.join();
        visualizer.drawRaster(result, new VisualizerOptions("./outPutRaster.tif", false, true));
        System.out.println("Done joining");

    }
}
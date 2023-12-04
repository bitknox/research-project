package dk.itu.raven;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.hadoop.fs.Path;

import com.github.davidmoten.rtree2.RTree;
import com.github.davidmoten.rtree2.geometry.Geometry;
import com.github.davidmoten.rtree2.geometry.Rectangle;

import dk.itu.raven.geometry.PixelRange;
import dk.itu.raven.geometry.Polygon;
import dk.itu.raven.io.GeneratorRasterReader;
import dk.itu.raven.io.MilRasterReader;
import dk.itu.raven.io.BeastRasterReader;
import dk.itu.raven.io.FileRasterReader;
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

        FileRasterReader rasterReader = new MilRasterReader(new File(
                "C:\\Users\\Johan\\Documents\\Research Project\\research-project\\data\\testdata\\raster\\glc2000\\"));

        // RasterReader rasterReader = new GeneratorRasterReader(4000, 4000, 129384129,
        // 10,
        // new TFWFormat(0.09, 0, 0, -0.09, -180, 90));
        TFWFormat format = rasterReader.getTransform();

        RTree<String, Geometry> rtree = RTree.star().maxChildren(6).create();
        ShapfileReader featureReader = new ShapfileReader(format);
        Pair<Iterable<Polygon>, ShapfileReader.ShapeFileBounds> geometries = featureReader.readShapefile(
                "C:\\Users\\Johan\\Documents\\Research Project\\research-project\\data\\testdata\\vector\\cb_2018_us_state_500k.zip\\");

        for (Polygon geom : geometries.first) {
            geom.offset(-geometries.second.minx, -geometries.second.miny);
            rtree = rtree.add(null, geom);
        }
        System.out.print(rtree.mbr().get());
        Rectangle rect = rtree.mbr().get();
        Visualizer visualizer = new Visualizer((int) (rect.x2() - rect.x1()), (int) (rect.y2() - rect.y1()));

        Matrix rasterData = rasterReader.readRasters(rtree.mbr().get());
        for (Geometry geom : geometries.first) {
            rtree = rtree.add(null, geom);
        }

        K2Raster k2Raster = new K2Raster(rasterData);
        System.out.println("Done Building Raster");
        System.out.println(k2Raster.Tree.size());

        visualizer.drawShapefile(geometries.first, format);

        System.out.println("Done Building rtree");

        RavenJoin join = new RavenJoin(k2Raster, rtree);
        List<Pair<Geometry, Collection<PixelRange>>> result = join.join();
        System.out.println(result.size());
        visualizer.drawRaster(result, new VisualizerOptions("./outPutRaster.tif",
                false, true));
        System.out.println("Done joining");

    }
}
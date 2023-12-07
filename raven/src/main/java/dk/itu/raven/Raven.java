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

import com.github.davidmoten.rtree2.RTree;
import com.github.davidmoten.rtree2.geometry.Geometries;
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
import dk.itu.raven.join.Square;
import dk.itu.raven.ksquared.K2Raster;
import dk.itu.raven.util.Pair;
import dk.itu.raven.util.matrix.ArrayMatrix;
import dk.itu.raven.util.matrix.Matrix;
import dk.itu.raven.util.matrix.RandomMatrix;
import dk.itu.raven.visualizer.Visualizer;
import dk.itu.raven.visualizer.VisualizerOptions;
import dk.itu.raven.util.Logger;

public class Raven {

        public static void main(String[] args) throws IOException {
                Logger.setDebug(false);

                // testThings();

                FileRasterReader rasterReader = new MilRasterReader(new File(
                                "/home/joinpro/research-project/data/testdata/raster/glc2000/"));
                // RasterReader rasterReader = new GeneratorRasterReader(4000, 4000, 129384129,
                // 12,
                // new TFWFormat(0.09, 0, 0, -0.09 , -180, 90));
                TFWFormat format = rasterReader.getTransform();

                RTree<String, Geometry> rtree = RTree.star().maxChildren(6).create();
                ShapfileReader featureReader = new ShapfileReader(format);
                // Pair<Iterable<Polygon>, ShapfileReader.ShapeFileBounds> geometries =
                // featureReader.readShapefile(
                // "c:\\Users\\alexa\\Downloads\\cb_2018_us_state_500k.zip");
                Pair<Iterable<Polygon>, ShapfileReader.ShapeFileBounds> geometries = featureReader.readShapefile(
                                "/home/joinpro/research-project/data/testdata/vector/cb_2018_us_state_500k.zip");

                Rectangle rect = Geometries.rectangle(geometries.second.minx, geometries.second.miny,
                                geometries.second.maxx,
                                geometries.second.maxy);
                // Visualizer visualizer = new Visualizer((int) (rect.x2() - rect.x1()), (int)
                // (rect.y2() - rect.y1()));

                Matrix rasterData = rasterReader.readRasters(rect);
                for (Polygon geom : geometries.first) {
                        geom.offset(-geometries.second.minx, -geometries.second.miny);
                        rtree = rtree.add(null, geom);
                }
                // Logger.log(rasterData.get(8000, 5000));
                // for (Geometry geom : geometries.first) {
                // rtree = rtree.add(null, geom);
                // }
                long startBuildNano = System.nanoTime();
                K2Raster k2Raster = new K2Raster(rasterData);
                long endBuildNano = System.nanoTime();
                Logger.log("Build time: " + (endBuildNano - startBuildNano) / 1000000000 + "s");
                // visualizer.drawVectorRasterOverlap(geometries.first, rasterData, rtree,
                // k2Raster);
                Logger.log("Done Building Raster");
                Logger.log(k2Raster.Tree.size());

                // visualizer.drawShapefile(geometries.first, format);

                Logger.log("Done Building rtree");

                RavenJoin join = new RavenJoin(k2Raster, rtree);
                long startJoinNano = System.nanoTime();
                List<Pair<Geometry, Collection<PixelRange>>> result = join.join();
                long endJoinNano = System.nanoTime();
                System.out.println("Build time: " + (endJoinNano - startJoinNano) / 1000000000 + "s");

                Logger.log("Done joining");
        }
}
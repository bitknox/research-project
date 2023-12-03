package dk.itu.raven;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;

import org.apache.hadoop.fs.Path;
// import java.io.File.Path;

import org.locationtech.jts.geom.Coordinate;

import com.github.davidmoten.rtree2.RTree;
import com.github.davidmoten.rtree2.geometry.Geometries;
import com.github.davidmoten.rtree2.geometry.Geometry;
import com.github.davidmoten.rtree2.geometry.Point;

import dk.itu.raven.geometry.PixelRange;
import dk.itu.raven.geometry.Polygon;
import dk.itu.raven.io.FileRasterReader;
import dk.itu.raven.io.RasterReader;
import dk.itu.raven.io.GeneratorRasterReader;
import dk.itu.raven.io.TFWFormat;
import dk.itu.raven.join.RavenJoin;
import dk.itu.raven.ksquared.K2Raster;
import dk.itu.raven.util.BitMap;
import dk.itu.raven.util.Pair;
import dk.itu.raven.util.matrix.ArrayMatrix;
import dk.itu.raven.util.matrix.Matrix;
import dk.itu.raven.util.matrix.RandomMatrix;
import dk.itu.raven.util.matrix.RastersMatrix;
import edu.ucr.cs.bdlab.beast.common.BeastOptions;
import edu.ucr.cs.bdlab.beast.geolite.IFeature;
import edu.ucr.cs.bdlab.beast.io.shapefile.ShapefileFeatureReader;
import mil.nga.tiff.FileDirectory;
import mil.nga.tiff.Rasters;
import mil.nga.tiff.TIFFImage;
import mil.nga.tiff.TiffReader;

import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.image.BufferedImage;

public class Raven {

    private static Matrix generateRandom(int width, int height, int maxValue) {
        Random r = new Random(42);
        int[][] data = new int[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                data[i][j] = r.nextInt(maxValue + 1);
            }
        }
        return new ArrayMatrix(data, width, height);
    }

    public static void main(String[] args) throws IOException {
        // FileRasterReader rasterReader = new FileRasterReader(new File(
        // "C:\\Users\\Johan\\Documents\\Research
        // Project\\research-project\\data\\testdata\\raster\\glc2000"));
        RasterReader rasterReader = new GeneratorRasterReader(4000, 4000, 129384129, 10,
                new TFWFormat(0.09, 0, 0, -0.09, -180, 90));
        Pair<Matrix, TFWFormat> rasterData = rasterReader.readRasters();
        System.out.println(rasterData.second.transFromCoordinateToPixel(-180, -90));
        System.out.println(rasterData.second.transFromCoordinateToPixel(180, 90));

        K2Raster k2Raster = new K2Raster(rasterData.first);
        System.out.println("Done Building Raster");
        System.out.println(k2Raster.Tree.size());

        RTree<String, Geometry> rtree = RTree.star().maxChildren(6).create();
        BufferedImage vectorimage = new BufferedImage(4000, 4000, BufferedImage.TYPE_INT_RGB);
        Graphics2D vectorGraphics = vectorimage.createGraphics();
        vectorGraphics.setColor(Color.white);
        vectorGraphics.fillRect(0, 0, 4000, 4000); // give the whole image a white background
        vectorGraphics.setColor(Color.red);
        Random r = new Random();
        try (ShapefileFeatureReader featureReader = new ShapefileFeatureReader()) {
            featureReader.initialize(new Path(
                    "C:\\Users\\Johan\\Documents\\Research Project\\research-project\\data\\testdata\\vector\\cb_2018_us_state_500k.zip"),
                    new BeastOptions());
            for (IFeature feature : featureReader) {
                System.out.println(feature.getGeometry().getNumGeometries());
                vectorGraphics.setColor(new Color(r.nextInt(256), r.nextInt(256), r.nextInt(256)));
                for (int i = 0; i < feature.getGeometry().getNumGeometries(); i++) {
                    org.locationtech.jts.geom.Geometry geom = feature.getGeometry().getGeometryN(i);
                    Polygon poly = new Polygon(geom.getCoordinates(), rasterData.second);
                    Point old = poly.getFirst();
                    for (Point next : poly) {
                        vectorGraphics.drawLine((int) old.x(), (int) old.y(), (int) next.x(), (int) next.y());
                        old = next;
                    }
                    rtree = rtree.add(null, poly);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
        System.out.println("Done Building rtree");

        RavenJoin join = new RavenJoin(k2Raster, rtree);
        List<Pair<Geometry, Collection<PixelRange>>> result = join.join();

        BufferedImage image = new BufferedImage(4000, 4000,
                BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics(); // not sure on this line, but this
        // seems more right
        g.setColor(Color.white);
        g.fillRect(0, 0, 4000, 4000); // give the whole image a white background
        g.setColor(Color.black);

        for (Pair<Geometry, Collection<PixelRange>> pair : result) {
            g.setColor(new Color(r.nextInt(256), r.nextInt(256), r.nextInt(256)));
            for (PixelRange range : pair.second) {
                // System.out.println(range.x2 - range.x1);
                g.drawLine(range.x1, range.row, range.x2, range.row);
            }
        }

        try {
            ImageIO.write(image, "tif", new File("CustomImage.tif"));
            ImageIO.write(vectorimage, "tif", new File("CustomImageVector.tif"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Matrix M = generateRandom(2000, 2000, 10);
        // // System.out.println("done building mat
        // long start = System.currentTim
        // K2Raster
        // M = null;
        // // System.out.println(K2.getCell(
        // System.out.println(K2Raster.num);
        // System.out.println(System.currentTimeMillis() - start);
        // K2.getCell(3, 254);

        // for (int i = 0; i < 1000; i++) {
        // System.out.println(i);
        // for (int j = 0; j < 1000; j++) {

        // // System.out.println(M.get(i, j) + " != " + K2.getCell(i, j) + ", i: " + i +
        // ",
        // // j: " + j);
        // long start2 = System.nanoTime();
        // K2.getWindow(i, i + 1000, j, j + 1000);
        // System.out.println(System.nanoTime() - start2);

        // }
        // }
        // int num = K2.getWindow(0, 0, 99, 99).length;
        // System.out.println(num);
        // for (int i : K2.getWindow(0, 0, 99, 99)) {
        // if (i != 0)
        // System.out.println(i);
        // }
        // RTree<String, Geometry> rtree = RTree.star().maxChildren(6).create();
        // K2Raster k2Raster = null;
        // try {
        // TIFFImage tiffImage = TiffReader.readTiff(new
        // File("C:\\Users\\alexa\\Downloads\\glc2000_v1_1_Tiff\\Tiff\\glc2000_v1_1.tif"));
        // List<FileDirectory> directories = tiffImage.getFileDirectories();
        // FileDirectory directory = directories.get(0);
        // Rasters rasters = directory.readRasters();
        // k2Raster = new K2Raster(new RastersMatrix(rasters));

        // } catch (Exception e) {
        // e.printStackTrace();
        // System.exit(-1);
        // }

        // System.out.println("done building R2");

        // System.out.println("done building R-Tree");

        // // Access nodes by checking if they are leafs or nonleafs
        // // if they are nonleafs then we can access their children by casting them to
        // // NonLeaf and then calling children() on them
        // // if (tree.root().get() instanceof com.github.davidmoten.rtree2.NonLeaf) {

        // // System.out.println("NonLeaf");
        // // } else {
        // // System.out.println("Leaf");
        // // }

        // RavenJoin join = new RavenJoin(k2Raster, rtree);
        // List<Pair<Geometry, Collection<PixelRange>>> result = join.join();
        // System.out.println(result.size());
    }
}
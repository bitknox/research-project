package dk.itu.raven;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.hadoop.fs.Path;
import org.locationtech.jts.geom.Coordinate;

import com.github.davidmoten.rtree2.RTree;
import com.github.davidmoten.rtree2.geometry.Geometries;
import com.github.davidmoten.rtree2.geometry.Geometry;

import dk.itu.raven.join.RavenJoin;
import dk.itu.raven.ksquared.K2Raster;
import dk.itu.raven.util.BitMap;
import dk.itu.raven.util.Pair;
import dk.itu.raven.util.PixelRange;
import dk.itu.raven.util.Polygon;
import dk.itu.raven.util.matrix.RastersMatrix;
import edu.ucr.cs.bdlab.beast.common.BeastOptions;
import edu.ucr.cs.bdlab.beast.geolite.IFeature;
import edu.ucr.cs.bdlab.beast.io.shapefile.ShapefileFeatureReader;
import mil.nga.tiff.FileDirectory;
import mil.nga.tiff.Rasters;
import mil.nga.tiff.TIFFImage;
import mil.nga.tiff.TiffReader;

public class Raven {

    public static void main(String[] args) {
        RTree<String, Geometry> rtree = RTree.star().maxChildren(6).create();
        K2Raster k2Raster = null;
        try {
            TIFFImage tiffImage = TiffReader.readTiff(new File("C:\\Users\\alexa\\Downloads\\glc2000_v1_1_Tiff\\Tiff\\glc2000_v1_1.tif"));
            List<FileDirectory> directories = tiffImage.getFileDirectories();
            FileDirectory directory = directories.get(0);
            Rasters rasters = directory.readRasters();
            k2Raster = new K2Raster(new RastersMatrix(rasters));
            
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        } 

        System.out.println("done building R2");
        
        try (ShapefileFeatureReader featureReader = new ShapefileFeatureReader()) {
            featureReader.initialize(new Path("C:\\Users\\alexa\\Downloads\\cb_2018_us_state_500k.zip"), new BeastOptions());
            for (IFeature feature : featureReader) {
                Polygon poly = new Polygon(feature.getGeometry().getCoordinates());
                rtree = rtree.add(null, poly);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
        
        System.out.println("done building R-Tree");
        
        // Access nodes by checking if they are leafs or nonleafs
        // if they are nonleafs then we can access their children by casting them to
        // NonLeaf and then calling children() on them
        // if (tree.root().get() instanceof com.github.davidmoten.rtree2.NonLeaf) {

        //     System.out.println("NonLeaf");
        // } else {
        //     System.out.println("Leaf");
        // }

        RavenJoin join = new RavenJoin(k2Raster, rtree);
        List<Pair<Geometry, Collection<PixelRange>>> result = join.join();
        System.out.println(result.size());
    }
}
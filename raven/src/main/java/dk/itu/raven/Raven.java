package dk.itu.raven;

import com.github.davidmoten.rtree2.RTree;
import com.github.davidmoten.rtree2.geometry.Geometries;
import com.github.davidmoten.rtree2.geometry.Geometry;
import com.github.davidmoten.rtree2.Entry;
import dk.itu.raven.ksquared.K2Raster;

public class Raven {

    public static void main(String[] args) {
        int[][] M = { { 5, 5, 4, 4, 4, 4, 1, 1 }, //
                { 5, 4, 4, 4, 4, 4, 1, 1 }, //
                { 4, 4, 4, 4, 1, 2, 2, 1 }, //
                { 3, 3, 4, 3, 2, 1, 2, 2 }, //
                { 3, 4, 3, 3, 2, 2, 2, 2 }, //
                { 4, 3, 3, 2, 2, 2, 2, 2 }, //
                { 1, 1, 1, 3, 2, 2, 2, 2 }, //
                { 1, 1, 1, 2, 2, 2, 2, 2 } }; //
        K2Raster K2 = new K2Raster(M, 8, 8);
        for (int i = 0; i < 10; i++) {
            int[] res = K2.getChildren(i);

            for (int j : res) {
                System.out.println(K2.LMax.accessFT(j));
            }
            System.out.println("");
        }

        RTree<String, Geometry> tree = RTree.star().maxChildren(6).create();
        tree = tree.add("asd", Geometries.circle(300, 300, 200));
        tree = tree.add("asd", Geometries.point(300, 400));
        tree = tree.add("asd", Geometries.point(300, 200));
        tree = tree.add("asd", Geometries.point(300, 100));
        tree = tree.add("asd", Geometries.point(100, 10));
        tree = tree.add("asd", Geometries.point(130, 10));
        tree = tree.add("asd", Geometries.point(140, 10));
        tree = tree.add("asd", Geometries.point(150, 10));
        tree = tree.add("asd", Geometries.point(160, 10));
        tree = tree.add("asd", Geometries.point(170, 10));

        // Access nodes by checking if they are leafs or nonleafs
        // if they are nonleafs then we can access their children by casting them to
        // NonLeaf and then calling children() on them
        if (tree.root().get() instanceof com.github.davidmoten.rtree2.NonLeaf) {

            System.out.println("NonLeaf");
        } else {
            System.out.println("Leaf");
        }

        for (Entry<String, Geometry> entry : tree.entries()) {
            System.out.println(entry.toString());
        }

        tree.visualize(600, 600).save("target/tree.png");
        for (int i : K2.getWindow(0, 7, 0, 7)) {
            System.out.print(i + " ");
        }
    }
}
package dk.itu.raven.util;

import com.github.davidmoten.rtree2.Leaf;
import com.github.davidmoten.rtree2.Node;
import com.github.davidmoten.rtree2.NonLeaf;
import com.github.davidmoten.rtree2.geometry.Geometry;

public class TreeExtensions {
    public static Iterable<Node<String, Geometry>> getChildren(Node<String, Geometry> node) {
        if (node instanceof NonLeaf) {
			return ((NonLeaf<String,Geometry>)node).children();
        } else {
            return null;
        }
    }

    public static boolean isLeaf(Node<String, Geometry> node) {
        return node instanceof Leaf;
    }
}
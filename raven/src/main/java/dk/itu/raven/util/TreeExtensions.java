package dk.itu.raven.util;

import java.util.ArrayList;
import java.util.List;

import com.github.davidmoten.rtree2.Leaf;
import com.github.davidmoten.rtree2.Node;
import com.github.davidmoten.rtree2.NonLeaf;
import com.github.davidmoten.rtree2.geometry.Geometry;

public class TreeExtensions {
    public static Iterable<Node<String, Geometry>> getChildren(Node<String, Geometry> node) {
        if (node instanceof NonLeaf) {
			return ((NonLeaf<String,Geometry>)node).children();
        } else {
            List<Node<String,Geometry>> res = new ArrayList<>();
            res.add(node);
            return res;
        }
    }

    public static boolean isLeaf(Node<String, Geometry> node) {
        return node instanceof Leaf;
    }
}
package dk.itu.raven.join;

import java.util.ArrayList;
import java.util.Stack;

import com.github.davidmoten.rtree2.RTree;
import com.github.davidmoten.rtree2.Node;
import com.github.davidmoten.rtree2.geometry.Geometry;
import com.github.davidmoten.rtree2.geometry.Rectangle;

import dk.itu.raven.util.TreeExtensions;
import dk.itu.raven.util.Tuple3;
import dk.itu.raven.ksquared.K2Raster;
import dk.itu.raven.util.Pair;

public class RavenJoin {
	private enum OverlapType {
		TotalOverlap,
		PossibleOverlap,
		PartialOverlap,
		NoOverlap;
	}

	private K2Raster k2Raster;
	private RTree<String, Geometry> tree;

	public RavenJoin(K2Raster k2Raster, RTree<String, Geometry> tree) {
		this.k2Raster = k2Raster;
		this.tree = tree;

	}

	private Tuple3<OverlapType, Integer, Square> checkQuandrant(int k2Index, Square rasterBounding, Rectangle bounding) {
		int[] children = k2Raster.getChildren(k2Index);
		int childSize = rasterBounding.getSize() / K2Raster.k;
		for (int i = 0; i < children.length; i++) {
			int child = children[i];
			Square childRasterBounding = rasterBounding.getChildSquare(childSize, i, K2Raster.k);
			if (childRasterBounding.contains(bounding)) {
				return checkQuandrant(child, childRasterBounding, bounding);
			}
		}
		if (rasterBounding.contains(bounding)) {
			return new Tuple3<>(OverlapType.TotalOverlap, k2Index, rasterBounding);
		} else {
			return new Tuple3<>(OverlapType.NoOverlap, k2Index, rasterBounding);
		}
	}

	public void join() {
		ArrayList<Pair<Geometry, ArrayList<Integer>>> Def = new ArrayList<>(), Prob = new ArrayList<>();
		Stack<Tuple3<Node<String, Geometry>, Integer, Square>> S = new Stack<>();
		
		for(Node<String,Geometry> node : TreeExtensions.getChildren(tree.root().get())) {
			System.out.println(node.toString());
			S.push(new Tuple3<>(node, 0, new Square(0, 0, k2Raster.getSize())));
		}
		
		while (!S.empty()) {
			Tuple3<Node<String,Geometry>, Integer, Square> p = S.pop();
			Tuple3<OverlapType, Integer, Square> checked = checkQuandrant(p.b, p.c, p.a.geometry().mbr());
			if (checked.a == OverlapType.TotalOverlap) {
				if(TreeExtensions.isLeaf(p.a)) {
					Def.add(p.a.)
				}
			}
		}
	}
}

package dk.itu.raven.join;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Stack;

import com.github.davidmoten.rtree2.RTree;
import com.github.davidmoten.rtree2.Leaf;
import com.github.davidmoten.rtree2.Node;
import com.github.davidmoten.rtree2.NonLeaf;
import com.github.davidmoten.rtree2.geometry.Geometry;
import com.github.davidmoten.rtree2.geometry.Point;
import com.github.davidmoten.rtree2.geometry.Rectangle;

import dk.itu.raven.util.TreeExtensions;
import dk.itu.raven.util.Tuple3;
import dk.itu.raven.ksquared.K2Raster;
import dk.itu.raven.util.BitMap;
import dk.itu.raven.util.Pair;
import dk.itu.raven.util.PixelRange;
import dk.itu.raven.util.Polygon;

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

	/*
	 * TODO:
	 * 	instead of sorting indices, store a boolean array of whether there is an intersection at a given x coordinate (rounded to nearest integer).
	 * 	Then do a linear pass over this array to store the indices in sorted order.
	 */

	private Collection<PixelRange> ExtractCellsPolygon(Polygon polygon, int pk, Square rasterBounding) {
		// find out whether the left-most cell of each row of rasterBounding intersects the polygon

		// 1 on index i * rasterBounding.geetSize() + j if an intersection between a line of the polygon and the line y=j happens at point (i,j) 
		BitMap hasIntersection = new BitMap(rasterBounding.getSize()*rasterBounding.getSize());
		// 1 on index i if the left-most pixel of row i intersects the polygon, 0 otherwise
		BitMap leftIncluded = new BitMap(rasterBounding.getSize());
		Point old = polygon.getFirst();
		int linesCrossed = 0;
		for (Point next : polygon) {
			double a = (next.y() - old.y());
			double b = (old.x() - next.x());
			double c = old.y() * (next.x() - old.x()) - (next.y() - old.y()) * old.x();
			int x = rasterBounding.getTopX();
			int y = rasterBounding.getTopY();
			double side = a*x+b*y-c;
			linesCrossed += (side < 0) ? 1 : 0;
			old = next;
		}

		leftIncluded.setTo(0, linesCrossed % 2);
		
		// polygon.
		// leftIncluded.setTo(1,0);

		// construct pixel ranges in linear time using the idea from the todo above 

		throw new UnsupportedOperationException("Unimplemented method 'ExtractCellsPolygon'");
	}

	// based loosely on: https://bitbucket.org/bdlabucr/beast/src/master/raptor/src/main/java/edu/ucr/cs/bdlab/raptor/Intersections.java
	private Collection<PixelRange> ExtractCells(Leaf<String, Geometry> pr, int pk, Square rasterBounding ) {
		return ExtractCellsPolygon((Polygon) pr.geometry(), pk, rasterBounding);
	}

	private void addDescendantsLeaves(NonLeaf<String, Geometry> pr, int pk, Square rasterBounding, List<Pair<Geometry, Collection<PixelRange>>> Def) {
		for (Node<String, Geometry> n : pr.children()) {
			// I could not find a better way than this:
			if (TreeExtensions.isLeaf(n)) {
				Def.add(new Pair<>(n.geometry(),ExtractCells((Leaf<String,Geometry>) n, pk, rasterBounding)));
			} else {
				addDescendantsLeaves((NonLeaf<String,Geometry>) n, pk, rasterBounding, Def);
			}
		}
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

	// based on: https://journals.plos.org/plosone/article/file?id=10.1371/journal.pone.0226943&type=printable
	public List<Pair<Geometry, Collection<PixelRange>>> join() { // should maybe return pair of such lists
		List<Pair<Geometry, Collection<PixelRange>>> Def = new ArrayList<>(), Prob = new ArrayList<>();
		Stack<Tuple3<Node<String, Geometry>, Integer, Square>> S = new Stack<>();
		
		for(Node<String,Geometry> node : TreeExtensions.getChildren(tree.root().get())) {
			System.out.println(node.toString());
			S.push(new Tuple3<>(node, 0, new Square(0, 0, k2Raster.getSize())));
		}
		
		while (!S.empty()) {
			Tuple3<Node<String,Geometry>, Integer, Square> p = S.pop();
			Tuple3<OverlapType, Integer, Square> checked = checkQuandrant(p.b, p.c, p.a.geometry().mbr());
			if (checked.a == OverlapType.TotalOverlap) {
				if (TreeExtensions.isLeaf(p.a)) {
					Def.add(new Pair<>(p.a.geometry(), ExtractCells((Leaf<String,Geometry>) p.a,checked.b, checked.c)));
				} else {
					addDescendantsLeaves((NonLeaf<String,Geometry>) p.a, checked.b, checked.c, Def);
				}
			} else if (checked.a == OverlapType.PossibleOverlap) {
				if (!TreeExtensions.isLeaf(p.a)) {
					for (Node<String,Geometry> c : ((NonLeaf<String,Geometry>) p.a).children()) {
						S.push(new Tuple3<Node<String,Geometry>,Integer,Square>(c, checked.b, checked.c));
					}
				} else {
					// do checkMBR business
				}
			}
		}

		return Def; // new Pair<>(Def,Prob)
	}
}

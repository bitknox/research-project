package dk.itu.raven.join;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Stack;

import com.github.davidmoten.rtree2.RTree;
import com.github.davidmoten.rtree2.Entry;
import com.github.davidmoten.rtree2.Leaf;
import com.github.davidmoten.rtree2.Node;
import com.github.davidmoten.rtree2.NonLeaf;
import com.github.davidmoten.rtree2.geometry.Geometry;
import com.github.davidmoten.rtree2.geometry.Point;
import com.github.davidmoten.rtree2.geometry.Rectangle;

import dk.itu.raven.util.TreeExtensions;
import dk.itu.raven.util.Tuple3;
import dk.itu.raven.geometry.PixelRange;
import dk.itu.raven.geometry.Polygon;
import dk.itu.raven.ksquared.K2Raster;
import dk.itu.raven.util.BST;
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

	protected Collection<PixelRange> ExtractCellsPolygonSimple(Polygon polygon, int pk, Square rasterBounding) {
		Collection<PixelRange> ranges = new ArrayList<>();
		Point old = polygon.getFirst();
		for (int x = rasterBounding.getTopX(); x < rasterBounding.getTopX() + rasterBounding.getSize(); x++) {
			for (int y = rasterBounding.getTopY(); y < rasterBounding.getTopY() + rasterBounding.getSize(); y++) {
				int toLeft = 0;
				for (Point next : polygon) {
					double a = (next.y() - old.y());
					double b = (old.x() - next.x());
					double c = a * old.x() + b * old.y();
					toLeft += (a * x + b * y - c < 0) ? 1 : 0;
					old = next;
				}
				if (toLeft % 2 == 1) {
					ranges.add(new PixelRange(y, x, x));
				}
			}
		}
		return ranges;
	}

	/*
	 * instead of sorting indices, store a boolean array of whether there is an
	 * intersection at a given x coordinate (rounded to nearest integer).
	 * Then do a linear pass over this array to store the indices in sorted order.
	 */

	protected Collection<PixelRange> ExtractCellsPolygon(Polygon polygon, int pk, Square rasterBounding) {
		// 1 on index i * rasterBounding.geetSize() + j if an intersection between a
		// line of the polygon and the line y=j happens at point (i,j)
		// 1 on index i if the left-most pixel of row i intersects the polygon, 0
		// otherwise
		List<BST<Integer, Integer>> intersections = new ArrayList<BST<Integer, Integer>>(rasterBounding.getSize());
		for (int i = 0; i < rasterBounding.getSize(); i++) {
			intersections.add(i, new BST<>());
		}

		// a line is of the form a*x + b*y = c
		Point old = polygon.getFirst();
		for (Point next : polygon) {
			double a = (next.y() - old.y());
			double b = (old.x() - next.x());
			double c = a * old.x() + b * old.y();

			int miny = (int) Math.round(Math.min(old.y(), next.y()));
			int maxy = (int) Math.round(Math.max(old.y(), next.y()));

			for (int y = miny; y < maxy; y++) {
				if (miny == maxy) {
					if (Math.round(b * y - c) == 0) {
						int start = (int) Math.round(Math.min(old.x(), next.x())) - rasterBounding.getTopX();
						int end = (int) Math.round(Math.max(old.x(), next.x())) - rasterBounding.getTopX();
						BST<Integer, Integer> bst = intersections.get(y - rasterBounding.getTopY());
						incrementSet(bst, start);
						incrementSet(bst, end);
					}
				} else {
					double x = (c - b * y) / a;
					assert x >= 0;
					int ix = (int) Math.round(x - rasterBounding.getTopX());
					BST<Integer, Integer> bst = intersections.get(y - rasterBounding.getTopY());
					incrementSet(bst, ix);
				}
			}
			old = next;
		}

		Collection<PixelRange> ranges = new ArrayList<>();
		int x1 = Integer.MAX_VALUE, x2 = 0, y1 = Integer.MAX_VALUE, y2 = 0;
		for (int y = 0; y < rasterBounding.getSize(); y++) {
			BST<Integer, Integer> bst = intersections.get(y);
			boolean inRange = false;
			int start = 0;
			for (int x : bst.keys()) {
				y1 = Math.min(y1, y);
				y2 = Math.max(y2, y);
				x1 = Math.min(x1, x);
				x2 = Math.max(x2, x);
				if (bst.get(x) % 2 == 0) {
					if (!inRange) {
						x += rasterBounding.getTopX();
						ranges.add(new PixelRange(y, x, x));
						assert x >= 0;
					}
				} else {
					if (inRange) {
						inRange = false;
						ranges.add(new PixelRange(y + rasterBounding.getTopY(), start + rasterBounding.getTopX(),
								x + rasterBounding.getTopX()));
						assert (x + rasterBounding.getTopX() >= 0);
						assert (start + rasterBounding.getTopX() >= 0);
					} else {
						inRange = true;
						start = x;
					}
				}
			}
		}

		return ranges;
	}

	private void incrementSet(BST<Integer, Integer> bst, Integer key) {
		Integer num = bst.get(key);
		if (num == null) {
			bst.put(key, 1);
		} else {
			bst.put(key, num + 1);
		}
	}

	// based loosely on:
	// https://bitbucket.org/bdlabucr/beast/src/master/raptor/src/main/java/edu/ucr/cs/bdlab/raptor/Intersections.java
	private void ExtractCells(Leaf<String, Geometry> pr, int pk, Square rasterBounding,
			List<Pair<Geometry, Collection<PixelRange>>> Def) {
		for (Entry<String, Geometry> entry : ((Leaf<String, Geometry>) pr).entries()) {
			Def.add(new Pair<>(entry.geometry(), ExtractCellsPolygon((Polygon) entry.geometry(), pk, rasterBounding)));
		}
	}

	private void addDescendantsLeaves(NonLeaf<String, Geometry> pr, int pk, Square rasterBounding,
			List<Pair<Geometry, Collection<PixelRange>>> Def) {
		for (Node<String, Geometry> n : pr.children()) {
			// I could not find a better way than this:
			if (TreeExtensions.isLeaf(n)) {
				ExtractCells((Leaf<String, Geometry>) n, pk, rasterBounding, Def);
			} else {
				addDescendantsLeaves((NonLeaf<String, Geometry>) n, pk, rasterBounding, Def);
			}
		}
	}

	private Tuple3<OverlapType, Integer, Square> checkQuadrant(int k2Index, Square rasterBounding,
			Rectangle bounding, int lo, int hi, int min, int max) {
		int[] children = k2Raster.getChildren(k2Index);
		int childSize = rasterBounding.getSize() / K2Raster.k;
		for (int i = 0; i < children.length; i++) {
			int child = children[i];
			Square childRasterBounding = rasterBounding.getChildSquare(childSize, i, K2Raster.k);
			if (childRasterBounding.contains(bounding)) {
				return checkQuadrant(child, childRasterBounding, bounding, lo, hi, min + k2Raster.getLMin(child),
						max - k2Raster.getLMax(child));
			}
		}
		boolean contained = rasterBounding.contains(bounding);
		if (contained && lo <= min && hi >= max) {
			return new Tuple3<>(OverlapType.TotalOverlap, k2Index, rasterBounding);
		} else if (contained) {
			return new Tuple3<>(OverlapType.PartialOverlap, k2Index, rasterBounding);
		} else {
			return new Tuple3<>(OverlapType.NoOverlap, k2Index, rasterBounding);
		}
	}

	public List<Pair<Geometry, Collection<PixelRange>>> join() {
		return join(0, Integer.MAX_VALUE);
	}

	// based on:
	// https://journals.plos.org/plosone/article/file?id=10.1371/journal.pone.0226943&type=printable
	public List<Pair<Geometry, Collection<PixelRange>>> join(int lo, int hi) { // should maybe return pair of such lists
		List<Pair<Geometry, Collection<PixelRange>>> Def = new ArrayList<>(), Prob = new ArrayList<>();
		Stack<Tuple3<Node<String, Geometry>, Integer, Square>> S = new Stack<>();

		for (Node<String, Geometry> node : TreeExtensions.getChildren(tree.root().get())) {
			S.push(new Tuple3<>(node, 0, new Square(0, 0, k2Raster.getSize())));
		}

		while (!S.empty()) {
			Tuple3<Node<String, Geometry>, Integer, Square> p = S.pop();
			int[] range = k2Raster.getValueRange();
			Tuple3<OverlapType, Integer, Square> checked = checkQuadrant(p.b, p.c, p.a.geometry().mbr(), lo, hi, range[0],
					range[1]);
			if (checked.a == OverlapType.TotalOverlap) {
				if (TreeExtensions.isLeaf(p.a)) {
					ExtractCells((Leaf<String, Geometry>) p.a, checked.b, checked.c, Def);
				} else {
					addDescendantsLeaves((NonLeaf<String, Geometry>) p.a, checked.b, checked.c, Def);
				}
			} else if (checked.a == OverlapType.PossibleOverlap) {
				if (!TreeExtensions.isLeaf(p.a)) {
					for (Node<String, Geometry> c : ((NonLeaf<String, Geometry>) p.a).children()) {
						S.push(new Tuple3<Node<String, Geometry>, Integer, Square>(c, checked.b, checked.c));
					}
				} else {
					System.out.println("This should not happen");
					// do checkMBR business
				}
			}
		}

		return Def; // new Pair<>(Def,Prob)
	}
}

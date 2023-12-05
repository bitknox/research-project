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
import dk.itu.raven.util.Tuple4;
import dk.itu.raven.util.Tuple5;
import dk.itu.raven.geometry.PixelRange;
import dk.itu.raven.geometry.Polygon;
import dk.itu.raven.ksquared.K2Raster;
import dk.itu.raven.util.BST;
import dk.itu.raven.util.Pair;

public class RavenJoin {
	private enum QuadOverlapType {
		TotalOverlap,
		PossibleOverlap,
		NoOverlap;
	}

	private enum MBROverlapType {
		TotalOverlap,
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

			// int miny = (int) Math.round(Math.min(old.y(), next.y()));
			// int maxy = (int) Math.round(Math.max(old.y(), next.y()));
			int miny = (int) Math.min(rasterBounding.getTopY() + rasterBounding.getSize(), Math.max(rasterBounding.getTopY(), Math.round(Math.min(old.y(), next.y()))));
          	int maxy = (int) Math.min(rasterBounding.getTopY() + rasterBounding.getSize(), Math.max(rasterBounding.getTopY(), Math.round(Math.max(old.y(), next.y()))));

			for (int y = miny; y < maxy; y++) {
				if (miny == maxy) {
					if (Math.round(b * (y+0.5) - c) == 0) {
						int start = (int) Math.round(Math.min(old.x(), next.x())) - rasterBounding.getTopX();
						int end = (int) Math.round(Math.max(old.x(), next.x())) - rasterBounding.getTopX();
						BST<Integer, Integer> bst = intersections.get(y - rasterBounding.getTopY());
						incrementSet(bst, start);
						incrementSet(bst, end);
					}
				} else {
					double x = (c - b * (y+0.5)) / a;
					assert x - rasterBounding.getTopX() >= 0;
					int ix = (int) Math.round(x - rasterBounding.getTopX());
					ix = Math.min(rasterBounding.getSize(), Math.max(ix, 0));
					BST<Integer, Integer> bst = intersections.get(y - rasterBounding.getTopY());
					incrementSet(bst, ix);
				}
			}
			old = next;
		}

		Collection<PixelRange> ranges = new ArrayList<>();
		for (int y = 0; y < rasterBounding.getSize(); y++) {
			BST<Integer, Integer> bst = intersections.get(y);
			boolean inRange = false;
			int start = 0;
			for (int x : bst.keys()) {
				if ((bst.get(x) & 1) == 0) {
					if (!inRange) {
						ranges.add(new PixelRange(y + rasterBounding.getTopY(), x + rasterBounding.getTopX(), x + rasterBounding.getTopX()));
						assert x >= 0;
					}
				} else {
					if (inRange) {
						inRange = false;
						ranges.add(new PixelRange(y + rasterBounding.getTopY(), start + rasterBounding.getTopX(),
								x + rasterBounding.getTopX()));
						assert (x >= 0);
						assert (start >= 0);
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
			List<Pair<Geometry, Collection<PixelRange>>> def) {
		for (Entry<String, Geometry> entry : ((Leaf<String, Geometry>) pr).entries()) {
			def.add(new Pair<>(entry.geometry(), ExtractCellsPolygon((Polygon) entry.geometry(), pk, rasterBounding)));
		}
	}

	private void addDescendantsLeaves(NonLeaf<String, Geometry> pr, int pk, Square rasterBounding,
			List<Pair<Geometry, Collection<PixelRange>>> def) {
		for (Node<String, Geometry> n : pr.children()) {
			// I could not find a better way than this:
			if (TreeExtensions.isLeaf(n)) {
				ExtractCells((Leaf<String, Geometry>) n, pk, rasterBounding, def);
			} else {
				addDescendantsLeaves((NonLeaf<String, Geometry>) n, pk, rasterBounding, def);
			}
		}
	}

	//FIXME: should not always return the same index and rasterBounding as the one given to it
	private Tuple5<QuadOverlapType, Integer, Square, Integer, Integer> checkQuadrant(int k2Index, Square rasterBounding,
			Rectangle bounding, int lo, int hi, int min, int max) {
		int minSeen = min;
		int maxSeen = max;
		int returnedK2Index = k2Index;
		Square returnedrasterBounding = rasterBounding;
		Stack<Tuple4<Integer, Square, Integer, Integer>> k2Nodes = new Stack<>();
		k2Nodes.push(new Tuple4<>(k2Index, rasterBounding, min, max));
		while (!k2Nodes.empty()) {
			Tuple4<Integer, Square, Integer, Integer> node = k2Nodes.pop();
			int[] children = k2Raster.getChildren(node.a);
			int childSize = node.b.getSize() / K2Raster.k;
			for (int i = 0; i < children.length; i++) {
				int child = children[i];
				Square childRasterBounding = node.b.getChildSquare(childSize, i, K2Raster.k);
				if (childRasterBounding.contains(bounding)) {
					minSeen = k2Raster.computeVMin(node.d, node.c, child);
					maxSeen = k2Raster.computeVMax(node.d, child);
					k2Nodes.push(new Tuple4<>(child, childRasterBounding, k2Raster.computeVMin(node.d, node.c, child), k2Raster.computeVMax(node.d, child)));
					returnedK2Index = child;
					returnedrasterBounding = childRasterBounding;
					break;
				}
			}
		}

		if (lo <= minSeen && hi >= maxSeen) {
			return new Tuple5<>(QuadOverlapType.TotalOverlap, returnedK2Index, returnedrasterBounding, minSeen, maxSeen);
		} else if (minSeen > hi || maxSeen < lo) {
			return new Tuple5<>(QuadOverlapType.NoOverlap, returnedK2Index, returnedrasterBounding, minSeen, maxSeen);
		} else {
			return new Tuple5<>(QuadOverlapType.PossibleOverlap, returnedK2Index, returnedrasterBounding, minSeen, maxSeen);
		}
	}

	private MBROverlapType checkMBR(int k2Index, Square rasterBounding, Rectangle bounding,
			int lo, int hi, int min, int max) {
		// System.out.println("checking MBR");
		int minSeen = Integer.MAX_VALUE;
		int maxSeen = Integer.MIN_VALUE;
		Stack<Tuple4<Integer, Square, Integer, Integer>> k2Nodes = new Stack<>();
		k2Nodes.push(new Tuple4<>(k2Index, rasterBounding, min, max));
		while (!k2Nodes.empty()) {
			Tuple4<Integer, Square, Integer, Integer> node = k2Nodes.pop();
			int[] children = k2Raster.getChildren(node.a);
			int childSize = node.b.getSize() / K2Raster.k;
			for (int i = 0; i < children.length; i++) {
				int child = children[i];
				Square childRasterBounding = node.b.getChildSquare(childSize, i, K2Raster.k);
				if (childRasterBounding.intersects(bounding)) {
					if (childRasterBounding.isContained(bounding)) {
						minSeen = Math.min(minSeen, k2Raster.computeVMin(node.d, node.c, child));
						maxSeen = Math.max(maxSeen, k2Raster.computeVMax(node.d, child));
					} else {
						k2Nodes.push(new Tuple4<>(child, childRasterBounding, k2Raster.computeVMin(node.d, node.c, child), k2Raster.computeVMax(node.d, child)));
					}
				}
			}
		}
		if (minSeen >= lo && maxSeen <= hi) {
			// System.out.println("total");
			return MBROverlapType.TotalOverlap;
		} else if (minSeen > maxSeen || maxSeen < lo) {
			// System.out.println("no");
			return MBROverlapType.NoOverlap;
		} else {
			// System.out.println("partial");
			return MBROverlapType.PartialOverlap;
		}
	}

	public List<Pair<Geometry, Collection<PixelRange>>> join() {
		return join(Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	// based on:
	// https://journals.plos.org/plosone/article/file?id=10.1371/journal.pone.0226943&type=printable
	public List<Pair<Geometry, Collection<PixelRange>>> join(int lo, int hi) { // should maybe return pair of such lists
		List<Pair<Geometry, Collection<PixelRange>>> def = new ArrayList<>(), prob = new ArrayList<>();
		Stack<Tuple3<Node<String, Geometry>, Integer, Square>> S = new Stack<>();

		for (Node<String, Geometry> node : TreeExtensions.getChildren(tree.root().get())) {
			S.push(new Tuple3<>(node, 0, new Square(0, 0, k2Raster.getSize())));
		}

		while (!S.empty()) {
			Tuple3<Node<String, Geometry>, Integer, Square> p = S.pop();
			int[] range = k2Raster.getValueRange();
			Tuple5<QuadOverlapType, Integer, Square, Integer, Integer> checked = checkQuadrant(p.b, p.c, p.a.geometry().mbr(),
					lo, hi, range[0],
					range[1]);
			switch (checked.a) {
				case TotalOverlap:
					if (TreeExtensions.isLeaf(p.a)) {
						ExtractCells((Leaf<String, Geometry>) p.a, checked.b, checked.c, def);
					} else {
						addDescendantsLeaves((NonLeaf<String, Geometry>) p.a, checked.b, checked.c, def);
					}
					break;
				case PossibleOverlap:
					if (!TreeExtensions.isLeaf(p.a)) {
						for (Node<String, Geometry> c : ((NonLeaf<String, Geometry>) p.a).children()) {
							S.push(new Tuple3<Node<String, Geometry>, Integer, Square>(c, checked.b, checked.c));
						}
					} else {
						MBROverlapType overlap = checkMBR(checked.b, checked.c, p.a.geometry().mbr(), lo, hi, checked.d, checked.e);
						switch (overlap) {
							case TotalOverlap:
								ExtractCells((Leaf<String, Geometry>) p.a, checked.b, checked.c, def);
								break;
							case PartialOverlap:
								ExtractCells((Leaf<String, Geometry>) p.a, checked.b, checked.c, prob);
								break;
							case NoOverlap:
								// ignored
								break;
						}
					}
					break;
				case NoOverlap:
					// ignored
					break;
			}
		}

		// combineLists(def, prob, lo, hi);

		return def;
	}

	public void combineLists(List<Pair<Geometry, Collection<PixelRange>>> def,
			List<Pair<Geometry, Collection<PixelRange>>> prob, int lo, int hi) {
		System.out.println("def: " + def.size() + ", prob: " + prob.size());
		for (Pair<Geometry, Collection<PixelRange>> pair : prob) {
			Pair<Geometry, Collection<PixelRange>> result = new Pair<>(pair.first, new ArrayList<>());
			for (PixelRange range : pair.second) {
				int[] values = k2Raster.getWindow(range.row, range.row, range.x1, range.x2);
				for (int i = 0; i < values.length; i++) {
					int start = i;
					while (i < values.length && values[i] >= lo && values[i] <= hi)
						i++;
					if (start != i) {
						result.second.add(new PixelRange(range.row, start + range.x1, i - 1 + range.x1));
					}
				}
			}
			def.add(result);
		}
	}
}

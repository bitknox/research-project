package dk.itu.raven.join;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Stack;

import com.github.davidmoten.rtree2.RTree;
import com.github.davidmoten.rtree2.Leaf;
import com.github.davidmoten.rtree2.Node;
import com.github.davidmoten.rtree2.NonLeaf;
import com.github.davidmoten.rtree2.geometry.Geometries;
import com.github.davidmoten.rtree2.geometry.Geometry;
import com.github.davidmoten.rtree2.geometry.Point;
import com.github.davidmoten.rtree2.geometry.Rectangle;

import dk.itu.raven.util.TreeExtensions;
import dk.itu.raven.util.Tuple3;
import dk.itu.raven.ksquared.K2Raster;
import dk.itu.raven.util.BST;
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

	// protected boolean pointInPolygon(Polygon polygon, int x, int y) {
	// Point old = polygon.getFirst();
	// int linesCrossed = 0;
	// for (Point next : polygon) {
	// double a = (next.y() - old.y());
	// double b = (old.x() - next.x());
	// double c = old.y() * (next.x() - old.x()) - (next.y() - old.y()) * old.x();
	// double side = a*x+b*y-c;
	// linesCrossed += (side < 0) ? 1 : 0;
	// old = next;
	// }

	// return (linesCrossed % 2) == 1;
	// }

	/*
	 * TODO:
	 * instead of sorting indices, store a boolean array of whether there is an
	 * intersection at a given x coordinate (rounded to nearest integer).
	 * Then do a linear pass over this array to store the indices in sorted order.
	 */

	protected Collection<PixelRange> ExtractCellsPolygon(Polygon polygon, int pk, Square rasterBounding) {
		// find out whether the left-most cell of each row of rasterBounding intersects
		// the polygon

		// 1 on index i * rasterBounding.geetSize() + j if an intersection between a
		// line of the polygon and the line y=j happens at point (i,j)
		// BitMap hasIntersection = new BitMap(rasterBounding.getSize() *
		// rasterBounding.getSize());
		// hasIntersection.get(rasterBounding.getSize()*rasterBounding.getSize());
		// 1 on index i if the left-most pixel of row i intersects the polygon, 0
		// otherwise
		// BitMap leftIncluded = new BitMap(rasterBounding.getSize());
		List<BST<Integer, Integer>> intersections = new ArrayList<BST<Integer, Integer>>(rasterBounding.getSize());
		for (int i = 0; i < rasterBounding.getSize(); i++) {
			intersections.add(i, new BST<>());
		}

		// leftIncluded.setTo(0, pointInPolygon(polygon, rasterBounding.getTopX(),
		// rasterBounding.getTopY())? 1 : 0);

		// a line is of the form a*x + b*y + c = 0
		Point old = polygon.getFirst();
		// int linesCrossed = 0;
		for (Point next : polygon) {
			double a = (next.y() - old.y());
			double b = (old.x() - next.x());
			double c = old.y() * (next.x() - old.x()) - (next.y() - old.y()) * old.x();
			// double side = a*x+b*y-c;
			// by-c = 0
			int miny = (int) Math.round(Math.min(old.y(), next.y()));
			int maxy = (int) Math.round(Math.max(old.y(), next.y()));
			for (int y = rasterBounding.getTopY(); y < rasterBounding.getTopY() + rasterBounding.getSize(); y++) {
				if (y < miny || y > maxy)
					continue;
				if (a == 0.0) {
					if (Math.round(b * y + c) == 0) {
						int start = (int) Math.round(Math.min(old.x(), next.x())) - rasterBounding.getTopX();
						int end = (int) Math.round(Math.max(old.x(), next.x())) - rasterBounding.getTopX();
						for (int x = start; x <= end; x++) {
							// hasIntersection.flip((y - rasterBounding.getTopY()) *
							// rasterBounding.getSize() + x);
							BST<Integer, Integer> bst = intersections.get(y - rasterBounding.getTopY());
							incrementSet(bst, x);
						}
					}
				} else {
					double x = -(b * y + c) / a;
					int ix = (int) Math.round(x - rasterBounding.getTopX()); // TODO: maybe fix
					// hasIntersection.flip((y - rasterBounding.getTopY()) *
					// rasterBounding.getSize() + ix);
					BST<Integer, Integer> bst = intersections.get(y - rasterBounding.getTopY());
					incrementSet(bst, ix);
				}
			}

			// linesCrossed += (side < 0) ? 1 : 0;
			old = next;
		}

		// for (int y = 0; y < rasterBounding.getSize(); y++) {
		// for (int x = 0; x < rasterBounding.getSize(); x++) {
		// System.out.print(hasIntersection.isSet(y*rasterBounding.getSize()+x) ? "#" :
		// " ");
		// }
		// System.out.println();
		// }

		Collection<PixelRange> ranges = new ArrayList<>();
		// for (int y = 0; y < rasterBounding.getSize(); y++) {
		// boolean inRange = false;
		// int start = 0;
		// for (int x = 0; x < rasterBounding.getSize(); x++) {
		// if (hasIntersection.isSet(y*rasterBounding.getSize()+x)) {
		// if (inRange) {
		// inRange = false;
		// ranges.add(new PixelRange(y+rasterBounding.getTopY(),
		// start+rasterBounding.getTopX(), x+rasterBounding.getTopX()));
		// } else {
		// inRange = true;
		// start = x;
		// }
		// }
		// }
		// }

		for (int y = 0; y < rasterBounding.getSize(); y++) {
			BST<Integer, Integer> bst = intersections.get(y);
			boolean inRange = false;
			int start = 0;
			for (int x : bst.keys()) {
				if (bst.get(x) > 1) {
					ranges.add(new PixelRange(y, x, x));
				} else {
					if (inRange) {
						inRange = false;
						ranges.add(new PixelRange(y + rasterBounding.getTopY(), start + rasterBounding.getTopX(),
								x + rasterBounding.getTopX()));
					} else {
						inRange = true;
						start = x;
					}
				}

				// if (hasIntersection.isSet(y * rasterBounding.getSize() + x)) {
				// }
			}
		}

		// construct pixel ranges in linear time using the idea from the todo above
		return ranges;
		// throw new UnsupportedOperationException("Unimplemented method
		// 'ExtractCellsPolygon'");
	}

	private void incrementSet(BST<Integer, Integer> bst, Integer key) {
		Integer num = bst.get(key);
		if (num == null) {
			bst.put(key, 1);
		} else {
			bst.put(key, num + 1);
		}
	}

	public static void main(String[] args) {
		List<Point> points = new ArrayList<>();
		points.add(Geometries.point(0, 0));
		points.add(Geometries.point(5, 5));
		// points.add(Geometries.point(20, 0));
		// points.add(Geometries.point(20, 30));
		// points.add(Geometries.point(10, 15));
		// points.add(Geometries.point(0, 30));
		Polygon poly = new Polygon(points);

		Square square = new Square(0, 0, 30);
		RavenJoin join = new RavenJoin(null, null);
		System.out.println(join.ExtractCellsPolygon(poly, 0, square).size());
	}

	// based loosely on:
	// https://bitbucket.org/bdlabucr/beast/src/master/raptor/src/main/java/edu/ucr/cs/bdlab/raptor/Intersections.java
	private Collection<PixelRange> ExtractCells(Leaf<String, Geometry> pr, int pk, Square rasterBounding) {
		return ExtractCellsPolygon((Polygon) pr.geometry(), pk, rasterBounding);
	}

	private void addDescendantsLeaves(NonLeaf<String, Geometry> pr, int pk, Square rasterBounding,
			List<Pair<Geometry, Collection<PixelRange>>> Def) {
		for (Node<String, Geometry> n : pr.children()) {
			// I could not find a better way than this:
			if (TreeExtensions.isLeaf(n)) {
				Def.add(new Pair<>(n.geometry(), ExtractCells((Leaf<String, Geometry>) n, pk, rasterBounding)));
			} else {
				addDescendantsLeaves((NonLeaf<String, Geometry>) n, pk, rasterBounding, Def);
			}
		}
	}

	private Tuple3<OverlapType, Integer, Square> checkQuandrant(int k2Index, Square rasterBounding,
			Rectangle bounding) {
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

	// based on:
	// https://journals.plos.org/plosone/article/file?id=10.1371/journal.pone.0226943&type=printable
	public List<Pair<Geometry, Collection<PixelRange>>> join() { // should maybe return pair of such lists
		List<Pair<Geometry, Collection<PixelRange>>> Def = new ArrayList<>(), Prob = new ArrayList<>();
		Stack<Tuple3<Node<String, Geometry>, Integer, Square>> S = new Stack<>();

		for (Node<String, Geometry> node : TreeExtensions.getChildren(tree.root().get())) {
			System.out.println(node.toString());
			S.push(new Tuple3<>(node, 0, new Square(0, 0, k2Raster.getSize())));
		}

		while (!S.empty()) {
			Tuple3<Node<String, Geometry>, Integer, Square> p = S.pop();
			Tuple3<OverlapType, Integer, Square> checked = checkQuandrant(p.b, p.c, p.a.geometry().mbr());
			if (checked.a == OverlapType.TotalOverlap) {
				if (TreeExtensions.isLeaf(p.a)) {
					Def.add(new Pair<>(p.a.geometry(),
							ExtractCells((Leaf<String, Geometry>) p.a, checked.b, checked.c)));
				} else {
					addDescendantsLeaves((NonLeaf<String, Geometry>) p.a, checked.b, checked.c, Def);
				}
			} else if (checked.a == OverlapType.PossibleOverlap) {
				if (!TreeExtensions.isLeaf(p.a)) {
					for (Node<String, Geometry> c : ((NonLeaf<String, Geometry>) p.a).children()) {
						S.push(new Tuple3<Node<String, Geometry>, Integer, Square>(c, checked.b, checked.c));
					}
				} else {
					// do checkMBR business
				}
			}
		}

		return Def; // new Pair<>(Def,Prob)
	}
}

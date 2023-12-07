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
import dk.itu.raven.util.Tuple4;
import dk.itu.raven.util.Tuple5;
import dk.itu.raven.geometry.PixelRange;
import dk.itu.raven.geometry.Polygon;
import dk.itu.raven.ksquared.K2Raster;
import dk.itu.raven.util.BST;
import dk.itu.raven.util.Pair;
import dk.itu.raven.util.Logger;

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

	/**
	 * 
	 * @param polygon the vector shape
	 * @param pk an index of a node in the k2 raster tree
	 * @param rasterBounding teh bounding box of the sub-matrix corresponding to the node with index {@code pk} in the k2 raster tree
	 * @return A collection of pixels that are contained in the vector shape described by {@code polygon}
	 */
	protected Collection<PixelRange> ExtractCellsPolygon(Polygon polygon, int pk, Square rasterBounding, int maxX) {
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
			// compute the standard form of teh line segment between the points old and next
			double a = (next.y() - old.y());
			double b = (old.x() - next.x());
			double c = a * old.x() + b * old.y();

			int miny = (int) Math.min(rasterBounding.getTopY() + rasterBounding.getSize(), Math.max(rasterBounding.getTopY(), Math.round(Math.min(old.y(), next.y()))));
			int maxy = (int) Math.min(rasterBounding.getTopY() + rasterBounding.getSize(), Math.max(rasterBounding.getTopY(), Math.round(Math.max(old.y(), next.y()))));

			// compute all intersections between the line segment and horizontal pixel lines
			for (int y = miny; y < maxy; y++) {
				// if (miny == maxy) { // horizontal line segment
				// 	if (Math.round(b * (y+0.5) - c) == 0) {
				// 		int start = (int) Math.floor(Math.min(old.x(), next.x())) - rasterBounding.getTopX();
				// 		int end = (int) Math.ceil(Math.max(old.x(), next.x())) - rasterBounding.getTopX();
				// 		BST<Integer, Integer> bst = intersections.get(y - rasterBounding.getTopY());
				// 		incrementSet(bst, start);
				// 		incrementSet(bst, end);
				// 	}
				// } else {
				double x = (c - b * (y+0.5)) / a;
				assert x - rasterBounding.getTopX() >= 0;
				int ix = (int) Math.floor(x - rasterBounding.getTopX());
				ix = Math.min(rasterBounding.getSize(), Math.max(ix, 0));
				ix = Math.min(maxX, ix);
				BST<Integer, Integer> bst = intersections.get(y - rasterBounding.getTopY());
				incrementSet(bst, ix);
				// }
			}
			old = next;
		}

		Collection<PixelRange> ranges = new ArrayList<>();
		for (int y = 0; y < rasterBounding.getSize(); y++) {
			BST<Integer, Integer> bst = intersections.get(y);
			boolean inRange = false;
			int start = 0;
			for (int x : bst.keys()) {
				if ((bst.get(x) % 2) == 0) { // an even number of intersections happen at this point
					if (!inRange) {
						// if a range is ongoing, ignore these intersections, otherwise add this single pixel as a range
						ranges.add(new PixelRange(y + rasterBounding.getTopY(), x + rasterBounding.getTopX(), x + rasterBounding.getTopX()));
					}
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
			}
		}

		return ranges;
	}

	/**
	 * increments the stored number of intersections that happen at the given x-ordinate
	 * @param bst a set of intersections
	 * @param key an x-ordinate of an intersection
	 */
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
			List<Pair<Geometry, Collection<PixelRange>>> def, int maxX) {
		for (Entry<String, Geometry> entry : ((Leaf<String, Geometry>) pr).entries()) {
			// all geometries we store are polygons
			def.add(new Pair<>(entry.geometry(), ExtractCellsPolygon((Polygon) entry.geometry(), pk, rasterBounding, maxX)));
		}
	}

	/**
	 * adds all descendant geometries of a given R*-tree node
	 * @param pr the node of the R*-tree
	 * @param pk the index of the k2 raster tree node
	 * @param rasterBounding the bounding box of the sum-matrix corresponding to the node with index {@code pk} in the k2 raster tree
	 * @param def the list all the pixelranges should be added to
	 */
	private void addDescendantsLeaves(NonLeaf<String, Geometry> pr, int pk, Square rasterBounding,
			List<Pair<Geometry, Collection<PixelRange>>> def, int maxX) {
		for (Node<String, Geometry> n : pr.children()) {
			if (TreeExtensions.isLeaf(n)) {
				ExtractCells((Leaf<String, Geometry>) n, pk, rasterBounding, def, maxX);
			} else {
				addDescendantsLeaves((NonLeaf<String, Geometry>) n, pk, rasterBounding, def, maxX);
			}
		}
	}

	/**
	 * Finds the smallest node of the k2 raster tree that fully contains the given bounding box 
	 * @param k2Index the index of the starting node in the k2 raster tree. This node should always contain {@code bounding}
	 * @param rasterBounding the bounding box of the sum-matrix corresponding to the node with index {@code k2Index} in the k2 raster tree
	 * @param bounding the bounding rectangle of some geometry
	 * @param lo the minimum pixel-value we are looking for
	 * @param hi the maximum pixel-value we are looking for
	 * @param min the value of VMin for the node with index {@code k2Index} in the k2 raster tree
	 * @param max the value of VMax for the node with index {@code k2Index} in the k2 raster tree
	 * @return a 5-tuple (OverlapType, k2Index', rasterBounding', min', max')
	 * where:
	 * <ul>
	 * <li>OverlapType is one of {TotalOverlap, PossibleOverlap, NoOverlap}.</li>
	 * <li>k2Index' is the smallest k2 raster node that fully contains {@code bounding}</li>
	 * <li>rasterBounding' is the bounding box of k2Index'</li>
	 * <li>min' is the value of VMin for the node with index k2Index' in the k2 raster tree</li>
	 * <li>max' is the value of VMax for the node with index k2Index' in the k2 raster tree</li>
	 * </ul>
	 */
	private Tuple5<QuadOverlapType, Integer, Square, Integer, Integer> checkQuadrant(int k2Index, Square rasterBounding,
			Rectangle bounding, int lo, int hi, int min, int max) {
		int vMinMBR = min;
		int vMaxMBR = max;
		Logger.log(vMinMBR + ", " + vMaxMBR);
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
					vMinMBR = k2Raster.computeVMin(node.d, node.c, child);
					vMaxMBR = k2Raster.computeVMax(node.d, child);

					k2Nodes.push(new Tuple4<>(child, childRasterBounding, vMinMBR, vMaxMBR));
					returnedK2Index = child;
					returnedrasterBounding = childRasterBounding;
					// break;
				}
			}
		}

		Logger.log(vMinMBR + ", " + vMaxMBR);

		if (lo <= vMinMBR && hi >= vMaxMBR) {
			Logger.log("total overlap for " + returnedrasterBounding + " with mbr " + bounding);
			return new Tuple5<>(QuadOverlapType.TotalOverlap, returnedK2Index, returnedrasterBounding, vMinMBR, vMaxMBR);
		} else if (vMinMBR > hi || vMaxMBR < lo) {
			return new Tuple5<>(QuadOverlapType.NoOverlap, returnedK2Index, returnedrasterBounding, vMinMBR, vMaxMBR);
		} else {
			return new Tuple5<>(QuadOverlapType.PossibleOverlap, returnedK2Index, returnedrasterBounding, vMinMBR, vMaxMBR);
		}
	}


	private MBROverlapType checkMBR(int k2Index, Square rasterBounding, Rectangle bounding,
			int lo, int hi, int min, int max) {
		int vMinMBR = Integer.MAX_VALUE;
		int vMaxMBR = Integer.MIN_VALUE;

		Stack<Tuple4<Integer, Square, Integer, Integer>> k2Nodes = new Stack<>();
		k2Nodes.push(new Tuple4<>(k2Index, rasterBounding, min, max));

		while (!k2Nodes.empty()) {
			Tuple4<Integer, Square, Integer, Integer> node = k2Nodes.pop();
			int[] children = k2Raster.getChildren(node.a);
			int childSize = node.b.getSize() / K2Raster.k;

			if (children.length == 0 && rasterBounding.intersects(bounding)) {
				vMinMBR = Math.min(k2Raster.computeVMax(node.d, node.a), vMinMBR);
				vMaxMBR = Math.max(k2Raster.computeVMax(node.d, node.a), vMaxMBR);
			}

			for (int i = 0; i < children.length; i++) {
				int child = children[i];
				Square childRasterBounding = node.b.getChildSquare(childSize, i, K2Raster.k);
				
				if (childRasterBounding.intersects(bounding)) {
					int vminVal = k2Raster.computeVMin(node.d, node.c, child);
					int vmaxVal = k2Raster.computeVMax(node.d, child);
					if (childRasterBounding.isContained(bounding)) {
						vMinMBR = Math.min(vminVal, vMinMBR);
						vMaxMBR = Math.max(vmaxVal, vMaxMBR);
					} else {
						k2Nodes.push(new Tuple4<>(child, childRasterBounding, vminVal, vmaxVal));
					}
				}
			}
		}
		if (vMinMBR == Integer.MAX_VALUE || vMaxMBR == Integer.MIN_VALUE) {
			throw new RuntimeException("rasterBounding was never contained in bounding");
		}
		if (vMinMBR >= lo && vMaxMBR <= hi) {
			return MBROverlapType.TotalOverlap;
		} else if (vMinMBR > hi || vMaxMBR < lo) {
			return MBROverlapType.NoOverlap;
		} else {
			return MBROverlapType.PartialOverlap;
		}
	}

	/**
	 * joins without filtering based on values
	 * @return a list of Geometries paired with a collection of the pixelranges that it contains
	 */
	public List<Pair<Geometry, Collection<PixelRange>>> join() {
		return join(Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	// based on:
	// https://journals.plos.org/plosone/article/file?id=10.1371/journal.pone.0226943&type=printable
	/**
	 * joins while filtering based on values
	 * @param lo the minimum pixel-value that should be included in the join
	 * @param hi the maximum pixel-value that should be included in the join
	 * @return a list of Geometries paired with a collection of the pixelranges, whose values fall within the given range, that it contains
	 */
	public List<Pair<Geometry, Collection<PixelRange>>> join(int lo, int hi) {
		List<Pair<Geometry, Collection<PixelRange>>> def = new ArrayList<>(), prob = new ArrayList<>();
		Stack<Tuple5<Node<String, Geometry>, Integer, Square,Integer,Integer>> S = new Stack<>();

		int[] minmax = k2Raster.getValueRange();

		for (Node<String, Geometry> node : TreeExtensions.getChildren(tree.root().get())) {
			S.push(new Tuple5<>(node, 0, new Square(0, 0, k2Raster.getSize()), minmax[0], minmax[1]));
		}

		while (!S.empty()) {
			Tuple5<Node<String, Geometry>, Integer, Square, Integer, Integer> p = S.pop();
			Tuple5<QuadOverlapType, Integer, Square, Integer, Integer> checked = checkQuadrant(p.b, p.c, p.a.geometry().mbr(),
					lo, hi, p.d,
					p.e);
			switch (checked.a) {
				case TotalOverlap:
					if (TreeExtensions.isLeaf(p.a)) {
						ExtractCells((Leaf<String, Geometry>) p.a, checked.b, checked.c, def, k2Raster.getSize()-1);
					} else {
						addDescendantsLeaves((NonLeaf<String, Geometry>) p.a, checked.b, checked.c, def, k2Raster.getSize()-1);
					}
					break;
				case PossibleOverlap:
					if (!TreeExtensions.isLeaf(p.a)) {
						for (Node<String, Geometry> c : ((NonLeaf<String, Geometry>) p.a).children()) {
							S.push(new Tuple5<Node<String, Geometry>, Integer, Square, Integer, Integer>(c, checked.b, checked.c,checked.d, checked.e));
						}
					} else {
						MBROverlapType overlap = checkMBR(checked.b, checked.c, p.a.geometry().mbr(), lo, hi, checked.d, checked.e);
						switch (overlap) {
							case TotalOverlap:
								ExtractCells((Leaf<String, Geometry>) p.a, checked.b, checked.c, def, k2Raster.getSize()-1);
								break;
							case PartialOverlap:
								ExtractCells((Leaf<String, Geometry>) p.a, checked.b, checked.c, prob, k2Raster.getSize()-1);
								Logger.log(p.a.geometry().mbr());
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

		combineLists(def, prob, lo, hi);

		return def;
	}

	protected void combineLists(List<Pair<Geometry, Collection<PixelRange>>> def,
			List<Pair<Geometry, Collection<PixelRange>>> prob, int lo, int hi) {
		Logger.log("def: " + def.size() + ", prob: " + prob.size());
		for (Pair<Geometry, Collection<PixelRange>> pair : prob) {
			Pair<Geometry, Collection<PixelRange>> result = new Pair<>(pair.first, new ArrayList<>());
			for (PixelRange range : pair.second) {
				int[] values = k2Raster.getWindow(range.row, range.row, range.x1, range.x2);
				for (int i = 0; i < values.length; i++) {
					int start = i;
					while (i < values.length && values[i] >= lo && values[i] <= hi) {
						i++;
					}
					if (start != i) {
						result.second.add(new PixelRange(range.row, start + range.x1, i - 1 + range.x1));
					}
				}
			}
			def.add(result);
		}
	}
}

package dk.itu.raven.join;

import com.github.davidmoten.rtree2.RTree;
import com.github.davidmoten.rtree2.geometry.Geometry;
import com.github.davidmoten.rtree2.geometry.Rectangle;
import dk.itu.raven.util.*;
import dk.itu.raven.ksquared.K2Raster;

public class RavenJoin {
	private enum OverlapType {
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

	private Pair<OverlapType, Integer> checkQuandrant(int k2Index, Square rasterBounding, Rectangle bounding) {
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
			return new Pair<>(OverlapType.TotalOverlap, k2Index);
		} else {
			return new Pair<>(OverlapType.NoOverlap, k2Index);
		}
	}

	public void join() {

	}
}

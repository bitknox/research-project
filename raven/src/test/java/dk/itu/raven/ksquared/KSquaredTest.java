package dk.itu.raven.ksquared;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Random;
import java.util.Stack;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.RepeatedTest;

import dk.itu.raven.util.matrix.RandomMatrix;
import dk.itu.raven.join.Square;
import dk.itu.raven.util.matrix.ArrayMatrix;
import dk.itu.raven.util.matrix.Matrix;

public class KSquaredTest {
	private final int[][] M = {
			{ 5, 5, 4, 4, 4, 4, 1, 1 }, //
			{ 5, 4, 4, 4, 4, 4, 1, 1 }, //
			{ 4, 4, 4, 4, 1, 2, 2, 1 }, //
			{ 3, 3, 4, 3, 2, 1, 2, 2 }, //
			{ 3, 4, 3, 3, 2, 2, 2, 2 }, //
			{ 4, 3, 3, 2, 2, 2, 2, 2 }, //
			{ 1, 1, 1, 3, 2, 2, 2, 2 }, //
			{ 1, 1, 1, 2, 2, 2, 2, 2 } }; //

	@RepeatedTest(10)
	public void testGetWindowRow() {
		Random r = new Random();
		Matrix matrix = new RandomMatrix(100, 100, 1000000);
		K2Raster k2Raster = new K2Raster(matrix);
		int row = r.nextInt(100);
		int col1 = r.nextInt(25);
		int col2 = 75 + r.nextInt(25);
		int[] res = k2Raster.getWindow(row, row, col1, col2);
		for (int i = 0; i < res.length; i++) {
			assertEquals(res[i], matrix.get(row, col1 + i));
		}
	}

	private void testElements(int[] a1, int[] a2) {
		assertEquals(a1.length, a2.length);
		for (int i = 0; i < a1.length; i++) {
			assertEquals(a1[i], a2[i]);
		}
	}

	@Test
	public void testWithNonSquareMatrix() {
		Matrix matrix = new RandomMatrix(2000, 500, 1000000);
		K2Raster k2Raster = new K2Raster(matrix);
		for (int i = 0; i < matrix.getHeight(); i++) {
			int[] row = k2Raster.getWindow(i, i, 0, matrix.getWidth() - 1);
			for (int j = 0; j < matrix.getWidth(); j++) {
				assertEquals(matrix.get(i, j), row[j]);
			}
		}
	}

	@Test
	public void testGetWindowValuesRow() {
		K2Raster k2 = new K2Raster(new ArrayMatrix(M, 8, 8));
		// k2.searchValuesInWindow(0, 7, 0, 7, 1, 1);
		k2.searchValuesInWindowPaper(0, 7, 0, 7, 1, 1);
	}

	@Test
	public void testGetChildren() {
		K2Raster k2 = new K2Raster(new ArrayMatrix(M, 8, 8));
		testElements(k2.getChildren(0), new int[] { 1, 2, 3, 4 });
		testElements(k2.getChildren(1), new int[] { 5, 6, 7, 8 });
		testElements(k2.getChildren(2), new int[] { 9, 10, 11, 12 });
		testElements(k2.getChildren(3), new int[] { 13, 14, 15, 16 });
		testElements(k2.getChildren(4), new int[] {});
		testElements(k2.getChildren(5), new int[] { 17, 18, 19, 20 });
	}

	@Test
	public void testVmin() {
		K2Raster k2 = new K2Raster(new ArrayMatrix(M, 8, 8));
		assertEquals(3, k2.computeVMin(5, 1, 1));
		assertEquals(1, k2.computeVMin(5, 1, 2));
		assertEquals(1, k2.computeVMin(5, 1, 3));
		assertEquals(2, k2.computeVMin(5, 1, 4));
		assertEquals(4, k2.computeVMin(5, 3, 5));
		assertEquals(1, k2.computeVMin(4, 1, 10));
		assertEquals(2, k2.computeVMin(4, 1, 14));
	}

	@Test
	public void testVmax() {
		K2Raster k2 = new K2Raster(new ArrayMatrix(M, 8, 8));
		assertEquals(5, k2.computeVMax(5, 1));
		assertEquals(4, k2.computeVMax(5, 2));
		assertEquals(4, k2.computeVMax(5, 3));
		assertEquals(2, k2.computeVMax(5, 4));
		assertEquals(5, k2.computeVMax(5, 5));
		assertEquals(1, k2.computeVMax(4, 10));
		assertEquals(3, k2.computeVMax(4, 14));
	}

	@RepeatedTest(100)
	public void testHasChildren() {
		Matrix matrix = new RandomMatrix(200, 200, 1);
		K2Raster k2 = new K2Raster(matrix);
		Stack<Square> squares = new Stack<>();
		Stack<Integer> indices = new Stack<>();
		indices.push(0);
		squares.push(new Square(0, 0, k2.getSize()));
		while (!indices.empty()) {
			int index = indices.pop();
			Square square = squares.pop();

			int seen = matrix.get(square.getTopY(), square.getTopX());
			boolean isLeaf = true;

			for (int i = 0; i < square.getSize() && isLeaf; i++) {
				for (int j = 0; j < square.getSize(); j++) {
					if (seen != matrix.get(square.getTopY() + i, square.getTopX() + j)) {
						isLeaf = false;
						break;
					}
				}
			}

			assertEquals(!isLeaf, k2.hasChildren(index));

			int[] children = k2.getChildren(index);
			for (int i = 0; i < children.length; i++) {
				int child = children[i];
				indices.push(child);
				squares.push(square.getChildSquare(square.getSize() / K2Raster.k, i, K2Raster.k));
			}
		}
	}

	@Test
	public void testVMinVMax() {
		Matrix matrix = new RandomMatrix(2000, 2000, 100);
		K2Raster k2 = new K2Raster(matrix);
		Stack<Square> squares = new Stack<>();
		Stack<Integer> indices = new Stack<>();
		Stack<Integer> parentMin = new Stack<>();
		Stack<Integer> parentMax = new Stack<>();
		indices.push(0);
		squares.push(new Square(0, 0, k2.getSize()));
		parentMin.push(0); // value doesn't matter
		parentMax.push(0); // value doesn't matter
		while (!indices.empty()) {
			int index = indices.pop();
			Square square = squares.pop();
			int parmin = parentMin.pop();
			int parmax = parentMax.pop();

			int min = Integer.MAX_VALUE;
			int max = 0;
			boolean isLeaf = true;

			for (int i = 0; i < square.getSize() && isLeaf; i++) {
				for (int j = 0; j < square.getSize(); j++) {
					int val = matrix.get(square.getTopY() + i, square.getTopX() + j);
					min = Math.min(min, val);
					max = Math.max(max, val);
				}
			}

			assertEquals(min, k2.computeVMin(parmax, parmin, index));
			assertEquals(max, k2.computeVMax(parmax, index));

			int[] children = k2.getChildren(index);
			for (int i = 0; i < children.length; i++) {
				int child = children[i];
				indices.push(child);
				squares.push(square.getChildSquare(square.getSize() / K2Raster.k, i, K2Raster.k));
				parentMin.push(min);
				parentMax.push(max);
			}
		}
	}

}

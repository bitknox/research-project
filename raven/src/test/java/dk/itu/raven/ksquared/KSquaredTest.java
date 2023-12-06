package dk.itu.raven.ksquared;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Random;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.RepeatedTest;

import dk.itu.raven.util.matrix.RandomMatrix;
import dk.itu.raven.util.matrix.ArrayMatrix;
import dk.itu.raven.util.matrix.Matrix;

public class KSquaredTest {
	@RepeatedTest(100)
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
		assertEquals(a1.length,a2.length);
		for (int i = 0; i < a1.length; i++) {
			assertEquals(a1[i],a2[i]);
		}
	}
	
	@Test
	public void testWithNonSquareMatrix() {
		Matrix matrix = new RandomMatrix(2000, 500, 1000000);
		K2Raster k2Raster = new K2Raster(matrix);
		for (int i = 0; i < matrix.getHeight(); i++) {
			int[] row = k2Raster.getWindow(0,matrix.getWidth()-1,i,i);
			for (int j = 0; j < matrix.getWidth(); j++) {
				assertEquals(matrix.get(j, i), row[j]);
			}
		}
		
	}

	@Test
	public void testGetChildren() {
		int[][] M = {   {5,5,4,4,4,4,1,1}, //
						{5,4,4,4,4,4,1,1}, //
						{4,4,4,4,1,2,2,1}, //
						{3,3,4,3,2,1,2,2}, //
						{3,4,3,3,2,2,2,2}, //
						{4,3,3,2,2,2,2,2}, //
						{1,1,1,3,2,2,2,2}, //
						{1,1,1,2,2,2,2,2}}; //
		
		K2Raster k2 = new K2Raster(new ArrayMatrix(M, 8, 8));
		testElements(k2.getChildren(0), new int[] {1,2,3,4});
		testElements(k2.getChildren(1), new int[] {5,6,7,8});
		testElements(k2.getChildren(2), new int[] {9,10,11,12});
		testElements(k2.getChildren(3), new int[] {13,14,15,16});
		testElements(k2.getChildren(4), new int[] {});
		testElements(k2.getChildren(5), new int[] {17,18,19,20});
	}

}

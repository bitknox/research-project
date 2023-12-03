package dk.itu.raven.ksquared;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Random;

import org.junit.jupiter.api.RepeatedTest;

import dk.itu.raven.util.matrix.RandomMatrix;
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

}

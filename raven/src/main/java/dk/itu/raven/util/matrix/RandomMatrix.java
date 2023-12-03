package dk.itu.raven.util.matrix;

import java.util.Random;

public class RandomMatrix extends Matrix {
	public int[][] M;

	public RandomMatrix(long seed, int width, int height, int maxValue) {
		super(width, height);
		Random r = new Random(seed);
		M = new int[width][height];
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				int random = r.nextInt(maxValue + 1);
				M[i][j] = random;
			}
		}
	}

	@Override
	public int getWithinRange(int r, int c) {
		return M[r][c];
	}

}

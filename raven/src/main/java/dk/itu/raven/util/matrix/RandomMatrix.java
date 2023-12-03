package dk.itu.raven.util.matrix;

import java.util.Random;

public class RandomMatrix extends Matrix {
	public int[][] M;

	public RandomMatrix(long seed, int width, int height, int maxValue) {
		super(width, height);
		Random r = new Random(seed);
		init(r, maxValue);
	}

	public RandomMatrix(int width, int height, int maxValue) {
		super(width, height);
		init(new Random(), maxValue);
	}

	private void init(Random r, int maxValue) {
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

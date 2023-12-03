package dk.itu.raven.util.matrix;

public class ArrayMatrix extends Matrix {
    private int[][] M;

    public ArrayMatrix(int[][] M, int width, int height) {
        super(width, height);
        this.M = M;
    }

    @Override
    public int getWithinRange(int r, int c) {
        return M[r][c];
    }

}

package dk.itu.raven.util.matrix;

public abstract class Matrix {
    protected int width, height;
    public Matrix(int width, int height) {
        this.width = width;
        this.height = height;
    }
    public int get(int r, int c) {
        if (r < 0 || r >= height || c < 0 || c >= width) return 0;
        return getWithinRange(r, c);
    }

    public int getWidth() {
        return this.width;
    }
    public int getHeight() {
        return this.height;
    }

    protected abstract int getWithinRange(int r,int c);
}

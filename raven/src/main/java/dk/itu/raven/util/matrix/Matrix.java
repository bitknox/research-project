package dk.itu.raven.util.matrix;

import java.io.IOException;

public abstract class Matrix {
    protected int width, height;

    public Matrix(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int get(int r, int c) {
        if (c < 0 || c >= width || r < 0 || r >= height)
            return 0;
        try {
            return getWithinRange(r, c);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
            return 0;
        }
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    
    public void print() {
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                System.out.print(get(i, j) + " ");
            }
            System.out.println();
        }
    }

    protected abstract int getWithinRange(int r, int c) throws IOException;
}

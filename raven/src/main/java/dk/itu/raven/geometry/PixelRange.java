package dk.itu.raven.geometry;

public class PixelRange {
    public int row;
    public int x1, x2;

    public PixelRange(int row, int x1, int x2) {
        this.row = row;
        this.x1 = x1;
        this.x2 = x2;
    }

    @Override
    public String toString() {
        return String.format("PixelRange(row=%d, x1=%d, x2=%d)", row, x1, x2);
    }
}

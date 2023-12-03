package dk.itu.raven.geometry;

public class PixelCoordinate {
	public int x;
	public int y;

	public PixelCoordinate(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public PixelCoordinate(double x, double y) {
		this.x = (int) x;
		this.y = (int) y;
	}
}

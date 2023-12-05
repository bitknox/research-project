package dk.itu.raven.join;

import com.github.davidmoten.rtree2.geometry.Geometries;
import com.github.davidmoten.rtree2.geometry.Rectangle;

import dk.itu.raven.geometry.PixelCoordinate;

public class Square {
	private int size;
	private PixelCoordinate topLeft;

	public Square(int x, int y, int size) {
		this.topLeft = new PixelCoordinate(x, y);
		this.size = size;
	}

	public int getSize() {
		return size;
	}

	public boolean contains(PixelCoordinate p) {
		return contains(p.x, p.y);
	}

	public boolean intersects(Square other) {
		return contains(other.topLeft) || contains(new PixelCoordinate(other.topLeft.x + other.size, other.topLeft.y))
				|| contains(new PixelCoordinate(other.topLeft.x, other.topLeft.y + other.size))
				|| contains(new PixelCoordinate(other.topLeft.x + other.size, other.topLeft.y + other.size));
	}

	public boolean intersects(Rectangle rect) {
		if (topLeft.x > rect.x2()) return false;
		if (topLeft.y > rect.y2()) return false;
		if (topLeft.x + size < rect.x1()) return false;
		if (topLeft.y + size < rect.y1()) return false;
		return true;
	}

	private boolean contains(double x, double y) {
		return x >= topLeft.x && x <= topLeft.x + size && y >= topLeft.y && y <= topLeft.y + size;
	}

	public boolean contains(Rectangle rect) {
		return contains(rect.x1(), rect.y1()) && contains(rect.x2(), rect.y2());
	}

	public boolean isContained(Rectangle rect) {
		return rect.x1() <= topLeft.x && rect.y1() <= topLeft.y && rect.x2() >= topLeft.x + size
				&& rect.y2() >= topLeft.y + size;
	}

	public Square[] split() {
		Square[] squares = new Square[4];
		squares[0] = new Square(topLeft.x, topLeft.y, size / 2);
		squares[1] = new Square(topLeft.x + size / 2, topLeft.y, size / 2);
		squares[2] = new Square(topLeft.x, topLeft.y + size / 2, size / 2);
		squares[3] = new Square(topLeft.x + size / 2, topLeft.y + size / 2, size / 2);
		return squares;
	}

	public Square getChildSquare(int childSize, int index, int k) {
		int x = topLeft.x + (index % k) * childSize;
		int y = topLeft.y + (index / k) * childSize;
		return new Square(x, y, childSize);
	}

	public int getTopX() {
		return topLeft.x;
	}

	public int getTopY() {
		return topLeft.y;
	}

	@Override
	public String toString() {
		return ("(" + topLeft.x + ", " + topLeft.y + "), size: " + size);
	}

}

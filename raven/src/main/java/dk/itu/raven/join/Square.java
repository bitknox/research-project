package dk.itu.raven.join;

import com.github.davidmoten.rtree2.geometry.Geometries;
import com.github.davidmoten.rtree2.geometry.Rectangle;

public class Square {
	private int size;
	private Point topLeft;

	public Square(int x, int y, int size) {
		this.topLeft = new Point(x, y);
		this.size = size;
	}

	public int getSize() {
		return size;
	}

	public boolean contains(Point p) {
		return contains(p.x, p.y);
	}

	public boolean intersects(Square other) {
		return contains(other.topLeft) || contains(new Point(other.topLeft.x + other.size, other.topLeft.y))
				|| contains(new Point(other.topLeft.x, other.topLeft.y + other.size))
				|| contains(new Point(other.topLeft.x + other.size, other.topLeft.y + other.size));
	}

	public boolean intersects(Rectangle rect) {
		return Geometries.rectangle(topLeft.x, topLeft.y, topLeft.x + size, topLeft.y + size).intersects(rect);
	}

	private boolean contains(double x, double y) {
		return x >= topLeft.x && x <= topLeft.x + size && y >= topLeft.y && y <= topLeft.y + size;
	}

	public boolean contains(Rectangle rect) {
		return contains(rect.x1(), rect.y1()) && contains(rect.x2(), rect.y2());
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

	private class Point {
		public int x;
		public int y;

		public Point(int x, int y) {
			this.x = x;
			this.y = y;
		}
	}

}

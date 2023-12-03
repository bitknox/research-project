package dk.itu.raven.join;

import org.junit.jupiter.api.Test;

//junit test for the square class

import static org.junit.jupiter.api.Assertions.*;

import com.github.davidmoten.rtree2.geometry.Geometries;
import com.github.davidmoten.rtree2.geometry.Rectangle;

public class SquareTest {

	@Test
	public void testContains() {
		Square square = new Square(0, 0, 10);
		Rectangle inside = Geometries.rectangle(1, 1, 5, 5);
		Rectangle outside = Geometries.rectangle(11, 11, 15, 15);
		Rectangle intersect = Geometries.rectangle(7, 7, 15, 15);

		assertTrue(square.contains(inside));
		assertFalse(square.contains(outside));
		assertFalse(square.contains(intersect));
	}

}

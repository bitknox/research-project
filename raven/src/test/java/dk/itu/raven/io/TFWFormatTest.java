package dk.itu.raven.io;

//test file for TFWFormat.java

import java.io.IOException;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import com.github.davidmoten.rtree2.geometry.Geometries;
import com.github.davidmoten.rtree2.geometry.Point;

import dk.itu.raven.geometry.PixelCoordinate;

public class TFWFormatTest {
	TFWFormat tfw;
	Random random = new Random();

	public TFWFormatTest() {
		tfw = new TFWFormat(0.1, 0, 0, -0.1, 3, 4);
	}

	@Test
	public void testTransformationToPixelCoordinate() throws IOException {

		assertEquals(570, tfw.transFromCoordinateToPixel(60, 15).x());
		assertEquals(110, tfw.transFromCoordinateToPixel(60, 15).y());

	}

	@Test
	public void testTransformFromPixelToCoordinate() throws IOException {

		assertEquals(3.0, tfw.transformFromPixelToCoordinate(0, 0).x(), 0.0);
		assertEquals(4.0, tfw.transformFromPixelToCoordinate(0, 0).y(), 0.0);
		assertEquals(3.1, tfw.transformFromPixelToCoordinate(1, 0).x(), 0.0);
		assertEquals(4.0, tfw.transformFromPixelToCoordinate(1, 0).y(), 0.0);
		assertEquals(3.0, tfw.transformFromPixelToCoordinate(0, 1).x(), 0.0);
		assertEquals(4.1, tfw.transformFromPixelToCoordinate(0, 1).y(), 0.0);
		assertEquals(3.1, tfw.transformFromPixelToCoordinate(1, 1).x(), 0.0);
		assertEquals(4.1, tfw.transformFromPixelToCoordinate(1, 1).y(), 0.0);
	}

	@RepeatedTest(10000)
	public void testTransformationToPixelCoordinateBackAndForthFromLat() {
		Point p = generateRandomCoordinate();
		Point pixelCoordinate = tfw.transFromCoordinateToPixel(p.x(), p.y());
		Point p2 = tfw.transformFromPixelToCoordinate(pixelCoordinate.x(), pixelCoordinate.y());
		assertEquals(p.x(), p2.x(), 0.0000001);
		assertEquals(p.y(), p2.y(), 0.0000001);
	}

	private Point generateRandomCoordinate() {
		double randomLat = -90 + (random.nextDouble() * (90 + 90));
		double randomLon = -180 + (random.nextDouble() * (180 + 180));
		return Geometries.point(randomLat, randomLon);
	}

}

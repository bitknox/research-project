package dk.itu.raven.util;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class BitMapTest {
	@Test
	public void testSetTo() {
		BitMap bm = new BitMap(2);
		bm.setTo(0, 0);
		assertFalse(bm.isSet(0));
		bm.setTo(0, 1);
		assertFalse(bm.isSet(1));
		assertTrue(bm.isSet(0));
		bm.setTo(1, 1);
		assertTrue(bm.isSet(1));
	}

	@Test
	public void testFlip() {
		BitMap bm = new BitMap(2);
		bm.set(1);
		assertFalse(bm.isSet(0));
		assertTrue(bm.isSet(1));
		bm.flip(0);
		assertTrue(bm.isSet(0));
		assertTrue(bm.isSet(1));
		bm.flip(0);
		assertFalse(bm.isSet(0));
		assertTrue(bm.isSet(1));
	}
}

package dk.itu.raven.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class BitMapTest {
    @Test
	public void testBitMap() {
        BitMap bm = new BitMap(2);
        bm.setTo(0, 0);
		assertFalse(bm.isSet(0));
        bm.setTo(0, 1);
		assertFalse(bm.isSet(1));
		assertTrue(bm.isSet(0));
        bm.setTo(1, 1);
		assertTrue(bm.isSet(1));
	}
}

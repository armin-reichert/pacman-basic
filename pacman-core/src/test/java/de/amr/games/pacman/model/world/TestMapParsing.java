package de.amr.games.pacman.model.world;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import de.amr.games.pacman.lib.V2i;

public class TestMapParsing {

	@Test
	public void testMapParsing() {
		WorldMap map = WorldMap.load("/testmap.txt");
		String undefinedKey = "_";

		assertEquals((Integer) 42, map.integer("i"));
		assertEquals(null, map.integer(undefinedKey));

		assertEquals("Brandon", map.string("s"));
		assertEquals(null, map.string(undefinedKey));

		assertEquals(new V2i(42, 42), map.vector("v"));
		assertEquals(null, map.vector(undefinedKey));

		List<V2i> vecList = map.vectorList("L");
		assertEquals(List.of(//
				new V2i(0, 0), new V2i(0, 1), new V2i(0, 2), new V2i(0, 3)), vecList);
		assertEquals(null, map.vectorList(undefinedKey));

	}
}

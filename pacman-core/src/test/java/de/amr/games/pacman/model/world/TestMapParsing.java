package de.amr.games.pacman.model.world;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import de.amr.games.pacman.lib.V2i;

public class TestMapParsing {

	@Test
	public void testMapParsing() {
		WorldMap map = WorldMap.load("/testmap.txt");
		assertEquals(42, map.integer("int_val"));
		assertEquals("abc", map.string("string_val"));
		assertEquals(new V2i(42, 42), map.vector("vector_val"));
		List<V2i> vecList = map.vectorList("vector_list");
		assertEquals(List.of(//
				new V2i(0, 0), new V2i(0, 1), new V2i(0, 2), new V2i(0, 3)), vecList);
	}
}

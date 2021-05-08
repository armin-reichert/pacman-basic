package de.amr.games.pacman.model.world;

import static java.util.function.Predicate.not;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TestWorldFunctions {

	@Test
	public void testPacManWorld() {
		PacManGameWorld world = new MapBasedPacManGameWorld("/pacman/maps/map1.txt");
		assertEquals(36, world.numRows());
		assertEquals(28, world.numCols());
		assertEquals(4, world.energizerTiles().count());
		assertEquals(244, world.tiles().filter(world::isFoodTile).count());
		assertEquals(240, world.tiles().filter(world::isFoodTile).filter(not(world::isEnergizerTile)).count());
		assertEquals(1, world.numPortals());
	}

	@Test
	public void testMsPacManWorld1() {
		PacManGameWorld world = new MapBasedPacManGameWorld("/mspacman/maps/map1.txt");
		assertEquals(36, world.numRows());
		assertEquals(28, world.numCols());
		assertEquals(4, world.energizerTiles().count());
		assertEquals(220 + 4, world.tiles().filter(world::isFoodTile).count());
		assertEquals(220, world.tiles().filter(world::isFoodTile).filter(not(world::isEnergizerTile)).count());
		assertEquals(2, world.numPortals());
	}

	@Test
	public void testMsPacManWorld2() {
		PacManGameWorld world = new MapBasedPacManGameWorld("/mspacman/maps/map2.txt");
		assertEquals(36, world.numRows());
		assertEquals(28, world.numCols());
		assertEquals(4, world.energizerTiles().count());
		assertEquals(240 + 4, world.tiles().filter(world::isFoodTile).count());
		assertEquals(240, world.tiles().filter(world::isFoodTile).filter(not(world::isEnergizerTile)).count());
		assertEquals(2, world.numPortals());
	}

	@Test
	public void testMsPacManWorld3() {
		PacManGameWorld world = new MapBasedPacManGameWorld("/mspacman/maps/map3.txt");
		assertEquals(36, world.numRows());
		assertEquals(28, world.numCols());
		assertEquals(4, world.energizerTiles().count());
		assertEquals(238 + 4, world.tiles().filter(world::isFoodTile).count());
		assertEquals(238, world.tiles().filter(world::isFoodTile).filter(not(world::isEnergizerTile)).count());
		assertEquals(1, world.numPortals());
	}

	@Test
	public void testMsPacManWorld4() {
		PacManGameWorld world = new MapBasedPacManGameWorld("/mspacman/maps/map4.txt");
		assertEquals(36, world.numRows());
		assertEquals(28, world.numCols());
		assertEquals(4, world.energizerTiles().count());
		assertEquals(236 + 4, world.tiles().filter(world::isFoodTile).count());
		assertEquals(236, world.tiles().filter(world::isFoodTile).filter(not(world::isEnergizerTile)).count());
		assertEquals(2, world.numPortals());
	}
}

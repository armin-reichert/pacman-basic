package de.amr.games.pacman.model.world;

import static java.util.function.Predicate.not;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2i;

public class TestWorldFunctions {

	@Test
	public void testPacManWorld() {
		MapBasedPacManGameWorld world = new MapBasedPacManGameWorld();
		world.setMap(WorldMap.load("/pacman/maps/map1.txt"));
		assertEquals(36, world.numRows());
		assertEquals(28, world.numCols());
		assertEquals(4, world.energizerTiles().count());
		assertEquals(244, world.tiles().filter(world::isFoodTile).count());
		assertEquals(240, world.tiles().filter(world::isFoodTile).filter(not(world::isEnergizerTile)).count());
		assertEquals(70, world.pelletsToEatForBonus(0));
		assertEquals(170, world.pelletsToEatForBonus(1));
		assertEquals(1, world.portals().size());
		assertEquals(Direction.RIGHT, world.playerStartDirection());
	}

	@Test
	public void testMsPacManWorld1() {
		MapBasedPacManGameWorld world = new MapBasedPacManGameWorld();
		world.setMap(WorldMap.load("/mspacman/maps/map1.txt"));
		assertEquals(36, world.numRows());
		assertEquals(28, world.numCols());
		assertEquals(4, world.energizerTiles().count());
		assertEquals(220 + 4, world.tiles().filter(world::isFoodTile).count());
		assertEquals(220, world.tiles().filter(world::isFoodTile).filter(not(world::isEnergizerTile)).count());
		assertEquals(64, world.pelletsToEatForBonus(0));
		assertEquals(172, world.pelletsToEatForBonus(1));
		assertEquals(2, world.portals().size());
		assertEquals(new V2i(13, 26), world.playerHomeTile());
		assertEquals(Direction.LEFT, world.playerStartDirection());
		assertEquals(Direction.LEFT, world.ghostStartDirection(0));
		assertEquals(Direction.DOWN, world.ghostStartDirection(1));
		assertEquals(Direction.UP, world.ghostStartDirection(2));
		assertEquals(Direction.UP, world.ghostStartDirection(3));
	}

	@Test
	public void testMsPacManWorld2() {
		MapBasedPacManGameWorld world = new MapBasedPacManGameWorld();
		world.setMap(WorldMap.load("/mspacman/maps/map2.txt"));
		assertEquals(36, world.numRows());
		assertEquals(28, world.numCols());
		assertEquals(4, world.energizerTiles().count());
		assertEquals(240 + 4, world.tiles().filter(world::isFoodTile).count());
		assertEquals(240, world.tiles().filter(world::isFoodTile).filter(not(world::isEnergizerTile)).count());
		assertEquals(64, world.pelletsToEatForBonus(0));
		assertEquals(172, world.pelletsToEatForBonus(1));
		assertEquals(2, world.portals().size());
		assertEquals(new V2i(13, 26), world.playerHomeTile());
		assertEquals(Direction.LEFT, world.playerStartDirection());
		assertEquals(Direction.LEFT, world.ghostStartDirection(0));
		assertEquals(Direction.DOWN, world.ghostStartDirection(1));
		assertEquals(Direction.UP, world.ghostStartDirection(2));
		assertEquals(Direction.UP, world.ghostStartDirection(3));
	}

	@Test
	public void testMsPacManWorld3() {
		MapBasedPacManGameWorld world = new MapBasedPacManGameWorld();
		world.setMap(WorldMap.load("/mspacman/maps/map3.txt"));
		assertEquals(36, world.numRows());
		assertEquals(28, world.numCols());
		assertEquals(4, world.energizerTiles().count());
		assertEquals(238 + 4, world.tiles().filter(world::isFoodTile).count());
		assertEquals(238, world.tiles().filter(world::isFoodTile).filter(not(world::isEnergizerTile)).count());
		assertEquals(70, world.pelletsToEatForBonus(0));
		assertEquals(170, world.pelletsToEatForBonus(1));
		assertEquals(1, world.portals().size());
		assertEquals(new V2i(13, 26), world.playerHomeTile());
		assertEquals(Direction.LEFT, world.playerStartDirection());
		assertEquals(Direction.LEFT, world.ghostStartDirection(0));
		assertEquals(Direction.DOWN, world.ghostStartDirection(1));
		assertEquals(Direction.UP, world.ghostStartDirection(2));
		assertEquals(Direction.UP, world.ghostStartDirection(3));
	}

	@Test
	public void testMsPacManWorld4() {
		MapBasedPacManGameWorld world = new MapBasedPacManGameWorld();
		world.setMap(WorldMap.load("/mspacman/maps/map4.txt"));
		assertEquals(36, world.numRows());
		assertEquals(28, world.numCols());
		assertEquals(4, world.energizerTiles().count());
		assertEquals(234 + 4, world.tiles().filter(world::isFoodTile).count());
		assertEquals(234, world.tiles().filter(world::isFoodTile).filter(not(world::isEnergizerTile)).count());
		assertEquals(70, world.pelletsToEatForBonus(0));
		assertEquals(170, world.pelletsToEatForBonus(1));
		assertEquals(2, world.portals().size());
		assertEquals(new V2i(13, 26), world.playerHomeTile());
		assertEquals(Direction.LEFT, world.playerStartDirection());
		assertEquals(Direction.LEFT, world.ghostStartDirection(0));
		assertEquals(Direction.DOWN, world.ghostStartDirection(1));
		assertEquals(Direction.UP, world.ghostStartDirection(2));
		assertEquals(Direction.UP, world.ghostStartDirection(3));
	}
}
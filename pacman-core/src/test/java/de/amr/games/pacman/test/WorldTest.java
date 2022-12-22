package de.amr.games.pacman.test;

import static java.util.function.Predicate.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

import de.amr.games.pacman.lib.U;
import de.amr.games.pacman.lib.math.Vector2d;
import de.amr.games.pacman.lib.math.Vector2i;
import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.model.common.world.ArcadeWorld;
import de.amr.games.pacman.model.common.world.World;
import de.amr.games.pacman.model.mspacman.MsPacManGame;
import de.amr.games.pacman.model.pacman.PacManGame;

public class WorldTest {

	@Test
	public void testTileCoordinates() {
		Vector2d pos = new Vector2d(0.0, 0.0);
		assertEquals(Vector2i.ZERO, World.tileAt(pos));
		pos = new Vector2d(7.9, 7.9);
		assertEquals(new Vector2i(0, 0), World.tileAt(pos));
		pos = new Vector2d(8.0, 7.9);
		assertEquals(new Vector2i(1, 0), World.tileAt(pos));
		pos = new Vector2d(8.0, 0.0);
		assertEquals(new Vector2i(1, 0), World.tileAt(pos));
		pos = new Vector2d(0.0, 8.0);
		assertEquals(new Vector2i(0, 1), World.tileAt(pos));

		var guy = new Ghost(Ghost.ID_RED_GHOST, "Guy");

		guy.setPosition(3.99, 0);
		assertEquals(new Vector2i(0, 0), guy.tile());
		assertEquals(new Vector2d(3.99, 0.0), guy.offset());

		guy.setPosition(4.0, 0);
		assertEquals(new Vector2i(1, 0), guy.tile());
		assertEquals(new Vector2d(-4.0, 0.0), guy.offset());
	}

	@Test
	public void testPacManWorld() {
		ArcadeWorld world = (ArcadeWorld) new PacManGame().level().world();
		assertEquals(ArcadeWorld.TILES_Y, world.numRows());
		assertEquals(ArcadeWorld.TILES_X, world.numCols());
		assertEquals(4, world.energizerTiles().count());
		assertEquals(244, world.tiles().filter(world::isFoodTile).count());
		assertEquals(240, world.tiles().filter(world::isFoodTile).filter(not(world::isEnergizerTile)).count());
		assertEquals(1, world.portals().size());
	}

	@Test
	public void testMsPacManWorld1() {
		var world = new ArcadeWorld(MsPacManGame.MAP1);
		assertEquals(ArcadeWorld.TILES_Y, world.numRows());
		assertEquals(ArcadeWorld.TILES_X, world.numCols());
		assertEquals(4, world.energizerTiles().count());
		assertEquals(220 + 4, world.tiles().filter(world::isFoodTile).count());
		assertEquals(220, world.tiles().filter(world::isFoodTile).filter(not(world::isEnergizerTile)).count());
		assertEquals(2, world.portals().size());
	}

	@Test
	public void testMsPacManWorld2() {
		var world = new ArcadeWorld(MsPacManGame.MAP2);
		assertEquals(ArcadeWorld.TILES_Y, world.numRows());
		assertEquals(ArcadeWorld.TILES_X, world.numCols());
		assertEquals(4, world.energizerTiles().count());
		assertEquals(240 + 4, world.tiles().filter(world::isFoodTile).count());
		assertEquals(240, world.tiles().filter(world::isFoodTile).filter(not(world::isEnergizerTile)).count());
		assertEquals(2, world.portals().size());
	}

	@Test
	public void testMsPacManWorld3() {
		var world = new ArcadeWorld(MsPacManGame.MAP3);
		assertEquals(ArcadeWorld.TILES_Y, world.numRows());
		assertEquals(ArcadeWorld.TILES_X, world.numCols());
		assertEquals(4, world.energizerTiles().count());
		assertEquals(238 + 4, world.tiles().filter(world::isFoodTile).count());
		assertEquals(238, world.tiles().filter(world::isFoodTile).filter(not(world::isEnergizerTile)).count());
		assertEquals(1, world.portals().size());
	}

	@Test
	public void testMsPacManWorld4() {
		var world = new ArcadeWorld(MsPacManGame.MAP4);
		assertEquals(ArcadeWorld.TILES_Y, world.numRows());
		assertEquals(ArcadeWorld.TILES_X, world.numCols());
		assertEquals(4, world.energizerTiles().count());
		assertEquals(234 + 4, world.tiles().filter(world::isFoodTile).count());
		assertEquals(234, world.tiles().filter(world::isFoodTile).filter(not(world::isEnergizerTile)).count());
		assertEquals(2, world.portals().size());
	}

	@Test
	public void testCopyMapData() {
		byte[][] map = { { 0, 1, 2 }, { 3, 4, 5 }, { 6, 7, 8 } };
		byte[][] copy = U.copyByteArray2D(map);
		assertEquals(map.length, copy.length);
		for (int i = 0; i < map.length; ++i) {
			assertEquals(map[i].length, copy[i].length);
		}
		for (int i = 0; i < map.length; ++i) {
			for (int j = 0; j < map[0].length; ++j) {
				assertEquals(map[i][j], copy[i][j]);
			}
		}
		assertEquals(4, map[1][1]);
		assertEquals(4, copy[1][1]);
		copy[1][1] = (byte) 42;
		assertNotEquals(map[1][1], copy[1][1]);
	}
}
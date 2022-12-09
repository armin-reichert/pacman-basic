package de.amr.games.pacman.model.world;

import static java.util.function.Predicate.not;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.amr.games.pacman.lib.V2d;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.model.common.world.ArcadeWorld;
import de.amr.games.pacman.model.common.world.World;
import de.amr.games.pacman.model.mspacman.MsPacManGame;
import de.amr.games.pacman.model.pacman.PacManGame;

public class WorldTests {

	@Test
	public void testTileCoordinates() {
		V2d pos = new V2d(0.0, 0.0);
		assertEquals(V2i.ZERO, World.tileAt(pos));
		pos = new V2d(7.9, 7.9);
		assertEquals(new V2i(0, 0), World.tileAt(pos));
		pos = new V2d(8.0, 7.9);
		assertEquals(new V2i(1, 0), World.tileAt(pos));
		pos = new V2d(8.0, 0.0);
		assertEquals(new V2i(1, 0), World.tileAt(pos));
		pos = new V2d(0.0, 8.0);
		assertEquals(new V2i(0, 1), World.tileAt(pos));

		var guy = new Ghost(Ghost.ID_RED_GHOST, "Guy");

		guy.setPosition(3.99, 0);
		assertEquals(new V2i(0, 0), guy.tile());
		assertEquals(new V2d(3.99, 0.0), guy.offset());

		guy.setPosition(4.0, 0);
		assertEquals(new V2i(1, 0), guy.tile());
		assertEquals(new V2d(-4.0, 0.0), guy.offset());
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

	@Test(expected = IllegalArgumentException.class)
	public void testIllegalMapNumber() {
		MsPacManGame.createWorld(42);
	}

	@Test
	public void testMsPacManWorld1() {
		World world = MsPacManGame.createWorld(1);
		assertEquals(ArcadeWorld.TILES_Y, world.numRows());
		assertEquals(ArcadeWorld.TILES_X, world.numCols());
		assertEquals(4, world.energizerTiles().count());
		assertEquals(220 + 4, world.tiles().filter(world::isFoodTile).count());
		assertEquals(220, world.tiles().filter(world::isFoodTile).filter(not(world::isEnergizerTile)).count());
		assertEquals(2, world.portals().size());
	}

	@Test
	public void testMsPacManWorld2() {
		World world = MsPacManGame.createWorld(2);
		assertEquals(ArcadeWorld.TILES_Y, world.numRows());
		assertEquals(ArcadeWorld.TILES_X, world.numCols());
		assertEquals(4, world.energizerTiles().count());
		assertEquals(240 + 4, world.tiles().filter(world::isFoodTile).count());
		assertEquals(240, world.tiles().filter(world::isFoodTile).filter(not(world::isEnergizerTile)).count());
		assertEquals(2, world.portals().size());
	}

	@Test
	public void testMsPacManWorld3() {
		World world = MsPacManGame.createWorld(3);
		assertEquals(ArcadeWorld.TILES_Y, world.numRows());
		assertEquals(ArcadeWorld.TILES_X, world.numCols());
		assertEquals(4, world.energizerTiles().count());
		assertEquals(238 + 4, world.tiles().filter(world::isFoodTile).count());
		assertEquals(238, world.tiles().filter(world::isFoodTile).filter(not(world::isEnergizerTile)).count());
		assertEquals(1, world.portals().size());
	}

	@Test
	public void testMsPacManWorld4() {
		World world = MsPacManGame.createWorld(4);
		assertEquals(ArcadeWorld.TILES_Y, world.numRows());
		assertEquals(ArcadeWorld.TILES_X, world.numCols());
		assertEquals(4, world.energizerTiles().count());
		assertEquals(234 + 4, world.tiles().filter(world::isFoodTile).count());
		assertEquals(234, world.tiles().filter(world::isFoodTile).filter(not(world::isEnergizerTile)).count());
		assertEquals(2, world.portals().size());
	}
}
package de.amr.games.pacman.model.world;

import static de.amr.games.pacman.model.common.actors.Ghost.CYAN_GHOST;
import static de.amr.games.pacman.model.common.actors.Ghost.ORANGE_GHOST;
import static de.amr.games.pacman.model.common.actors.Ghost.PINK_GHOST;
import static de.amr.games.pacman.model.common.actors.Ghost.RED_GHOST;
import static java.util.function.Predicate.not;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.world.ArcadeWorld;
import de.amr.games.pacman.model.common.world.World;
import de.amr.games.pacman.model.mspacman.MsPacManGame;
import de.amr.games.pacman.model.pacman.PacManGame;

public class TestWorldFunctions {

	@Test
	public void testPacManWorld() {
		World world = PacManGame.createWorld();
		assertEquals(ArcadeWorld.TILES_Y, world.numRows());
		assertEquals(ArcadeWorld.TILES_X, world.numCols());
		assertEquals(4, world.energizerTiles().count());
		assertEquals(244, world.tiles().filter(world::isFoodTile).count());
		assertEquals(240, world.tiles().filter(world::isFoodTile).filter(not(world::isEnergizerTile)).count());
		assertEquals(1, world.portals().size());
		assertEquals(Direction.LEFT, world.playerStartDir());
		assertEquals(Direction.LEFT, world.ghostStartDir(RED_GHOST));
		assertEquals(Direction.DOWN, world.ghostStartDir(PINK_GHOST));
		assertEquals(Direction.UP, world.ghostStartDir(CYAN_GHOST));
		assertEquals(Direction.UP, world.ghostStartDir(ORANGE_GHOST));
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
		assertEquals(new V2i(13, 26), world.playerHomeTile());
		assertEquals(Direction.LEFT, world.playerStartDir());
		assertEquals(Direction.LEFT, world.ghostStartDir(RED_GHOST));
		assertEquals(Direction.DOWN, world.ghostStartDir(PINK_GHOST));
		assertEquals(Direction.UP, world.ghostStartDir(CYAN_GHOST));
		assertEquals(Direction.UP, world.ghostStartDir(ORANGE_GHOST));
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
		assertEquals(new V2i(13, 26), world.playerHomeTile());
		assertEquals(Direction.LEFT, world.playerStartDir());
		assertEquals(Direction.LEFT, world.ghostStartDir(RED_GHOST));
		assertEquals(Direction.DOWN, world.ghostStartDir(PINK_GHOST));
		assertEquals(Direction.UP, world.ghostStartDir(CYAN_GHOST));
		assertEquals(Direction.UP, world.ghostStartDir(ORANGE_GHOST));
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
		assertEquals(new V2i(13, 26), world.playerHomeTile());
		assertEquals(Direction.LEFT, world.playerStartDir());
		assertEquals(Direction.LEFT, world.ghostStartDir(RED_GHOST));
		assertEquals(Direction.DOWN, world.ghostStartDir(PINK_GHOST));
		assertEquals(Direction.UP, world.ghostStartDir(CYAN_GHOST));
		assertEquals(Direction.UP, world.ghostStartDir(ORANGE_GHOST));
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
		assertEquals(new V2i(13, 26), world.playerHomeTile());
		assertEquals(Direction.LEFT, world.playerStartDir());
		assertEquals(Direction.LEFT, world.ghostStartDir(RED_GHOST));
		assertEquals(Direction.DOWN, world.ghostStartDir(PINK_GHOST));
		assertEquals(Direction.UP, world.ghostStartDir(CYAN_GHOST));
		assertEquals(Direction.UP, world.ghostStartDir(ORANGE_GHOST));
	}
}
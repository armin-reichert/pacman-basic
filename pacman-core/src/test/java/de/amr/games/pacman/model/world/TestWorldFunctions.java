package de.amr.games.pacman.model.world;

import static java.util.function.Predicate.not;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.world.World;
import de.amr.games.pacman.model.mspacman.MsPacManWorld1;
import de.amr.games.pacman.model.mspacman.MsPacManWorld2;
import de.amr.games.pacman.model.mspacman.MsPacManWorld3;
import de.amr.games.pacman.model.mspacman.MsPacManWorld4;
import de.amr.games.pacman.model.pacman.PacManWorld;

public class TestWorldFunctions {

	@Test
	public void testPacManWorld() {
		World world = new PacManWorld();
		assertEquals(36, world.numRows());
		assertEquals(28, world.numCols());
		assertEquals(4, world.energizerTiles().size());
		assertEquals(244, world.tiles().filter(world::isFoodTile).count());
		assertEquals(240, world.tiles().filter(world::isFoodTile).filter(not(world::isEnergizerTile)).count());
		assertEquals(70, world.pelletsToEatForBonus(0));
		assertEquals(170, world.pelletsToEatForBonus(1));
		assertEquals(1, world.portals().size());
		assertEquals(Direction.LEFT, world.playerStartDirection());
		assertEquals(Direction.LEFT, world.ghostStartDirection(GameModel.RED_GHOST));
		assertEquals(Direction.DOWN, world.ghostStartDirection(GameModel.PINK_GHOST));
		assertEquals(Direction.UP, world.ghostStartDirection(GameModel.CYAN_GHOST));
		assertEquals(Direction.UP, world.ghostStartDirection(GameModel.ORANGE_GHOST));
	}

	@Test
	public void testMsPacManWorld1() {
		World world = new MsPacManWorld1();
		assertEquals(36, world.numRows());
		assertEquals(28, world.numCols());
		assertEquals(4, world.energizerTiles().size());
		assertEquals(220 + 4, world.tiles().filter(world::isFoodTile).count());
		assertEquals(220, world.tiles().filter(world::isFoodTile).filter(not(world::isEnergizerTile)).count());
		assertEquals(64, world.pelletsToEatForBonus(0));
		assertEquals(172, world.pelletsToEatForBonus(1));
		assertEquals(2, world.portals().size());
		assertEquals(new V2i(13, 26), world.playerHomeTile());
		assertEquals(Direction.LEFT, world.playerStartDirection());
		assertEquals(Direction.LEFT, world.ghostStartDirection(GameModel.RED_GHOST));
		assertEquals(Direction.DOWN, world.ghostStartDirection(GameModel.PINK_GHOST));
		assertEquals(Direction.UP, world.ghostStartDirection(GameModel.CYAN_GHOST));
		assertEquals(Direction.UP, world.ghostStartDirection(GameModel.ORANGE_GHOST));
	}

	@Test
	public void testMsPacManWorld2() {
		World world = new MsPacManWorld2();
		assertEquals(36, world.numRows());
		assertEquals(28, world.numCols());
		assertEquals(4, world.energizerTiles().size());
		assertEquals(240 + 4, world.tiles().filter(world::isFoodTile).count());
		assertEquals(240, world.tiles().filter(world::isFoodTile).filter(not(world::isEnergizerTile)).count());
		assertEquals(64, world.pelletsToEatForBonus(0));
		assertEquals(172, world.pelletsToEatForBonus(1));
		assertEquals(2, world.portals().size());
		assertEquals(new V2i(13, 26), world.playerHomeTile());
		assertEquals(Direction.LEFT, world.playerStartDirection());
		assertEquals(Direction.LEFT, world.ghostStartDirection(GameModel.RED_GHOST));
		assertEquals(Direction.DOWN, world.ghostStartDirection(GameModel.PINK_GHOST));
		assertEquals(Direction.UP, world.ghostStartDirection(GameModel.CYAN_GHOST));
		assertEquals(Direction.UP, world.ghostStartDirection(GameModel.ORANGE_GHOST));
	}

	@Test
	public void testMsPacManWorld3() {
		World world = new MsPacManWorld3();
		assertEquals(36, world.numRows());
		assertEquals(28, world.numCols());
		assertEquals(4, world.energizerTiles().size());
		assertEquals(238 + 4, world.tiles().filter(world::isFoodTile).count());
		assertEquals(238, world.tiles().filter(world::isFoodTile).filter(not(world::isEnergizerTile)).count());
		assertEquals(70, world.pelletsToEatForBonus(0));
		assertEquals(170, world.pelletsToEatForBonus(1));
		assertEquals(1, world.portals().size());
		assertEquals(new V2i(13, 26), world.playerHomeTile());
		assertEquals(Direction.LEFT, world.playerStartDirection());
		assertEquals(Direction.LEFT, world.ghostStartDirection(GameModel.RED_GHOST));
		assertEquals(Direction.DOWN, world.ghostStartDirection(GameModel.PINK_GHOST));
		assertEquals(Direction.UP, world.ghostStartDirection(GameModel.CYAN_GHOST));
		assertEquals(Direction.UP, world.ghostStartDirection(GameModel.ORANGE_GHOST));
	}

	@Test
	public void testMsPacManWorld4() {
		World world = new MsPacManWorld4();
		assertEquals(36, world.numRows());
		assertEquals(28, world.numCols());
		assertEquals(4, world.energizerTiles().size());
		assertEquals(234 + 4, world.tiles().filter(world::isFoodTile).count());
		assertEquals(234, world.tiles().filter(world::isFoodTile).filter(not(world::isEnergizerTile)).count());
		assertEquals(70, world.pelletsToEatForBonus(0));
		assertEquals(170, world.pelletsToEatForBonus(1));
		assertEquals(2, world.portals().size());
		assertEquals(new V2i(13, 26), world.playerHomeTile());
		assertEquals(Direction.LEFT, world.playerStartDirection());
		assertEquals(Direction.LEFT, world.ghostStartDirection(GameModel.RED_GHOST));
		assertEquals(Direction.DOWN, world.ghostStartDirection(GameModel.PINK_GHOST));
		assertEquals(Direction.UP, world.ghostStartDirection(GameModel.CYAN_GHOST));
		assertEquals(Direction.UP, world.ghostStartDirection(GameModel.ORANGE_GHOST));
	}
}
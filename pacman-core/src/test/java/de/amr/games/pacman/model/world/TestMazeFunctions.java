package de.amr.games.pacman.model.world;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.mspacman.MsPacManGame;
import de.amr.games.pacman.model.pacman.PacManGame;

public class TestMazeFunctions {

	@Test
	public void testPacManMazeNumber() {
		GameModel game = new PacManGame();
		assertEquals(1, game.mazeNumber(1));
		assertEquals(1, game.mazeNumber(10));
		assertEquals(1, game.mazeNumber(100));
	}

	@Test
	public void testPacManMapForLevel1() {
		GameModel game = new PacManGame();
		game.enterLevel(1);

		assertTrue(game.world.getMap().isPresent());
		WorldMap map = game.world.getMap().get();
		assertEquals(new V2i(28, 36), map.vector("size"));
		assertEquals(new V2i(70, 170), map.vector("bonus_pellets_to_eat"));
	}

	@Test
	public void testMsPacManMazeNumber() {
		GameModel game = new MsPacManGame();
		assertEquals(1, game.mazeNumber(1));
		assertEquals(1, game.mazeNumber(2));
		assertEquals(2, game.mazeNumber(3));
		assertEquals(2, game.mazeNumber(4));
		assertEquals(2, game.mazeNumber(5));
		assertEquals(3, game.mazeNumber(6));
		assertEquals(3, game.mazeNumber(7));
		assertEquals(3, game.mazeNumber(8));
		assertEquals(3, game.mazeNumber(9));
		assertEquals(4, game.mazeNumber(10));
		assertEquals(4, game.mazeNumber(11));
		assertEquals(4, game.mazeNumber(12));
		assertEquals(4, game.mazeNumber(13));
		assertEquals(5, game.mazeNumber(14));
		assertEquals(5, game.mazeNumber(15));
		assertEquals(5, game.mazeNumber(16));
		assertEquals(5, game.mazeNumber(17));
		assertEquals(6, game.mazeNumber(18));
		assertEquals(6, game.mazeNumber(19));
		assertEquals(6, game.mazeNumber(20));
		assertEquals(6, game.mazeNumber(21));
		assertEquals(5, game.mazeNumber(22));
	}

	@Test
	public void testMsPacManMapForLevels() {
		MsPacManGame game = new MsPacManGame();
		WorldMap map;

		game.enterLevel(1);
		assertTrue(game.world.getMap().isPresent());
		map = game.world.getMap().get();
		assertEquals(new V2i(28, 36), map.vector("size"));
		assertEquals(new V2i(64, 172), map.vector("bonus_pellets_to_eat"));

		game.enterLevel(5);
		assertTrue(game.world.getMap().isPresent());
		map = game.world.getMap().get();
		assertEquals(new V2i(28, 36), map.vector("size"));
		assertEquals(new V2i(64, 172), map.vector("bonus_pellets_to_eat"));

		game.enterLevel(9);
		assertTrue(game.world.getMap().isPresent());
		map = game.world.getMap().get();
		assertEquals(new V2i(28, 36), map.vector("size"));
		assertEquals(new V2i(70, 170), map.vector("bonus_pellets_to_eat"));

		game.enterLevel(13);
		assertTrue(game.world.getMap().isPresent());
		map = game.world.getMap().get();
		assertEquals(new V2i(28, 36), map.vector("size"));
		assertEquals(new V2i(70, 170), map.vector("bonus_pellets_to_eat"));
	}
}
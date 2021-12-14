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
		game.enterLevel(1);
		assertEquals(1, game.mazeNumber);
		game.enterLevel(10);
		assertEquals(1, game.mazeNumber);
		game.enterLevel(100);
		assertEquals(1, game.mazeNumber);
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
		final int[] mazeNumbers = { 0, 1, 1, 2, 2, 2, 3, 3, 3, 3, 4, 4, 4, 4, 5, 5, 5, 5, 6, 6, 6, 6, 5 };
		for (int levelNumber = 1; levelNumber <= 22; ++levelNumber) {
			game.enterLevel(levelNumber);
			assertEquals(mazeNumbers[levelNumber], game.mazeNumber);
		}
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
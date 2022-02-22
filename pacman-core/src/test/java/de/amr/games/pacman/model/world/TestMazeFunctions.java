package de.amr.games.pacman.model.world;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.mspacman.MsPacManGame;
import de.amr.games.pacman.model.pacman.PacManGame;

public class TestMazeFunctions {

	@Test
	public void testPacManMazeNumber() {
		GameModel game = new PacManGame();
		game.setLevel(1);
		assertEquals(1, game.mazeNumber);
		game.setLevel(10);
		assertEquals(1, game.mazeNumber);
		game.setLevel(100);
		assertEquals(1, game.mazeNumber);
	}

	@Test
	public void testPacManMapForLevel1() {
		GameModel game = new PacManGame();
		game.setLevel(1);

		WorldMap map = ((MapWorld) game.world).getMap();
		assertEquals(new V2i(28, 36), map.vector("size"));
		assertEquals(new V2i(70, 170), map.vector("bonus_pellets_to_eat"));
	}

	@Test
	public void testMsPacManMazeNumber() {
		GameModel game = new MsPacManGame();
		final int[] mazeNumbers = { 0, 1, 1, 2, 2, 2, 3, 3, 3, 3, 4, 4, 4, 4, 5, 5, 5, 5, 6, 6, 6, 6, 5 };
		for (int levelNumber = 1; levelNumber <= 22; ++levelNumber) {
			game.setLevel(levelNumber);
			assertEquals(mazeNumbers[levelNumber], game.mazeNumber);
		}
	}

	@Test
	public void testMsPacManMapForLevels() {
		MsPacManGame game = new MsPacManGame();
		WorldMap map;

		game.setLevel(1);
		map = ((MapWorld) game.world).getMap();
		assertEquals(new V2i(28, 36), map.vector("size"));
		assertEquals(new V2i(64, 172), map.vector("bonus_pellets_to_eat"));

		game.setLevel(5);
		map = ((MapWorld) game.world).getMap();
		assertEquals(new V2i(28, 36), map.vector("size"));
		assertEquals(new V2i(64, 172), map.vector("bonus_pellets_to_eat"));

		game.setLevel(9);
		map = ((MapWorld) game.world).getMap();
		assertEquals(new V2i(28, 36), map.vector("size"));
		assertEquals(new V2i(70, 170), map.vector("bonus_pellets_to_eat"));

		game.setLevel(13);
		map = ((MapWorld) game.world).getMap();
		assertEquals(new V2i(28, 36), map.vector("size"));
		assertEquals(new V2i(70, 170), map.vector("bonus_pellets_to_eat"));
	}
}
package de.amr.games.pacman.model.world;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.mspacman.MsPacManGame;
import de.amr.games.pacman.model.pacman.PacManGame;

public class TestMazeFunctions {

	@Test
	public void testPacManMazeNumber() {
		GameModel game = new PacManGame();
		game.setLevel(1);
		assertEquals(1, game.level.mazeNumber);
		game.setLevel(10);
		assertEquals(1, game.level.mazeNumber);
		game.setLevel(100);
		assertEquals(1, game.level.mazeNumber);
	}

	@Test
	public void testMsPacManMazeNumber() {
		GameModel game = new MsPacManGame();
		final int[] mazeNumbers = { 0, 1, 1, 2, 2, 2, 3, 3, 3, 3, 4, 4, 4, 4, 5, 5, 5, 5, 6, 6, 6, 6, 5 };
		for (int levelNumber = 1; levelNumber <= 22; ++levelNumber) {
			game.setLevel(levelNumber);
			assertEquals(mazeNumbers[levelNumber], game.level.mazeNumber);
		}
	}
}
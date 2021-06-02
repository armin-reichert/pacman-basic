package de.amr.games.pacman.model.world;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.amr.games.pacman.model.common.PacManGameModel;
import de.amr.games.pacman.model.mspacman.MsPacManGame;
import de.amr.games.pacman.model.pacman.PacManGame;

public class TestMazeFunctions {

	@Test
	public void testPacManMazeNumber() {
		PacManGameModel game = new PacManGame();
		assertEquals(1, game.mazeNumber(1));
		assertEquals(1, game.mazeNumber(10));
		assertEquals(1, game.mazeNumber(100));
	}

	@Test
	public void testMsPacManMazeNumber() {
		PacManGameModel game = new MsPacManGame();
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
}
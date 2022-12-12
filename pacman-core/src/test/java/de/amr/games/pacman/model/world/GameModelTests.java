/*
MIT License

Copyright (c) 2022 Armin Reichert

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

package de.amr.games.pacman.model.world;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import de.amr.games.pacman.lib.V2d;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.actors.Pac;
import de.amr.games.pacman.model.pacman.PacManGame;

/**
 * @author Armin Reichert
 */
public class GameModelTests {

	private GameModel game;

	@Before
	public void setUp() {
		game = new PacManGame();
	}

	@Test
	public void testPacInitialized() {
		assertTrue(game.pac() instanceof Pac);
		assertFalse(game.pac().isDead());
		assertEquals(0, game.pac().restingTicks());
		assertEquals(0, game.pac().starvingTicks());
	}

	@Test
	public void testPacResting() {
		game.pac().rest(3);
		assertEquals(3, game.pac().restingTicks());
	}

	@Test
	public void testPacStarving() {
		game.pac().starve();
		assertEquals(1, game.pac().starvingTicks());
		game.pac().starve();
		assertEquals(2, game.pac().starvingTicks());
	}

	@Test
	public void testPacDying() {
		assertFalse(game.pac().isDead());
		game.pac().die();
		assertTrue(game.pac().isDead());
	}

	@Test
	public void testDeadPacHasZeroSpeed() {
		game.pac().setAbsSpeed(42);
		assertEquals(42.0, game.pac().velocity().length(), V2d.EPSILON);
		game.pac().die();
		assertEquals(0.0, game.pac().velocity().length(), V2d.EPSILON);
	}

	@Test
	public void testInitialScore() {
		assertEquals(0, game.gameScore().points());
	}

	@Test
	public void testDisabledGameScore() {
		game.enableScores(false);
		game.scorePoints(42);
		assertEquals(0, game.gameScore().points());
	}

	@Test
	public void testEnabledGameScore() {
		game.enableScores(true);
		game.scorePoints(42);
		assertEquals(42, game.gameScore().points());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testScoreNegativePoints() {
		game.scorePoints(-42);
	}
}
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

package de.amr.games.pacman.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import de.amr.games.pacman.lib.V2d;
import de.amr.games.pacman.model.common.GameLevel;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.actors.Bonus;
import de.amr.games.pacman.model.common.actors.BonusState;
import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.model.common.actors.Pac;
import de.amr.games.pacman.model.pacman.PacManGame;

/**
 * @author Armin Reichert
 */
public class GameModelTest {

	private GameModel game;

	@Before
	public void setUp() {
		game = new PacManGame();
	}

	@Test
	public void testLevelInitialized() {
		assertTrue(game.level() instanceof GameLevel);
		assertEquals(1, game.level().number());
		assertEquals(1, game.gameScore().levelNumber());
		assertTrue(game.bonus() instanceof Bonus);
		assertEquals(BonusState.INACTIVE, game.bonus().state());
		assertEquals(0, game.numGhostsKilledInLevel());
		assertEquals(0, game.numGhostsKilledByEnergizer());
		assertEquals(0, game.ghost(Ghost.ID_RED_GHOST).cruiseElroyState());
	}

	@Test
	public void testPacCreatedAndInitialized() {
		assertTrue(game.pac() instanceof Pac);
		assertFalse(game.pac().isDead());
		assertEquals(0, game.pac().restingTicks());
		assertEquals(0, game.pac().starvingTicks());
	}

	@Test
	public void testGhostsCreatedAndInitialized() {
		var redGhost = game.ghost(Ghost.ID_RED_GHOST);
		assertTrue(redGhost instanceof Ghost);
		assertEquals(-1, redGhost.killedIndex());
		assertNotNull(redGhost.homePosition());
		assertNotNull(redGhost.revivalPosition());
		assertNotNull(redGhost.scatterTile());

		var pinkGhost = game.ghost(Ghost.ID_PINK_GHOST);
		assertTrue(pinkGhost instanceof Ghost);
		assertEquals(-1, pinkGhost.killedIndex());
		assertNotNull(pinkGhost.homePosition());
		assertNotNull(pinkGhost.revivalPosition());
		assertNotNull(pinkGhost.scatterTile());

		var cyanGhost = game.ghost(Ghost.ID_CYAN_GHOST);
		assertTrue(cyanGhost instanceof Ghost);
		assertEquals(-1, cyanGhost.killedIndex());
		assertNotNull(cyanGhost.homePosition());
		assertNotNull(cyanGhost.revivalPosition());
		assertNotNull(cyanGhost.scatterTile());

		var orangeGhost = game.ghost(Ghost.ID_ORANGE_GHOST);
		assertTrue(orangeGhost instanceof Ghost);
		assertEquals(-1, orangeGhost.killedIndex());
		assertNotNull(orangeGhost.homePosition());
		assertNotNull(orangeGhost.revivalPosition());
		assertNotNull(orangeGhost.scatterTile());
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

	@Test
	public void testHighScore() {
		game.enableScores(true);
		assertEquals(0, game.highScore().points());
		game.scorePoints(42);
		assertEquals(42, game.highScore().points());
		game.scorePoints(0);
		assertEquals(42, game.highScore().points());
		game.scorePoints(1);
		assertEquals(43, game.highScore().points());
	}

	@Test
	public void testChangeCredit() {
		assertEquals(0, game.credit());
		game.changeCredit(2);
		assertEquals(2, game.credit());
		game.changeCredit(-2);
		assertEquals(0, game.credit());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testIllegalKilledIndex() {
		game.ghost(Ghost.ID_RED_GHOST).setKilledIndex(42);
	}

	@Test
	public void testLegalCruiseElroyState() {
		game.ghost(Ghost.ID_RED_GHOST).setCruiseElroyState(-2);
		game.ghost(Ghost.ID_RED_GHOST).setCruiseElroyState(-1);
		game.ghost(Ghost.ID_RED_GHOST).setCruiseElroyState(0);
		game.ghost(Ghost.ID_RED_GHOST).setCruiseElroyState(1);
		game.ghost(Ghost.ID_RED_GHOST).setCruiseElroyState(2);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testIllegalCruiseElroyState() {
		game.ghost(Ghost.ID_RED_GHOST).setCruiseElroyState(42);
	}
}
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

import de.amr.games.pacman.lib.math.Vector2f;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.actors.Bonus;
import de.amr.games.pacman.model.common.actors.BonusState;
import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.model.common.actors.Pac;
import de.amr.games.pacman.model.pacman.PacManGame;
import de.amr.games.pacman.model.pacman.StaticBonus;

/**
 * @author Armin Reichert
 */
public class PacManGameTest {

	private GameModel game;

	@Before
	public void setUp() {
		game = new PacManGame();
		game.buildAndEnterLevel(1);
	}

	@Test
	public void testLevelInitialized() {
		assertTrue(game.level().isPresent());
		var level = game.level().get();
		assertEquals(1, level.number());
		assertEquals(1, game.gameScore().levelNumber());
		assertTrue(level.bonus() instanceof Bonus);
		assertEquals(BonusState.INACTIVE, level.bonus().state());
		assertEquals(0, level.numGhostsKilledInLevel());
		assertEquals(0, level.numGhostsKilledByEnergizer());
		assertEquals(0, level.ghost(Ghost.ID_RED_GHOST).cruiseElroyState());
	}

	@Test
	public void testPacCreatedAndInitialized() {
		var level = game.level().get();
		var pac = level.pac();
		assertTrue(pac instanceof Pac);
		assertFalse(pac.isDead());
		assertEquals(0, pac.restingTicks());
		assertEquals(0, pac.starvingTicks());
	}

	@Test
	public void testGhostsCreatedAndInitialized() {
		var level = game.level().get();
		var redGhost = level.ghost(Ghost.ID_RED_GHOST);
		assertTrue(redGhost instanceof Ghost);
		assertEquals(-1, redGhost.killedIndex());
		assertNotNull(redGhost.homePosition());
		assertNotNull(redGhost.revivalPosition());
		assertNotNull(redGhost.scatterTile());

		var pinkGhost = level.ghost(Ghost.ID_PINK_GHOST);
		assertTrue(pinkGhost instanceof Ghost);
		assertEquals(-1, pinkGhost.killedIndex());
		assertNotNull(pinkGhost.homePosition());
		assertNotNull(pinkGhost.revivalPosition());
		assertNotNull(pinkGhost.scatterTile());

		var cyanGhost = level.ghost(Ghost.ID_CYAN_GHOST);
		assertTrue(cyanGhost instanceof Ghost);
		assertEquals(-1, cyanGhost.killedIndex());
		assertNotNull(cyanGhost.homePosition());
		assertNotNull(cyanGhost.revivalPosition());
		assertNotNull(cyanGhost.scatterTile());

		var orangeGhost = level.ghost(Ghost.ID_ORANGE_GHOST);
		assertTrue(orangeGhost instanceof Ghost);
		assertEquals(-1, orangeGhost.killedIndex());
		assertNotNull(orangeGhost.homePosition());
		assertNotNull(orangeGhost.revivalPosition());
		assertNotNull(orangeGhost.scatterTile());
	}

	@Test
	public void testPacResting() {
		var level = game.level().get();
		var pac = level.pac();
		pac.rest(3);
		assertEquals(3, pac.restingTicks());
	}

	@Test
	public void testPacStarving() {
		var level = game.level().get();
		var pac = level.pac();
		pac.starve();
		assertEquals(1, pac.starvingTicks());
		pac.starve();
		assertEquals(2, pac.starvingTicks());
	}

	@Test
	public void testPacDying() {
		var level = game.level().get();
		var pac = level.pac();
		assertFalse(pac.isDead());
		pac.kill();
		assertTrue(pac.isDead());
	}

	@Test
	public void testDeadPacHasZeroSpeed() {
		var level = game.level().get();
		var pac = level.pac();
		pac.setAbsSpeed(42);
		assertEquals(42.0, pac.velocity().length(), Vector2f.EPSILON);
		pac.kill();
		assertEquals(0.0, pac.velocity().length(), Vector2f.EPSILON);
	}

	@Test
	public void testBonus() {
		Bonus bonus = null;

		bonus = game.createBonus(1);
		assertTrue(bonus instanceof StaticBonus);
		assertEquals(100, bonus.points());

		bonus = game.createBonus(2);
		assertTrue(bonus instanceof StaticBonus);
		assertEquals(300, bonus.points());

		bonus = game.createBonus(3);
		assertTrue(bonus instanceof StaticBonus);
		assertEquals(500, bonus.points());

		bonus = game.createBonus(4);
		assertTrue(bonus instanceof StaticBonus);
		assertEquals(500, bonus.points());

		bonus = game.createBonus(5);
		assertTrue(bonus instanceof StaticBonus);
		assertEquals(700, bonus.points());

		bonus = game.createBonus(6);
		assertTrue(bonus instanceof StaticBonus);
		assertEquals(700, bonus.points());

		bonus = game.createBonus(7);
		assertTrue(bonus instanceof StaticBonus);
		assertEquals(1000, bonus.points());

		bonus = game.createBonus(8);
		assertTrue(bonus instanceof StaticBonus);
		assertEquals(1000, bonus.points());

		bonus = game.createBonus(9);
		assertTrue(bonus instanceof StaticBonus);
		assertEquals(2000, bonus.points());

		bonus = game.createBonus(10);
		assertTrue(bonus instanceof StaticBonus);
		assertEquals(2000, bonus.points());

		bonus = game.createBonus(11);
		assertTrue(bonus instanceof StaticBonus);
		assertEquals(3000, bonus.points());

		bonus = game.createBonus(12);
		assertTrue(bonus instanceof StaticBonus);
		assertEquals(3000, bonus.points());
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
		var level = game.level().get();
		level.ghost(Ghost.ID_RED_GHOST).setKilledIndex(42);
	}

	@Test
	public void testLegalCruiseElroyState() {
		var level = game.level().get();
		level.ghost(Ghost.ID_RED_GHOST).setCruiseElroyState(-2);
		level.ghost(Ghost.ID_RED_GHOST).setCruiseElroyState(-1);
		level.ghost(Ghost.ID_RED_GHOST).setCruiseElroyState(0);
		level.ghost(Ghost.ID_RED_GHOST).setCruiseElroyState(1);
		level.ghost(Ghost.ID_RED_GHOST).setCruiseElroyState(2);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testIllegalCruiseElroyState() {
		var level = game.level().get();
		level.ghost(Ghost.ID_RED_GHOST).setCruiseElroyState(42);
	}
}
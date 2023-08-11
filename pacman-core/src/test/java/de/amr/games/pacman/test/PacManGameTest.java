/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.test;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.IllegalGameVariantException;
import de.amr.games.pacman.model.actors.StaticBonus;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.Pac;

import static org.junit.Assert.*;

/**
 * @author Armin Reichert
 */
public class PacManGameTest {

	private GameModel game;

	@BeforeClass
	public static void setUp() {
		GameController.create(GameVariant.PACMAN);
	}

	@Before
	public void setUpTest() {
		game = GameController.it().game();
		game.enterLevel(1, true);
	}

	@Test
	public void testGameControllerCreated() {
		assertNotNull(GameController.it());
	}

	@Test(expected = IllegalStateException.class)
	public void testGameControllerCreatedTwice() {
		GameController.create(GameVariant.MS_PACMAN);
	}

	@Test
	public void testLevelInitialized() {
		assertTrue(game.level().isPresent());
		var level = game.level().get();
		assertEquals(1, level.number());
		assertEquals(0, level.numGhostsKilledInLevel());
		assertEquals(0, level.numGhostsKilledByEnergizer());
		assertEquals(0, level.cruiseElroyState());
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
		var redGhost = level.ghost(GameModel.RED_GHOST);
		assertTrue(redGhost instanceof Ghost);
		assertEquals(-1, redGhost.killedIndex());
		assertNotEquals(Vector2f.ZERO, redGhost.initialPosition());
		assertNotEquals(Vector2f.ZERO, redGhost.revivalPosition());
		assertNotEquals(Vector2i.ZERO, redGhost.scatterTile());

		var pinkGhost = level.ghost(GameModel.PINK_GHOST);
		assertTrue(pinkGhost instanceof Ghost);
		assertEquals(-1, pinkGhost.killedIndex());
		assertNotEquals(Vector2f.ZERO, pinkGhost.initialPosition());
		assertNotEquals(Vector2f.ZERO, pinkGhost.revivalPosition());
		assertNotEquals(Vector2i.ZERO, pinkGhost.scatterTile());

		var cyanGhost = level.ghost(GameModel.CYAN_GHOST);
		assertTrue(cyanGhost instanceof Ghost);
		assertEquals(-1, cyanGhost.killedIndex());
		assertNotEquals(Vector2f.ZERO, cyanGhost.initialPosition());
		assertNotEquals(Vector2f.ZERO, cyanGhost.revivalPosition());
		assertNotEquals(Vector2i.ZERO, cyanGhost.scatterTile());

		var orangeGhost = level.ghost(GameModel.ORANGE_GHOST);
		assertTrue(orangeGhost instanceof Ghost);
		assertEquals(-1, orangeGhost.killedIndex());
		assertNotEquals(Vector2f.ZERO, orangeGhost.initialPosition());
		assertNotEquals(Vector2f.ZERO, orangeGhost.revivalPosition());
		assertNotEquals(Vector2i.ZERO, orangeGhost.scatterTile());
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
		pac.killed();
		assertTrue(pac.isDead());
	}

	@Test
	public void testDeadPacHasZeroSpeed() {
		var level = game.level().get();
		var pac = level.pac();
		pac.setPixelSpeed(42);
		assertEquals(42.0, pac.velocity().length(), Vector2f.EPSILON);
		pac.killed();
		assertEquals(0.0, pac.velocity().length(), Vector2f.EPSILON);
	}

	@Test
	public void testPacManGameBonus() {
		for (int levelNumber = 1; levelNumber <= 21; ++levelNumber) {
			game.enterLevel(levelNumber, false);
			game.level().ifPresent(level -> {
				level.handleBonusReached(0);
				assertTrue(level.getBonus().isPresent());
				level.getBonus().ifPresent(bonus -> {
					assertTrue(bonus instanceof StaticBonus);
					assertEquals(GameModel.BONUS_VALUES_PACMAN[bonus.symbol()] * 100, bonus.points());
				});
			});
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void testScoreNegativePoints() {
		game.scorePoints(-42);
	}

	@Test
	public void testChangeCredit() {
		assertEquals(0, GameController.it().credit());
		GameController.it().changeCredit(2);
		assertEquals(2, GameController.it().credit());
		GameController.it().changeCredit(-2);
		assertEquals(0, GameController.it().credit());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testIllegalKilledIndex() {
		var level = game.level().get();
		level.ghost(GameModel.RED_GHOST).setKilledIndex(42);
	}

	@Test
	public void testLegalCruiseElroyState() {
		var level = game.level().get();
		level.setCruiseElroyState((byte) -2);
		assertEquals(-2, level.cruiseElroyState());
		level.setCruiseElroyState((byte) -1);
		assertEquals(-1, level.cruiseElroyState());
		level.setCruiseElroyState((byte) 0);
		assertEquals(0, level.cruiseElroyState());
		level.setCruiseElroyState((byte) 1);
		assertEquals(1, level.cruiseElroyState());
		level.setCruiseElroyState((byte) 2);
		assertEquals(2, level.cruiseElroyState());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testIllegalCruiseElroyState() {
		var level = game.level().get();
		level.setCruiseElroyState((byte) 42);
	}
}
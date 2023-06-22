/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import de.amr.games.pacman.controller.GameController;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.Pac;

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
		game.enterLevel(1);
	}

	@Test
	public void testLevelInitialized() {
		assertTrue(game.level().isPresent());
		var level = game.level().get();
		assertEquals(1, level.number());
//		assertTrue(level.bonus() instanceof Bonus);
//		assertEquals(Bonus.STATE_INACTIVE, level.bonus().state());
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

//	@Test
//	public void testPacManGameBonus() {
//		game.enterLevel(1);
//		game.level().ifPresent(level -> {
//			assertTrue(level.bonus() instanceof StaticBonus);
//			assertEquals(100, level.bonus().points());
//		});
//
//		game.enterLevel(2);
//		game.level().ifPresent(level -> {
//			assertTrue(level.bonus() instanceof StaticBonus);
//			assertEquals(300, level.bonus().points());
//		});
//
//		game.enterLevel(3);
//		game.level().ifPresent(level -> {
//			assertTrue(level.bonus() instanceof StaticBonus);
//			assertEquals(500, level.bonus().points());
//		});
//
//		game.enterLevel(4);
//		game.level().ifPresent(level -> {
//			assertTrue(level.bonus() instanceof StaticBonus);
//			assertEquals(500, level.bonus().points());
//		});
//
//		game.enterLevel(5);
//		game.level().ifPresent(level -> {
//			assertTrue(level.bonus() instanceof StaticBonus);
//			assertEquals(700, level.bonus().points());
//		});
//
//		game.enterLevel(6);
//		game.level().ifPresent(level -> {
//			assertTrue(level.bonus() instanceof StaticBonus);
//			assertEquals(700, level.bonus().points());
//		});
//
//		game.enterLevel(7);
//		game.level().ifPresent(level -> {
//			assertTrue(level.bonus() instanceof StaticBonus);
//			assertEquals(1000, level.bonus().points());
//		});
//
//		game.enterLevel(8);
//		game.level().ifPresent(level -> {
//			assertTrue(level.bonus() instanceof StaticBonus);
//			assertEquals(1000, level.bonus().points());
//		});
//
//		game.enterLevel(9);
//		game.level().ifPresent(level -> {
//			assertTrue(level.bonus() instanceof StaticBonus);
//			assertEquals(2000, level.bonus().points());
//		});
//
//		game.enterLevel(10);
//		game.level().ifPresent(level -> {
//			assertTrue(level.bonus() instanceof StaticBonus);
//			assertEquals(2000, level.bonus().points());
//		});
//
//		game.enterLevel(11);
//		game.level().ifPresent(level -> {
//			assertTrue(level.bonus() instanceof StaticBonus);
//			assertEquals(3000, level.bonus().points());
//		});
//
//		game.enterLevel(12);
//		game.level().ifPresent(level -> {
//			assertTrue(level.bonus() instanceof StaticBonus);
//			assertEquals(3000, level.bonus().points());
//		});
//	}

	@Test(expected = IllegalArgumentException.class)
	public void testScoreNegativePoints() {
		game.scorePoints(-42);
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
		level.ghost(GameModel.RED_GHOST).setKilledIndex(42);
	}

	@Test
	public void testLegalCruiseElroyState() {
		var level = game.level().get();
		level.setCruiseElroyState(-2);
		assertEquals(-2, level.cruiseElroyState());
		level.setCruiseElroyState(-1);
		assertEquals(-1, level.cruiseElroyState());
		level.setCruiseElroyState(0);
		assertEquals(0, level.cruiseElroyState());
		level.setCruiseElroyState(1);
		assertEquals(1, level.cruiseElroyState());
		level.setCruiseElroyState(2);
		assertEquals(2, level.cruiseElroyState());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testIllegalCruiseElroyState() {
		var level = game.level().get();
		level.setCruiseElroyState(42);
	}
}
/*
MIT License

Copyright (c) 2021-22 Armin Reichert

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
package de.amr.games.pacman.model.pacman;

import static de.amr.games.pacman.lib.NavigationPoint.np;
import static de.amr.games.pacman.lib.V2i.v2i;
import static de.amr.games.pacman.model.common.world.World.HTS;
import static de.amr.games.pacman.model.common.world.World.TS;

import java.io.File;
import java.util.List;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.event.GameEvents;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.NavigationPoint;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.GameLevel;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.model.common.actors.GhostState;
import de.amr.games.pacman.model.common.world.ArcadeWorld;
import de.amr.games.pacman.model.common.world.World;

/**
 * Model of the Pac-Man game.
 * 
 * @author Armin Reichert
 */
public class PacManGame extends GameModel {

	private static final Logger LOGGER = LogManager.getFormatterLogger();

	//@formatter:off
	private static final byte[][] LEVELS = {
	/* 1*/ {0 /* CHERRIES */,    80, 75, 40,  20,  80, 10,  85,  90, 50, 6, 5},
	/* 2*/ {1 /* STRAWBERRY */,  90, 85, 45,  30,  90, 15,  95,  95, 55, 5, 5},
	/* Intermission scene 1 */
	/* 3*/ {2 /* PEACH */,       90, 85, 45,  40,  90, 20,  95,  95, 55, 4, 5},
	/* 4*/ {2,                   90, 85, 45,  40,  90, 20,  95,  95, 55, 3, 5},
	/* 5*/ {3 /* APPLE */,      100, 95, 50,  40, 100, 20, 105, 100, 60, 2, 5},
	/* Intermission scene 2 */
	/* 6*/ {3,                  100, 95, 50,  50, 100, 25, 105, 100, 60, 5, 5},
	/* 7*/ {4 /* GRAPES */,     100, 95, 50,  50, 100, 25, 105, 100, 60, 2, 5},
	/* 8*/ {4,                  100, 95, 50,  50, 100, 25, 105, 100, 60, 2, 5},
	/* 9*/ {5 /* GALAXIAN */,   100, 95, 50,  60, 100, 30, 105, 100, 60, 1, 3},
	/* Intermission scene 3 */
	/*10*/ {5,                  100, 95, 50,  60, 100, 30, 105, 100, 60, 5, 5},
	/*11*/ {6 /* BELL */,       100, 95, 50,  60, 100, 30, 105, 100, 60, 2, 5},
	/*12*/ {6,                  100, 95, 50,  80, 100, 40, 105, 100, 60, 1, 3},
	/*13*/ {7 /* KEY */,        100, 95, 50,  80, 100, 40, 105, 100, 60, 1, 3},
	/* Intermission scene 3 */
	/*14*/ {7,                  100, 95, 50,  80, 100, 40, 105, 100, 60, 3, 5},
	/*15*/ {7,                  100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3},
	/*16*/ {7,                  100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3},
	/*17*/ {7,                  100, 95, 50, 100, 100, 50, 105,   0,  0, 0, 0},
	/* Intermission scene 3 */
	/*18*/ {7,                  100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3},
	/*19*/ {7,                  100, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0},
	/*20*/ {7,                  100, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0},
	/*21*/ {7,                   90, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0},
	};
	/*@formatter:on*/

	private static final short[] BONUS_VALUES = { 100, 300, 500, 700, 1000, 2000, 3000, 5000 };

	private static final byte[][] MAP = {
	//@formatter:off
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,},
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,},
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,},
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,},
		{1,3,3,3,3,3,3,3,3,3,3,3,3,1,1,3,3,3,3,3,3,3,3,3,3,3,3,1,},
		{1,3,1,1,1,1,3,1,1,1,1,1,3,1,1,3,1,1,1,1,1,3,1,1,1,1,3,1,},
		{1,4,1,1,1,1,3,1,1,1,1,1,3,1,1,3,1,1,1,1,1,3,1,1,1,1,4,1,},
		{1,3,1,1,1,1,3,1,1,1,1,1,3,1,1,3,1,1,1,1,1,3,1,1,1,1,3,1,},
		{1,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,1,},
		{1,3,1,1,1,1,3,1,1,3,1,1,1,1,1,1,1,1,3,1,1,3,1,1,1,1,3,1,},
		{1,3,1,1,1,1,3,1,1,3,1,1,1,1,1,1,1,1,3,1,1,3,1,1,1,1,3,1,},
		{1,3,3,3,3,3,3,1,1,3,3,3,3,1,1,3,3,3,3,1,1,3,3,3,3,3,3,1,},
		{1,1,1,1,1,1,3,1,1,1,1,1,0,1,1,0,1,1,1,1,1,3,1,1,1,1,1,1,},
		{0,0,0,0,0,1,3,1,1,1,1,1,0,1,1,0,1,1,1,1,1,3,1,0,0,0,0,0,},
		{0,0,0,0,0,1,3,1,1,0,0,0,0,0,0,0,0,0,0,1,1,3,1,0,0,0,0,0,},
		{0,0,0,0,0,1,3,1,1,0,1,1,1,0,0,1,1,1,0,1,1,3,1,0,0,0,0,0,},
		{1,1,1,1,1,1,3,1,1,0,1,0,0,0,0,0,0,1,0,1,1,3,1,1,1,1,1,1,},
		{2,2,2,2,2,2,3,0,0,0,1,0,0,0,0,0,0,1,0,0,0,3,2,2,2,2,2,2,},
		{1,1,1,1,1,1,3,1,1,0,1,0,0,0,0,0,0,1,0,1,1,3,1,1,1,1,1,1,},
		{0,0,0,0,0,1,3,1,1,0,1,1,1,1,1,1,1,1,0,1,1,3,1,0,0,0,0,0,},
		{0,0,0,0,0,1,3,1,1,0,0,0,0,0,0,0,0,0,0,1,1,3,1,0,0,0,0,0,},
		{0,0,0,0,0,1,3,1,1,0,1,1,1,1,1,1,1,1,0,1,1,3,1,0,0,0,0,0,},
		{1,1,1,1,1,1,3,1,1,0,1,1,1,1,1,1,1,1,0,1,1,3,1,1,1,1,1,1,},
		{1,3,3,3,3,3,3,3,3,3,3,3,3,1,1,3,3,3,3,3,3,3,3,3,3,3,3,1,},
		{1,3,1,1,1,1,3,1,1,1,1,1,3,1,1,3,1,1,1,1,1,3,1,1,1,1,3,1,},
		{1,3,1,1,1,1,3,1,1,1,1,1,3,1,1,3,1,1,1,1,1,3,1,1,1,1,3,1,},
		{1,4,3,3,1,1,3,3,3,3,3,3,3,0,0,3,3,3,3,3,3,3,1,1,3,3,4,1,},
		{1,1,1,3,1,1,3,1,1,3,1,1,1,1,1,1,1,1,3,1,1,3,1,1,3,1,1,1,},
		{1,1,1,3,1,1,3,1,1,3,1,1,1,1,1,1,1,1,3,1,1,3,1,1,3,1,1,1,},
		{1,3,3,3,3,3,3,1,1,3,3,3,3,1,1,3,3,3,3,1,1,3,3,3,3,3,3,1,},
		{1,3,1,1,1,1,1,1,1,1,1,1,3,1,1,3,1,1,1,1,1,1,1,1,1,1,3,1,},
		{1,3,1,1,1,1,1,1,1,1,1,1,3,1,1,3,1,1,1,1,1,1,1,1,1,1,3,1,},
		{1,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,1,},
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,},
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,},
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,},
		//@formatter:on
	};

	//@formatter:off
	public static final List<NavigationPoint> ATTRACT_ROUTE_PACMAN = List.of(
		np(12, 26), np( 9, 26),  np(12, 32), np(15, 32), np(24, 29),
		np(21, 23), np(18, 23),  np(18, 20), np(18, 17), np(15, 14),
		np(12, 14), np( 9, 17),  np( 6, 17), np( 6, 11), np( 6, 8), 
		np( 6, 4),  np( 1,  8),  np( 6,  8), np( 9,  8), np(12, 8),
		np( 6, 4),  np( 6,  8),  np( 6, 11), np( 1,  8), np( 6, 8),
		np( 9, 8),  np(12, 14),  np( 9, 17), np( 6, 17), np(0, 17),
		np(21, 17), np(21, 23),  np(21, 26), np(24, 29),
		/* avoid moving up: */ np(26, 29), 
		np(15, 32), np(12, 32),  np(3, 29),  np( 6, 23), np(9, 23),
		np(12, 26), np(15, 26), np(18, 23), np(21, 23), np(24, 29),
		/* avoid moving up: */ np(26, 29), 
		np(15, 32), np(12, 32), np(3, 29), np(6, 23)
	);
	
	public static final List<NavigationPoint> ATTRACT_FRIGHTENED_RED_GHOST = List.of(
			np(21, 4,  Direction.DOWN),
			np(21, 8,  Direction.DOWN),
			np(21, 11, Direction.RIGHT),
			np(26,  8, Direction.LEFT),
			np(21, 8,  Direction.DOWN),
			np(26,  8, Direction.UP),
			np(26,  8, Direction.DOWN),
			np(21, 11, Direction.DOWN),
			np(21, 17, Direction.RIGHT), // enters right tunnel
			
			np(99,99,Direction.DOWN)
	);
	
	public static final List<NavigationPoint> ATTRACT_FRIGHTENED_PINK_GHOST = List.of();
	public static final List<NavigationPoint> ATTRACT_FRIGHTENED_CYAN_GHOST = List.of();
	public static final List<NavigationPoint> ATTRACT_FRIGHTENED_ORANGE_GHOST = List.of();
	//@formatter:on

	private static final Random RND = new Random();

	public static ArcadeWorld createWorld() {
		return new ArcadeWorld(MAP);
	}

	// Tiles where chasing ghosts cannot move upwards
	public static final List<V2i> RED_ZONE = List.of(v2i(12, 14), v2i(15, 14), v2i(12, 26), v2i(15, 26));

	private final StaticBonus bonus;

	public PacManGame() {
		super("Pac-Man", "Blinky", "Pinky", "Inky", "Clyde");
		var bonusLocation = v2i(13, 20).toDoubleVec().scaled(TS).plus(HTS, 0); // between tiles (13,20) and (13,21)
		bonus = new StaticBonus(bonusLocation);
		setHiscoreFile(new File(System.getProperty("user.home"), "highscore-pacman.xml"));
		setLevel(1);
	}

	@Override
	public GameVariant variant() {
		return GameVariant.PACMAN;
	}

	private void createLevel(int levelNumber) {
		int numLevels = LEVELS.length;
		level = new GameLevel(levelNumber, 1, createWorld(), -1,
				levelNumber <= numLevels ? LEVELS[levelNumber - 1] : LEVELS[numLevels - 1]);
		pacStarvingTimeLimit = (level.number() < 5 ? 4 : 3) * FPS;
		globalDotLimits = new byte[] { -1, 7, 17, -1 };
		privateDotLimits = switch (level.number()) {
		case 1 -> new byte[] { 0, 0, 30, 60 };
		case 2 -> new byte[] { 0, 0, 0, 50 };
		default -> new byte[] { 0, 0, 0, 0 };
		};
		numGhostsKilledInLevel = 0;
	}

	@Override
	public boolean isGhostAllowedMoving(Ghost ghost, Direction dir) {
		if (dir == Direction.UP && ghost.is(GhostState.HUNTING_PAC) && RED_ZONE.contains(ghost.tile())) {
			return false;
		}
		return super.isGhostAllowedMoving(ghost, dir);
	}

	@Override
	public void setLevel(int levelNumber) {
		createLevel(levelNumber);
		initGhosts();
		bonus.setInactive();
		ghostsKilledByEnergizer = 0;
		gameScore.levelNumber = levelNumber;
	}

	@Override
	public StaticBonus bonus() {
		return bonus;
	}

	@Override
	protected void onBonusReached() {
		int ticks = 9 * FPS + RND.nextInt(FPS); // between 9 and 10 seconds
		LOGGER.info("Bonus activated for %d ticks", ticks);
		bonus.setEdible(level.bonusIndex(), BONUS_VALUES[level.bonusIndex()], ticks);
		GameEvents.publish(GameEventType.BONUS_GETS_ACTIVE, World.tileAt(bonus.position()));
	}
}
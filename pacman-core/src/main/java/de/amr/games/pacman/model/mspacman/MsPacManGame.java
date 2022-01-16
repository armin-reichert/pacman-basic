/*
MIT License

Copyright (c) 2021 Armin Reichert

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
package de.amr.games.pacman.model.mspacman;

import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.lib.TickTimer.sec_to_ticks;

import java.io.File;
import java.util.Random;

import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.model.common.Pac;
import de.amr.games.pacman.model.mspacman.entities.MovingBonus;
import de.amr.games.pacman.model.world.MapBasedWorld;

/**
 * Model of the Ms. Pac-Man game.
 * <p>
 * TODO: are the level data except for the bonus symbols the same as in Pac-Man?
 * <p>
 * See https://gamefaqs.gamespot.com/arcade/583976-ms-pac-man/faqs/1298
 * 
 * @author Armin Reichert
 */
public class MsPacManGame extends GameModel {

//@formatter:off
	
	public static final String CHERRIES =   "Cherries";
	public static final String STRAWBERRY = "Strawberry";
	public static final String PEACH =      "Peach";
	public static final String PRETZEL =    "Pretzel";
	public static final String APPLE =      "Apple";
	public static final String PEAR =       "Pear";
	public static final String BANANA =     "Banana";
	
	private final Object[][] LEVELS = {
	/* 1*/ {CHERRIES,    80, 75, 40,  20,  80, 10,  85,  90, 50, 6, 5},
	/* 2*/ {STRAWBERRY,  90, 85, 45,  30,  90, 15,  95,  95, 55, 5, 5},
	/* Intermission scene 1: "They Meet" */
	/* 3*/ {PEACH,       90, 85, 45,  40,  90, 20,  95,  95, 55, 4, 5},
	/* 4*/ {PRETZEL,     90, 85, 45,  40,  90, 20,  95,  95, 55, 3, 5},
	/* 5*/ {APPLE,      100, 95, 50,  40, 100, 20, 105, 100, 60, 2, 5},
	/* Intermission scene 2: "The Chase" */
	/* 6*/ {PEAR,       100, 95, 50,  50, 100, 25, 105, 100, 60, 5, 5},
	/* 7*/ {BANANA,     100, 95, 50,  50, 100, 25, 105, 100, 60, 2, 5},
	/* 8*/ {BANANA,     100, 95, 50,  50, 100, 25, 105, 100, 60, 2, 5},
	/* 9*/ {BANANA,     100, 95, 50,  60, 100, 30, 105, 100, 60, 1, 3},
	/* Intermission scene 3: "Junior" */
	/*10*/ {BANANA,     100, 95, 50,  60, 100, 30, 105, 100, 60, 4, 5},
	/*11*/ {BANANA,     100, 95, 50,  60, 100, 30, 105, 100, 60, 2, 5},
	/*12*/ {BANANA,     100, 95, 50,  80, 100, 40, 105, 100, 60, 1, 3},
	/*13*/ {BANANA,     100, 95, 50,  80, 100, 40, 105, 100, 60, 1, 3},
	/* Intermission scene 3: "Junior" */
	/*14*/ {BANANA,     100, 95, 50,  80, 100, 40, 105, 100, 60, 3, 5},
	/*15*/ {BANANA,     100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3},
	/*16*/ {BANANA,     100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3},
	/*17*/ {BANANA,     100, 95, 50, 100, 100, 50, 105,   0,  0, 0, 0},
	/* Intermission scene 3: "Junior" */
	/*18*/ {BANANA,     100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3},
	/*19*/ {BANANA,     100, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0},
	/*20*/ {BANANA,     100, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0},
	/*21*/ {BANANA,      90, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0},
	};

/*@formatter:on*/

	public MsPacManGame() {
		player = new Pac("Ms. Pac-Man");
		ghosts = createGhosts("Blinky", "Pinky", "Inky", "Sue");
		bonus = new MovingBonus();
		hiscorePath = new File(System.getProperty("user.home"), "highscore-ms_pacman.xml");
	}

	@Override
	protected Object[] levelData(int levelNumber) {
		return levelNumber - 1 < LEVELS.length ? LEVELS[levelNumber - 1] : LEVELS[LEVELS.length - 1];
	}

	@Override
	public void enterLevel(int levelNumber) {
		this.levelNumber = levelNumber;
		mazeNumber = mazeNumber(levelNumber);
		mapNumber = (mazeNumber == 5) ? 3 : (mazeNumber == 6) ? 4 : mazeNumber;

		world = new MapBasedWorld("/mspacman/maps/map" + mapNumber + ".txt");

		// can only be called after world has been set!
		setLevelData(levelNumber);
		huntingPhaseTicks = DEFAULT_HUNTING_PHASE_TICKS[levelNumber == 1 ? 0 : levelNumber <= 4 ? 1 : 2];

		if (levelNumber >= 8) {
			bonusSymbol = randomBonusSymbol();
		}
		levelCounter.add(bonusSymbol);

		player.world = world;
		player.starvingTimeLimit = sec_to_ticks(levelNumber < 5 ? 4 : 3);

		resetGhostBounty();
		for (Ghost ghost : ghosts) {
			ghost.world = world;
			ghost.dotCounter = 0;
			ghost.elroy = 0;
		}

		ghosts[RED_GHOST].homeTile = world.ghostHouse().entryTile();
		ghosts[RED_GHOST].revivalTile = world.ghostHouse().seat(1);
		ghosts[RED_GHOST].globalDotLimit = Integer.MAX_VALUE;
		ghosts[RED_GHOST].privateDotLimit = 0;

		ghosts[PINK_GHOST].homeTile = world.ghostHouse().seat(1);
		ghosts[PINK_GHOST].revivalTile = world.ghostHouse().seat(1);
		ghosts[PINK_GHOST].globalDotLimit = 7;
		ghosts[PINK_GHOST].privateDotLimit = 0;

		ghosts[CYAN_GHOST].homeTile = world.ghostHouse().seat(0);
		ghosts[CYAN_GHOST].revivalTile = world.ghostHouse().seat(0);
		ghosts[CYAN_GHOST].globalDotLimit = 17;
		ghosts[CYAN_GHOST].privateDotLimit = levelNumber == 1 ? 30 : 0;

		ghosts[ORANGE_GHOST].homeTile = world.ghostHouse().seat(2);
		ghosts[ORANGE_GHOST].revivalTile = world.ghostHouse().seat(2);
		ghosts[ORANGE_GHOST].globalDotLimit = Integer.MAX_VALUE;
		ghosts[ORANGE_GHOST].privateDotLimit = levelNumber == 1 ? 60 : levelNumber == 2 ? 50 : 0;

		bonus.world = world;
		bonus.init();

		log("Ms. Pac-Man game entered level #%d, maze number is %d, map number is %d", levelNumber, mazeNumber, mapNumber);
	}

	@Override
	public int intermissionNumber(int levelNumber) {
		switch (levelNumber) {
		case 2:
			return 1;
		case 5:
			return 2;
		case 9:
		case 13:
		case 17:
			return 3;
		default:
			return 0; // no intermission after this level
		}
	}

	/**
	 * Returns the maze number used in the given game level.
	 * <p>
	 * Up to level 13, the used mazes are:
	 * <ul>
	 * <li>Maze #1: pink maze, white dots (level 1-2)
	 * <li>Maze #2: light blue maze, yellow dots (level 3-5)
	 * <li>Maze #3: orange maze, red dots (level 6-9)
	 * <li>Maze #4: dark blue maze, white dots (level 10-13)
	 * </ul>
	 * From level 14 on, the maze alternates every 4th level between maze #5 and maze #6.
	 * <ul>
	 * <li>Maze #5: pink maze, cyan dots (same map as maze #3)
	 * <li>Maze #6: orange maze, white dots (same map as maze #4)
	 * </ul>
	 */
	private int mazeNumber(int levelNumber) {
		if (levelNumber < 1) {
			throw new IllegalArgumentException("Illegal level number: " + levelNumber);
		}
		//@formatter:off
		return (levelNumber <=  2) ? 1
		     : (levelNumber <=  5) ? 2
		     : (levelNumber <=  9) ? 3 
		     : (levelNumber <= 13) ? 4
		     : (levelNumber - 14) % 8 < 4 ? 5 : 6;
		//@formatter:on
	}

	@Override
	public int bonusValue(String symbolName) {
		switch (symbolName) {
		case CHERRIES:
			return 100;
		case STRAWBERRY:
			return 200;
		case PEACH:
			return 500;
		case PRETZEL:
			return 700;
		case APPLE:
			return 1000;
		case PEAR:
			return 2000;
		case BANANA:
			return 5000;
		default:
			throw new IllegalArgumentException("Unknown symbol name: " + symbolName);
		}
	}

	private String randomBonusSymbol() {
		switch (new Random().nextInt(7)) {
		case 0:
			return CHERRIES;
		case 1:
			return STRAWBERRY;
		case 2:
			return PEACH;
		case 3:
			return PRETZEL;
		case 4:
			return APPLE;
		case 5:
			return PEAR;
		case 6:
			return BANANA;
		default:
			throw new IllegalStateException();
		}
	}
}
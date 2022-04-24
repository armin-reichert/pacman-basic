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

import de.amr.games.pacman.lib.TickTimer;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.Pac;

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

	public static final int CHERRIES = 0, STRAWBERRY = 1, PEACH = 2, PRETZEL = 3, APPLE = 4, PEAR = 5, BANANA = 6;

	private final Object[][] data = {
	/*@formatter:off*/
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
	/*@formatter:on*/
	};

	public MsPacManGame() {
		player = new Pac("Ms. Pac-Man");
		createGhosts("Blinky", "Pinky", "Inky", "Sue");
		bonus = new MovingBonus();
		hiscoreFile = new File(System.getProperty("user.home"), "highscore-ms_pacman.xml");
	}

	@Override
	public void setLevel(int levelNumber) {
		if (levelNumber < 1) {
			throw new IllegalArgumentException("Level number must be at least 1, but is: " + levelNumber);
		}
		initLevel(levelNumber, levelNumber - 1 < data.length ? data[levelNumber - 1] : data[data.length - 1]);
		mazeNumber = mazeNumber(levelNumber);
		int mapNumber = mapNumber(mazeNumber);
		world = switch (mapNumber) {
		case 1 -> new MsPacManWorld1();
		case 2 -> new MsPacManWorld2();
		case 3 -> new MsPacManWorld3();
		case 4 -> new MsPacManWorld4();
		default -> throw new IllegalArgumentException();
		};
		huntingPhaseDurations = huntingPhaseDurationsTable[levelNumber == 1 ? 0 : levelNumber <= 4 ? 1 : 2];
		if (levelNumber >= 8) {
			bonusSymbol = new Random().nextInt(7);
		}
		levelCounter.add(bonusSymbol);
		player.world = world;
		player.starvingTimeLimit = (int) sec_to_ticks(levelNumber < 5 ? 4 : 3);
		ghostBounty = firstGhostBounty;
		resetGhosts(world);
		bonus.world = world;
		bonus.init();
		log("Ms. Pac-Man game entered level #%d, maze number=%d, map number=%d", levelNumber, mazeNumber, mapNumber);
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
		return switch (levelNumber) {
		case 1, 2 -> 1;
		case 3, 4, 5 -> 2;
		case 6, 7, 8, 9 -> 3;
		case 10, 11, 12, 13 -> 4;
		default -> (levelNumber - 14) % 8 < 4 ? 5 : 6;
		};
	}

	private int mapNumber(int mazeNumber) {
		return switch (mazeNumber) {
		case 5 -> 3;
		case 6 -> 4;
		default -> mazeNumber;
		};
	}

	@Override
	public int bonusValue(int symbol) {
		return switch (symbol) {
		case CHERRIES -> 100;
		case STRAWBERRY -> 200;
		case PEACH -> 500;
		case PRETZEL -> 700;
		case APPLE -> 1000;
		case PEAR -> 2000;
		case BANANA -> 5000;
		default -> throw new IllegalArgumentException("Unknown symbol ID: " + symbol);
		};
	}

	@Override
	public long bonusActivationTicks() {
		return TickTimer.INDEFINITE;
	}
}
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
package de.amr.games.pacman.model.pacman;

import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.lib.TickTimer.sec_to_ticks;

import java.io.File;
import java.util.Random;

import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.Pac;
import de.amr.games.pacman.model.pacman.world.PacManWorld;

/**
 * Model of the Pac-Man game.
 * 
 * @author Armin Reichert
 */
public class PacManGame extends GameModel {

	public static final int CHERRIES = 0, STRAWBERRY = 1, PEACH = 2, APPLE = 3, GRAPES = 4, GALAXIAN = 5, BELL = 6,
			KEY = 7;

	private final Object[][] data = {
	/*@formatter:off*/
	/* 1*/ {CHERRIES,    80, 75, 40,  20,  80, 10,  85,  90, 50, 6, 5},
	/* 2*/ {STRAWBERRY,  90, 85, 45,  30,  90, 15,  95,  95, 55, 5, 5},
	/* Intermission scene 1 */
	/* 3*/ {PEACH,       90, 85, 45,  40,  90, 20,  95,  95, 55, 4, 5},
	/* 4*/ {PEACH,       90, 85, 45,  40,  90, 20,  95,  95, 55, 3, 5},
	/* 5*/ {APPLE,      100, 95, 50,  40, 100, 20, 105, 100, 60, 2, 5},
	/* Intermission scene 2 */
	/* 6*/ {APPLE,      100, 95, 50,  50, 100, 25, 105, 100, 60, 5, 5},
	/* 7*/ {GRAPES,     100, 95, 50,  50, 100, 25, 105, 100, 60, 2, 5},
	/* 8*/ {GRAPES,     100, 95, 50,  50, 100, 25, 105, 100, 60, 2, 5},
	/* 9*/ {GALAXIAN,   100, 95, 50,  60, 100, 30, 105, 100, 60, 1, 3},
	/* Intermission scene 3 */
	/*10*/ {GALAXIAN,   100, 95, 50,  60, 100, 30, 105, 100, 60, 5, 5},
	/*11*/ {BELL,       100, 95, 50,  60, 100, 30, 105, 100, 60, 2, 5},
	/*12*/ {BELL,       100, 95, 50,  80, 100, 40, 105, 100, 60, 1, 3},
	/*13*/ {KEY,        100, 95, 50,  80, 100, 40, 105, 100, 60, 1, 3},
	/* Intermission scene 3 */
	/*14*/ {KEY,        100, 95, 50,  80, 100, 40, 105, 100, 60, 3, 5},
	/*15*/ {KEY,        100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3},
	/*16*/ {KEY,        100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3},
	/*17*/ {KEY,        100, 95, 50, 100, 100, 50, 105,   0,  0, 0, 0},
	/* Intermission scene 3 */
	/*18*/ {KEY,        100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3},
	/*19*/ {KEY,        100, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0},
	/*20*/ {KEY,        100, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0},
	/*21*/ {KEY,         90, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0},
	/*@formatter:on*/
	};

	public PacManGame() {
		// all levels use the same world
		world = new PacManWorld();
		mazeNumber = 1;
		player = new Pac("Pac-Man");
		player.world = world;
		createGhosts("Blinky", "Pinky", "Inky", "Clyde");
		resetGhosts(world);
		bonus = new Bonus();
		bonus.world = world;
		hiscoreFile = new File(System.getProperty("user.home"), "highscore-pacman.xml");
	}

	@Override
	public void setLevel(int levelNumber) {
		if (levelNumber < 1) {
			throw new IllegalArgumentException("Level number must be at least 1, but is: " + levelNumber);
		}
		initLevel(levelNumber, levelNumber - 1 < data.length ? data[levelNumber - 1] : data[data.length - 1]);
		world.resetFood();
		huntingPhaseDurations = huntingPhaseDurationsTable[levelNumber == 1 ? 0 : levelNumber <= 4 ? 1 : 2];
		levelCounter.add(bonusSymbol);
		player.starvingTimeLimit = (int) sec_to_ticks(levelNumber < 5 ? 4 : 3);
		ghostBounty = firstGhostBounty;
		resetGhosts(world);
		bonus.init();
		log("Pac-Man game entered level #%d", levelNumber);
	}

	@Override
	public int bonusValue(int symbol) {
		return switch (symbol) {
		case CHERRIES -> 100;
		case STRAWBERRY -> 300;
		case PEACH -> 500;
		case APPLE -> 700;
		case GRAPES -> 1000;
		case GALAXIAN -> 2000;
		case BELL -> 3000;
		case KEY -> 5000;
		default -> throw new IllegalArgumentException("Unknown symbol ID: " + symbol);
		};
	}

	@Override
	public long bonusActivationTicks() {
		return sec_to_ticks(9.0 + new Random().nextDouble());
	}
}
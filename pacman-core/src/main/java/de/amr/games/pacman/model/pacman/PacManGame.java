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
import static de.amr.games.pacman.model.world.PacManGameWorld.HTS;

import java.io.File;

import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.model.common.Pac;
import de.amr.games.pacman.model.pacman.entities.Bonus;
import de.amr.games.pacman.model.world.MapBasedWorld;

/**
 * Model of the Pac-Man game.
 * 
 * @author Armin Reichert
 */
public class PacManGame extends GameModel {

//@formatter:off
	
	public static final String CHERRIES =   "Cherries";
	public static final String STRAWBERRY = "Strawberry";
	public static final String PEACH =      "Peach";
	public static final String APPLE =      "Apple";
	public static final String GRAPES =     "Grapes";
	public static final String GALAXIAN =   "Galaxian";
	public static final String BELL =       "Bell";
	public static final String KEY =        "Key";
	
	private final Object[][] LEVELS = {
	/* 1*/ {CHERRIES,    80, 75, 40,  20,  80, 10,  85,  90, 50, 6, 5},
	/* 2*/ {STRAWBERRY,  90, 85, 45,  30,  90, 15,  95,  95, 55, 5, 5},
	/* 3*/ {PEACH,       90, 85, 45,  40,  90, 20,  95,  95, 55, 4, 5},
	/* 4*/ {PEACH,       90, 85, 45,  40,  90, 20,  95,  95, 55, 3, 5},
	/* 5*/ {APPLE,      100, 95, 50,  40, 100, 20, 105, 100, 60, 2, 5},
	/* 6*/ {APPLE,      100, 95, 50,  50, 100, 25, 105, 100, 60, 5, 5},
	/* 7*/ {GRAPES,     100, 95, 50,  50, 100, 25, 105, 100, 60, 2, 5},
	/* 8*/ {GRAPES,     100, 95, 50,  50, 100, 25, 105, 100, 60, 2, 5},
	/* 9*/ {GALAXIAN,   100, 95, 50,  60, 100, 30, 105, 100, 60, 1, 3},
	/*10*/ {GALAXIAN,   100, 95, 50,  60, 100, 30, 105, 100, 60, 5, 5},
	/*11*/ {BELL,       100, 95, 50,  60, 100, 30, 105, 100, 60, 2, 5},
	/*12*/ {BELL,       100, 95, 50,  80, 100, 40, 105, 100, 60, 1, 3},
	/*13*/ {KEY,        100, 95, 50,  80, 100, 40, 105, 100, 60, 1, 3},
	/*14*/ {KEY,        100, 95, 50,  80, 100, 40, 105, 100, 60, 3, 5},
	/*15*/ {KEY,        100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3},
	/*16*/ {KEY,        100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3},
	/*17*/ {KEY,        100, 95, 50, 100, 100, 50, 105,   0,  0, 0, 0},
	/*18*/ {KEY,        100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3},
	/*19*/ {KEY,        100, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0},
	/*20*/ {KEY,        100, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0},
	/*21*/ {KEY,         90, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0},
	};
	
/*@formatter:on*/

	public PacManGame() {
		mazeNumber = 1;
		mapNumber = 1;
		world = new MapBasedWorld("/pacman/maps/map1.txt");
		player = new Pac("Pac-Man");
		ghosts = createGhosts("Blinky", "Pinky", "Inky", "Clyde");
		bonus = new Bonus();
		hiscorePath = new File(System.getProperty("user.home"), "highscore-pacman.xml");
	}

	@Override
	protected Object[] levelData(int levelNumber) {
		return levelNumber - 1 < LEVELS.length ? LEVELS[levelNumber - 1] : LEVELS[LEVELS.length - 1];
	}

	@Override
	public void enterLevel(int levelNumber) {
		this.levelNumber = levelNumber;
		loadLevel(levelNumber);

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
		bonus.placeAt(world.bonusTile(), HTS, 0);

		log("Pac-Man game entered level #%d", levelNumber);
	}

	@Override
	public int bonusValue(String symbolName) {
		switch (symbolName) {
		case CHERRIES:
			return 100;
		case STRAWBERRY:
			return 300;
		case PEACH:
			return 500;
		case APPLE:
			return 700;
		case GRAPES:
			return 1000;
		case GALAXIAN:
			return 2000;
		case BELL:
			return 3000;
		case KEY:
			return 5000;
		default:
			throw new IllegalArgumentException("Unknown symbol name: " + symbolName);
		}
	}
}
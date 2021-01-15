package de.amr.games.pacman.world;

import static de.amr.games.pacman.lib.Direction.LEFT;

import java.util.Random;

import de.amr.games.pacman.core.PacManGameLevel;
import de.amr.games.pacman.lib.Direction;

/**
 * Ms. Pac-Man game world. Has 6 maze variants.
 * 
 * TODO: lots of details still missing
 * 
 * @author Armin Reichert
 */
public class MsPacManWorld extends AbstractPacManGameWorld {

	public static final byte CHERRIES = 0, STRAWBERRY = 1, ORANGE = 2, PRETZEL = 3, APPLE = 4, PEAR = 5, BANANA = 6;

	public static final short[] BONUS_POINTS = { 100, 200, 500, 700, 1000, 2000, 5000 };

	// TODO how exactly are the levels of the Ms.Pac-Man game?
	/*@formatter:off*/
	public static final int[][] LEVELS = {
	/* 1*/ {CHERRIES,    80, 75, 40,  20,  80, 10,  85,  90, 50, 6, 5},
	/* 2*/ {STRAWBERRY,  90, 85, 45,  30,  90, 15,  95,  95, 55, 5, 5},
	/* 3*/ {ORANGE,      90, 85, 45,  40,  90, 20,  95,  95, 55, 4, 5},
	/* 4*/ {PRETZEL,     90, 85, 45,  40,  90, 20,  95,  95, 55, 3, 5},
	/* 5*/ {APPLE,      100, 95, 50,  40, 100, 20, 105, 100, 60, 2, 5},
	/* 6*/ {PEAR,       100, 95, 50,  50, 100, 25, 105, 100, 60, 5, 5},
	/* 7*/ {BANANA,     100, 95, 50,  50, 100, 25, 105, 100, 60, 2, 5},
	/* 8*/ {BANANA,     100, 95, 50,  50, 100, 25, 105, 100, 60, 2, 5},
	/* 9*/ {BANANA,     100, 95, 50,  60, 100, 30, 105, 100, 60, 1, 3},
	/*10*/ {BANANA,     100, 95, 50,  60, 100, 30, 105, 100, 60, 5, 5},
	/*11*/ {BANANA,     100, 95, 50,  60, 100, 30, 105, 100, 60, 2, 5},
	/*12*/ {BANANA,     100, 95, 50,  80, 100, 40, 105, 100, 60, 1, 3},
	/*13*/ {BANANA,     100, 95, 50,  80, 100, 40, 105, 100, 60, 1, 3},
	/*14*/ {BANANA,     100, 95, 50,  80, 100, 40, 105, 100, 60, 3, 5},
	/*15*/ {BANANA,     100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3},
	/*16*/ {BANANA,     100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3},
	/*17*/ {BANANA,     100, 95, 50, 100, 100, 50, 105,   0,  0, 0, 0},
	/*18*/ {BANANA,     100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3},
	/*19*/ {BANANA,     100, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0},
	/*20*/ {BANANA,     100, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0},
	/*21*/ {BANANA,      90, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0},
	};
	/*@formatter:on*/

	private static final String[] GHOST_NAMES = { "Blinky", "Pinky", "Inky", "Sue" };

	private final Random rnd = new Random();
	private int mapIndex; // 1-6

	private void selectMap(int mapIndex) {
		if (mapIndex < 1 || mapIndex > 6) {
			throw new IllegalArgumentException("Illegal map index: " + mapIndex);
		}
		this.mapIndex = mapIndex;
		// Map #5 is the same as #3, only different color, same for #6 vs. #4
		int fileIndex = mapIndex == 5 ? 3 : mapIndex == 6 ? 4 : mapIndex;
		loadMap("/worlds/mspacman/map" + fileIndex + ".txt");
	}

	@Override
	public void initLevel(int levelNumber) {
		if (levelNumber < 1) {
			throw new IllegalArgumentException("Illegal level number: " + levelNumber);
		}
		if (levelNumber <= 2) {
			selectMap(1); // pink maze
		} else if (levelNumber <= 5) {
			selectMap(2); // light blue maze
		} else if (levelNumber <= 9) {
			selectMap(3); // orange maze
		} else if (levelNumber <= 13) {
			selectMap(4); // dark blue maze
		} else {
			int mapIndex = (levelNumber - 14) % 8 < 4 ? 5 : 6; // TODO correct?
			selectMap(mapIndex);
		}
	}

	public int getMapIndex() {
		return mapIndex;
	}

	@Override
	public PacManGameLevel createLevel(int levelNumber) {
		int index = levelNumber <= 21 ? levelNumber - 1 : 20;
		PacManGameLevel level = new PacManGameLevel(LEVELS[index]);
		if (levelNumber > 7) {
			level.bonusSymbol = (byte) rnd.nextInt(7);
		}
		return level;
	}

	@Override
	public String pacName() {
		return "Ms. Pac-Man";
	}

	@Override
	public Direction pacStartDirection() {
		return LEFT;
	}

	@Override
	public String ghostName(int ghost) {
		return GHOST_NAMES[ghost];
	}

	@Override
	public boolean isUpwardsBlocked(int x, int y) {
		return false; // ghosts can travel all paths
	}
}
package de.amr.games.pacman.game.worlds;

import static de.amr.games.pacman.lib.Direction.RIGHT;

import java.util.Arrays;
import java.util.List;

import de.amr.games.pacman.game.core.PacManGameLevel;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2i;

/**
 * The game world used by the classic Pac-Man game.
 * 
 * @author Armin Reichert
 */
public class PacManClassicWorld extends AbstractPacManGameWorld {

	public static final byte BLINKY = 0, PINKY = 1, INKY = 2, CLYDE = 3;

	public static final byte CHERRIES = 0, STRAWBERRY = 1, PEACH = 2, APPLE = 3, GRAPES = 4, GALAXIAN = 5, BELL = 6,
			KEY = 7;

	public static final short[] BONUS_POINTS = { 100, 300, 500, 700, 1000, 2000, 3000, 5000 };

	public static final V2i BONUS_TILE = new V2i(13, 20);

	public static final List<V2i> UPWARDS_BLOCKED_TILES = Arrays.asList(new V2i(12, 13), new V2i(15, 13), new V2i(12, 25),
			new V2i(15, 25));

	private static final String[] GHOST_NAMES = { "Blinky", "Pinky", "Inky", "Clyde" };

	/*@formatter:off*/
	private static final int[][] LEVELS = {
	/* 1*/ {CHERRIES,   80, 75, 40,  20,  80, 10,  85,  90, 50, 6, 5},
	/* 2*/ {STRAWBERRY, 90, 85, 45,  30,  90, 15,  95,  95, 55, 5, 5},
	/* 3*/ {PEACH,      90, 85, 45,  40,  90, 20,  95,  95, 55, 4, 5},
	/* 4*/ {PEACH,      90, 85, 45,  40,  90, 20,  95,  95, 55, 3, 5},
	/* 5*/ {APPLE,     100, 95, 50,  40, 100, 20, 105, 100, 60, 2, 5},
	/* 6*/ {APPLE,     100, 95, 50,  50, 100, 25, 105, 100, 60, 5, 5},
	/* 7*/ {GRAPES,    100, 95, 50,  50, 100, 25, 105, 100, 60, 2, 5},
	/* 8*/ {GRAPES,    100, 95, 50,  50, 100, 25, 105, 100, 60, 2, 5},
	/* 9*/ {GALAXIAN,  100, 95, 50,  60, 100, 30, 105, 100, 60, 1, 3},
	/*10*/ {GALAXIAN,  100, 95, 50,  60, 100, 30, 105, 100, 60, 5, 5},
	/*11*/ {BELL,      100, 95, 50,  60, 100, 30, 105, 100, 60, 2, 5},
	/*12*/ {BELL,      100, 95, 50,  80, 100, 40, 105, 100, 60, 1, 3},
	/*13*/ {KEY,       100, 95, 50,  80, 100, 40, 105, 100, 60, 1, 3},
	/*14*/ {KEY,       100, 95, 50,  80, 100, 40, 105, 100, 60, 3, 5},
	/*15*/ {KEY,       100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3},
	/*16*/ {KEY,       100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3},
	/*17*/ {KEY,       100, 95, 50, 100, 100, 50, 105,   0,  0, 0, 0},
	/*18*/ {KEY,       100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3},
	/*19*/ {KEY,       100, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0},
	/*20*/ {KEY,       100, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0},
	/*21*/ {KEY,        90, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0},
	};
	/*@formatter:on*/

	public PacManClassicWorld() {
		loadMap("/worlds/classic/map.txt");
	}

	@Override
	public PacManGameLevel createLevel(int levelNumber) {
		PacManGameLevel level = new PacManGameLevel(LEVELS[levelNumber <= 21 ? levelNumber - 1 : 20]);
		restoreFood();
		return level;
	}

	@Override
	public String pacName() {
		return "Pac-Man";
	}

	@Override
	public Direction pacStartDirection() {
		return RIGHT;
	}

	@Override
	public String ghostName(int ghost) {
		return GHOST_NAMES[ghost];
	}

	@Override
	public boolean isUpwardsBlocked(int x, int y) {
		return UPWARDS_BLOCKED_TILES.contains(new V2i(x, y));
	}
}
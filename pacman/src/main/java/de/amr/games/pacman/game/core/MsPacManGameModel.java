package de.amr.games.pacman.game.core;

import static de.amr.games.pacman.game.heaven.God.random;
import static de.amr.games.pacman.lib.Direction.DOWN;
import static de.amr.games.pacman.lib.Direction.LEFT;
import static de.amr.games.pacman.lib.Direction.UP;
import static de.amr.games.pacman.lib.Logging.log;

import de.amr.games.pacman.game.creatures.Ghost;
import de.amr.games.pacman.game.creatures.MovingBonus;
import de.amr.games.pacman.game.creatures.Pac;
import de.amr.games.pacman.game.worlds.MapBasedPacManGameWorld;
import de.amr.games.pacman.lib.Direction;

/**
 * Game model of the Ms. Pac-Man game variant.
 * 
 * @author Armin Reichert
 */
public class MsPacManGameModel extends PacManGameModel {

	public enum MsPacManSymbols {
		CHERRIES, STRAWBERRY, PEACH, PRETZEL, APPLE, PEAR, BANANA;
	}

	// TODO how exactly are the levels of the Ms.Pac-Man game?
	/*@formatter:off*/
	public static final int[][] MS_PACMAN_LEVELS = {
	/* 1*/ {0,  80, 75, 40,  20,  80, 10,  85,  90, 50, 6, 5},
	/* 2*/ {1,  90, 85, 45,  30,  90, 15,  95,  95, 55, 5, 5},
	/* 3*/ {2,  90, 85, 45,  40,  90, 20,  95,  95, 55, 4, 5},
	/* 4*/ {3,  90, 85, 45,  40,  90, 20,  95,  95, 55, 3, 5},
	/* 5*/ {4, 100, 95, 50,  40, 100, 20, 105, 100, 60, 2, 5},
	/* 6*/ {5, 100, 95, 50,  50, 100, 25, 105, 100, 60, 5, 5},
	/* 7*/ {6, 100, 95, 50,  50, 100, 25, 105, 100, 60, 2, 5},
	/* 8*/ {0, 100, 95, 50,  50, 100, 25, 105, 100, 60, 2, 5},
	/* 9*/ {0, 100, 95, 50,  60, 100, 30, 105, 100, 60, 1, 3},
	/*10*/ {0, 100, 95, 50,  60, 100, 30, 105, 100, 60, 5, 5},
	/*11*/ {0, 100, 95, 50,  60, 100, 30, 105, 100, 60, 2, 5},
	/*12*/ {0, 100, 95, 50,  80, 100, 40, 105, 100, 60, 1, 3},
	/*13*/ {0, 100, 95, 50,  80, 100, 40, 105, 100, 60, 1, 3},
	/*14*/ {0, 100, 95, 50,  80, 100, 40, 105, 100, 60, 3, 5},
	/*15*/ {0, 100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3},
	/*16*/ {0, 100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3},
	/*17*/ {0, 100, 95, 50, 100, 100, 50, 105,   0,  0, 0, 0},
	/*18*/ {0, 100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3},
	/*19*/ {0, 100, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0},
	/*20*/ {0, 100, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0},
	/*21*/ {0,  90, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0},
	};
	/*@formatter:on*/

	private static int mazeNumber(int levelNumber) {
		if (levelNumber <= 2) {
			return 1; // pink maze, white dots
		}
		if (levelNumber <= 5) {
			return 2; // light blue maze, yellow dots
		}
		if (levelNumber <= 9) {
			return 3; // orange maze, red dots
		}
		if (levelNumber <= 13) {
			return 4; // dark blue maze, white dots
		}
		// from level 14 on, maze switches between 5 and 6 every 4 levels
		if ((levelNumber - 14) % 8 < 4) {
			return 5; // pink maze, cyan dots (same map as maze 3)
		}
		return 6; // orange maze, white dots (same map as maze 4)
	}

	public MsPacManGameModel() {
		variant = MS_PACMAN;
		world = new MapBasedPacManGameWorld();
		bonusNames = new String[] { "CHERRIES", "STRAWBERRY", "PEACH", "PRETZEL", "APPLE", "PEAR", "BANANA" };
		bonusValues = new int[] { 100, 200, 500, 700, 1000, 2000, 5000 };
		bonus = new MovingBonus(world);
		pac = new Pac(world);
		pac.name = "Ms. Pac-Man";
		pac.startDir = Direction.LEFT;
		ghosts = new Ghost[4];
		for (int ghostID = 0; ghostID < 4; ++ghostID) {
			ghosts[ghostID] = new Ghost(ghostID, world);
		}
		ghosts[0].name = "Blinky";
		ghosts[1].name = "Pinky";
		ghosts[2].name = "Inky";
		ghosts[3].name = "Sue";
		ghosts[0].startDir = LEFT;
		ghosts[1].startDir = UP;
		ghosts[2].startDir = DOWN;
		ghosts[3].startDir = DOWN;

		reset();
	}

	@Override
	public void createLevel(int number) {
		levelNumber = number;
		int mazeNumber = mazeNumber(number);
		// Maze #5 has the same map as #3 but a different color, same for #6 vs. #4
		int mapIndex = mazeNumber == 5 ? 3 : mazeNumber == 6 ? 4 : mazeNumber;
		world.loadMap("/worlds/mspacman/map" + mapIndex + ".txt");
		log("Use maze #%d at game level %d", mazeNumber, levelNumber);
		// map has been loaded, now create level
		level = new PacManGameLevel(world, MS_PACMAN_LEVELS[levelNumber <= 21 ? levelNumber - 1 : 20]);
		level.mazeNumber = mazeNumber;
		if (levelNumber > 7) {
			level.bonusSymbol = (byte) random.nextInt(7);
		}
	}
}
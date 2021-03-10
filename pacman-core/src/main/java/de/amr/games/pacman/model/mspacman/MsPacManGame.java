package de.amr.games.pacman.model.mspacman;

import static de.amr.games.pacman.lib.Direction.DOWN;
import static de.amr.games.pacman.lib.Direction.LEFT;
import static de.amr.games.pacman.lib.Direction.UP;
import static de.amr.games.pacman.lib.God.clock;
import static de.amr.games.pacman.lib.God.random;
import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.model.common.Ghost.BLINKY;
import static de.amr.games.pacman.model.common.Ghost.INKY;
import static de.amr.games.pacman.model.common.Ghost.PINKY;
import static de.amr.games.pacman.model.common.Ghost.SUE;

import de.amr.games.pacman.model.common.GameLevel;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.model.common.Pac;
import de.amr.games.pacman.model.world.MapBasedPacManGameWorld;
import de.amr.games.pacman.model.world.WorldMap;

/**
 * Game model of the Ms. Pac-Man game variant.
 * 
 * @author Armin Reichert
 */
public class MsPacManGame extends GameModel {

	/*@formatter:off*/
	public static final int[][] MSPACMAN_LEVELS = {
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

	public static final short[][] HUNTING_PHASE_DURATION = {
		//@formatter:off
		{ 7, 20, 7, 20, 5,   20,  5, Short.MAX_VALUE },
		{ 7, 20, 7, 20, 5, 1033, -1, Short.MAX_VALUE },
		{ 5, 20, 5, 20, 5, 1037, -1, Short.MAX_VALUE },
		//@formatter:on
	};

	private final MapBasedPacManGameWorld world;

	public MsPacManGame() {
		// all levels share one game world
		world = new MapBasedPacManGameWorld();

		// sanity check
		log("Checking Ms. Pac-Man game maps...");
		for (int mapNumber = 1; mapNumber <= 4; ++mapNumber) {
			try {
				new WorldMap("/mspacman/maps/map" + mapNumber + ".txt");
			} catch (Exception x) {
				log("Map '%s' contains errors, see log for details");
			}
		}

		bonusNames = new String[] { "CHERRIES", "STRAWBERRY", "PEACH", "PRETZEL", "APPLE", "PEAR", "BANANA" };
		bonusValues = new int[] { 100, 200, 500, 700, 1000, 2000, 5000 };
		bonus = new MsPacManBonus();

		pac = new Pac("Ms. Pac-Man", LEFT);

		ghosts = new Ghost[4];
		ghosts[BLINKY] = new Ghost(BLINKY, "Blinky", LEFT);
		ghosts[PINKY] = new Ghost(PINKY, "Pinky", UP);
		ghosts[INKY] = new Ghost(INKY, "Inky", DOWN);
		ghosts[SUE] = new Ghost(SUE, "Sue", DOWN);

		bonus.world = world;
		pac.world = world;
		for (Ghost ghost : ghosts) {
			ghost.world = world;
		}

		highscoreFileName = "hiscore-mspacman.xml";
	}

	@Override
	public int mazeNumber(int somelevelNumber) {
		switch (somelevelNumber) {
		case 1:
		case 2:
			return 1; // pink maze, white dots
		case 3:
		case 4:
		case 5:
			return 2; // light blue maze, yellow dots
		case 6:
		case 7:
		case 8:
		case 9:
			return 3; // orange maze, red dots
		case 10:
		case 11:
		case 12:
		case 13:
			return 4; // dark blue maze, white dots
		default:
			if (somelevelNumber < 1) {
				throw new IllegalArgumentException("Illegal level number: " + somelevelNumber);
			}
			// From level 14 on, maze switches between 5 and 6 every 4 levels
			// Maze #5 = pink maze, cyan dots (same map as maze 3)
			// Maze #6 = orange maze, white dots (same map as maze 4)
			return (somelevelNumber - 14) % 8 < 4 ? 5 : 6;
		}
	}

	@Override
	public int mapNumber(int mazeNumber) {
		// Maze #5 has the same map as #3 but a different color, same for #6 vs. #4
		return mazeNumber == 5 ? 3 : mazeNumber == 6 ? 4 : mazeNumber;
	}

	@Override
	protected void buildLevel(int someLevelNumber) {
		int mazeNumber = mazeNumber(someLevelNumber);
		world.setMap(new WorldMap("/mspacman/maps/map" + mapNumber(mazeNumber) + ".txt"));
		level = new GameLevel(MSPACMAN_LEVELS[someLevelNumber <= 21 ? someLevelNumber - 1 : 20]);
		level.setWorld(world);
		level.mazeNumber = mazeNumber;
		if (someLevelNumber > 7) {
			level.bonusSymbol = (byte) random.nextInt(7);
		}
		log("Ms. Pac-Man level %d created, maze index is %d", someLevelNumber, mazeNumber);
	}

	@Override
	public long getHuntingPhaseDuration(int phase) {
		int row = levelNumber == 1 ? 0 : levelNumber <= 4 ? 1 : 2;
		return huntingTicks(HUNTING_PHASE_DURATION[row][phase]);
	}

	private long huntingTicks(short duration) {
		if (duration == -1) {
			return 1; // -1 means a single tick
		}
		if (duration == Short.MAX_VALUE) {
			return Long.MAX_VALUE;
		}
		return clock.sec(duration);
	}

}
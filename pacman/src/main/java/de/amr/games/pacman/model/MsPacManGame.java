package de.amr.games.pacman.model;

import static de.amr.games.pacman.heaven.God.random;
import static de.amr.games.pacman.lib.Direction.DOWN;
import static de.amr.games.pacman.lib.Direction.LEFT;
import static de.amr.games.pacman.lib.Direction.UP;
import static de.amr.games.pacman.lib.Logging.log;

import de.amr.games.pacman.world.MapBasedPacManGameWorld;
import de.amr.games.pacman.world.WorldMap;

/**
 * Game model of the Ms. Pac-Man game variant.
 * 
 * @author Armin Reichert
 */
public class MsPacManGame extends AbstractPacManGame {

	private final MapBasedPacManGameWorld world;

	public MsPacManGame() {

		highscoreFileName = "hiscore-mspacman.xml";

		bonusNames = new String[] { "CHERRIES", "STRAWBERRY", "PEACH", "PRETZEL", "APPLE", "PEAR", "BANANA" };
		bonusValues = new int[] { 100, 200, 500, 700, 1000, 2000, 5000 };
		bonus = new MovingBonus();

		pac = new Pac("Ms. Pac-Man", LEFT);

		ghosts = new Ghost[4];
		ghosts[BLINKY] = new Ghost(BLINKY, "Blinky", LEFT);
		ghosts[PINKY] = new Ghost(PINKY, "Pinky", UP);
		ghosts[INKY] = new Ghost(INKY, "Inky", DOWN);
		ghosts[SUE] = new Ghost(SUE, "Sue", DOWN);

		// all levels share this world
		world = new MapBasedPacManGameWorld();
		pac.world = world;
		for (Ghost ghost : ghosts) {
			ghost.world = world;
		}
		bonus.world = world;

		reset();
	}

	@Override
	public int mazeNumber(int levelNumber) {
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

	@Override
	public int mapIndex(int mazeNumber) {
		// Maze #5 has the same map as #3 but a different color, same for #6 vs. #4
		return mazeNumber == 5 ? 3 : mazeNumber == 6 ? 4 : mazeNumber;
	}

	@Override
	public void buildLevel(int levelNumber) {
		log("Ms. Pac-Man level %d is getting created...", levelNumber);
		int mazeNumber = mazeNumber(levelNumber);
		world.setMap(new WorldMap("/mspacman/maps/map" + mapIndex(mazeNumber) + ".txt"));
		level = new GameLevel(MSPACMAN_LEVELS[levelNumber <= 21 ? levelNumber - 1 : 20]);
		level.setWorld(world);
		level.mazeNumber = mazeNumber;
		if (levelNumber > 7) {
			level.bonusSymbol = (byte) random.nextInt(7);
		}
		log("Ms. Pac-Man level %d created, maze index is %d", levelNumber, mazeNumber);
	}

	@Override
	public long bonusActivationTicks() {
		return Long.MAX_VALUE;
	}
}
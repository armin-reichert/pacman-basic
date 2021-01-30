package de.amr.games.pacman.game.core;

import static de.amr.games.pacman.lib.Direction.DOWN;
import static de.amr.games.pacman.lib.Direction.LEFT;
import static de.amr.games.pacman.lib.Direction.UP;
import static de.amr.games.pacman.lib.Logging.log;

import de.amr.games.pacman.game.creatures.Bonus;
import de.amr.games.pacman.game.creatures.Ghost;
import de.amr.games.pacman.game.creatures.Pac;
import de.amr.games.pacman.game.worlds.MapBasedPacManGameWorld;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2f;
import de.amr.games.pacman.lib.V2i;

/**
 * Game model of the classic Pac-Man game.
 * 
 * @author Armin Reichert
 */
public class PacManClassicGameModel extends PacManGameModel {

	public enum PacManClassicSymbols {
		CHERRIES, STRAWBERRY, PEACH, APPLE, GRAPES, GALAXIAN, BELL, KEY;
	}

	/*@formatter:off*/
	private static final int[][] PACMAN_CLASSIC_LEVELS = {
	/* 1*/ {0,  80, 75, 40,  20,  80, 10,  85,  90, 50, 6, 5},
	/* 2*/ {1,  90, 85, 45,  30,  90, 15,  95,  95, 55, 5, 5},
	/* 3*/ {2,  90, 85, 45,  40,  90, 20,  95,  95, 55, 4, 5},
	/* 4*/ {2,  90, 85, 45,  40,  90, 20,  95,  95, 55, 3, 5},
	/* 5*/ {3, 100, 95, 50,  40, 100, 20, 105, 100, 60, 2, 5},
	/* 6*/ {3, 100, 95, 50,  50, 100, 25, 105, 100, 60, 5, 5},
	/* 7*/ {4, 100, 95, 50,  50, 100, 25, 105, 100, 60, 2, 5},
	/* 8*/ {4, 100, 95, 50,  50, 100, 25, 105, 100, 60, 2, 5},
	/* 9*/ {5, 100, 95, 50,  60, 100, 30, 105, 100, 60, 1, 3},
	/*10*/ {5, 100, 95, 50,  60, 100, 30, 105, 100, 60, 5, 5},
	/*11*/ {6, 100, 95, 50,  60, 100, 30, 105, 100, 60, 2, 5},
	/*12*/ {6, 100, 95, 50,  80, 100, 40, 105, 100, 60, 1, 3},
	/*13*/ {7, 100, 95, 50,  80, 100, 40, 105, 100, 60, 1, 3},
	/*14*/ {7, 100, 95, 50,  80, 100, 40, 105, 100, 60, 3, 5},
	/*15*/ {7, 100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3},
	/*16*/ {7, 100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3},
	/*17*/ {7, 100, 95, 50, 100, 100, 50, 105,   0,  0, 0, 0},
	/*18*/ {7, 100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3},
	/*19*/ {7, 100, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0},
	/*20*/ {7, 100, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0},
	/*21*/ {7,  90, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0},
	};
	/*@formatter:on*/

	public PacManClassicGameModel() {
		variant = CLASSIC;
		world = new MapBasedPacManGameWorld();
		world.loadMap("/worlds/classic/map.txt");
		// TODO store this info inside map
		world.setUpwardsBlocked(new V2i(12, 13), new V2i(15, 13), new V2i(12, 25), new V2i(15, 25));

		bonusNames = new String[] { "CHERRIES", "STRAWBERRY", "PEACH", "APPLE", "GRAPES", "GALAXIAN", "BELL", "KEY" };
		bonusValues = new int[] { 100, 300, 500, 700, 1000, 2000, 3000, 5000 };
		bonus = new Bonus(world);
		bonus.position = new V2f(13 * PacManGameWorld.TS, 20 * PacManGameWorld.TS);

		pac = new Pac(world);
		pac.name = "Pac-Man";
		pac.startDir = Direction.RIGHT;

		ghosts = new Ghost[4];
		for (int ghostID = 0; ghostID < 4; ++ghostID) {
			ghosts[ghostID] = new Ghost(ghostID, world);
		}
		ghosts[0].name = "Blinky";
		ghosts[1].name = "Pinky";
		ghosts[2].name = "Inky";
		ghosts[3].name = "Clyde";
		ghosts[0].startDir = LEFT;
		ghosts[1].startDir = UP;
		ghosts[2].startDir = DOWN;
		ghosts[3].startDir = DOWN;

		reset();
	}

	@Override
	public void createLevel() {
		level = new PacManGameLevel(world, PACMAN_CLASSIC_LEVELS[currentLevelNumber <= 21 ? currentLevelNumber - 1 : 20]);
		log("Current level is %d, maze index is %d", currentLevelNumber, level.mazeNumber);
	}
}
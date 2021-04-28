package de.amr.games.pacman.model.mspacman;

import static de.amr.games.pacman.lib.Direction.DOWN;
import static de.amr.games.pacman.lib.Direction.LEFT;
import static de.amr.games.pacman.lib.Direction.UP;
import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.model.common.Ghost.BLINKY;
import static de.amr.games.pacman.model.common.Ghost.INKY;
import static de.amr.games.pacman.model.common.Ghost.PINKY;
import static de.amr.games.pacman.model.common.Ghost.SUE;

import java.util.Map;
import java.util.Random;

import de.amr.games.pacman.model.common.AbstractGameModel;
import de.amr.games.pacman.model.common.GameLevel;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.model.common.Pac;
import de.amr.games.pacman.model.world.MapBasedPacManGameWorld;
import de.amr.games.pacman.model.world.WorldMap;

/**
 * Game model of the Ms. Pac-Man game variant.
 * 
 * TODO: are the level specifications the same as in Pac-Man?
 * 
 * @author Armin Reichert
 */
public class MsPacManGame extends AbstractGameModel {

	//@formatter:off
	static Map<String, Integer> BONUS_MAP = Map.of(
			"CHERRIES", 	100,
			"STRAWBERRY", 200,
			"PEACH",			500,
			"PRETZEL",		700,
			"APPLE",			1000,
			"PEAR",				2000,
			"BANANA",			5000
	);
	//@formatter:on

	/*@formatter:off*/
  static final Object[][] LEVELS = {
	/* 1*/ {"CHERRIES",    80, 75, 40,  20,  80, 10,  85,  90, 50, 6, 5},
	/* 2*/ {"STRAWBERRY",  90, 85, 45,  30,  90, 15,  95,  95, 55, 5, 5},
	/* 3*/ {"PEACH",       90, 85, 45,  40,  90, 20,  95,  95, 55, 4, 5},
	/* 4*/ {"PRETZEL",     90, 85, 45,  40,  90, 20,  95,  95, 55, 3, 5},
	/* 5*/ {"APPLE",      100, 95, 50,  40, 100, 20, 105, 100, 60, 2, 5},
	/* 6*/ {"PEAR",       100, 95, 50,  50, 100, 25, 105, 100, 60, 5, 5},
	/* 7*/ {"BANANA",     100, 95, 50,  50, 100, 25, 105, 100, 60, 2, 5},
	/* 8*/ {"BANANA",     100, 95, 50,  50, 100, 25, 105, 100, 60, 2, 5},
	/* 9*/ {"BANANA",     100, 95, 50,  60, 100, 30, 105, 100, 60, 1, 3},
	/*10*/ {"BANANA",     100, 95, 50,  60, 100, 30, 105, 100, 60, 5, 5},
	/*11*/ {"BANANA",     100, 95, 50,  60, 100, 30, 105, 100, 60, 2, 5},
	/*12*/ {"BANANA",     100, 95, 50,  80, 100, 40, 105, 100, 60, 1, 3},
	/*13*/ {"BANANA",     100, 95, 50,  80, 100, 40, 105, 100, 60, 1, 3},
	/*14*/ {"BANANA",     100, 95, 50,  80, 100, 40, 105, 100, 60, 3, 5},
	/*15*/ {"BANANA",     100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3},
	/*16*/ {"BANANA",     100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3},
	/*17*/ {"BANANA",     100, 95, 50, 100, 100, 50, 105,   0,  0, 0, 0},
	/*18*/ {"BANANA",     100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3},
	/*19*/ {"BANANA",     100, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0},
	/*20*/ {"BANANA",     100, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0},
	/*21*/ {"BANANA",      90, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0},
	};
	/*@formatter:on*/

	private final MapBasedPacManGameWorld world = new MapBasedPacManGameWorld();

	public MsPacManGame() {
		player = new Pac("Ms. Pac-Man", LEFT);
		player.world = world;

		ghosts = new Ghost[4];
		ghosts[BLINKY] = new Ghost(BLINKY, "Blinky", LEFT);
		ghosts[PINKY] = new Ghost(PINKY, "Pinky", UP);
		ghosts[INKY] = new Ghost(INKY, "Inky", DOWN);
		ghosts[SUE] = new Ghost(SUE, "Sue", DOWN);
		for (Ghost ghost : ghosts) {
			ghost.world = world;
		}

		bonus = new MovingBonus();
		bonus.world = world;
	}

	@Override
	protected void createLevel(int levelNumber) {
		int mazeNumber = mazeNumber(levelNumber);
		int mapNumber = mapNumber(mazeNumber);
		world.setMap(WorldMap.load("/mspacman/maps/map" + mapNumber + ".txt"));
		currentLevel = new GameLevel(levelNumber, world);
		currentLevel.setValues(LEVELS[levelNumber <= 21 ? levelNumber - 1 : 20]);
		currentLevel.mazeNumber = mazeNumber;
		if (levelNumber > 7) {
			int random = new Random().nextInt(BONUS_MAP.size());
			currentLevel.bonusSymbol = bonusName(random);
		}
		log("Ms. Pac-Man level %d created, maze index is %d", levelNumber, mazeNumber);
	}

	@Override
	protected String hiscoreFileName() {
		return "hiscore-mspacman.xml";
	}

	@Override
	public String levelSymbol(int levelNumber) {
		Object[] row = LEVELS[levelNumber <= 21 ? levelNumber - 1 : 20];
		return (String) row[0];
	}

	@Override
	public int mazeNumber(int levelNumber) {
		if (levelNumber < 1) {
			throw new IllegalArgumentException("Illegal level number: " + levelNumber);
		} else if (levelNumber <= 2) {
			return 1; // pink maze, white dots
		} else if (levelNumber <= 5) {
			return 2; // light blue maze, yellow dots
		} else if (levelNumber <= 9) {
			return 3; // orange maze, red dots
		} else if (levelNumber <= 13) {
			return 4; // dark blue maze, white dots
		} else {
			// From level 14 on, maze number alternates between 5 and 6 every 4th level
			// Maze #5 = pink maze, cyan dots (same map as maze #3)
			// Maze #6 = orange maze, white dots (same map as maze #4)
			return (levelNumber - 14) % 8 < 4 ? 5 : 6;
		}
	}

	@Override
	public int mapNumber(int mazeNumber) {
		// Maze #5 has the same map as #3 but a different color, same for #6 vs. #4
		return mazeNumber == 5 ? 3 : mazeNumber == 6 ? 4 : mazeNumber;
	}

	@Override
	public Map<String, Integer> bonusMap() {
		return BONUS_MAP;
	}
}
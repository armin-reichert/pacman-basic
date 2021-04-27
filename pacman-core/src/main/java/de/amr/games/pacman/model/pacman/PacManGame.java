package de.amr.games.pacman.model.pacman;

import static de.amr.games.pacman.lib.Direction.DOWN;
import static de.amr.games.pacman.lib.Direction.LEFT;
import static de.amr.games.pacman.lib.Direction.RIGHT;
import static de.amr.games.pacman.lib.Direction.UP;
import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.model.common.Ghost.BLINKY;
import static de.amr.games.pacman.model.common.Ghost.CLYDE;
import static de.amr.games.pacman.model.common.Ghost.INKY;
import static de.amr.games.pacman.model.common.Ghost.PINKY;
import static de.amr.games.pacman.model.world.PacManGameWorld.HTS;
import static de.amr.games.pacman.model.world.PacManGameWorld.TS;

import java.util.stream.Stream;

import de.amr.games.pacman.model.common.AbstractGameModel;
import de.amr.games.pacman.model.common.GameLevel;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.model.common.Pac;
import de.amr.games.pacman.model.world.MapBasedPacManGameWorld;
import de.amr.games.pacman.model.world.WorldMap;

/**
 * Game model of the classic Pac-Man game.
 * 
 * @author Armin Reichert
 */
public class PacManGame extends AbstractGameModel {

	enum BonusSymbol {
		CHERRIES, STRAWBERRY, PEACH, APPLE, GRAPES, GALAXIAN, BELL, KEY;
	}

	static final int[] BONUS_VALUES = { 100, 300, 500, 700, 1000, 2000, 3000, 5000 };

	/*@formatter:off*/
	static final int[][] PACMAN_LEVELS = {
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

	static final int[][] HUNTING_PHASE_DURATION = {
		//@formatter:off
		{ 7, 20, 7, 20, 5,   20,  5, Integer.MAX_VALUE },
		{ 7, 20, 7, 20, 5, 1033, -1, Integer.MAX_VALUE },
		{ 5, 20, 5, 20, 5, 1037, -1, Integer.MAX_VALUE },
		//@formatter:on
	};

	private final MapBasedPacManGameWorld world = new MapBasedPacManGameWorld();

	public PacManGame() {
		highscoreFileName = "hiscore-pacman.xml";

		String mapPath = "/pacman/maps/map1.txt";
		try {
			WorldMap map = WorldMap.load(mapPath);
			world.setMap(map);
		} catch (Exception x) {
			log("Map '%s' contains errors", mapPath);
		}

		bonusNames = Stream.of(BonusSymbol.values()).map(Enum<BonusSymbol>::name).toArray(String[]::new);
		bonusValues = BONUS_VALUES;

		bonus = new Bonus();
		bonus.world = world;
		bonus.setPosition(world.bonusTile().x * TS + HTS, world.bonusTile().y * TS);

		player = new Pac("Pac-Man", RIGHT);
		player.world = world;

		ghosts = new Ghost[4];
		ghosts[BLINKY] = new Ghost(BLINKY, "Blinky", LEFT);
		ghosts[PINKY] = new Ghost(PINKY, "Pinky", UP);
		ghosts[INKY] = new Ghost(INKY, "Inky", DOWN);
		ghosts[CLYDE] = new Ghost(CLYDE, "Clyde", DOWN);
		for (Ghost ghost : ghosts) {
			ghost.world = world;
		}
	}

	@Override
	protected void createLevel(int n) {
		currentLevel = new GameLevel(PACMAN_LEVELS[n <= 21 ? n - 1 : 20]);
		currentLevel.number = n;
		currentLevel.setWorld(world);
		currentLevel.mazeNumber = mazeNumber(n);
		log("Pac-Man classic level %d created", n);
	}

	@Override
	public long getHuntingPhaseDuration(int phase) {
		int row = currentLevel.number == 1 ? 0 : currentLevel.number <= 4 ? 1 : 2;
		return huntingTicks(HUNTING_PHASE_DURATION[row][phase]);
	}

	private long huntingTicks(int duration) {
		if (duration == -1) {
			return 1; // -1 means a single tick
		}
		if (duration == Integer.MAX_VALUE) {
			return Long.MAX_VALUE;
		}
		return duration * 60;
	}

	@Override
	public int mapNumber(int anyMazeNumber) {
		return 1;
	}

	@Override
	public int mazeNumber(int anyLevelNumber) {
		return 1;
	}
}
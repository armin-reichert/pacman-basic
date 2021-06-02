package de.amr.games.pacman.model.mspacman;

import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.model.common.Ghost.BLINKY;
import static de.amr.games.pacman.model.common.Ghost.INKY;
import static de.amr.games.pacman.model.common.Ghost.PINKY;
import static de.amr.games.pacman.model.common.Ghost.SUE;

import java.util.Map;
import java.util.Random;

import de.amr.games.pacman.model.common.AbstractGameModel;
import de.amr.games.pacman.model.common.GameLevel;
import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.model.common.Pac;
import de.amr.games.pacman.model.mspacman.entities.MovingBonus;
import de.amr.games.pacman.model.world.MapBasedPacManGameWorld;
import de.amr.games.pacman.model.world.WorldMap;

/**
 * Model of the Ms. Pac-Man game.
 * 
 * TODO: are the level data except for the bonus symbols the same as in Pac-Man?
 * 
 * @author Armin Reichert
 */
public class MsPacManGame extends AbstractGameModel {

	//@formatter:off
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

	static Object[] levelData(int levelNumber) {
		return levelNumber - 1 < LEVELS.length ? LEVELS[levelNumber - 1] : LEVELS[LEVELS.length - 1];
	}

	static final Map<String, Integer> BONI = Map.of(//
			"CHERRIES", 100, //
			"STRAWBERRY", 200, //
			"PEACH", 500, //
			"PRETZEL", 700, //
			"APPLE", 1000, //
			"PEAR", 2000, //
			"BANANA", 5000);

	private final MapBasedPacManGameWorld world;

	public MsPacManGame() {
		variant = GameVariant.MS_PACMAN;
		initialLives = 3;
		pelletValue = 10;
		energizerValue = 50;

		world = new MapBasedPacManGameWorld();
		player = new Pac("Ms. Pac-Man", world);
		ghosts = new Ghost[] { //
				new Ghost(BLINKY, "Blinky", world), //
				new Ghost(PINKY, "Pinky", world), //
				new Ghost(INKY, "Inky", world), //
				new Ghost(SUE, "Sue", world) //
		};
		bonus = new MovingBonus(world);
	}

	@Override
	protected String highscoreFileName() {
		return "hiscore-mspacman.xml";
	}

	@Override
	public void enterLevel(int levelNumber) {
		var mazeNumber = mazeNumber(levelNumber);
		var mapNumber = mapNumber(mazeNumber);
		world.setMap(WorldMap.load("/mspacman/maps/map" + mapNumber + ".txt"));
		level = new GameLevel(levelNumber, world, levelData(levelNumber));
		level.mazeNumber = mazeNumber;
		// From level 8 on, bonus is chosen randomly
		if (levelNumber >= 8) {
			int random = new Random().nextInt(BONI.size());
			String randomBonus = BONI.keySet().toArray(String[]::new)[random];
			level.bonusSymbol = randomBonus;
		}
		levelCounter.add(level.bonusSymbol);
		ghostBounty = 200;
		for (Ghost ghost : ghosts) {
			ghost.dotCounter = 0;
			ghost.elroy = 0;
		}
		bonus.init();
		log("Ms. Pac-Man game level #%d created, maze number is %d", levelNumber, mazeNumber);
	}

	/**
	 * Returns the maze number used in the given game level.
	 * <p>
	 * Up to level 13, the used mazes are:
	 * <ul>
	 * <li>Maze #1: pink maze, white dots (level 1-2)
	 * <li>Maze #2: light blue maze, yellow dots (level 3-5)
	 * <li>Maze #3: orange maze, red dots (level 6-9)
	 * <li>Maze #4: dark blue maze, white dots (level 10-13)
	 * </ul>
	 * From level 14 on, the maze alternates every 4th level between maze #5 and maze #6.
	 * <ul>
	 * <li>Maze #5: pink maze, cyan dots (same map as maze #3)
	 * <li>Maze #6: orange maze, white dots (same map as maze #4)
	 * </ul>
	 */
	@Override
	public int mazeNumber(int levelNumber) {
		if (levelNumber < 1) {
			throw new IllegalArgumentException("Illegal level number: " + levelNumber);
		}
		//@formatter:off
		return (levelNumber <=  2) ? 1
				 : (levelNumber <=  5) ? 2
				 : (levelNumber <=  9) ? 3 
				 : (levelNumber <= 13) ? 4
				 : (levelNumber - 14) % 8 < 4 ? 5 : 6;
		//@formatter:on
	}

	/**
	 * Maze #5 has the same map as #3, same for #6 vs. #4.
	 */
	@Override
	public int mapNumber(int mazeNumber) {
		return mazeNumber == 5 ? 3 : mazeNumber == 6 ? 4 : mazeNumber;
	}

	@Override
	public int bonusValue(String symbolName) {
		return BONI.get(symbolName);
	}
}
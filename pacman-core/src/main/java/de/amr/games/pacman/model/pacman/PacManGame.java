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
import static de.amr.games.pacman.model.world.PacManGameWorld.t;

import java.util.Map;

import de.amr.games.pacman.model.common.AbstractGameModel;
import de.amr.games.pacman.model.common.GameLevel;
import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.model.common.Pac;
import de.amr.games.pacman.model.world.MapBasedPacManGameWorld;

/**
 * Model of the Pac-Man game.
 * 
 * @author Armin Reichert
 */
public class PacManGame extends AbstractGameModel {

	//@formatter:off
	static Map<String, Integer> BONUS_MAP = Map.of(
			"CHERRIES", 	100,
			"STRAWBERRY", 300,
			"PEACH",			500,
			"APPLE",			700,
			"GRAPES",			1000,
			"GALAXIAN",		2000,
			"BELL",				3000,
			"KEY",				5000
	);

	static final Object[][] LEVELS = {
	/* 1*/ {"CHERRIES",    80, 75, 40,  20,  80, 10,  85,  90, 50, 6, 5},
	/* 2*/ {"STRAWBERRY",  90, 85, 45,  30,  90, 15,  95,  95, 55, 5, 5},
	/* 3*/ {"PEACH",       90, 85, 45,  40,  90, 20,  95,  95, 55, 4, 5},
	/* 4*/ {"PEACH",       90, 85, 45,  40,  90, 20,  95,  95, 55, 3, 5},
	/* 5*/ {"APPLE",      100, 95, 50,  40, 100, 20, 105, 100, 60, 2, 5},
	/* 6*/ {"APPLE",      100, 95, 50,  50, 100, 25, 105, 100, 60, 5, 5},
	/* 7*/ {"GRAPES",     100, 95, 50,  50, 100, 25, 105, 100, 60, 2, 5},
	/* 8*/ {"GRAPES",     100, 95, 50,  50, 100, 25, 105, 100, 60, 2, 5},
	/* 9*/ {"GALAXIAN",   100, 95, 50,  60, 100, 30, 105, 100, 60, 1, 3},
	/*10*/ {"GALAXIAN",   100, 95, 50,  60, 100, 30, 105, 100, 60, 5, 5},
	/*11*/ {"BELL",       100, 95, 50,  60, 100, 30, 105, 100, 60, 2, 5},
	/*12*/ {"BELL",       100, 95, 50,  80, 100, 40, 105, 100, 60, 1, 3},
	/*13*/ {"KEY",        100, 95, 50,  80, 100, 40, 105, 100, 60, 1, 3},
	/*14*/ {"KEY",        100, 95, 50,  80, 100, 40, 105, 100, 60, 3, 5},
	/*15*/ {"KEY",        100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3},
	/*16*/ {"KEY",        100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3},
	/*17*/ {"KEY",        100, 95, 50, 100, 100, 50, 105,   0,  0, 0, 0},
	/*18*/ {"KEY",        100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3},
	/*19*/ {"KEY",        100, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0},
	/*20*/ {"KEY",        100, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0},
	/*21*/ {"KEY",         90, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0},
	};
	/*@formatter:on*/

	// in Pac-Man, all levels use the same world and map
	private final MapBasedPacManGameWorld world;

	public PacManGame() {
		world = new MapBasedPacManGameWorld("/pacman/maps/map1.txt");

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

		bonus = new Bonus();
		bonus.world = world;
		bonus.setPosition(t(world.bonusTile().x) + HTS, t(world.bonusTile().y));

		hiscoreFileName = "hiscore-pacman.xml";
	}

	@Override
	public GameVariant variant() {
		return GameVariant.PACMAN;
	}

	@Override
	protected void createLevel(int levelNumber) {
		currentLevel = new GameLevel(levelNumber, world);
		currentLevel.setValues(levelData(LEVELS, levelNumber));
		currentLevel.mazeNumber = 1;
		log("Pac-Man level #%d created", levelNumber);
	}

	@Override
	public String levelSymbol(int levelNumber) {
		return (String) levelData(LEVELS, levelNumber)[0];
	}

	@Override
	public int mapNumber(int levelNumber) {
		return 1;
	}

	@Override
	public int mazeNumber(int levelNumber) {
		return 1;
	}

	@Override
	public int bonusValue(String bonus) {
		return BONUS_MAP.get(bonus);
	}
}
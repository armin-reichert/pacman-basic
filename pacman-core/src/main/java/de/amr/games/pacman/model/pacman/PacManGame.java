package de.amr.games.pacman.model.pacman;

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
import de.amr.games.pacman.model.world.WorldMap;

/**
 * Model of the Pac-Man game.
 * 
 * @author Armin Reichert
 */
public class PacManGame extends AbstractGameModel {

	//@formatter:off
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

	static Object[] levelData(int levelNumber) {
		return levelNumber - 1 < LEVELS.length ? LEVELS[levelNumber - 1] : LEVELS[LEVELS.length - 1];
	}

	static final Map<String, Integer> BONI = Map.of(//
			"CHERRIES", 100, //
			"STRAWBERRY", 300, //
			"PEACH", 500, //
			"APPLE", 700, //
			"GRAPES", 1000, //
			"GALAXIAN", 2000, //
			"BELL", 3000, //
			"KEY", 5000);

	private final MapBasedPacManGameWorld world;

	public PacManGame() {
		variant = GameVariant.PACMAN;
		initialLives = 3;
		pelletValue = 10;
		energizerValue = 50;
		world = new MapBasedPacManGameWorld();
		world.setMap(WorldMap.load("/pacman/maps/map1.txt"));

		player = new Pac("Pac-Man", world);
		ghosts = new Ghost[] { //
				new Ghost(BLINKY, "Blinky", world), //
				new Ghost(PINKY, "Pinky", world), //
				new Ghost(INKY, "Inky", world), //
				new Ghost(CLYDE, "Clyde", world) //
		};
		bonus = new Bonus(world);
		bonus.setPosition(t(world.bonusTile().x) + HTS, t(world.bonusTile().y));
	}

	@Override
	protected String highscoreFileName() {
		return "hiscore-pacman.xml";
	}

	@Override
	public void enterLevel(int levelNumber) {
		level = new GameLevel(levelNumber, world, levelData(levelNumber));
		level.mazeNumber = mazeNumber(levelNumber);
		levelCounter.add(level.bonusSymbol);
		ghostBounty = 200;
		for (Ghost ghost : ghosts) {
			ghost.dotCounter = 0;
			ghost.elroy = 0;
		}
		bonus.init();
		log("Pac-Man game level #%d created", levelNumber);
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
	public int bonusValue(String symbolName) {
		return BONI.get(symbolName);
	}
}
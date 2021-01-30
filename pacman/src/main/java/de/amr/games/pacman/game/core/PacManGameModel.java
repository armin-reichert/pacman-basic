package de.amr.games.pacman.game.core;

import static de.amr.games.pacman.game.core.PacManGameWorld.TS;
import static de.amr.games.pacman.game.heaven.God.random;
import static de.amr.games.pacman.lib.Direction.DOWN;
import static de.amr.games.pacman.lib.Direction.LEFT;
import static de.amr.games.pacman.lib.Direction.UP;
import static de.amr.games.pacman.lib.Logging.log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.amr.games.pacman.game.creatures.Bonus;
import de.amr.games.pacman.game.creatures.Ghost;
import de.amr.games.pacman.game.creatures.MovingBonus;
import de.amr.games.pacman.game.creatures.Pac;
import de.amr.games.pacman.game.worlds.MsPacManWorld;
import de.amr.games.pacman.game.worlds.PacManClassicWorld;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Hiscore;
import de.amr.games.pacman.lib.V2f;
import de.amr.games.pacman.lib.V2i;

/**
 * The game data.
 * 
 * @author Armin Reichert
 */
public class PacManGameModel {

	public static final byte CLASSIC = 0, MS_PACMAN = 1;

	public static final byte BLINKY = 0, PINKY = 1, INKY = 2, CLYDE = 3;

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

	public PacManGameState state;
	public byte variant;
	public PacManGameWorld world;
	public short levelNumber;
	public PacManGameLevel level;
	public Pac pac;
	public Ghost[] ghosts;
	public Bonus bonus;
	public String[] bonusNames;
	public int[] bonusValues;
	public byte lives;
	public int score;
	public int highscoreLevel, highscorePoints;
	public byte huntingPhase;
	public short ghostBounty;
	public List<Byte> levelSymbols;
	public short globalDotCounter;
	public boolean globalDotCounterEnabled;

	public static PacManGameModel newPacManClassicGame() {
		PacManGameModel game = new PacManGameModel();
		game.variant = CLASSIC;
		game.world = new PacManClassicWorld();
		game.bonusNames = new String[] { "CHERRIES", "STRAWBERRY", "PEACH", "APPLE", "GRAPES", "GALAXIAN", "BELL", "KEY" };
		game.bonusValues = new int[] { 100, 300, 500, 700, 1000, 2000, 3000, 5000 };
		game.bonus = new Bonus(game.world);
		game.bonus.position = new V2f(13 * TS, 20 * TS);
		game.pac = new Pac(game.world);
		game.pac.name = "Pac-Man";
		game.pac.startDir = Direction.RIGHT;
		game.ghosts = new Ghost[4];
		for (int ghostID = 0; ghostID < 4; ++ghostID) {
			game.ghosts[ghostID] = new Ghost(ghostID, game.world);
		}
		game.ghosts[0].name = "Blinky";
		game.ghosts[1].name = "Pinky";
		game.ghosts[2].name = "Inky";
		game.ghosts[3].name = "Clyde";
		game.ghosts[0].startDir = LEFT;
		game.ghosts[1].startDir = UP;
		game.ghosts[2].startDir = DOWN;
		game.ghosts[3].startDir = DOWN;
		game.reset();
		return game;
	}

	public static PacManGameModel newMsPacManGame() {
		PacManGameModel game = new PacManGameModel();
		game.variant = MS_PACMAN;
		game.world = new MsPacManWorld();
		game.bonusNames = new String[] { "CHERRIES", "STRAWBERRY", "PEACH", "PRETZEL", "APPLE", "PEAR", "BANANA" };
		game.bonusValues = new int[] { 100, 200, 500, 700, 1000, 2000, 5000 };
		game.bonus = new MovingBonus(game.world);
		game.pac = new Pac(game.world);
		game.pac.name = "Ms. Pac-Man";
		game.pac.startDir = Direction.LEFT;
		game.ghosts = new Ghost[4];
		for (int ghostID = 0; ghostID < 4; ++ghostID) {
			game.ghosts[ghostID] = new Ghost(ghostID, game.world);
		}
		game.ghosts[0].name = "Blinky";
		game.ghosts[1].name = "Pinky";
		game.ghosts[2].name = "Inky";
		game.ghosts[3].name = "Sue";
		game.ghosts[0].startDir = LEFT;
		game.ghosts[1].startDir = UP;
		game.ghosts[2].startDir = DOWN;
		game.ghosts[3].startDir = DOWN;
		game.reset();
		return game;
	}

	public void reset() {
		score = 0;
		Hiscore hiscore = loadHighScore();
		highscoreLevel = hiscore.level;
		highscorePoints = hiscore.points;
		lives = 3;
		initLevel(1);
		levelSymbols = new ArrayList<>();
		levelSymbols.add(level.bonusSymbol);
	}

	public PacManGameLevel createLevel(int levelNumber) {
		if (variant == CLASSIC) {
			return createPacManClassicLevel(levelNumber);
		} else if (variant == MS_PACMAN) {
			return createMsPacManLevel(levelNumber);
		}
		throw new IllegalArgumentException("Illegal level number " + levelNumber);
	}

	private PacManGameLevel createPacManClassicLevel(int levelNumber) {
		return new PacManGameLevel(world, PACMAN_CLASSIC_LEVELS[levelNumber <= 21 ? levelNumber - 1 : 20]);
	}

	private PacManGameLevel createMsPacManLevel(int levelNumber) {
		int mazeNumber = MsPacManWorld.mazeNumber(levelNumber);
		// Maze #5 has the same map as #3 but a different color, same for #6 vs. #4
		int mapIndex = mazeNumber == 5 ? 3 : mazeNumber == 6 ? 4 : mazeNumber;
		world.loadMap("/worlds/mspacman/map" + mapIndex + ".txt");
		log("Use maze #%d at game level %d", mazeNumber, levelNumber);
		// map has been loaded, now create level
		PacManGameLevel level = new PacManGameLevel(world,
				PacManGameModel.MS_PACMAN_LEVELS[levelNumber <= 21 ? levelNumber - 1 : 20]);
		level.mazeNumber = mazeNumber;
		if (levelNumber > 7) {
			level.bonusSymbol = (byte) random.nextInt(7);
		}
		return level;
	}

	public void initLevel(int n) {
		levelNumber = (short) n;
		level = createLevel(n);
		ghostBounty = 200;
		huntingPhase = 0;
		bonus.edibleTicksLeft = 0;
		bonus.eatenTicksLeft = 0;
		for (Ghost ghost : ghosts) {
			ghost.dotCounter = 0;
			ghost.elroy = 0;
		}
	}

	public String stateDescription() {
		if (state == PacManGameState.HUNTING) {
			String phaseName = inScatteringPhase() ? "Scattering" : "Chasing";
			int phaseIndex = huntingPhase / 2;
			return String.format("%s-%s (%d of 4)", state, phaseName, phaseIndex + 1);
		}
		return state.name();
	}

	public boolean inScatteringPhase() {
		return huntingPhase % 2 == 0;
	}

	public Hiscore loadHighScore() {
		File dir = new File(System.getProperty("user.home"));
		String fileName = variant == CLASSIC ? "hiscore-pacman.xml" : "hiscore-mspacman.xml";
		Hiscore hiscore = new Hiscore(new File(dir, fileName));
		hiscore.load();
		return hiscore;
	}

	public void removeAllNormalPellets() {
		for (int x = 0; x < world.xTiles(); ++x) {
			for (int y = 0; y < world.yTiles(); ++y) {
				V2i tile = new V2i(x, y);
				if (level.containsFood(tile) && !world.isEnergizerTile(tile)) {
					level.removeFood(tile);
				}
			}
		}
	}
}
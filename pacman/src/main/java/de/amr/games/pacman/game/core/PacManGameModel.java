package de.amr.games.pacman.game.core;

import static de.amr.games.pacman.lib.Direction.DOWN;
import static de.amr.games.pacman.lib.Direction.LEFT;
import static de.amr.games.pacman.lib.Direction.UP;

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

/**
 * The game data.
 * 
 * @author Armin Reichert
 */
public class PacManGameModel {

	public static final byte CLASSIC = 0, MS_PACMAN = 1;

	public PacManGameState state;
	public byte variant;
	public PacManGameWorld world;
	public short levelNumber;
	public PacManGameLevel level;
	public Pac pac;
	public Ghost[] ghosts;
	public Bonus bonus;
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
		game.bonus = new Bonus(game.world);
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

	public void initLevel(int n) {
		levelNumber = (short) n;
		level = world.createLevel(n);
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
				if (level.containsFood(x, y) && !world.isEnergizerTile(x, y)) {
					level.removeFood(x, y);
				}
			}
		}
	}
}
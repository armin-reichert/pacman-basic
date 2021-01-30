package de.amr.games.pacman.game.core;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.amr.games.pacman.game.creatures.Bonus;
import de.amr.games.pacman.game.creatures.Ghost;
import de.amr.games.pacman.game.creatures.Pac;
import de.amr.games.pacman.lib.Hiscore;
import de.amr.games.pacman.lib.V2i;

/**
 * The common base class of the game data.
 * 
 * @author Armin Reichert
 */
public abstract class PacManGameModel {

	public static final byte CLASSIC = 0, MS_PACMAN = 1;

	public static final byte BLINKY = 0, PINKY = 1, INKY = 2, CLYDE = 3, SUE = 3;

	public PacManGameState state;
	public byte variant;
	public PacManGameWorld world;
	public int currentLevelNumber; // counting from 1
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

	public abstract void createLevel();

	public void setLevel(int levelNumber) {
		currentLevelNumber = levelNumber;
		createLevel();
		ghostBounty = 200;
		huntingPhase = 0;
		bonus.edibleTicksLeft = 0;
		bonus.eatenTicksLeft = 0;
		for (Ghost ghost : ghosts) {
			ghost.dotCounter = 0;
			ghost.elroy = 0;
		}
	}

	public void reset() {
		setLevel(1);
		Hiscore hiscore = loadHighScore();
		highscoreLevel = hiscore.level;
		highscorePoints = hiscore.points;
		score = 0;
		lives = 3;
		levelSymbols = new ArrayList<>();
		levelSymbols.add(level.bonusSymbol);
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
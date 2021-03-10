package de.amr.games.pacman.model.common;

import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.model.common.Ghost.BLINKY;
import static de.amr.games.pacman.model.common.GhostState.LOCKED;
import static de.amr.games.pacman.model.world.PacManGameWorld.HTS;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import de.amr.games.pacman.lib.Hiscore;
import de.amr.games.pacman.model.pacman.PacManBonus;

/**
 * Common base class for the models.
 * 
 * @author Armin Reichert
 */
public abstract class GameModel {

	public PacManGameState state;
	public boolean started;
	public boolean attractMode;

	public int levelNumber; // counting from 1
	public GameLevel level;
	public int intermissionNumber;
	public Pac pac;
	public Ghost[] ghosts;
	public PacManBonus bonus;
	public String[] bonusNames;
	public int[] bonusValues;
	public int lives;
	public int score;
	public String highscoreFileName;
	public int highscoreLevel, highscorePoints;
	public int huntingPhase;
	public short ghostBounty;
	public List<Byte> levelSymbols;
	public short globalDotCounter;
	public boolean globalDotCounterEnabled;

	public void reset() {
		Hiscore hiscore = loadHighScore();
		highscoreLevel = hiscore.level;
		highscorePoints = hiscore.points;
		score = 0;
		lives = 3;
		started = false;
		attractMode = false;
		levelSymbols = new ArrayList<>();
		enterLevel(1);
		levelSymbols.add(level.bonusSymbol);
	}

	protected abstract void buildLevel(int number);

	/**
	 * @param levelNumber 1-based game level number
	 * @return 1-based maze number of the maze used in that level
	 */
	public abstract int mazeNumber(int number);

	/**
	 * @param mazeNumber 1-based number of a maze
	 * @return 1-based number of the world map used by that maze
	 */
	public abstract int mapNumber(int mazeNumber);

	public void enterLevel(int number) {
		levelNumber = number;
		buildLevel(number);
		ghostBounty = 200;
		huntingPhase = 0;
		bonus.edibleTicksLeft = 0;
		bonus.eatenTicksLeft = 0;
		for (Ghost ghost : ghosts) {
			ghost.dotCounter = 0;
			ghost.elroy = 0;
		}
	}

	public void resetGuys() {
		pac.placeAt(level.world.pacHome(), HTS, 0);
		pac.dir = pac.wishDir = pac.startDir;
		pac.visible = false;
		pac.speed = 0;
		pac.targetTile = null; // used in autopilot mode
		pac.couldMove = true;
		pac.forcedOnTrack = true;
		pac.dead = false;
		pac.restingTicksLeft = 0;
		pac.starvingTicks = 0;
		pac.powerTimer.reset();

		for (Ghost ghost : ghosts) {
			ghost.placeAt(level.world.ghostHome(ghost.id), HTS, 0);
			ghost.dir = ghost.wishDir = ghost.startDir;
			ghost.visible = false;
			ghost.speed = 0;
			ghost.targetTile = null;
			ghost.couldMove = true;
			ghost.forcedDirection = ghost.id == BLINKY;
			ghost.forcedOnTrack = ghost.id == BLINKY;
			ghost.state = LOCKED;
			ghost.bounty = 0;
			// these are only reset when entering level:
//		ghost.dotCounter = 0;
//		ghost.elroyMode = 0;
		}

		bonus.visible = false;
		bonus.speed = 0;
		bonus.changedTile = true;
		bonus.couldMove = true;
		bonus.forcedOnTrack = true;
		bonus.edibleTicksLeft = 0;
		bonus.eatenTicksLeft = 0;
	}

	public Stream<Ghost> ghosts() {
		return Stream.of(ghosts);
	}

	public Stream<Ghost> ghosts(GhostState ghostState) {
		return Stream.of(ghosts).filter(ghost -> ghost.state.equals(ghostState));
	}

	public String stateDescription() {
		if (state == null) {
			return "not initialized";
		}
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

	public abstract long getHuntingPhaseDuration(int phase);

	public Hiscore loadHighScore() {
		File dir = new File(System.getProperty("user.home"));
		Hiscore hiscore = new Hiscore(new File(dir, highscoreFileName));
		hiscore.load();
		return hiscore;
	}

	public void saveHighscore() {
		Hiscore hiscore = loadHighScore();
		if (highscorePoints > hiscore.points) {
			hiscore.points = highscorePoints;
			hiscore.level = highscoreLevel;
			hiscore.save();
			log("New hiscore saved. %d points in level %d", hiscore.points, hiscore.level);
		}
	}
}
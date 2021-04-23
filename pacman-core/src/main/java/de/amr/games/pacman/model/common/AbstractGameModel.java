package de.amr.games.pacman.model.common;

import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.model.common.Ghost.BLINKY;
import static de.amr.games.pacman.model.world.PacManGameWorld.HTS;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import de.amr.games.pacman.lib.Hiscore;
import de.amr.games.pacman.model.pacman.PacManBonus;

/**
 * Common base class for the game models.
 * 
 * @author Armin Reichert
 */
public abstract class AbstractGameModel {

	public int currentLevelNumber; // counting from 1
	public GameLevel currentLevel;
	public int intermissionNumber; // 1,2,3
	public Pac player;
	public Ghost[] ghosts;
	public PacManBonus bonus;
	public String[] bonusNames;
	public int[] bonusValues;
	public int lives;
	public int score;
	public String highscoreFileName;
	public int highscoreLevel, highscorePoints;
	public int ghostBounty;
	public List<Byte> levelSymbols;
	public int globalDotCounter;
	public boolean globalDotCounterEnabled;

	public void resetGuys() {
		player.placeAt(currentLevel.world.pacHome(), HTS, 0);
		player.setDir(player.startDir);
		player.setWishDir(player.startDir);
		player.visible = true;
		player.speed = 0;
		player.targetTile = null; // used in autopilot mode
		player.stuck = false;
		player.forcedOnTrack = true;
		player.dead = false;
		player.restingTicksLeft = 0;
		player.starvingTicks = 0;
		player.powerTimer.reset();

		for (Ghost ghost : ghosts) {
			ghost.placeAt(currentLevel.world.ghostHome(ghost.id), HTS, 0);
			ghost.setDir(ghost.startDir);
			ghost.setWishDir(ghost.startDir);
			ghost.visible = true;
			ghost.speed = 0;
			ghost.targetTile = null;
			ghost.stuck = false;
			ghost.forced = ghost.id == BLINKY;
			ghost.forcedOnTrack = ghost.id == BLINKY;
			ghost.state = GhostState.LOCKED;
			ghost.bounty = 0;
			// these are reset when entering level:
			// ghost.dotCounter = 0;
			// ghost.elroyMode = 0;
		}

		bonus.visible = false;
		bonus.speed = 0;
		bonus.newTileEntered = true;
		bonus.stuck = false;
		bonus.forcedOnTrack = true;
		bonus.edibleTicksLeft = 0;
		bonus.eatenTicksLeft = 0;
	}

	public void reset() {
		score = 0;
		lives = 3;
		initLevel(1);
		levelSymbols = new ArrayList<>();
		levelSymbols.add(currentLevel.bonusSymbol);
		Hiscore hiscore = loadHighScore();
		highscoreLevel = hiscore.level;
		highscorePoints = hiscore.points;
	}

	/**
	 * @param levelNumber 1-based game level number
	 */
	public void initLevel(int levelNumber) {
		createLevel(levelNumber);
		ghostBounty = 200;
		for (Ghost ghost : ghosts) {
			ghost.dotCounter = 0;
			ghost.elroy = 0;
		}
		bonus.edibleTicksLeft = 0;
		bonus.eatenTicksLeft = 0;
		currentLevelNumber = levelNumber;
	}

	/**
	 * @param levelNumber 1-based game level number
	 */
	protected abstract void createLevel(int levelNumber);

	/**
	 * @param levelNumber 1-based game level number
	 * @return 1-based maze number of the maze used in that level
	 */
	public abstract int mazeNumber(int levelNumber);

	/**
	 * @param mazeNumber 1-based number of a maze
	 * @return 1-based number of the world map used by that maze
	 */
	public abstract int mapNumber(int mazeNumber);

	public Stream<Ghost> ghosts() {
		return Stream.of(ghosts);
	}

	public Stream<Ghost> ghosts(GhostState ghostState) {
		return ghosts().filter(ghost -> ghost.state == ghostState);
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
			log("New hiscore saved: %d points in level %d.", hiscore.points, hiscore.level);
		}
	}
}
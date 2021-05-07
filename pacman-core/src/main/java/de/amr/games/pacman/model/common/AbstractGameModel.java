package de.amr.games.pacman.model.common;

import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.model.common.Ghost.BLINKY;
import static de.amr.games.pacman.model.world.PacManGameWorld.HTS;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import de.amr.games.pacman.lib.Hiscore;
import de.amr.games.pacman.model.pacman.Bonus;

/**
 * Common base class for the game models.
 * 
 * @author Armin Reichert
 */
public abstract class AbstractGameModel implements GameModel {

	static final int INITIAL_NUM_LIVES = 3;

	static final Map<Integer, Integer> INTERMISSION_AFTER_LEVEL = Map.of(//
			2, 1, // intermission #1 after level #2
			5, 2, // intermission #2 after level #5
			9, 3, 13, 3, 17, 3 // intermission #3 after levels #9, #13, #17
	);

	static final int[][] HUNTING_PHASE_TICKS = {
		//@formatter:off
		{ 7 * 60, 20 * 60, 7 * 60, 20 * 60, 5 * 60,   20 * 60,  5 * 60, Integer.MAX_VALUE },
		{ 7 * 60, 20 * 60, 7 * 60, 20 * 60, 5 * 60, 1033 * 60,       1, Integer.MAX_VALUE },
		{ 5 * 60, 20 * 60, 5 * 60, 20 * 60, 5 * 60, 1037 * 60,       1, Integer.MAX_VALUE },
		//@formatter:on
	};

	protected static Object[] levelData(Object[][] levels, int levelNumber) {
		return levelNumber - 1 < levels.length ? levels[levelNumber - 1] : levels[levels.length - 1];
	}

	protected GameLevel currentLevel;
	protected Pac player;
	protected Ghost[] ghosts;
	protected Bonus bonus;
	protected int lives;
	protected int score;
	protected int hiscoreLevel;
	protected int hiscorePoints;
	protected int ghostBounty;
	protected List<String> levelSymbols;
	protected int globalDotCounter;
	protected boolean globalDotCounterEnabled;
	protected String hiscoreFileName;

	@Override
	public GameLevel currentLevel() {
		return currentLevel;
	}

	@Override
	public void addLevelSymbol(String symbol) {
		levelSymbols.add(symbol);
	}

	@Override
	public int intermissionNumber() {
		return INTERMISSION_AFTER_LEVEL.getOrDefault(currentLevel.number, 0);
	}

	@Override
	public int lives() {
		return lives;
	}

	@Override
	public void addLife() {
		++lives;
	}

	@Override
	public void removeLife() {
		if (lives > 0) {
			lives--;
		}
	}

	@Override
	public int score() {
		return score;
	}

	@Override
	public void addScore(int points) {
		score += points;
	}

	@Override
	public int hiscorePoints() {
		return hiscorePoints;
	}

	@Override
	public void setHiscorePoints(int points) {
		hiscorePoints = points;
	}

	@Override
	public int hiscoreLevel() {
		return hiscoreLevel;
	}

	@Override
	public void setHiscoreLevel(int number) {
		hiscoreLevel = number;
	}

	@Override
	public Pac player() {
		return player;
	}

	@Override
	public Stream<Ghost> ghosts() {
		return Stream.of(ghosts);
	}

	@Override
	public Stream<Ghost> ghosts(GhostState state) {
		return ghosts().filter(ghost -> ghost.state == state);
	}

	@Override
	public Ghost ghost(int id) {
		return ghosts[id];
	}

	@Override
	public int ghostBounty() {
		return ghostBounty;
	}

	@Override
	public void setGhostBounty(int value) {
		this.ghostBounty = value;
	}

	@Override
	public Bonus bonus() {
		return bonus;
	}

	@Override
	public void resetGuys() {
		player.placeAt(currentLevel.world.playerHomeTile(), HTS, 0);
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
			ghost.placeAt(currentLevel.world.ghostHomeTile(ghost.id), HTS, 0);
			ghost.setDir(ghost.startDir);
			ghost.setWishDir(ghost.startDir);
			ghost.visible = true;
			ghost.speed = 0;
			ghost.targetTile = null;
			ghost.stuck = false;
			// BLINKY starts outside of ghost house, so he must be on track initially
			ghost.forced = (ghost.id == BLINKY);
			ghost.forcedOnTrack = (ghost.id == BLINKY);
			ghost.state = GhostState.LOCKED;
			ghost.bounty = 0;
			// these are only reset when level starts:
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

	@Override
	public void reset() {
		score = 0;
		lives = INITIAL_NUM_LIVES;
		initLevel(1);
		levelSymbols = new ArrayList<>();
		levelSymbols.add(currentLevel.bonusSymbol);
		Hiscore hiscore = loadHiscore();
		hiscoreLevel = hiscore.level;
		hiscorePoints = hiscore.points;
	}

	@Override
	public void initLevel(int levelNumber) {
		createLevel(levelNumber);
		ghostBounty = 200;
		for (Ghost ghost : ghosts) {
			ghost.dotCounter = 0;
			ghost.elroy = 0;
		}
		bonus.edibleTicksLeft = 0;
		bonus.eatenTicksLeft = 0;
	}

	@Override
	public long getHuntingPhaseDuration(int phase) {
		int row = currentLevel.number == 1 ? 0 : currentLevel.number <= 4 ? 1 : 2;
		return HUNTING_PHASE_TICKS[row][phase];
	}

	@Override
	public void saveHiscore() {
		Hiscore hiscore = loadHiscore();
		if (hiscorePoints > hiscore.points) {
			hiscore.points = hiscorePoints;
			hiscore.level = hiscoreLevel;
			hiscore.save();
			log("New hiscore: %d points in level %d.", hiscore.points, hiscore.level);
		}
	}

	@Override
	public int globalDotCounter() {
		return globalDotCounter;
	}

	@Override
	public void setGlobalDotCounter(int globalDotCounter) {
		this.globalDotCounter = globalDotCounter;
	}

	@Override
	public boolean isGlobalDotCounterEnabled() {
		return globalDotCounterEnabled;
	}

	@Override
	public void enableGlobalDotCounter(boolean enable) {
		globalDotCounterEnabled = enable;
	}

	/**
	 * @param levelNumber 1-based game level number
	 */
	protected abstract void createLevel(int levelNumber);

	/**
	 * @param levelNumber 1-based game level number
	 * @return 1-based maze number of the maze used in that level
	 */
	protected abstract int mazeNumber(int levelNumber);

	/**
	 * @param mazeNumber 1-based number of a maze
	 * @return 1-based number of the world map used by that maze
	 */
	protected abstract int mapNumber(int mazeNumber);

	protected Hiscore loadHiscore() {
		File dir = new File(System.getProperty("user.home"));
		Hiscore hiscore = new Hiscore(new File(dir, hiscoreFileName));
		hiscore.load();
		return hiscore;
	}

}
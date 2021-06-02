package de.amr.games.pacman.model.common;

import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.model.common.Ghost.BLINKY;
import static de.amr.games.pacman.model.world.PacManGameWorld.HTS;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.stream.Stream;

import de.amr.games.pacman.lib.Hiscore;
import de.amr.games.pacman.model.pacman.Bonus;
import de.amr.games.pacman.model.world.PacManGameWorld;

/**
 * Common base class for the game models.
 * 
 * @author Armin Reichert
 */
public abstract class AbstractGameModel implements PacManGameModel {

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

	protected GameVariant variant;
	protected GameLevel level;
	protected Pac player;
	protected Ghost[] ghosts;
	protected Bonus bonus;
	protected int initialLives;
	protected int lives;
	protected int score;
	protected int hiscoreLevel;
	protected int hiscorePoints;
	protected int pelletValue;
	protected int energizerValue;
	protected int ghostBounty;
	protected List<String> levelCounter = new ArrayList<>();
	protected int globalDotCounter;
	protected boolean globalDotCounterEnabled;

	@Override
	public GameVariant variant() {
		return variant;
	}

	@Override
	public GameLevel level() {
		return level;
	}

	@Override
	public String levelSymbol(int levelNumber) {
		return levelCounter.get(levelNumber - 1);
	}

	@Override
	public OptionalInt intermissionAfterLevel(int levelNumber) {
		return INTERMISSION_AFTER_LEVEL.containsKey(levelNumber) ? OptionalInt.of(INTERMISSION_AFTER_LEVEL.get(levelNumber))
				: OptionalInt.empty();
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
	public int pelletValue() {
		return pelletValue;
	}

	@Override
	public int energizerValue() {
		return energizerValue;
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
	public int getNextGhostBounty() {
		return ghostBounty;
	}

	@Override
	public void resetGhostBounty() {
		ghostBounty = 200;
	}

	@Override
	public void increaseNextGhostBounty() {
		ghostBounty *= 2;
	}

	@Override
	public Bonus bonus() {
		return bonus;
	}

	@Override
	public boolean isBonusReached() {
		return level.eatenFoodCount() == level.world.pelletsToEatForBonus(0)
				|| level.eatenFoodCount() == level.world.pelletsToEatForBonus(1);
	}

	@Override
	public void resetGuys() {
		final PacManGameWorld world = level.world;
		player.placeAt(world.playerHomeTile(), HTS, 0);
		player.setDir(world.playerStartDirection());
		player.setWishDir(world.playerStartDirection());
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
			ghost.placeAt(world.ghostHomeTile(ghost.id), HTS, 0);
			ghost.setDir(world.ghostStartDirection(ghost.id));
			ghost.setWishDir(world.ghostStartDirection(ghost.id));
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

		bonus.init();
	}

	@Override
	public void reset() {
		score = 0;
		lives = initialLives;
		levelCounter.clear();
		enterLevel(1);
		Hiscore hiscore = loadHiscore();
		hiscoreLevel = hiscore.level;
		hiscorePoints = hiscore.points;
	}

	@Override
	public long getHuntingPhaseDuration(int phase) {
		int row = level.number == 1 ? 0 : level.number <= 4 ? 1 : 2;
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

	protected abstract String highscoreFileName();

	private Hiscore loadHiscore() {
		File dir = new File(System.getProperty("user.home"));
		Hiscore hiscore = new Hiscore(new File(dir, highscoreFileName()));
		hiscore.load();
		return hiscore;
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
}
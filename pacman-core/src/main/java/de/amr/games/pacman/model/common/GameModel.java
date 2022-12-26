/*
MIT License

Copyright (c) 2021-22 Armin Reichert

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package de.amr.games.pacman.model.common;

import static de.amr.games.pacman.lib.steering.Direction.UP;
import static de.amr.games.pacman.model.common.actors.Ghost.ID_CYAN_GHOST;
import static de.amr.games.pacman.model.common.actors.Ghost.ID_ORANGE_GHOST;
import static de.amr.games.pacman.model.common.actors.Ghost.ID_PINK_GHOST;
import static de.amr.games.pacman.model.common.actors.Ghost.ID_RED_GHOST;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.event.GameEvents;
import de.amr.games.pacman.lib.math.Vector2i;
import de.amr.games.pacman.lib.steering.Direction;
import de.amr.games.pacman.model.common.actors.AnimKeys;
import de.amr.games.pacman.model.common.actors.Bonus;
import de.amr.games.pacman.model.common.actors.Creature;
import de.amr.games.pacman.model.common.actors.Entity;
import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.model.common.actors.GhostState;
import de.amr.games.pacman.model.common.actors.Pac;
import de.amr.games.pacman.model.common.world.World;

/**
 * Common part of the Pac-Man and Ms. Pac-Man game models.
 * 
 * @author Armin Reichert
 */
public abstract class GameModel {

	protected static final Logger LOGGER = LogManager.getFormatterLogger();
	protected static final Random RND = new Random();

	/** Game loop speed in ticks/sec. */
	public static final short FPS = 60;
	/** Speed in pixels/tick at 100% relative speed. */
	public static final float SPEED_100_PERCENT_PX = 1.25f;
	public static final float SPEED_GHOST_INSIDE_HOUSE_PX = 0.5f; // unsure
	public static final float SPEED_GHOST_RETURNING_TO_HOUSE_PX = 2.0f; // unsure
	public static final float SPEED_GHOST_ENTERING_HOUSE_PX = 1.25f; // unsure
	public static final short MAX_CREDIT = 99;
	public static final short INITIAL_LIVES = 3;
	public static final short RESTING_TICKS_NORMAL_PELLET = 1;
	public static final short RESTING_TICKS_ENERGIZER = 3;
	public static final short POINTS_NORMAL_PELLET = 10;
	public static final short POINTS_ENERGIZER = 50;
	public static final short POINTS_ALL_GHOSTS_KILLED = 12_000;
	public static final short[] POINTS_GHOSTS_SEQUENCE = { 200, 400, 800, 1600 };
	public static final short SCORE_EXTRA_LIFE = 10_000;
	public static final short PELLETS_EATEN_BONUS1 = 70;
	public static final short PELLETS_EATEN_BONUS2 = 170;
	public static final short TICKS_BONUS_POINTS_SHOWN = 2 * FPS; // unsure

	//@formatter:off
	protected static final byte[][] LEVEL_PARAMETERS = {
	/* 1*/ { 80, 75, 40,  20,  80, 10,  85,  90, 50, 6, 5},
	/* 2*/ { 90, 85, 45,  30,  90, 15,  95,  95, 55, 5, 5},
	/* Intermission scene 1 */
	/* 3*/ { 90, 85, 45,  40,  90, 20,  95,  95, 55, 4, 5},
	/* 4*/ { 90, 85, 45,  40,  90, 20,  95,  95, 55, 3, 5},
	/* 5*/ {100, 95, 50,  40, 100, 20, 105, 100, 60, 2, 5},
	/* Intermission scene 2 */
	/* 6*/ {100, 95, 50,  50, 100, 25, 105, 100, 60, 5, 5},
	/* 7*/ {100, 95, 50,  50, 100, 25, 105, 100, 60, 2, 5},
	/* 8*/ {100, 95, 50,  50, 100, 25, 105, 100, 60, 2, 5},
	/* 9*/ {100, 95, 50,  60, 100, 30, 105, 100, 60, 1, 3},
	/* Intermission scene 3 */
	/*10*/ {100, 95, 50,  60, 100, 30, 105, 100, 60, 5, 5},
	/*11*/ {100, 95, 50,  60, 100, 30, 105, 100, 60, 2, 5},
	/*12*/ {100, 95, 50,  80, 100, 40, 105, 100, 60, 1, 3},
	/*13*/ {100, 95, 50,  80, 100, 40, 105, 100, 60, 1, 3},
	/* Intermission scene 3 */
	/*14*/ {100, 95, 50,  80, 100, 40, 105, 100, 60, 3, 5},
	/*15*/ {100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3},
	/*16*/ {100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3},
	/*17*/ {100, 95, 50, 100, 100, 50, 105,   0,  0, 0, 0},
	/* Intermission scene 3 */
	/*18*/ {100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3},
	/*19*/ {100, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0},
	/*20*/ {100, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0},
	/*21*/ { 90, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0},
	};
	//@formatter:on

	// from level 21 on, level parameters remain the same
	private static byte[] getLevelParams(int levelNumber) {
		return levelNumber <= LEVEL_PARAMETERS.length ? LEVEL_PARAMETERS[levelNumber - 1]
				: LEVEL_PARAMETERS[LEVEL_PARAMETERS.length - 1];
	}

	// Hunting duration (in ticks) of chase and scatter phases. See Pac-Man dossier.
	private static int[] getHuntingDurations(int levelNumber) {
		return switch (levelNumber) {
		case 1 -> new int[] { 7 * FPS, 20 * FPS, 7 * FPS, 20 * FPS, 5 * FPS, 20 * FPS, 5 * FPS, -1 };
		case 2, 3, 4 -> new int[] { 7 * FPS, 20 * FPS, 7 * FPS, 20 * FPS, 5 * FPS, 1033 * FPS, 1, -1 };
		default -> new int[] { 5 * FPS, 20 * FPS, 5 * FPS, 20 * FPS, 5 * FPS, 1037 * FPS, 1, -1 };
		};
	}

	protected static int checkGhostID(int id) {
		if (id < 0 || id > 3) {
			throw new IllegalArgumentException("Illegal ghost ID: %d".formatted(id));
		}
		return id;
	}

	protected static int checkLevelNumber(int levelNumber) {
		if (levelNumber < 1) {
			throw new IllegalArgumentException("Level number must be at least 1, but is: " + levelNumber);
		}
		return levelNumber;
	}

	// simulates the overflow bug from the original Arcade version
	protected static Vector2i tilesAhead(Creature guy, int n) {
		var ahead = guy.tile().plus(guy.moveDir().vec.scaled(n));
		return guy.moveDir() == UP ? ahead.minus(n, 0) : ahead;
	}

	protected GameLevel level;
	protected Pac pac;
	protected Ghost[] theGhosts;
	protected int credit;
	protected boolean playing;
	protected boolean oneLessLifeDisplayed; // to be replaced
	protected final LevelCounter levelCounter = new LevelCounter();
	protected final Score gameScore = new Score("SCORE");
	protected final Score highScore = new Score("HIGH SCORE");
	protected boolean scoresEnabled;

	/**
	 * Defines the ghost "AI": each ghost has a different way of computing his target tile when chasing Pac-Man.
	 */
	protected void defineGhostChasingBehavior() {
		// Red ghost attacks Pac-Man directly
		ghost(ID_RED_GHOST).setChasingBehavior(pac::tile);

		// Pink ghost ambushes Pac-Man
		ghost(ID_PINK_GHOST).setChasingBehavior(() -> tilesAhead(pac, 4));

		// Cyan ghost attacks from opposite side than red ghost
		ghost(ID_CYAN_GHOST).setChasingBehavior(() -> tilesAhead(pac, 2).scaled(2).minus(ghost(ID_RED_GHOST).tile()));

		// Orange ghost attacks directly but retreats if too near
		ghost(ID_ORANGE_GHOST).setChasingBehavior( //
				() -> ghost(ID_ORANGE_GHOST).tile().euclideanDistance(pac.tile()) < 8 ? //
						ghost(ID_ORANGE_GHOST).scatterTile() : pac.tile());
	}

	/**
	 * @return the game variant realized by this model
	 */
	public abstract GameVariant variant();

	/**
	 * 
	 * @param levelNumber Level number (starting at 1)
	 * @return world used in this level
	 */
	public abstract World createWorld(int levelNumber);

	/**
	 * 
	 * @param levelNumber Level number (starting at 1)
	 * @return bonus used in this level
	 */
	public abstract Bonus createBonus(int levelNumber);

	/**
	 * @param levelNumber Level number (starting at 1)
	 * @return ghost house rules used in this level
	 */
	protected GhostHouseRules createHouseRules(int levelNumber) {
		var rules = new GhostHouseRules();
		rules.setPacStarvingTimeLimit(levelNumber < 5 ? 4 * FPS : 3 * FPS);
		rules.setGlobalGhostDotLimits(GhostHouseRules.NO_LIMIT, 7, 17, GhostHouseRules.NO_LIMIT);
		switch (levelNumber) {
		case 1 -> rules.setPrivateGhostDotLimits(0, 0, 30, 60);
		case 2 -> rules.setPrivateGhostDotLimits(0, 0, 0, 50);
		default -> rules.setPrivateGhostDotLimits(0, 0, 0, 0);
		}
		return rules;
	}

	/**
	 * Resets the game to the initial state.
	 */
	public void reset() {
		LOGGER.trace("Reset game (%s)", variant());
		playing = false;
		pac.setLives(INITIAL_LIVES);
		oneLessLifeDisplayed = false;
		gameScore.reset();
		levelCounter.clear();
		enableScores(true);
		level = null;
	}

	/** Current level. */
	public Optional<GameLevel> level() {
		return Optional.ofNullable(level);
	}

	/**
	 * Builds the given level.
	 * 
	 * @param levelNumber 1-based level number
	 * @return game level object
	 */
	public GameLevel buildLevel(int levelNumber) {
		checkLevelNumber(levelNumber);
		LOGGER.trace("Build game level %d (%s)", levelNumber, variant());
		var world = createWorld(levelNumber);
		var bonus = createBonus(levelNumber);
		var houseRules = createHouseRules(levelNumber);
		var huntingDurations = getHuntingDurations(levelNumber);
		var params = getLevelParams(levelNumber);
		return new GameLevel(levelNumber, this, world, bonus, huntingDurations, houseRules, params);
	}

	public void buildAndEnterLevel(int levelNumber) {
		checkLevelNumber(levelNumber);
		level = buildLevel(levelNumber);
		level.enter();
		levelCounter.addSymbol(level.bonus().symbol());
		gameScore.setLevelNumber(levelNumber);
		ghost(ID_RED_GHOST).setCruiseElroyState(0);
	}

	public void exitLevel() {
		level.exit();
		pac.rest(Integer.MAX_VALUE);
		pac.selectAndResetAnimation(AnimKeys.PAC_MUNCHING);
		ghosts().forEach(Ghost::hide);
	}

	public void enterAttractMode() {
		reset();
		buildAndEnterLevel(1);
		guys().forEach(Entity::show);
		enableScores(false);
		gameScore.setShowContent(false);
		levelCounter.clear();
	}

	public void startHunting() {
		if (level != null) {
			level.startHuntingPhase(0);
		}
	}

	/** Tells if the game play is running. */
	public boolean isPlaying() {
		return playing;
	}

	public void setPlaying(boolean playing) {
		this.playing = playing;
	}

	/** List of collected level symbols. */
	public LevelCounter levelCounter() {
		return levelCounter;
	}

	public Score gameScore() {
		return gameScore;
	}

	public Score highScore() {
		return highScore;
	}

	public void enableScores(boolean enabled) {
		this.scoresEnabled = enabled;
	}

	public void scorePoints(int points) {
		if (points < 0) {
			throw new IllegalArgumentException("Scored points must not be negative but is: " + points);
		}
		if (!scoresEnabled) {
			return;
		}
		final int oldScore = gameScore.points();
		final int newScore = oldScore + points;
		gameScore.setPoints(newScore);
		if (newScore > highScore.points()) {
			highScore.setPoints(newScore);
			highScore.setLevelNumber(level.number());
			highScore.setDate(LocalDate.now());
		}
		if (oldScore < SCORE_EXTRA_LIFE && newScore >= SCORE_EXTRA_LIFE) {
			pac.setLives(pac.lives() + 1);
			GameEvents.publish(GameEventType.PLAYER_GETS_EXTRA_LIFE, pac.tile());
		}
	}

	/** Number of coins inserted. */
	public int credit() {
		return credit;
	}

	public boolean setCredit(int credit) {
		if (0 <= credit && credit <= MAX_CREDIT) {
			this.credit = credit;
			return true;
		}
		return false;
	}

	public boolean changeCredit(int delta) {
		return setCredit(credit + delta);
	}

	public boolean hasCredit() {
		return credit > 0;
	}

	/**
	 * @return Pac-Man and the ghosts in order RED, PINK, CYAN, ORANGE
	 */
	public Stream<Creature> guys() {
		return Stream.of(pac, theGhosts[ID_RED_GHOST], theGhosts[ID_PINK_GHOST], theGhosts[ID_CYAN_GHOST],
				theGhosts[ID_ORANGE_GHOST]);
	}

	/**
	 * @return Pac-Man or Ms. Pac-Man
	 */
	public Pac pac() {
		return pac;
	}

	/** If one less life is displayed in the lives counter. */
	public boolean isOneLessLifeDisplayed() {
		return oneLessLifeDisplayed;
	}

	public void setOneLessLifeDisplayed(boolean value) {
		this.oneLessLifeDisplayed = value;
	}

	/**
	 * @param id ghost ID (0, 1, 2, 3)
	 * @return the ghost with the given ID
	 */
	public Ghost ghost(int id) {
		return theGhosts[checkGhostID(id)];
	}

	/**
	 * @param states states specifying which ghosts are returned
	 * @return all ghosts which are in any of the given states or all ghosts, if no states are specified
	 */
	public Stream<Ghost> ghosts(GhostState... states) {
		if (states.length > 0) {
			return Stream.of(theGhosts).filter(ghost -> ghost.is(states));
		}
		// when no states are given, return *all* ghosts (ghost.is() would return *no* ghosts!)
		return Stream.of(theGhosts);
	}

	/**
	 * @param ghost a ghost
	 * @param dir   a direction
	 * @return tells if the ghost can currently move towards the given direction
	 */
	public boolean isGhostAllowedMoving(Ghost ghost, Direction dir) {
		return true;
	}

	/**
	 * @param levelNumber game level number
	 * @return 1-based intermission (cut scene) number that is played after given level or <code>0</code> if no
	 *         intermission is played after given level.
	 */
	public int intermissionNumber(int levelNumber) {
		checkLevelNumber(levelNumber);
		return switch (levelNumber) {
		case 2 -> 1;
		case 5 -> 2;
		case 9, 13, 17 -> 3;
		default -> 0;
		};
	}

	public abstract void onBonusReached(Bonus bonus);
}
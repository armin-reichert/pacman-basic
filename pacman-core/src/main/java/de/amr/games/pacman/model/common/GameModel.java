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
import static de.amr.games.pacman.model.common.actors.GhostState.FRIGHTENED;
import static de.amr.games.pacman.model.common.actors.GhostState.HUNTING_PAC;
import static de.amr.games.pacman.model.common.actors.GhostState.LEAVING_HOUSE;
import static de.amr.games.pacman.model.common.actors.GhostState.LOCKED;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.event.GameEvents;
import de.amr.games.pacman.lib.math.Vector2i;
import de.amr.games.pacman.lib.steering.Direction;
import de.amr.games.pacman.lib.timer.TickTimer;
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

	/** Duration (in ticks) of chase and scatter phases. See Pac-Man dossier. */
	public static final int[][] HUNTING_DURATION = {
		{ 7 *FPS, 20 *FPS, 7 *FPS, 20 *FPS, 5 *FPS,   20 *FPS, 5 *FPS, -1 },
		{ 7 *FPS, 20 *FPS, 7 *FPS, 20 *FPS, 5 *FPS, 1033 *FPS,      1, -1 },
		{ 5 *FPS, 20 *FPS, 5 *FPS, 20 *FPS, 5 *FPS, 1037 *FPS,      1, -1 },
	};
	//@formatter:on

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
	/** Remembers what happens during a tick. */
	public final Memory memo = new Memory();

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
	 * Resets the game to the initial state and first level.
	 */
	public void reset() {
		LOGGER.info("Reset game (%s)", variant());
		playing = false;
		pac.setLives(INITIAL_LIVES);
		oneLessLifeDisplayed = false;
		gameScore.reset();
		levelCounter.clear();
		enableScores(true);
		enterLevel(1);
	}

	/** Current level. */
	public GameLevel level() {
		return level;
	}

	/**
	 * Builds the given level.
	 * 
	 * @param levelNumber 1-based level number
	 */
	public void initLevel(int levelNumber) {
		checkLevelNumber(levelNumber);
		LOGGER.info("Init level %d (%s)", levelNumber, variant());
		var world = createWorld(levelNumber);
		var bonus = createBonus(levelNumber);
		var houseRules = createHouseRules(levelNumber);
		var data = levelNumber <= LEVEL_PARAMETERS.length ? LEVEL_PARAMETERS[levelNumber - 1]
				: LEVEL_PARAMETERS[LEVEL_PARAMETERS.length - 1];
		level = new GameLevel(levelNumber, world, bonus, houseRules, data);
		level.world().assignGhostPositions(theGhosts);
		ghost(ID_RED_GHOST).setCruiseElroyState(0);
		gameScore.setLevelNumber(levelNumber);
	}

	public void enterLevel(int levelNumber) {
		checkLevelNumber(levelNumber);
		LOGGER.info("Enter level %d (%s)", levelNumber, variant());
		initLevel(levelNumber);
		getReadyToRumble();
		levelCounter.addSymbol(level.bonus().symbol());
		level.houseRules().resetPrivateGhostDotCounters();
	}

	public void exitLevel() {
		// TODO level.exit()
		level().huntingTimer().stop();
		level.energizerPulse().reset();
		level.bonus().setInactive();
		pac.rest(Integer.MAX_VALUE);
		pac.selectAndResetAnimation(AnimKeys.PAC_MUNCHING);
		ghosts().forEach(Ghost::hide);
	}

	public void enterAttractMode() {
		reset();
		guys().forEach(Entity::show);
		enableScores(false);
		gameScore.setShowContent(false);
		levelCounter.clear();
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
	 * Sets the game state to be ready for playing. Pac-Man and the ghosts are placed at their initial positions, made
	 * visible and their state is initialized. Also the power timer and energizers are reset.
	 */
	public void getReadyToRumble() {
		pac.reset();
		pac.setPosition(level.world().pacStartPosition());
		pac.setMoveAndWishDir(Direction.LEFT);
		var initialDirs = List.of(Direction.LEFT, Direction.DOWN, Direction.UP, Direction.UP);
		ghosts().forEach(ghost -> {
			ghost.reset();
			ghost.setPosition(ghost.homePosition());
			ghost.setMoveAndWishDir(initialDirs.get(ghost.id()));
			ghost.enterStateLocked();
		});
		guys().forEach(Creature::hide);
		level.bonus().setInactive();
		level.energizerPulse().reset();
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
	 * Hunting happens in different phases. Phases 0, 2, 4, 6 are scattering phases where the ghosts target for their
	 * respective corners and circle around the walls in their corner, phases 1, 3, 5, 7 are chasing phases where the
	 * ghosts attack Pac-Man.
	 * 
	 * @param phase hunting phase (0..7)
	 */
	public void startHuntingPhase(int phase) {
		level().huntingTimer().startPhase(phase, huntingPhaseTicks(phase));
	}

	/**
	 * Advances the current hunting phase and enters the next phase when the current phase ends. On every change between
	 * phases, the living ghosts outside of the ghost house reverse their move direction.
	 */
	private void advanceHunting() {
		level().huntingTimer().advance();
		if (level().huntingTimer().hasExpired()) {
			startHuntingPhase(level().huntingTimer().phase() + 1);
			// locked and house-leaving ghost will reverse as soon as he has left the house
			ghosts(HUNTING_PAC, LOCKED, LEAVING_HOUSE).forEach(Ghost::reverseDirectionASAP);
		}
	}

	/**
	 * @param phase hunting phase (0, ... 7)
	 * @return hunting (scattering or chasing) ticks for current level and given phase
	 */
	private long huntingPhaseTicks(int phase) {
		if (phase < 0 || phase > 7) {
			throw new IllegalArgumentException("Hunting phase must be 0..7, but is " + phase);
		}
		var ticks = switch (level.number()) {
		case 1 -> HUNTING_DURATION[0][phase];
		case 2, 3, 4 -> HUNTING_DURATION[1][phase];
		default -> HUNTING_DURATION[2][phase];
		};
		return ticks == -1 ? TickTimer.INDEFINITE : ticks;
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

	// Bonus

	public abstract void onBonusReached(Bonus bonus);

	// Game logic

	public void update() {
		pac.update(this);
		checkIfGhostCanGetUnlocked();
		ghosts().forEach(ghost -> ghost.update(this));
		level.bonus().update(this);
		level.energizerPulse().animate();
		advanceHunting();
	}

	private void checkIfGhostCanGetUnlocked() {
		level.houseRules().checkIfGhostUnlocked(this).ifPresent(unlock -> {
			memo.unlockedGhost = Optional.of(unlock.ghost());
			memo.unlockReason = unlock.reason();
			LOGGER.info("Unlocked %s: %s", unlock.ghost().name(), unlock.reason());
			if (unlock.ghost().id() == ID_ORANGE_GHOST && ghost(ID_RED_GHOST).cruiseElroyState() < 0) {
				// Blinky's "cruise elroy" state is re-enabled when orange ghost is unlocked
				ghost(ID_RED_GHOST).setCruiseElroyStateEnabled(true);
			}
		});
	}

	public void checkHowTheGuysAreDoing() {
		if (memo.pacPowered) {
			onPacPowerBegin();
		}

		memo.pacMetKiller = pac.isMeetingKiller(this);
		if (memo.pacMetKiller) {
			onPacMeetsKiller();
			return; // enter new game state
		}

		memo.edibleGhosts = ghosts(FRIGHTENED).filter(pac::sameTile).toList();
		if (!memo.edibleGhosts.isEmpty()) {
			killGhosts(memo.edibleGhosts);
			memo.ghostsKilled = true;
			return; // enter new game state
		}

		memo.pacPowerFading = pac.powerTimer().remaining() == pac.powerFadingTicks();
		memo.pacPowerLost = pac.powerTimer().hasExpired();
		if (memo.pacPowerFading) {
			GameEvents.publish(GameEventType.PAC_STARTS_LOSING_POWER, pac.tile());
		}
		if (memo.pacPowerLost) {
			onPacPowerEnd();
		}
	}

	public void killAllPossibleGhosts() {
		var prey = ghosts(HUNTING_PAC, FRIGHTENED).toList();
		level.setNumGhostsKilledByEnergizer(0);
		killGhosts(prey);
	}

	private void killGhosts(List<Ghost> prey) {
		prey.forEach(this::killGhost);
		level.setNumGhostsKilledInLevel(level.numGhostsKilledInLevel() + prey.size());
		if (level.numGhostsKilledInLevel() == 16) {
			scorePoints(POINTS_ALL_GHOSTS_KILLED);
			LOGGER.info("All ghosts killed at level %d, %s wins %d points", level.number(), pac.name(),
					POINTS_ALL_GHOSTS_KILLED);
		}
	}

	private void killGhost(Ghost ghost) {
		ghost.setKilledIndex(level.numGhostsKilledByEnergizer());
		ghost.enterStateEaten(this);
		level.setNumGhostsKilledByEnergizer(level.numGhostsKilledByEnergizer() + 1);
		memo.killedGhosts.add(ghost);
		int points = POINTS_GHOSTS_SEQUENCE[ghost.killedIndex()];
		scorePoints(points);
		LOGGER.info("%s killed at tile %s, %s wins %d points", ghost.name(), ghost.tile(), pac.name(), points);
	}

	// Pac-Man

	private void onPacMeetsKiller() {
		pac.kill();
		level.houseRules().resetGlobalDotCounterAndSetEnabled(true);
		ghost(ID_RED_GHOST).setCruiseElroyStateEnabled(false);
		LOGGER.info("%s died at tile %s", pac.name(), pac.tile());
	}

	private void onPacPowerBegin() {
		LOGGER.info("%s power begins", pac.name());
		level().huntingTimer().stop();
		pac.powerTimer().restartSeconds(level.params().pacPowerSeconds());
		LOGGER.info("Timer started: %s", pac.powerTimer());
		ghosts(HUNTING_PAC).forEach(ghost -> ghost.enterStateFrightened(this));
		ghosts(FRIGHTENED).forEach(Ghost::reverseDirectionASAP);
		GameEvents.publish(GameEventType.PAC_GETS_POWER, pac.tile());
	}

	private void onPacPowerEnd() {
		LOGGER.info("%s power ends", pac.name());
		level().huntingTimer().start();
		pac.powerTimer().stop();
		pac.powerTimer().resetIndefinitely();
		LOGGER.info("Timer stopped: %s", pac.powerTimer());
		ghosts(FRIGHTENED).forEach(ghost -> ghost.enterStateHuntingPac(this));
		GameEvents.publish(GameEventType.PAC_LOSES_POWER, pac.tile());
	}

	// Food

	public void checkIfPacFindsFood() {
		var world = level.world();
		var tile = pac.tile();
		if (world.containsFood(tile)) {
			memo.foodFoundTile = Optional.of(tile);
			memo.lastFoodFound = world.foodRemaining() == 1;
			memo.energizerFound = world.isEnergizerTile(tile);
			memo.pacPowered = memo.energizerFound && level.params().pacPowerSeconds() > 0;
			memo.bonusReached = world.eatenFoodCount() == PELLETS_EATEN_BONUS1
					|| world.eatenFoodCount() == PELLETS_EATEN_BONUS2;
			onFoodFound(tile);
		} else {
			pac.starve();
		}
	}

	private void onFoodFound(Vector2i tile) {
		level.world().removeFood(tile);
		pac.endStarving();
		if (memo.energizerFound) {
			level.setNumGhostsKilledByEnergizer(0);
			pac.rest(RESTING_TICKS_ENERGIZER);
			scorePoints(POINTS_ENERGIZER);
		} else {
			pac.rest(RESTING_TICKS_NORMAL_PELLET);
			scorePoints(POINTS_NORMAL_PELLET);
		}
		if (memo.bonusReached) {
			onBonusReached(level.bonus());
		}
		checkIfRedGhostBecomesCruiseElroy();
		level.houseRules().updateGhostDotCounters(this);
		GameEvents.publish(GameEventType.PAC_FINDS_FOOD, tile);
	}

	private void checkIfRedGhostBecomesCruiseElroy() {
		var foodRemaining = level.world().foodRemaining();
		if (foodRemaining == level.params().elroy1DotsLeft()) {
			ghost(ID_RED_GHOST).setCruiseElroyState(1);
		} else if (foodRemaining == level.params().elroy2DotsLeft()) {
			ghost(ID_RED_GHOST).setCruiseElroyState(2);
		}
	}
}
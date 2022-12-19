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

import static de.amr.games.pacman.lib.Direction.UP;
import static de.amr.games.pacman.model.common.actors.Ghost.ID_CYAN_GHOST;
import static de.amr.games.pacman.model.common.actors.Ghost.ID_ORANGE_GHOST;
import static de.amr.games.pacman.model.common.actors.Ghost.ID_PINK_GHOST;
import static de.amr.games.pacman.model.common.actors.Ghost.ID_RED_GHOST;
import static de.amr.games.pacman.model.common.actors.GhostState.FRIGHTENED;
import static de.amr.games.pacman.model.common.actors.GhostState.HUNTING_PAC;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.event.GameEvents;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.TickTimer;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.animation.SingleEntityAnimation;
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

	/** Pixels/tick at 100% relative speed. */
	public static final double BASE_SPEED = 1.25;

	/** Game loop speed in ticks/sec. */
	public static final int FPS = 60;

	public static final Random RND = new Random();

	/** Duration (in ticks) of chase and scatter phases. See Pac-Man dossier. */
	public static final int[][] HUNTING_DURATION = {
		//@formatter:off
		{ 7 *FPS, 20 *FPS, 7 *FPS, 20 *FPS, 5 *FPS,   20 *FPS, 5 *FPS, -1 },
		{ 7 *FPS, 20 *FPS, 7 *FPS, 20 *FPS, 5 *FPS, 1033 *FPS,      1, -1 },
		{ 5 *FPS, 20 *FPS, 5 *FPS, 20 *FPS, 5 *FPS, 1037 *FPS,      1, -1 },
		//@formatter:on
	};

	public static final short MAX_CREDIT = 99;
	public static final short PELLET_VALUE = 10;
	public static final short PELLET_RESTING_TICKS = 1;
	public static final short ENERGIZER_VALUE = 50;
	public static final short ENERGIZER_RESTING_TICKS = 3;
	public static final short INITIAL_LIVES = 3;
	public static final short[] GHOST_EATEN_POINTS = { 200, 400, 800, 1600 };
	public static final short ALL_GHOSTS_KILLED_POINTS = 12_000;
	public static final short EXTRA_LIFE_POINTS = 10_000;
	public static final short BONUS1_PELLETS_EATEN = 70;
	public static final short BONUS2_PELLETS_EATEN = 170;
	public static final short BONUS_EATEN_TICKS = 2 * FPS; // unsure
	public static final short PAC_POWER_FADING_TICKS = 2 * FPS; // unsure
	public static final float GHOST_SPEED_INSIDE_HOUSE = 0.5f; // unsure
	public static final float GHOST_SPEED_RETURNING = 2.0f; // unsure

	/**
	 * @param levelNumber level number (1, 2, ...)
	 * @param mazeNumber  maze number (1, 2, ...)
	 * @param world       world used in this level
	 * @param symbol      if value <code>!= -1</code>, overrides the value in the data array
	 * @param data        array with level data
	 */
	public static GameLevel createLevelFromData(int levelNumber, int mazeNumber, World world, Bonus bonus,
			GhostHouseRules houseRules, byte[] data) {
		float playerSpeed = percentage(data[1]);
		float ghostSpeed = percentage(data[2]);
		float ghostSpeedTunnel = percentage(data[3]);
		int elroy1DotsLeft = data[4];
		float elroy1Speed = percentage(data[5]);
		int elroy2DotsLeft = data[6];
		float elroy2Speed = percentage(data[7]);
		float playerSpeedPowered = percentage(data[8]);
		float ghostSpeedFrightened = percentage(data[9]);
		int ghostFrightenedSeconds = data[10];
		int numFlashes = data[11];
		return new GameLevel(levelNumber, mazeNumber, world, bonus, houseRules, playerSpeed, ghostSpeed, ghostSpeedTunnel,
				elroy1DotsLeft, elroy1Speed, elroy2DotsLeft, elroy2Speed, playerSpeedPowered, ghostSpeedFrightened,
				ghostFrightenedSeconds, numFlashes);
	}

	private static float percentage(int value) {
		return value / 100f;
	}

	protected GameLevel level;
	protected final Pac pac;
	protected Ghost[] theGhosts;
	protected final HuntingTimer huntingTimer = new HuntingTimer();
	protected final TickTimer pacPowerTimer = new TickTimer("PacPower", FPS);
	protected final SingleEntityAnimation<Boolean> energizerPulse = SingleEntityAnimation.pulse(10);
	protected int credit;
	protected int lives;
	protected int numGhostsKilledInLevel;
	protected int numGhostsKilledByEnergizer;
	protected boolean playing;
	protected boolean pacImmune;
	protected boolean oneLessLifeDisplayed;
	protected final LevelCounter levelCounter = new LevelCounter();
	protected final Score gameScore = new Score("SCORE");
	protected final Score highScore = new Score("HIGH SCORE");
	protected boolean scoresEnabled;

	/** Remembers what happens during a tick. */
	public final Memory memo = new Memory();

	protected static int checkGhostID(int id) {
		if (id < 0 || id > 3) {
			throw new IllegalArgumentException("Illegal ghost ID: %d".formatted(id));
		}
		return id;
	}

	protected static void checkLevelNumber(int levelNumber) {
		if (levelNumber < 1) {
			throw new IllegalArgumentException("Level number must be at least 1, but is: " + levelNumber);
		}
	}

	// simulates the overflow bug from the original Arcade version
	protected static Vector2i tilesAhead(Creature guy, int n) {
		var ahead = guy.tile().plus(guy.moveDir().vec.scaled(n));
		return guy.moveDir() == UP ? ahead.minus(n, 0) : ahead;
	}

	/**
	 * Creates the game model.
	 */
	protected GameModel() {
		pac = createPac();
		theGhosts = createGhosts();
		setGhostBehavior();
		setLevel(1);
	}

	/**
	 * @return the Pac-person of this game
	 */
	protected abstract Pac createPac();

	/**
	 * Creates the ghosts.
	 * 
	 * @return the ghosts in order RED, PINK, CYAN, ORANGE
	 */
	protected abstract Ghost[] createGhosts();

	/**
	 * Defines the ghost "AI": each ghost has a different way of computing his target tile when chasing Pac-Man.
	 */
	protected void setGhostBehavior() {
		var redGhost = ghost(ID_RED_GHOST);
		var pinkGhost = ghost(ID_PINK_GHOST);
		var cyanGhost = ghost(ID_CYAN_GHOST);
		var orangeGhost = ghost(ID_ORANGE_GHOST);
		// Red ghost attacks Pac-Man directly
		redGhost.setChasingBehavior(pac::tile);
		// Pink ghost ambushes Pac-Man
		pinkGhost.setChasingBehavior(() -> tilesAhead(pac, 4));
		// Cyan ghost attacks from opposite side than red ghost
		cyanGhost.setChasingBehavior(() -> tilesAhead(pac, 2).scaled(2).minus(redGhost.tile()));
		// Orange ghost attacks and retreats if too near
		orangeGhost.setChasingBehavior(
				() -> orangeGhost.tile().euclideanDistance(pac.tile()) < 8 ? orangeGhost.scatterTile() : pac.tile());
	}

	/**
	 * @return the game variant realized by this model
	 */
	public abstract GameVariant variant();

	/**
	 * Creates the specified level.
	 * 
	 * @param levelNumber Level number (starting at 1)
	 */
	protected abstract void createLevel(int levelNumber);

	/**
	 * @param levelNumber Level number (starting at 1)
	 * @return ghost house rules used in this level
	 */
	protected GhostHouseRules createHouseRules(int levelNumber) {
		var rules = new GhostHouseRules();
		rules.setPacStarvingTimeLimit(levelNumber < 5 ? 240 : 180);
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
		playing = false;
		lives = INITIAL_LIVES;
		oneLessLifeDisplayed = false;
		gameScore.reset();
		enableScores(true);
		setLevel(1);
	}

	/** Current level. */
	public GameLevel level() {
		return level;
	}

	/**
	 * Initializes the model for given game level.
	 * 
	 * @param levelNumber 1-based level number
	 */
	public void setLevel(int levelNumber) {
		checkLevelNumber(levelNumber);
		createLevel(levelNumber);
		numGhostsKilledInLevel = 0;
		numGhostsKilledByEnergizer = 0;
		level.world().assignGhostPositions(theGhosts);
		ghost(ID_RED_GHOST).setCruiseElroyState(0);
		gameScore().setLevelNumber(levelNumber);
		if (levelNumber == 1) {
			levelCounter().clear();
			levelCounter().addSymbol(level().bonus().symbol());
		}
	}

	public void startLevel(int levelNumber) {
		setLevel(levelNumber);
		getReadyToRumble();
		guys().forEach(Entity::hide);
		levelCounter.addSymbol(level.bonus().symbol());
		level.houseRules().resetPrivateGhostDotCounters();
	}

	public void endLevel() {
		huntingTimer.stop();
		level.bonus().setInactive();
		pac.rest(Integer.MAX_VALUE);
		pac.selectAndResetAnimation(AnimKeys.PAC_MUNCHING);
		ghosts().forEach(Ghost::hide);
		energizerPulse.reset();
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
		if (oldScore < EXTRA_LIFE_POINTS && newScore >= EXTRA_LIFE_POINTS) {
			lives++;
			GameEvents.publish(new GameEvent(this, GameEventType.PLAYER_GETS_EXTRA_LIFE, null, pac.tile()));
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
		guys().forEach(Creature::show);
		level.bonus().setInactive();
		pacPowerTimer.reset(0);
		energizerPulse.reset();
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

	/** Tells if Pac-Man can be killed by ghosts. Not part of original game. */
	public boolean isPacImmune() {
		return pacImmune;
	}

	public void setPacImmune(boolean immune) {
		this.pacImmune = immune;
	}

	/** Number of ghosts killed at the current level. */
	public int numGhostsKilledInLevel() {
		return numGhostsKilledInLevel;
	}

	/** Number of ghosts killed by current energizer. */
	public int numGhostsKilledByEnergizer() {
		return numGhostsKilledByEnergizer;
	}

	/** Number of lives remaining. */
	public int lives() {
		return lives;
	}

	public void setLives(int lives) {
		this.lives = lives;
	}

	/** If one less life is displayed in the lives counter. */
	public boolean isOneLessLifeDisplayed() {
		return oneLessLifeDisplayed;
	}

	public void setOneLessLifeDisplayed(boolean value) {
		this.oneLessLifeDisplayed = value;
	}

	public SingleEntityAnimation<Boolean> energizerPulse() {
		return energizerPulse;
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

	// Hunting

	/** Timer used to control hunting phases. */
	public HuntingTimer huntingTimer() {
		return huntingTimer;
	}

	/**
	 * Hunting happens in different phases. Phases 0, 2, 4, 6 are scattering phases where the ghosts target for their
	 * respective corners and circle around the walls in their corner, phases 1, 3, 5, 7 are chasing phases where the
	 * ghosts attack Pac-Man.
	 * 
	 * @param phase hunting phase (0..7)
	 */
	public void startHuntingPhase(int phase) {
		huntingTimer.startPhase(phase, huntingPhaseTicks(phase));
	}

	/**
	 * Advances the current hunting phase and enters the next phase when the current phase ends. On every change between
	 * phases, the living ghosts outside of the ghost house reverse their move direction.
	 */
	private void advanceHunting() {
		huntingTimer.advance();
		if (huntingTimer.hasExpired()) {
			startHuntingPhase(huntingTimer.phase() + 1);
			// locked and house-leaving ghost will reverse as soon as he has left the house
			ghosts(GhostState.HUNTING_PAC, GhostState.LOCKED, GhostState.LEAVING_HOUSE).forEach(Ghost::reverseDirectionASAP);
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

	/** Controls the time Pac has power. */
	public TickTimer pacPowerTimer() {
		return pacPowerTimer;
	}

	// Bonus stuff

	protected abstract void onBonusReached();

	// Game logic

	public void update() {
		pac.update(this);
		checkIfGhostCanGetUnlocked();
		ghosts().forEach(ghost -> ghost.update(this));
		level.bonus().update(this);
		advanceHunting();
		pacPowerTimer.advance();
		energizerPulse.animate();
	}

	private void checkIfGhostCanGetUnlocked() {
		level.houseRules().checkIfGhostCanBeGetUnlocked(this).ifPresent(unlock -> {
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

		memo.pacMetKiller = isPacMeetingKiller();
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

		memo.pacPowerFading = pacPowerTimer.remaining() == PAC_POWER_FADING_TICKS;
		memo.pacPowerLost = pacPowerTimer.hasExpired();
		if (memo.pacPowerFading) {
			GameEvents.publish(GameEventType.PAC_STARTS_LOSING_POWER, pac.tile());
		}
		if (memo.pacPowerLost) {
			onPacPowerEnd();
		}
	}

	public void killAllPossibleGhosts() {
		var prey = ghosts(GhostState.HUNTING_PAC, GhostState.FRIGHTENED).toList();
		numGhostsKilledByEnergizer = 0;
		killGhosts(prey);
	}

	private void killGhosts(List<Ghost> prey) {
		prey.forEach(this::killGhost);
		numGhostsKilledInLevel += prey.size();
		if (numGhostsKilledInLevel == 16) {
			scorePoints(ALL_GHOSTS_KILLED_POINTS);
			LOGGER.info("All ghosts killed at level %d, %s wins %d points", level.number(), pac.name(),
					ALL_GHOSTS_KILLED_POINTS);
		}
	}

	private void killGhost(Ghost ghost) {
		ghost.setKilledIndex(numGhostsKilledByEnergizer++);
		ghost.enterStateEaten(this);
		memo.killedGhosts.add(ghost);
		int points = GHOST_EATEN_POINTS[ghost.killedIndex()];
		scorePoints(points);
		LOGGER.info("%s killed at tile %s, %s wins %d points", ghost.name(), ghost.tile(), pac.name(), points);
	}

	// Pac-Man

	public boolean isPacPowerFading() {
		return pacPowerTimer.isRunning() && pacPowerTimer.remaining() <= PAC_POWER_FADING_TICKS;
	}

	private boolean isPacMeetingKiller() {
		return !pacImmune && !pacPowerTimer.isRunning() && ghosts(HUNTING_PAC).anyMatch(pac::sameTile);
	}

	private void onPacMeetsKiller() {
		pac.die();
		level.houseRules().resetGlobalDotCounterAndSetEnabled(true);
		ghost(ID_RED_GHOST).setCruiseElroyStateEnabled(false);
		LOGGER.info("%s died at tile %s", pac.name(), pac.tile());
	}

	private void onPacPowerBegin() {
		LOGGER.info("%s power begins", pac.name());
		huntingTimer.stop();
		pacPowerTimer.resetSeconds(level.ghostFrightenedSeconds());
		pacPowerTimer.start();
		LOGGER.info("Timer started: %s", pacPowerTimer);
		ghosts(HUNTING_PAC).forEach(ghost -> ghost.enterStateFrightened(this));
		ghosts(FRIGHTENED).forEach(Ghost::reverseDirectionASAP);
		GameEvents.publish(GameEventType.PAC_GETS_POWER, pac.tile());
	}

	private void onPacPowerEnd() {
		LOGGER.info("%s power ends", pac.name());
		huntingTimer.start();
		pacPowerTimer.stop();
		pacPowerTimer.resetIndefinitely();
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
			memo.pacPowered = memo.energizerFound && level.ghostFrightenedSeconds() > 0;
			memo.bonusReached = world.eatenFoodCount() == BONUS1_PELLETS_EATEN
					|| world.eatenFoodCount() == BONUS2_PELLETS_EATEN;
			onFoodFound(tile);
		} else {
			pac.starve();
		}
	}

	private void onFoodFound(Vector2i tile) {
		level.world().removeFood(tile);
		pac.endStarving();
		if (memo.energizerFound) {
			numGhostsKilledByEnergizer = 0;
			pac.rest(ENERGIZER_RESTING_TICKS);
			scorePoints(ENERGIZER_VALUE);
		} else {
			pac.rest(PELLET_RESTING_TICKS);
			scorePoints(PELLET_VALUE);
		}
		if (memo.bonusReached) {
			onBonusReached();
		}
		checkIfRedGhostBecomesCruiseElroy();
		level.houseRules().updateGhostDotCounters(this);
		GameEvents.publish(GameEventType.PAC_FINDS_FOOD, tile);
	}

	private void checkIfRedGhostBecomesCruiseElroy() {
		var foodRemaining = level.world().foodRemaining();
		if (foodRemaining == level.elroy1DotsLeft()) {
			ghost(ID_RED_GHOST).setCruiseElroyState(1);
		} else if (foodRemaining == level.elroy2DotsLeft()) {
			ghost(ID_RED_GHOST).setCruiseElroyState(2);
		}
	}
}
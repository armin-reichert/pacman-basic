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

import static de.amr.games.pacman.lib.TickTimer.secToTicks;
import static de.amr.games.pacman.model.common.actors.Ghost.ORANGE_GHOST;
import static de.amr.games.pacman.model.common.actors.Ghost.RED_GHOST;
import static de.amr.games.pacman.model.common.actors.GhostState.DEAD;
import static de.amr.games.pacman.model.common.actors.GhostState.ENTERING_HOUSE;
import static de.amr.games.pacman.model.common.actors.GhostState.FRIGHTENED;
import static de.amr.games.pacman.model.common.actors.GhostState.HUNTING_PAC;
import static de.amr.games.pacman.model.common.actors.GhostState.LOCKED;

import java.util.Optional;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.event.GameEvents;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.TickTimer;
import de.amr.games.pacman.lib.animation.SingleEntityAnimation;
import de.amr.games.pacman.model.common.actors.Bonus;
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

	private static final Logger logger = LogManager.getFormatterLogger();

	/** Speed in pixels/tick at 100%. */
	public static final double BASE_SPEED = 1.25;

	protected static final long[][] HUNTING_TIMES = {
	//@formatter:off
		{ 7*60, 20*60, 7*60, 20*60, 5*60,   20*60, 5*60, TickTimer.INDEFINITE },
		{ 7*60, 20*60, 7*60, 20*60, 5*60, 1033*60,    1, TickTimer.INDEFINITE },
		{ 5*60, 20*60, 5*60, 20*60, 5*60, 1037*60,    1, TickTimer.INDEFINITE }
	//@formatter:on
	};

	public static final int PELLET_VALUE = 10;
	public static final int PELLET_RESTING_TICKS = 1;
	public static final int ENERGIZER_VALUE = 50;
	public static final int ENERGIZER_RESTING_TICKS = 3;
	public static final int INITIAL_LIFES = 3;
	public static final int ALL_GHOSTS_KILLED_POINTS = 12_000;
	public static final int EXTRA_LIFE = 10_000;

	// TODO not sure exactly how long Pac-Man is losing power
	public static final long PAC_POWER_FADING_TICKS = secToTicks(2);

	protected static final int[] GHOST_VALUES = { 200, 400, 800, 1600 };

	/** The game variant respresented by this model. */
	public final GameVariant variant;

	/** Credit for playing. */
	public int credit;

	/** Tells if the game play is active. */
	public boolean playing;

	/** Pac-Man or Ms. Pac-Man. */
	public final Pac pac;

	/** Controls the time Pac has power. */
	public final TickTimer powerTimer = new TickTimer("Pac-power-timer");

	/** Tells if Pac-Man can be killed by ghosts. */
	public boolean isPacImmune;

	/** If Pac-Man is controlled by autopilot. */
	public boolean autoControlled;

	/** The four ghosts in order RED, PINK, CYAN, ORANGE. */
	public final Ghost[] theGhosts;

	/** Timer used to control hunting phase. */
	public final HuntingTimer huntingTimer = new HuntingTimer();

	/** Current level. */
	public GameLevel level;

	/** Number of lives remaining. */
	public int lives;

	/** Number of ghosts killed by current energizer. */
	public int ghostsKilledByEnergizer;

	/** List of collected level symbols. */
	public final LevelCounter levelCounter = new LevelCounter(7);

	/** Energizer animation. */
	public final SingleEntityAnimation<Boolean> energizerPulse = SingleEntityAnimation.pulse(10);

	/** Game score and high score. */
	public final GameScores scores = new GameScores(this);

	/** Counter used by ghost house logic. */
	public int globalDotCounter;

	/** Enabled state of the counter used by ghost house logic. */
	public boolean globalDotCounterEnabled;

	/** Number of current intermission scene in test mode. */
	public int intermissionTestNumber;

	public final WhatHappened was = new WhatHappened();

	protected GameModel(GameVariant gameVariant, Pac pac, Ghost... ghosts) {
		if (ghosts.length != 4) {
			throw new IllegalArgumentException("We need exactly 4 ghosts in order RED, PINK, CYAN, ORANGE");
		}
		this.variant = gameVariant;
		this.pac = pac;
		this.theGhosts = ghosts;
	}

	public void consumeCredit() {
		if (credit > 0) {
			--credit;
		}
	}

	/**
	 * @return the world of the current level. May be overriden with covariant return type.
	 */
	public World world() {
		return level.world;
	}

	public void reset() {
		globalDotCounter = 0;
		globalDotCounterEnabled = false;
		playing = false;
		lives = INITIAL_LIFES;
		intermissionTestNumber = 1;
		levelCounter.clear();
		setLevel(1);
		scores.reload();
		scores.gameScore.reset();
	}

	public void resetGuys() {
		powerTimer.reset(0);
		energizerPulse.reset();
		pac.reset();
		resetGhosts();
	}

	private void resetGhosts() {
		ghosts().forEach(ghost -> {
			switch (ghost.id) {
			case Ghost.RED_GHOST -> {
				ghost.setAbsSpeed(0);
				ghost.setBothDirs(Direction.LEFT);
			}
			case Ghost.PINK_GHOST -> {
				ghost.setAbsSpeed(0.5);
				ghost.setBothDirs(Direction.DOWN);
			}
			case Ghost.CYAN_GHOST, Ghost.ORANGE_GHOST -> {
				ghost.setAbsSpeed(0.5);
				ghost.setBothDirs(Direction.UP);
			}
			default -> {
				// ignore
			}
			}
			ghost.setPosition(ghost.homePosition);
			ghost.targetTile = null;
			ghost.stuck = false;
			ghost.newTileEntered = true;
			ghost.reverse = false;
			ghost.killIndex = -1;
			ghost.show();
			ghost.enterStateLocked();
		});
	}

	public Stream<Ghost> ghosts(GhostState... states) {
		if (states.length == 0) {
			return Stream.of(theGhosts); // because is() would return an empty stream
		}
		return ghosts().filter(ghost -> ghost.is(states));
	}

	protected int ghostValue(int ghostKillIndex) {
		return GHOST_VALUES[ghostKillIndex];
	}

	/**
	 * Initializes the model for given game level.
	 * 
	 * @param levelNumber 1-based level number
	 */
	public abstract void setLevel(int levelNumber);

	// Hunting

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
	 * phases, the living ghosts outside of the ghosthouse reverse their move direction.
	 */
	public void advanceHunting() {
		huntingTimer.advance();
		if (huntingTimer.hasExpired()) {
			startHuntingPhase(huntingTimer.phase() + 1);
			ghosts().filter(ghost -> ghost.is(HUNTING_PAC, FRIGHTENED)).forEach(Ghost::forceTurningBack);
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
		return switch (level.number) {
		case 1 -> HUNTING_TIMES[0][phase];
		case 2, 3, 4 -> HUNTING_TIMES[1][phase];
		default -> HUNTING_TIMES[2][phase];
		};
	}

	/**
	 * @param levelNumber game level number
	 * @return 1-based intermission (cut scene) number that is played after given level or <code>0</code> if no
	 *         intermission is played after given level.
	 */
	public int intermissionNumber(int levelNumber) {
		return switch (levelNumber) {
		case 2 -> 1;
		case 5 -> 2;
		case 9, 13, 17 -> 3;
		default -> 0;
		};
	}

	// Game logic

	public static class WhatHappened {
		public boolean allFoodEaten;
		public boolean foodFound;
		public boolean energizerFound;
		public boolean bonusReached;
		public boolean pacMetKiller;
		public boolean pacGotPower;
		public boolean pacPowerLost;
		public boolean pacPowerFading;
		public boolean ghostsKilled;
		public Ghost[] edibleGhosts;
		public Optional<Ghost> unlockedGhost;
		public String unlockReason;

		public WhatHappened() {
			nothingToRemember();
		}

		public void nothingToRemember() {
			allFoodEaten = false;
			foodFound = false;
			energizerFound = false;
			bonusReached = false;
			pacMetKiller = false;
			pacGotPower = false;
			pacPowerLost = false;
			pacPowerFading = false;
			ghostsKilled = false;
			edibleGhosts = new Ghost[0];
			unlockedGhost = Optional.empty();
			unlockReason = null;
		}
	}

	public void whatAboutFood() {
		checkFoodFound();
		if (was.foodFound) {
			onFoodFound();
			if (was.bonusReached) {
				onBonusReached();
			}
		} else {
			pac.setStarvingTicks(1 + pac.getStarvingTicks());
		}
	}

	public void whatAboutTheGuys() {
		if (was.pacGotPower) {
			onPacGetsPower();
		}
		checkPacMeetsKiller();
		if (was.pacMetKiller) {
			onPacMetKiller();
			return; // enter new game state
		}
		checkEdibleGhosts();
		if (was.edibleGhosts.length > 0) {
			killGhosts(was.edibleGhosts);
			was.ghostsKilled = true;
			return; // enter new game state
		}
		checkPacPower();
		if (was.pacPowerFading) {
			GameEvents.publish(GameEventType.PAC_STARTS_LOSING_POWER, pac.tile());
		}
		if (was.pacPowerLost) {
			onPacPowerLost();
		}
	}

	private void checkPacMeetsKiller() {
		if (!isPacImmune && !powerTimer.isRunning() && ghosts(HUNTING_PAC).anyMatch(pac::sameTile)) {
			was.pacMetKiller = true;
		}
	}

	private void onPacMetKiller() {
		pac.die();
		var redGhost = theGhosts[RED_GHOST];
		if (redGhost.elroy > 0) {
			logger.info("Cruise Elroy mode %d for %s disabled", redGhost.elroy, redGhost.name);
			redGhost.elroy = -redGhost.elroy; // negative value means "disabled"
		}
		globalDotCounter = 0;
		globalDotCounterEnabled = true;
		logger.info("Global dot counter got reset and enabled because %s died", pac.name);
	}

	private void checkEdibleGhosts() {
		was.edibleGhosts = ghosts(FRIGHTENED).filter(pac::sameTile).toArray(Ghost[]::new);
	}

	/**
	 * Cheat.
	 */
	public void killAllPossibleGhosts() {
		var prey = ghosts(GhostState.HUNTING_PAC, GhostState.FRIGHTENED).toArray(Ghost[]::new);
		ghostsKilledByEnergizer = 0;
		killGhosts(prey);
	}

	private void killGhosts(Ghost[] prey) {
		Stream.of(prey).forEach(this::killGhost);
		level.numGhostsKilled += prey.length;
		if (level.numGhostsKilled == 16) {
			logger.info("All ghosts killed at level %d, Pac-Man wins additional %d points", level.number,
					ALL_GHOSTS_KILLED_POINTS);
			scores.addPoints(ALL_GHOSTS_KILLED_POINTS);
		}
	}

	private void killGhost(Ghost ghost) {
		ghost.killIndex = ghostsKilledByEnergizer;
		ghostsKilledByEnergizer++;
		int points = ghostValue(ghost.killIndex);
		scores.addPoints(points);
		ghost.enterStateDead();
		ghost.targetTile = level.world.ghostHouse().entry();
		logger.info("Ghost %s killed at tile %s, Pac-Man wins %d points", ghost.name, ghost.tile(), points);
	}

	private void startPowerTimer(double seconds) {
		powerTimer.resetSeconds(seconds);
		powerTimer.start();
		logger.info("Power timer started: %s", powerTimer);
	}

	private void checkPacPower() {
		was.pacPowerFading = powerTimer.remaining() == PAC_POWER_FADING_TICKS;
		was.pacPowerLost = powerTimer.hasExpired();
	}

	public boolean isPacPowerFading() {
		return powerTimer.isRunning() && powerTimer.remaining() <= PAC_POWER_FADING_TICKS;
	}

	private void onPacPowerLost() {
		logger.info("%s lost power, timer=%s", pac.name, powerTimer);
		/* TODO hack: leave state EXPIRED to avoid repetitions. */
		powerTimer.resetIndefinitely();
		huntingTimer.start();
		ghosts(FRIGHTENED).forEach(Ghost::enterStateHunting);
		GameEvents.publish(GameEventType.PAC_LOSES_POWER, pac.tile());
	}

	private void checkFoodFound() {
		if (level.world.containsFood(pac.tile())) {
			was.foodFound = true;
			was.allFoodEaten = level.world.foodRemaining() == 1;
			if (level.world.isEnergizerTile(pac.tile())) {
				was.energizerFound = true;
				if (level.ghostFrightenedSeconds > 0) {
					was.pacGotPower = true;
				}
			}
			was.bonusReached = isBonusReached();
		}
	}

	private void onFoodFound() {
		if (was.energizerFound) {
			ghostsKilledByEnergizer = 0;
			eatFood(ENERGIZER_VALUE, ENERGIZER_RESTING_TICKS);
		} else {
			eatFood(PELLET_VALUE, PELLET_RESTING_TICKS);
		}
	}

	private void eatFood(int value, int restingTicks) {
		pac.setStarvingTicks(0);
		pac.rest(restingTicks);
		level.world.removeFood(pac.tile());
		checkIfRedGhostBecomesCruiseElroy();
		updateGhostDotCounters();
		scores.addPoints(value);
		GameEvents.publish(GameEventType.PAC_FINDS_FOOD, pac.tile());
	}

	private void checkIfRedGhostBecomesCruiseElroy() {
		var redGhost = theGhosts[RED_GHOST];
		var foodRemaining = world().foodRemaining();
		if (foodRemaining == level.elroy1DotsLeft) {
			redGhost.elroy = 1;
			logger.info("%s becomes Cruise Elroy 1", redGhost.name);
		} else if (foodRemaining == level.elroy2DotsLeft) {
			redGhost.elroy = 2;
			logger.info("%s becomes Cruise Elroy 2", redGhost.name);
		}
	}

	private void onPacGetsPower() {
		huntingTimer.stop();
		startPowerTimer(level.ghostFrightenedSeconds);
		ghosts(HUNTING_PAC).forEach(ghost -> {
			ghost.enterStateFrightened();
			ghost.forceTurningBack();
		});
		GameEvents.publish(GameEventType.PAC_GETS_POWER, pac.tile());
	}

	// Ghosts

	public void updateGhosts() {
		checkGhostCanBeUnlocked(was);
		was.unlockedGhost.ifPresent(ghost -> {
			unlockGhost(ghost, was.unlockReason);
			GameEvents.publish(new GameEvent(this, GameEventType.GHOST_STARTS_LEAVING_HOUSE, ghost, ghost.tile()));
		});
		ghosts().forEach(ghost -> ghost.update(this));
	}

	public void letDeadGhostsReturnHome() {
		// fire event(s) only for dead ghosts not yet returning home (killIndex >= 0)
		ghosts(DEAD).filter(ghost -> ghost.killIndex >= 0).forEach(ghost -> {
			ghost.killIndex = -1;
			GameEvents.publish(new GameEvent(this, GameEventType.GHOST_STARTS_RETURNING_HOME, ghost, null));
		});
	}

	/**
	 * Updates the ghosts that are returning home while the game is stalled because of a dying ghost.
	 */
	public void updateGhostsReturningHome() {
		ghosts().filter(ghost -> ghost.is(DEAD) && ghost.killIndex == -1 || ghost.is(ENTERING_HOUSE))
				.forEach(ghost -> ghost.update(this));
	}

	// Ghost house rules, see Pac-Man dossier

	private void checkGhostCanBeUnlocked(WhatHappened result) {
		ghosts(LOCKED).findFirst().ifPresent(ghost -> {
			if (ghost.id == RED_GHOST) {
				result.unlockedGhost = Optional.of(theGhosts[RED_GHOST]);
				result.unlockReason = "Blinky is always unlocked immediately";
				return;
			}
			// first check private dot counter
			if (!globalDotCounterEnabled && ghost.dotCounter >= level.privateDotLimits[ghost.id]) {
				result.unlockedGhost = Optional.of(ghost);
				result.unlockReason = "Private dot counter reached limit (%d)".formatted(level.privateDotLimits[ghost.id]);
				return;
			}
			// check global dot counter
			if (globalDotCounter >= level.globalDotLimits[ghost.id]) {
				result.unlockedGhost = Optional.of(ghost);
				result.unlockReason = "Global dot counter reached limit (%d)".formatted(level.globalDotLimits[ghost.id]);
			} else if (pac.getStarvingTicks() >= level.pacStarvingTimeLimit) {
				result.unlockedGhost = Optional.of(ghost);
				result.unlockReason = "%s reached starving limit (%d ticks)".formatted(pac.name, pac.getStarvingTicks());
				pac.setStarvingTicks(0);
			}
		});
	}

	private void unlockGhost(Ghost ghost, String reason) {
		logger.info("Unlock ghost %s (%s)", ghost.name, reason);
		var redGhost = theGhosts[RED_GHOST];
		var orangeGhost = theGhosts[ORANGE_GHOST];
		if (ghost == orangeGhost && redGhost.elroy < 0) {
			redGhost.elroy = -redGhost.elroy; // resume Elroy mode
			logger.info("%s Elroy mode %d resumed", redGhost.name, redGhost.elroy);
		}
		ghost.unlock(this);
	}

	private void updateGhostDotCounters() {
		if (globalDotCounterEnabled) {
			if (theGhosts[ORANGE_GHOST].is(LOCKED) && globalDotCounter == 32) {
				globalDotCounterEnabled = false;
				globalDotCounter = 0;
				logger.info("Global dot counter disabled and reset, Clyde was in house when counter reached 32");
			} else {
				globalDotCounter++;
			}
		} else {
			ghosts(LOCKED).filter(ghost -> ghost.id != RED_GHOST).findFirst().ifPresent(ghost -> ++ghost.dotCounter);
		}
	}

	// Bonus stuff

	public abstract Bonus bonus();

	protected boolean isBonusReached() {
		return level.world.eatenFoodCount() == 70 || level.world.eatenFoodCount() == 170;
	}

	protected abstract void onBonusReached();

	public void updateBonus() {
		bonus().update(this);
	}
}
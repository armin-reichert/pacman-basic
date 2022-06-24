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
import static de.amr.games.pacman.lib.V2i.v;
import static de.amr.games.pacman.model.common.actors.Ghost.CYAN_GHOST;
import static de.amr.games.pacman.model.common.actors.Ghost.ORANGE_GHOST;
import static de.amr.games.pacman.model.common.actors.Ghost.PINK_GHOST;
import static de.amr.games.pacman.model.common.actors.Ghost.RED_GHOST;
import static de.amr.games.pacman.model.common.actors.GhostState.DEAD;
import static de.amr.games.pacman.model.common.actors.GhostState.ENTERING_HOUSE;
import static de.amr.games.pacman.model.common.actors.GhostState.FRIGHTENED;
import static de.amr.games.pacman.model.common.actors.GhostState.HUNTING_PAC;
import static de.amr.games.pacman.model.common.actors.GhostState.LOCKED;
import static de.amr.games.pacman.model.common.world.World.HTS;

import java.util.Optional;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.amr.games.pacman.controller.common.GameController;
import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.event.GameEvents;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.TickTimer;
import de.amr.games.pacman.lib.animation.SingleSpriteAnimation;
import de.amr.games.pacman.lib.animation.SpriteAnimation;
import de.amr.games.pacman.model.common.actors.AnimKeys;
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
	protected static final int[] GHOST_VALUES = { 200, 400, 800, 1600 };
	public static final int INITIAL_LIFES = 3;
	public static final int ALL_GHOSTS_KILLED_POINTS = 12_000;

	/** The game variant respresented by this model. */
	public final GameVariant variant;

	/** Credit for playing. */
	public int credit;

	/** Tells if the game play is active. */
	public boolean playing;

	/** The player, Pac-Man or Ms. Pac-Man. */
	public final Pac pac;

	/** Tells if the player can be killed by ghosts. */
	public boolean isPacImmune;

	/** If player is controlled by autopilot. */
	public boolean autoControlled;

	/** The four ghosts in order RED, PINK, CYAN, ORANGE. */
	public final Ghost[] theGhosts;

	/** Timer used to control hunting phase. */
	public final HuntingTimer huntingTimer = new HuntingTimer();

	/** Current level. */
	public GameLevel level;

	/** Number of lives remaining. */
	public int lives;

	/** At which score an extra life is granted. */
	public int extraLifeScore = 10_000;

	/** Bounty for eating the next ghost. */
	public int ghostKillIndex;

	/** List of collected level symbols. */
	public final LevelCounter levelCounter = new LevelCounter(7);

	/** Energizer animation. */
	public final SingleSpriteAnimation<Boolean> energizerPulse = SingleSpriteAnimation.pulse(10);

	/** Maze flashing. */
	private SingleSpriteAnimation<?> mazeFlashingAnimation;

	/** Game score and high score. */
	public final GameScores scores = new GameScores(this);

	/** Counter used by ghost house logic. */
	public int globalDotCounter;

	/** Enabled state of the counter used by ghost house logic. */
	public boolean globalDotCounterEnabled;

	/** Number of current intermission scene in test mode. */
	public int intermissionTestNumber;

	private GameSounds sounds;

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

	public Optional<SpriteAnimation> mazeFlashingAnimation() {
		return Optional.ofNullable(mazeFlashingAnimation);
	}

	public void setMazeFlashingAnimation(SingleSpriteAnimation<?> mazeFlashingAnimation) {
		this.mazeFlashingAnimation = mazeFlashingAnimation;
	}

	public void setSounds(GameSounds sounds) {
		this.sounds = sounds;
	}

	public Optional<GameSounds> sounds() {
		return Optional.ofNullable(sounds);
	}

	public void reset() {
		playing = false;
		lives = INITIAL_LIFES;
		intermissionTestNumber = 1;
		levelCounter.clear();
		setLevel(1);
		scores.reload();
		scores.gameScore.reset();
	}

	public void getReadyToPlay() {
		energizerPulse.reset();

		pac.visible = true;
		pac.placeAt(v(13, 26), HTS, 0);
		pac.setBothDirs(Direction.LEFT);
		pac.setAbsSpeed(0);
		pac.targetTile = null; // used in autopilot mode
		pac.stuck = false;
		pac.killed = false;
		pac.restingTicks = 0;
		pac.starvingTicks = 0;
		pac.powerTimer.resetIndefinitely();
		pac.animations().ifPresent(anim -> {
			anim.select(AnimKeys.PAC_MUNCHING);
			anim.selectedAnimation().reset();
		});

		for (Ghost ghost : theGhosts) {
			ghost.visible = true;
			ghost.placeAt(ghost.homeTile, HTS, 0);
			ghost.setAbsSpeed(ghost.id == RED_GHOST ? 0 : 0.5);
			ghost.setBothDirs(switch (ghost.id) {
			case RED_GHOST -> Direction.LEFT;
			case PINK_GHOST -> Direction.DOWN;
			case CYAN_GHOST, ORANGE_GHOST -> Direction.UP;
			default -> null;
			});
			ghost.targetTile = null;
			ghost.stuck = false;
			ghost.killIndex = -1;
			ghost.enterLockedState();
		}
	}

	public Stream<Ghost> ghosts() {
		return Stream.of(theGhosts);
	}

	public Stream<Ghost> ghosts(GhostState state) {
		return ghosts().filter(ghost -> ghost.is(state));
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
	 * ghosts attack the player.
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
			ghosts().filter(ghost -> ghost.is(HUNTING_PAC, FRIGHTENED)).forEach(ghost -> ghost.forceTurningBack(level.world));
		}
	}

	/**
	 * @param phase hunting phase (0, ... 7)
	 * @return hunting (scattering or chasing) ticks for current level and given phase
	 */
	public long huntingPhaseTicks(int phase) {
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

	public static class CheckResult {
		public boolean allFoodEaten;
		public boolean foodFound;
		public boolean energizerFound;
		public boolean bonusReached;
		public boolean playerKilled;
		public boolean playerGotPower;
		public boolean playerPowerLost;
		public boolean playerPowerFading;
		public boolean ghostsKilled;
		public Ghost[] edibleGhosts;
		public Optional<Ghost> unlockedGhost;
		public String unlockReason;

		public CheckResult() {
			clear();
		}

		public void clear() {
			allFoodEaten = false;
			foodFound = false;
			energizerFound = false;
			bonusReached = false;
			playerKilled = false;
			playerGotPower = false;
			playerPowerLost = false;
			playerPowerFading = false;
			ghostsKilled = false;
			edibleGhosts = new Ghost[0];
			unlockedGhost = Optional.empty();
			unlockReason = null;
		}
	}

	/**
	 * Performs a complete simulation step for the player and stores the collected information in the check list.
	 * 
	 * @param result checklist with collected information
	 */
	public void updatePlayer(CheckResult result) {
		pac.update(level);
		checkPlayerFindsFood(result);
		if (result.foodFound) {
			if (result.energizerFound) {
				onPlayerFindsEnergizer();
			} else {
				onPlayerFindsPellet();
			}
			if (result.bonusReached) {
				onBonusReached();
			}
			if (result.allFoodEaten) {
				return; // level complete
			}
		} else {
			onPlayerFindsNoFood();
		}
		if (result.playerGotPower) {
			onPlayerGetsPower();
		}
		if (!isPacImmune && !pac.hasPower() && playerMeetsHuntingGhost()) {
			killPlayer();
			result.playerKilled = true;
			return; // player killed
		}
		checkEdibleGhosts(result);
		if (result.edibleGhosts.length > 0) {
			killGhosts(result.edibleGhosts);
			result.ghostsKilled = true;
			return; // ghost killed
		}
		checkPlayerPower(result);
		if (result.playerPowerFading) {
			onPlayerPowerFading();
		}
		if (result.playerPowerLost) {
			onPlayerLosesPower();
		}
	}

	private boolean playerMeetsHuntingGhost() {
		return ghosts(HUNTING_PAC).anyMatch(pac::sameTile);
	}

	private void killPlayer() {
		pac.killed = true;
		theGhosts[RED_GHOST].stopCruiseElroyMode();
		// See Pac-Man dossier:
		globalDotCounter = 0;
		globalDotCounterEnabled = true;
		logger.info("Global dot counter got reset and enabled because player died");
	}

	private void checkEdibleGhosts(CheckResult result) {
		result.edibleGhosts = ghosts(FRIGHTENED).filter(pac::sameTile).toArray(Ghost[]::new);
	}

	/** This method is public because {@link GameController#cheatKillAllEatableGhosts()} calls it. */
	public void killGhosts(Ghost[] prey) {
		Stream.of(prey).forEach(this::killGhost);
		level.numGhostsKilled += prey.length;
		if (level.numGhostsKilled == 16) {
			logger.info("All ghosts killed at level %d, Pac-Man wins additional %d points", level.number,
					ALL_GHOSTS_KILLED_POINTS);
			scores.addPoints(ALL_GHOSTS_KILLED_POINTS);
		}
	}

	private void killGhost(Ghost ghost) {
		ghost.killIndex = ++ghostKillIndex;
		int points = ghostValue(ghost.killIndex);
		scores.addPoints(points);
		ghost.enterDeadState(level.world.ghostHouse().entry());
		logger.info("Ghost %s killed at tile %s, Pac-Man wins %d points", ghost.name, ghost.tile(), points);
	}

	private void checkPlayerPower(CheckResult result) {
		// TODO not sure exactly how long the player is losing power
		result.playerPowerFading = pac.powerTimer.remaining() == secToTicks(1);
		result.playerPowerLost = pac.powerTimer.hasExpired();
	}

	private void onPlayerPowerFading() {
		GameEvents.publish(GameEventType.PLAYER_STARTS_LOSING_POWER, pac.tile());
	}

	private void onPlayerLosesPower() {
		logger.info("%s lost power, timer=%s", pac.name, pac.powerTimer);
		/* TODO hack: leave state EXPIRED to avoid repetitions. */
		pac.powerTimer.resetIndefinitely();
		huntingTimer.start();
		sounds().ifPresent(snd -> {
			snd.stop(GameSound.PACMAN_POWER);
			snd.ensureSirenStarted(huntingTimer.phase() / 2);
		});
		ghosts(FRIGHTENED).forEach(Ghost::enterHuntingState);
		GameEvents.publish(GameEventType.PLAYER_LOSES_POWER, pac.tile());
	}

	private void checkPlayerFindsFood(CheckResult result) {
		if (level.world.containsFood(pac.tile())) {
			result.foodFound = true;
			result.allFoodEaten = level.world.foodRemaining() == 1;
			if (level.world.isEnergizerTile(pac.tile())) {
				result.energizerFound = true;
				if (level.ghostFrightenedSeconds > 0) {
					result.playerGotPower = true;
				}
			}
			result.bonusReached = isBonusReached();
		}
	}

	private void onPlayerFindsNoFood() {
		pac.starvingTicks++;
	}

	private void onPlayerFindsPellet() {
		eatFood(PELLET_VALUE, PELLET_RESTING_TICKS);
	}

	private void onPlayerFindsEnergizer() {
		eatFood(ENERGIZER_VALUE, ENERGIZER_RESTING_TICKS);
		ghostKillIndex = -1;
	}

	private void eatFood(int value, int restingTicks) {
		pac.starvingTicks = 0;
		pac.restingTicks = restingTicks;
		level.world.removeFood(pac.tile());
		theGhosts[RED_GHOST].checkCruiseElroyStart(level);
		updateGhostDotCounters();
		scores.addPoints(value);
		sounds().ifPresent(snd -> snd.ensureLoop(GameSound.PACMAN_MUNCH, GameSounds.FOREVER));
		GameEvents.publish(GameEventType.PLAYER_FINDS_FOOD, pac.tile());
	}

	private void onPlayerGetsPower() {
		huntingTimer.stop();
		pac.powerTimer.resetSeconds(level.ghostFrightenedSeconds);
		pac.powerTimer.start();
		logger.info("%s power timer started: %s", pac.name, pac.powerTimer);
		ghosts(HUNTING_PAC).forEach(ghost -> {
			ghost.enterFrightenedState();
			ghost.forceTurningBack(level.world);
		});
		ghosts(LOCKED).forEach(ghost -> ghost.selectAnimation(AnimKeys.GHOST_BLUE));
		sounds().ifPresent(snd -> {
			snd.stopSirens();
			snd.ensureLoop(GameSound.PACMAN_POWER, GameSounds.FOREVER);
		});
		GameEvents.publish(GameEventType.PLAYER_GETS_POWER, pac.tile());
	}

	// Ghosts

	public void updateGhosts(CheckResult result) {
		checkGhostCanBeUnlocked(result);
		result.unlockedGhost.ifPresent(ghost -> {
			unlockGhost(ghost, result.unlockReason);
			GameEvents.publish(new GameEvent(this, GameEventType.GHOST_STARTS_LEAVING_HOUSE, ghost, ghost.tile()));
		});
		ghosts().forEach(ghost -> ghost.update(this));
	}

	public void letDeadGhostsReturnHome() {
		// fire event(s) only for dead ghosts not yet returning home (bounty != 0)
		ghosts(DEAD).filter(ghost -> ghost.killIndex != -1).forEach(ghost -> {
			ghost.killIndex = -1;
			sounds().ifPresent(snd -> snd.ensurePlaying(GameSound.GHOST_RETURNING));
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

	private void checkGhostCanBeUnlocked(CheckResult result) {
		ghosts(LOCKED).findFirst().ifPresent(ghost -> {
			if (ghost.id == RED_GHOST) {
				result.unlockedGhost = Optional.of(theGhosts[RED_GHOST]);
				result.unlockReason = "Blinky is always unlocked immediately";
			} else if (globalDotCounterEnabled && globalDotCounter >= level.globalDotLimits[ghost.id]) {
				result.unlockedGhost = Optional.of(ghost);
				result.unlockReason = "Global dot counter reached limit (%d)".formatted(level.globalDotLimits[ghost.id]);
			} else if (!globalDotCounterEnabled && ghost.dotCounter >= level.privateDotLimits[ghost.id]) {
				result.unlockedGhost = Optional.of(ghost);
				result.unlockReason = "Private dot counter reached limit (%d)".formatted(level.privateDotLimits[ghost.id]);
			} else if (pac.starvingTicks >= level.pacStarvingTimeLimit) {
				result.unlockedGhost = Optional.of(ghost);
				result.unlockReason = "%s reached starving limit (%d ticks)".formatted(pac.name, pac.starvingTicks);
				pac.starvingTicks = 0;
			}
		});
	}

	private void unlockGhost(Ghost ghost, String reason) {
		logger.info("Unlock ghost %s (%s)", ghost.name, reason);
		if (ghost.id == ORANGE_GHOST && theGhosts[RED_GHOST].elroy < 0) {
			theGhosts[RED_GHOST].elroy = -theGhosts[RED_GHOST].elroy; // resume Elroy mode
			logger.info("%s Elroy mode %d resumed", theGhosts[RED_GHOST].name, theGhosts[RED_GHOST].elroy);
		}
		if (ghost.id == RED_GHOST) {
			ghost.enterHuntingState();
		} else {
			ghost.enterLeavingHouseState(this);
		}
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
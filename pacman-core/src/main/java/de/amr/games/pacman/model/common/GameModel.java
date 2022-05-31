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

import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.lib.TickTimer.sec_to_ticks;
import static de.amr.games.pacman.lib.V2i.v;
import static de.amr.games.pacman.model.common.HuntingTimer.isScatteringPhase;
import static de.amr.games.pacman.model.common.actors.Ghost.CYAN_GHOST;
import static de.amr.games.pacman.model.common.actors.Ghost.ORANGE_GHOST;
import static de.amr.games.pacman.model.common.actors.Ghost.PINK_GHOST;
import static de.amr.games.pacman.model.common.actors.Ghost.RED_GHOST;
import static de.amr.games.pacman.model.common.actors.GhostState.DEAD;
import static de.amr.games.pacman.model.common.actors.GhostState.FRIGHTENED;
import static de.amr.games.pacman.model.common.actors.GhostState.HUNTING_PAC;
import static de.amr.games.pacman.model.common.actors.GhostState.LEAVING_HOUSE;
import static de.amr.games.pacman.model.common.actors.GhostState.LOCKED;
import static de.amr.games.pacman.model.common.world.World.HTS;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import de.amr.games.pacman.controller.common.GameController;
import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.event.GameEventListener;
import de.amr.games.pacman.event.GameEventSupport;
import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.event.ScatterPhaseStartsEvent;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.TickTimer;
import de.amr.games.pacman.lib.V2d;
import de.amr.games.pacman.lib.V2i;
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

	/** Speed in pixels/tick at 100%. */
	public static final double BASE_SPEED = 1.25;

	public static final long[][] HUNTING_TIMES = {
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
	public static final int FIRST_GHOST_BOUNTY = 200;
	public static final int INITIAL_LIFES = 3;
	public static final int ALL_GHOSTS_KILLED_POINTS = 12_000;

	/** The game variant respresented by this model. */
	public final GameVariant variant;

	/** The player, Pac-Man or Ms. Pac-Man. */
	public final Pac player;

	/** The four ghosts in order RED, PINK, CYAN, ORANGE. */
	public final Ghost[] ghosts;

	/** Timer used to control hunting phase. */
	public final HuntingTimer huntingTimer = new HuntingTimer();

	/** Current level. */
	public GameLevel level;

	/** Number of lives remaining. */
	public int lives;

	/** At which score an extra life is granted. */
	public int extraLifeScore = 10_000;

	/** Bounty for eating the next ghost. */
	public int ghostBounty;

	/** List of collected level symbols. */
	public List<Integer> levelCounter = new ArrayList<>();

	/** Counter used by ghost house logic. */
	public int globalDotCounter;

	/** Enabled state of the counter used by ghost house logic. */
	public boolean globalDotCounterEnabled;

	/** Number of current intermission scene in test mode. */
	public int intermissionTestNumber;

	private final GameEventSupport eventSupport = new GameEventSupport(this);

	public void addEventListener(GameEventListener subscriber) {
		eventSupport.addEventListener(subscriber);
	}

	public void publish(GameEvent event) {
		eventSupport.publish(event);
	}

	public void publish(GameEventType eventType, V2i tile) {
		eventSupport.publish(eventType, tile);
	}

	public void setEventsPublished(boolean enabled) {
		eventSupport.setEnabled(enabled);
	}

	public GameModel(GameVariant gameVariant, Pac player, Ghost... ghosts) {
		if (ghosts.length != 4) {
			throw new IllegalArgumentException("We need exactly 4 ghosts in order RED, PINK, CYAN, ORANGE");
		}
		this.variant = gameVariant;
		this.player = player;
		this.ghosts = ghosts;
	}

	/**
	 * @return the world of the current level. May be overriden with covariant return type.
	 */
	public World world() {
		return level.world;
	}

	public abstract ScoreSupport scoreSupport();

	public void reset() {
		lives = INITIAL_LIFES;
		levelCounter.clear();
		setLevel(1);
		scoreSupport().reset();
	}

	public void resetGuys() {
		player.placeAt(v(13, 26), HTS, 0);
		player.setBothDirs(Direction.LEFT);
		player.show();
		player.velocity = V2d.NULL;
		player.targetTile = null; // used in autopilot mode
		player.stuck = false;
		player.killed = false;
		player.restingCountdown = 0;
		player.starvingTicks = 0;
		player.powerTimer.setDurationIndefinite();

		for (Ghost ghost : ghosts) {
			ghost.placeAt(ghost.homeTile, HTS, 0);
			ghost.setBothDirs(switch (ghost.id) {
			case RED_GHOST -> Direction.LEFT;
			case PINK_GHOST -> Direction.DOWN;
			case CYAN_GHOST, ORANGE_GHOST -> Direction.UP;
			default -> null;
			});
			ghost.show();
			ghost.velocity = V2d.NULL;
			ghost.targetTile = null;
			ghost.stuck = false;
			ghost.state = GhostState.LOCKED;
			ghost.bounty = 0;
			// ghost.dotCounter = 0;
			// ghost.elroyMode = 0;
		}
		if (bonus() != null) {
			bonus().init();
		}
	}

	public Stream<Ghost> ghosts() {
		return Stream.of(ghosts);
	}

	public Stream<Ghost> ghosts(GhostState state) {
		return ghosts().filter(ghost -> ghost.state == state);
	}

	/**
	 * Initializes model for given game level.
	 * 
	 * @param levelNumber 1-based level number
	 */
	public abstract void setLevel(int levelNumber);

	public void startHuntingPhase(int phase) {
		huntingTimer.startPhase(phase, huntingPhaseTicks(phase));
		if (isScatteringPhase(huntingTimer.phase())) {
			eventSupport.publish(new ScatterPhaseStartsEvent(this, huntingTimer.scatteringPhase()));
		}
	}

	public void startNextHuntingPhase() {
		startHuntingPhase(huntingTimer.phase() + 1);
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
		public boolean levelComplete = false;
		public boolean foodFound = false;
		public boolean energizerFound = false;
		public boolean bonusReached = false;
		public boolean playerGotPower = false;
		public boolean playerPowerLost = false;
		public boolean playerPowerFading = false;
		public boolean playerKilled = false;
		public boolean ghostsCanBeEaten = false;
		public Ghost[] edibleGhosts;
		public Ghost unlockGhost = null;
		public String unlockReason = null;
	}

	public void checkLevelComplete(CheckResult result) {
		if (level.world.foodRemaining() == 0) {
			result.levelComplete = true;
		}
	}

	public void checkGhostsCanBeEaten(CheckResult result) {
		result.edibleGhosts = ghosts(FRIGHTENED).filter(player::sameTile).toArray(Ghost[]::new);
		result.ghostsCanBeEaten = result.edibleGhosts.length > 0;
	}

	/** This method is public because {@link GameController#cheatKillAllEatableGhosts()} calls it. */
	public void eatGhosts(Ghost[] prey) {
		Stream.of(prey).forEach(this::killGhost);
		level.numGhostsKilled += prey.length;
		if (level.numGhostsKilled == 16) {
			log("All ghosts killed at level %d, Pac-Man wins additional %d points", level.number, ALL_GHOSTS_KILLED_POINTS);
			scoreSupport().addPoints(ALL_GHOSTS_KILLED_POINTS);
		}
	}

	private void killGhost(Ghost ghost) {
		ghost.state = DEAD;
		ghost.targetTile = level.world.ghostHouse().entry();
		ghost.bounty = ghostBounty;
		ghostBounty *= 2;
		scoreSupport().addPoints(ghost.bounty);
		log("Ghost %s killed at tile %s, Pac-Man wins %d points", ghost.name, ghost.tile(), ghost.bounty);
	}

	public void checkPlayerKilled(CheckResult result) {
		if (!player.powerTimer.isRunning()) {
			Optional<Ghost> killer = ghosts(HUNTING_PAC).filter(player::sameTile).findAny();
			result.playerKilled = killer.isPresent();
			if (killer.isPresent()) {
				log("%s got killed by %s at tile %s", player.name, killer.get().name, player.tile());
			}
		}
	}

	public void onPlayerKilled() {
		player.killed = true;
		ghosts[RED_GHOST].stopCruiseElroyMode();
		// See Pac-Man dossier:
		globalDotCounter = 0;
		globalDotCounterEnabled = true;
		log("Global dot counter got reset and enabled because player died");

	}

	public void checkPlayerPower(CheckResult result) {
		// TODO not sure exactly how long the player is losing power
		result.playerPowerFading = player.powerTimer.remaining() == sec_to_ticks(1);
		result.playerPowerLost = player.powerTimer.hasExpired();
	}

	public void onPlayerLostPower() {
		log("%s lost power, timer=%s", player.name, player.powerTimer);
		// TODO this is a hack to leave expired state
		player.powerTimer.setDurationIndefinite();
		huntingTimer.start();
		ghosts(FRIGHTENED).forEach(ghost -> ghost.state = HUNTING_PAC);
	}

	private void checkTileForFood(V2i tile, CheckResult result) {
		if (level.world.containsFood(tile)) {
			result.foodFound = true;
			result.energizerFound = level.world.isEnergizerTile(tile);
			result.bonusReached = checkBonusReached();
		}
	}

	public CheckResult checkPlayerFindsFood(CheckResult result) {
		checkTileForFood(player.tile(), result);
		if (!result.foodFound) {
			player.starvingTicks++;
			return result;
		}
		level.world.removeFood(player.tile());
		player.starvingTicks = 0;
		if (result.energizerFound) {
			scoreSupport().addPoints(ENERGIZER_VALUE);
			player.restingCountdown = ENERGIZER_RESTING_TICKS;
			ghostBounty = FIRST_GHOST_BOUNTY;
			if (level.ghostFrightenedSeconds > 0) {
				huntingTimer.stop();
				player.powerTimer.setDurationSeconds(level.ghostFrightenedSeconds).start();
				log("%s power timer started: %s", player.name, player.powerTimer);
				ghosts(HUNTING_PAC).forEach(ghost -> {
					ghost.state = FRIGHTENED;
					ghost.forceTurningBack(level.world);
				});
				result.playerGotPower = true;
			}
		} else {
			scoreSupport().addPoints(PELLET_VALUE);
			player.restingCountdown = PELLET_RESTING_TICKS;
		}
		ghosts[RED_GHOST].checkCruiseElroyStart(level);
		updateGhostDotCounters();
		return result;
	}

	// Bonus stuff

	public abstract Bonus bonus();

	public abstract boolean checkBonusReached();

	public void updateBonus() {
		if (bonus() != null) {
			bonus().update(this);
		}
	}

	// Ghost house rules, see Pac-Man dossier

	public void checkUnlockGhost(CheckResult result) {
		ghosts(LOCKED).findFirst().ifPresent(ghost -> {
			if (ghost.id == RED_GHOST) {
				result.unlockGhost = ghosts[RED_GHOST];
				result.unlockReason = "Blinky is released immediately";
			} else if (globalDotCounterEnabled && globalDotCounter >= level.globalDotLimits[ghost.id]) {
				result.unlockGhost = ghost;
				result.unlockReason = "Global dot counter reached limit (%d)".formatted(level.globalDotLimits[ghost.id]);
			} else if (!globalDotCounterEnabled && ghost.dotCounter >= level.privateDotLimits[ghost.id]) {
				result.unlockGhost = ghost;
				result.unlockReason = "Private dot counter reached limit (%d)".formatted(level.privateDotLimits[ghost.id]);
			} else if (player.starvingTicks >= level.pacStarvingTimeLimit) {
				result.unlockGhost = ghost;
				result.unlockReason = "%s reached starving limit (%d ticks)".formatted(player.name, player.starvingTicks);
				player.starvingTicks = 0;
			}
		});
	}

	public void unlockGhost(Ghost ghost, String reason) {
		log("Unlock ghost %s (%s)", ghost.name, reason);
		if (ghost.id == ORANGE_GHOST && ghosts[RED_GHOST].elroy < 0) {
			ghosts[RED_GHOST].elroy = -ghosts[RED_GHOST].elroy; // resume Elroy mode
			log("%s Elroy mode %d resumed", ghosts[RED_GHOST].name, ghosts[RED_GHOST].elroy);
		}
		if (ghost.id == RED_GHOST) {
			ghost.state = HUNTING_PAC;
		} else {
			ghost.state = LEAVING_HOUSE;
			publish(new GameEvent(this, GameEventType.GHOST_STARTS_LEAVING_HOUSE, ghost, ghost.tile()));
		}
	}

	private void updateGhostDotCounters() {
		if (globalDotCounterEnabled) {
			if (ghosts[ORANGE_GHOST].is(LOCKED) && globalDotCounter == 32) {
				globalDotCounterEnabled = false;
				globalDotCounter = 0;
				log("Global dot counter disabled and reset, Clyde was in house when counter reached 32");
			} else {
				globalDotCounter++;
			}
		} else {
			ghosts(LOCKED).filter(ghost -> ghost.id != RED_GHOST).findFirst().ifPresent(ghost -> ++ghost.dotCounter);
		}
	}
}
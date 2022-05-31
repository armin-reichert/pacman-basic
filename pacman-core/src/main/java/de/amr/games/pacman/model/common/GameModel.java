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
import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.event.GameEventing;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.TickTimer;
import de.amr.games.pacman.lib.V2d;
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

	public static class CheckList {
		public boolean levelComplete;
		public boolean foodFound;
		public boolean energizerFound;
		public boolean bonusReached;
		public boolean playerGotPower;
		public boolean playerPowerLost;
		public boolean playerPowerFading;
		public boolean playerMeetsHuntingGhost;
		public boolean edibleGhostsFound;
		public Ghost[] edibleGhosts;
		public Optional<Ghost> unlockedGhost;
		public String unlockReason;

		public CheckList() {
			clear();
		}

		public void clear() {
			levelComplete = false;
			foodFound = false;
			energizerFound = false;
			bonusReached = false;
			playerGotPower = false;
			playerPowerLost = false;
			playerPowerFading = false;
			playerMeetsHuntingGhost = false;
			edibleGhostsFound = false;
			edibleGhosts = null;
			unlockedGhost = Optional.empty();
			unlockReason = null;
		}
	}

	public final CheckList checkList = new CheckList();

	/** The game variant respresented by this model. */
	public final GameVariant variant;

	/** The player, Pac-Man or Ms. Pac-Man. */
	public final Pac player;

	/** Tells if the player can be killed by ghosts. */
	public boolean playerImmune;

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

	public abstract ScoreSupport scoring();

	public void reset() {
		lives = INITIAL_LIFES;
		levelCounter.clear();
		setLevel(1);
		scoring().reset();
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
	}

	public void advanceHunting() {
		if (huntingTimer.advance().hasExpired()) {
			startHuntingPhase(huntingTimer.phase() + 1);
			ghosts(HUNTING_PAC).forEach(ghost -> ghost.forceTurningBack(level.world));
			ghosts(FRIGHTENED).forEach(ghost -> ghost.forceTurningBack(level.world));
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

	public void checkLevelComplete() {
		if (level.world.foodRemaining() == 0) {
			checkList.levelComplete = true;
		}
	}

	public void checkPlayerMeetsHuntingGhost() {
		if (!playerImmune && !player.powerTimer.isRunning()) {
			ghosts(HUNTING_PAC).filter(player::sameTile).findAny().ifPresent(ghost -> {
				checkList.playerMeetsHuntingGhost = true;
				log("%s collides with hunting %s at %s", player.name, ghost.name, player.tile());
			});
		}
	}

	public void onPlayerMeetsHuntingGhost() {
		player.killed = true;
		ghosts[RED_GHOST].stopCruiseElroyMode();
		// See Pac-Man dossier:
		globalDotCounter = 0;
		globalDotCounterEnabled = true;
		log("Global dot counter got reset and enabled because player died");
	}

	public void checkPlayerFindsEdibleGhosts() {
		checkList.edibleGhosts = ghosts(FRIGHTENED).filter(player::sameTile).toArray(Ghost[]::new);
		checkList.edibleGhostsFound = checkList.edibleGhosts.length > 0;
	}

	/** This method is public because {@link GameController#cheatKillAllEatableGhosts()} calls it. */
	public void onEdibleGhostsFound(Ghost[] prey) {
		Stream.of(prey).forEach(this::killGhost);
		level.numGhostsKilled += prey.length;
		if (level.numGhostsKilled == 16) {
			log("All ghosts killed at level %d, Pac-Man wins additional %d points", level.number, ALL_GHOSTS_KILLED_POINTS);
			scoring().addPoints(ALL_GHOSTS_KILLED_POINTS);
		}
	}

	private void killGhost(Ghost ghost) {
		ghost.state = DEAD;
		ghost.targetTile = level.world.ghostHouse().entry();
		ghost.bounty = ghostBounty;
		ghostBounty *= 2;
		scoring().addPoints(ghost.bounty);
		log("Ghost %s killed at tile %s, Pac-Man wins %d points", ghost.name, ghost.tile(), ghost.bounty);
	}

	public void checkPlayerPower() {
		// TODO not sure exactly how long the player is losing power
		checkList.playerPowerFading = player.powerTimer.remaining() == sec_to_ticks(1);
		checkList.playerPowerLost = player.powerTimer.hasExpired();
	}

	public void onPlayerPowerFading() {
		GameEventing.publish(GameEventType.PLAYER_STARTS_LOSING_POWER, player.tile());
	}

	public void onPlayerLostPower() {
		log("%s lost power, timer=%s", player.name, player.powerTimer);
		/* TODO hack: leave state EXPIRED to avoid repetitions. */
		player.powerTimer.setDurationIndefinite();
		huntingTimer.start();
		ghosts(FRIGHTENED).forEach(ghost -> ghost.state = HUNTING_PAC);
		GameEventing.publish(GameEventType.PLAYER_LOSES_POWER, player.tile());
	}

	public void checkPlayerFindsFood() {
		if (level.world.containsFood(player.tile())) {
			checkList.foodFound = true;
			if (level.world.isEnergizerTile(player.tile())) {
				checkList.energizerFound = true;
				if (level.ghostFrightenedSeconds > 0) {
					checkList.playerGotPower = true;
				}
			}
			checkList.bonusReached = checkBonusReached();
		}
	}

	public void onPlayerFindsNoFood() {
		player.starvingTicks++;
	}

	public void onPlayerFindsPellet() {
		onPlayerFindsFood(PELLET_VALUE, PELLET_RESTING_TICKS);
	}

	public void onPlayerFindsEnergizer() {
		onPlayerFindsFood(ENERGIZER_VALUE, ENERGIZER_RESTING_TICKS);
		ghostBounty = FIRST_GHOST_BOUNTY;
	}

	private void onPlayerFindsFood(int value, int restingTicks) {
		player.starvingTicks = 0;
		player.restingCountdown = restingTicks;
		level.world.removeFood(player.tile());
		ghosts[RED_GHOST].checkCruiseElroyStart(level);
		updateGhostDotCounters();
		scoring().addPoints(value);
		GameEventing.publish(GameEventType.PLAYER_FINDS_FOOD, player.tile());
	}

	public void onPlayerGotPower() {
		huntingTimer.stop();
		player.powerTimer.setDurationSeconds(level.ghostFrightenedSeconds).start();
		log("%s power timer started: %s", player.name, player.powerTimer);
		ghosts(HUNTING_PAC).forEach(ghost -> {
			ghost.state = FRIGHTENED;
			ghost.forceTurningBack(level.world);
		});
		GameEventing.publish(GameEventType.PLAYER_GETS_POWER, player.tile());
	}

	// Bonus stuff

	public abstract Bonus bonus();

	public abstract boolean checkBonusReached();

	public void onBonusReached() {
		GameEventing.publish(GameEventType.BONUS_GETS_ACTIVE, bonus().tile());
	}

	public void updateBonus() {
		if (bonus() != null) {
			bonus().update(this);
		}
	}

	// Ghost house rules, see Pac-Man dossier

	public void checkUnlockGhost() {
		ghosts(LOCKED).findFirst().ifPresent(ghost -> {
			if (ghost.id == RED_GHOST) {
				checkList.unlockedGhost = Optional.of(ghosts[RED_GHOST]);
				checkList.unlockReason = "Blinky is released immediately";
			} else if (globalDotCounterEnabled && globalDotCounter >= level.globalDotLimits[ghost.id]) {
				checkList.unlockedGhost = Optional.of(ghost);
				checkList.unlockReason = "Global dot counter reached limit (%d)".formatted(level.globalDotLimits[ghost.id]);
			} else if (!globalDotCounterEnabled && ghost.dotCounter >= level.privateDotLimits[ghost.id]) {
				checkList.unlockedGhost = Optional.of(ghost);
				checkList.unlockReason = "Private dot counter reached limit (%d)".formatted(level.privateDotLimits[ghost.id]);
			} else if (player.starvingTicks >= level.pacStarvingTimeLimit) {
				checkList.unlockedGhost = Optional.of(ghost);
				checkList.unlockReason = "%s reached starving limit (%d ticks)".formatted(player.name, player.starvingTicks);
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
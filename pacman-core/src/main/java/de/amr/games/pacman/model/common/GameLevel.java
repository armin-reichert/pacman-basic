/*
MIT License

Copyright (c) 2021-2023 Armin Reichert

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

import static de.amr.games.pacman.event.GameEvents.publishGameEvent;
import static de.amr.games.pacman.event.GameEvents.publishGameEventOfType;
import static de.amr.games.pacman.event.GameEvents.publishSoundEvent;
import static de.amr.games.pacman.lib.math.Vector2i.v2i;
import static de.amr.games.pacman.lib.steering.Direction.LEFT;
import static de.amr.games.pacman.model.common.actors.Ghost.ID_CYAN_GHOST;
import static de.amr.games.pacman.model.common.actors.Ghost.ID_ORANGE_GHOST;
import static de.amr.games.pacman.model.common.actors.Ghost.ID_PINK_GHOST;
import static de.amr.games.pacman.model.common.actors.Ghost.ID_RED_GHOST;
import static de.amr.games.pacman.model.common.actors.GhostState.FRIGHTENED;
import static de.amr.games.pacman.model.common.actors.GhostState.HUNTING_PAC;
import static de.amr.games.pacman.model.common.actors.GhostState.LEAVING_HOUSE;
import static de.amr.games.pacman.model.common.actors.GhostState.LOCKED;
import static de.amr.games.pacman.model.common.world.World.halfTileRightOf;
import static java.util.Objects.requireNonNull;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.amr.games.pacman.controller.common.Steering;
import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.lib.U;
import de.amr.games.pacman.lib.anim.Animated;
import de.amr.games.pacman.lib.math.Vector2f;
import de.amr.games.pacman.lib.math.Vector2i;
import de.amr.games.pacman.lib.steering.Direction;
import de.amr.games.pacman.lib.timer.TickTimer;
import de.amr.games.pacman.model.common.actors.Bonus;
import de.amr.games.pacman.model.common.actors.Creature;
import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.model.common.actors.GhostState;
import de.amr.games.pacman.model.common.actors.Pac;
import de.amr.games.pacman.model.common.world.World;

/**
 * @author Armin Reichert
 */
public class GameLevel {

	private static final Logger LOG = LogManager.getFormatterLogger();

	private static final float percent(byte value) {
		return value / 100f;
	}

	/** Relative Pac-Man speed in this level. */
	public final float pacSpeed;

	/** Relative ghost speed in this level. */
	public final float ghostSpeed;

	/** Relative ghost speed when inside tunnel in this level. */
	public final float ghostSpeedTunnel;

	/** Number of pellets left before player becomes "Cruise Elroy" with severity 1. */
	public final int elroy1DotsLeft;

	/** Relative speed of player being "Cruise Elroy" at severity 1. */
	public final float elroy1Speed;

	/** Number of pellets left before player becomes "Cruise Elroy" with severity 2. */
	public final int elroy2DotsLeft;

	/** Relative speed of player being "Cruise Elroy" with severity 2. */
	public final float elroy2Speed;

	/** Relative speed of Pac-Man in power mode. */
	public final float pacSpeedPowered;

	/** Relative speed of frightened ghost. */
	public final float ghostSpeedFrightened;

	/** Number of seconds Pac-Man gets power int this level. */
	public final int pacPowerSeconds;

	/** Number of maze flashes at end of this level. */
	public final int numFlashes;

	/** Number of intermission scene played after this level (1, 2, 3, 0 = no intermission). */
	public final int intermissionNumber;

	private final GameModel game;

	private final int number;

	private final TickTimer huntingTimer = new TickTimer("HuntingTimer");

	private final Memory memo = new Memory();

	private final World world;

	private final Pac pac;

	private final Ghost[] ghosts;

	private final Bonus bonus;

	private final int[] huntingDurations;

	private Steering pacSteering;

	private int huntingPhase;

	private int numGhostsKilledInLevel;

	private int numGhostsKilledByEnergizer;

	private byte cruiseElroyState;

	private static final List<Vector2i> RED_ZONE = List.of(v2i(12, 14), v2i(15, 14), v2i(12, 26), v2i(15, 26));

	public GameLevel(GameModel game, int number) {
		GameModel.checkGameNotNull(game);
		GameModel.checkLevelNumber(number);
		this.game = game;
		this.number = number;
		world = game.createWorld(number);
		pac = game.createPac();
		ghosts = game.createGhosts();
		bonus = game.createBonus(number);
		huntingDurations = game.huntingDurations(number);
		defineGhostHouseRules();
		defineGhostAI();

		var data = game.levelData(number);
		pacSpeed = percent(data[0]);
		ghostSpeed = percent(data[1]);
		ghostSpeedTunnel = percent(data[2]);
		elroy1DotsLeft = data[3];
		elroy1Speed = percent(data[4]);
		elroy2DotsLeft = data[5];
		elroy2Speed = percent(data[6]);
		pacSpeedPowered = percent(data[7]);
		ghostSpeedFrightened = percent(data[8]);
		pacPowerSeconds = data[9];
		numFlashes = data[10];
		intermissionNumber = data[11];

		LOG.trace("Game level %d created. (%s)", number, game.variant());
	}

	private void defineGhostAI() {
		// Red ghost attacks Pac-Man directly
		ghost(ID_RED_GHOST).setChasingTarget(pac::tile);
		// Pink ghost ambushes Pac-Man
		ghost(ID_PINK_GHOST).setChasingTarget(() -> tilesAhead(pac, 4));
		// Cyan ghost attacks from opposite side than red ghost
		ghost(ID_CYAN_GHOST).setChasingTarget(() -> tilesAhead(pac, 2).scaled(2).minus(ghost(ID_RED_GHOST).tile()));
		// Orange ghost attacks directly but retreats if too near
		ghost(ID_ORANGE_GHOST).setChasingTarget( //
				() -> ghost(ID_ORANGE_GHOST).tile().euclideanDistance(pac.tile()) < 8 ? //
						ghostScatterTargetTile(ID_ORANGE_GHOST) : pac.tile());
	}

	/**
	 * Simulates the overflow bug from the original Arcade version.
	 * 
	 * @param guy      a creature
	 * @param numTiles number of tiles
	 * @return the tile located the given number of tiles in front of the creature (towards move direction). In case
	 *         creature looks up, additional n tiles are added towards left. This simulates an overflow error in the
	 *         original Arcade game.
	 */
	private static Vector2i tilesAhead(Creature guy, int numTiles) {
		Vector2i ahead = guy.tile().plus(guy.moveDir().vector().scaled(numTiles));
		return guy.moveDir() == Direction.UP ? ahead.minus(numTiles, 0) : ahead;
	}

	public List<Vector2i> upwardsBlockedTiles() {
		return switch (game.variant()) {
		case MS_PACMAN -> Collections.emptyList();
		case PACMAN -> RED_ZONE;
		default -> throw new IllegalGameVariantException(game.variant());
		};
	}

	public void update() {
		memo.forgetEverything(); // ich scholze jetzt
		world.animation(GameModel.AK_MAZE_ENERGIZER_BLINKING).ifPresent(Animated::animate);
		pac.update(this);
		checkIfGhostCanGetUnlocked();
		ghosts().forEach(ghost -> ghost.update(this));
		boolean newHuntingPhaseStarted = updateHuntingTimer();
		if (newHuntingPhaseStarted) {
			ghosts(HUNTING_PAC, LOCKED, LEAVING_HOUSE).forEach(Ghost::reverseAsSoonAsPossible);
		}
		bonus.update(this);
		checkIfPacFoundFood();
		checkPacPower();
		checkIfPacManGetsKilled();
		findEdibleGhosts();
	}

	public void exit() {
		LOG.trace("Exit level %d (%s)", number, game.variant());
		pac.rest(Pac.REST_FOREVER);
		pac.selectAndResetAnimation(GameModel.AK_PAC_MUNCHING);
		ghosts().forEach(Ghost::hide);
		bonus.setInactive();
		world.animation(GameModel.AK_MAZE_ENERGIZER_BLINKING).ifPresent(Animated::reset);
		stopHunting();
	}

	public GameModel game() {
		return game;
	}

	/** @return level number, starting with 1. */
	public int number() {
		return number;
	}

	public World world() {
		return world;
	}

	/**
	 * @return Pac-Man or Ms. Pac-Man
	 */
	public Pac pac() {
		return pac;
	}

	public Optional<Steering> pacSteering() {
		return Optional.ofNullable(pacSteering);
	}

	public void setPacSteering(Steering pacSteering) {
		this.pacSteering = pacSteering;
	}

	/**
	 * @param id ghost ID, one of {@link Ghost#ID_RED_GHOST}, {@link Ghost#ID_PINK_GHOST}, {@value Ghost#ID_CYAN_GHOST},
	 *           {@link Ghost#ID_ORANGE_GHOST}
	 * @return the ghost with the given ID
	 */
	public Ghost ghost(byte id) {
		GameModel.checkGhostID(id);
		return ghosts[id];
	}

	/**
	 * @param states states specifying which ghosts are returned
	 * @return all ghosts which are in any of the given states or all ghosts, if no states are specified
	 */
	public Stream<Ghost> ghosts(GhostState... states) {
		if (states.length > 0) {
			return Stream.of(ghosts).filter(ghost -> ghost.is(states));
		}
		// when no states are given, return *all* ghosts (ghost.is() would return *no* ghosts!)
		return Stream.of(ghosts);
	}

	/**
	 * @return Pac-Man and the ghosts in order RED, PINK, CYAN, ORANGE
	 */
	public Stream<Creature> guys() {
		return Stream.of(pac, ghosts[ID_RED_GHOST], ghosts[ID_PINK_GHOST], ghosts[ID_CYAN_GHOST], ghosts[ID_ORANGE_GHOST]);
	}

	public Bonus bonus() {
		return bonus;
	}

	public TickTimer huntingTimer() {
		return huntingTimer;
	}

	/**
	 * @return information about what happened during the current simulation step
	 */
	public Memory memo() {
		return memo;
	}

	/** @return Blinky's "cruise elroy" state. Values: <code>0, 1, 2, -1, -2</code>. (0=off, negative=disabled). */
	public byte cruiseElroyState() {
		return cruiseElroyState;
	}

	/**
	 * @param cruiseElroyState Values: <code>0, 1, 2, -1, -2</code>. (0=off, negative=disabled).
	 */
	public void setCruiseElroyState(int cruiseElroyState) {
		if (cruiseElroyState < -2 || cruiseElroyState > 2) {
			throw new IllegalArgumentException(
					"Cruise Elroy state must be one of -2, -1, 0, 1, 2, but is " + cruiseElroyState);
		}
		this.cruiseElroyState = (byte) cruiseElroyState;
		LOG.trace("Cruise Elroy state set to %d", cruiseElroyState);
	}

	private void setCruiseElroyStateEnabled(boolean enabled) {
		if (enabled && cruiseElroyState < 0 || !enabled && cruiseElroyState > 0) {
			cruiseElroyState = (byte) (-cruiseElroyState);
			LOG.trace("Cruise Elroy state set to %d", cruiseElroyState);
		}
	}

	public int numGhostsKilledInLevel() {
		return numGhostsKilledInLevel;
	}

	public int numGhostsKilledByEnergizer() {
		return numGhostsKilledByEnergizer;
	}

	/**
	 * @param ghost a ghost
	 * @param dir   a direction
	 * @return tells if the ghost can steer towards the given direction
	 */
	public boolean isSteeringAllowed(Ghost ghost, Direction dir) {
		requireNonNull(ghost);
		requireNonNull(dir);
		if (dir == Direction.UP && ghost.is(HUNTING_PAC) && upwardsBlockedTiles().contains(ghost.tile())) {
			return false;
		}
		return true;
	}

	/**
	 * Hunting happens in different phases. Phases 0, 2, 4, 6 are scattering phases where the ghosts target for their
	 * respective corners and circle around the walls in their corner, phases 1, 3, 5, 7 are chasing phases where the
	 * ghosts attack Pac-Man.
	 * 
	 * @param phase hunting phase (0..7)
	 */
	public void startHunting(int phase) {
		if (phase < 0 || phase > 7) {
			throw new IllegalArgumentException("Hunting phase must be 0..7, but is " + phase);
		}
		this.huntingPhase = phase;
		huntingTimer.reset(huntingTicks(phase));
		huntingTimer.start();
		LOG.info("Hunting phase %d (%s) started. %s", phase, currentHuntingPhaseName(), huntingTimer);
	}

	private void stopHunting() {
		huntingTimer.stop();
		LOG.info("Hunting timer stopped");
	}

	private long huntingTicks(int phase) {
		return huntingDurations[phase] == -1 ? TickTimer.INDEFINITE : huntingDurations[phase];
	}

	/**
	 * Advances the current hunting phase and enters the next phase when the current phase ends. On every change between
	 * phases, the living ghosts outside of the ghost house reverse their move direction.
	 * 
	 * @return if new hunting phase has been started
	 */
	private boolean updateHuntingTimer() {
		huntingTimer.advance();
		if (huntingTimer.hasExpired()) {
			startHunting(huntingPhase + 1);
			return true;
		}
		return false;
	}

	/**
	 * @return number of current phase <code>(0-7)
	 */
	public int huntingPhase() {
		return huntingPhase;
	}

	/**
	 * @return (optional) number of current scattering phase <code>(0-3)</code>
	 */
	public OptionalInt scatterPhase() {
		return U.isEven(huntingPhase) ? OptionalInt.of(huntingPhase / 2) : OptionalInt.empty();
	}

	/**
	 * @return (optional) number of current chasing phase <code>(0-3)</code>
	 */
	public OptionalInt chasingPhase() {
		return U.isOdd(huntingPhase) ? OptionalInt.of(huntingPhase / 2) : OptionalInt.empty();
	}

	public String currentHuntingPhaseName() {
		return U.isEven(huntingPhase) ? "Scattering" : "Chasing";
	}

	/**
	 * Pac-Man and the ghosts are placed at their initial positions and locked. Also the bonus, Pac-Man power timer and
	 * energizer pulse are reset.
	 * 
	 * @param guysVisible if the guys are made visible
	 */
	public void letsGetReadyToRumbleAndShowGuys(boolean guysVisible) {
		pac.reset();
		pac.setPosition(halfTileRightOf(13, 26));
		pac.setMoveAndWishDir(Direction.LEFT);
		pac.setVisible(guysVisible);
		ghosts().forEach(ghost -> {
			ghost.reset();
			ghost.setPosition(ghostInitialPosition(ghost.id()));
			ghost.setMoveAndWishDir(ghostInitialDirection(ghost.id()));
			ghost.setVisible(guysVisible);
			ghost.enterStateLocked();
		});
		bonus.setInactive();
		world.animation(GameModel.AK_MAZE_ENERGIZER_BLINKING).ifPresent(Animated::reset);
	}

	public Vector2f ghostInitialPosition(byte ghostID) {
		return switch (ghostID) {
		case Ghost.ID_RED_GHOST -> world.ghostHouse().doors().get(0).entryPosition();
		case Ghost.ID_PINK_GHOST -> world.ghostHouse().seatPositions().get(1);
		case Ghost.ID_CYAN_GHOST -> world.ghostHouse().seatPositions().get(0);
		case Ghost.ID_ORANGE_GHOST -> world.ghostHouse().seatPositions().get(2);
		default -> throw new IllegalGhostIDException(ghostID);
		};
	}

	public Vector2f ghostRevivalPosition(byte ghostID) {
		return switch (ghostID) {
		case Ghost.ID_RED_GHOST -> world.ghostHouse().seatPositions().get(1);
		case Ghost.ID_PINK_GHOST, Ghost.ID_CYAN_GHOST, Ghost.ID_ORANGE_GHOST -> ghostInitialPosition(ghostID);
		default -> throw new IllegalGhostIDException(ghostID);
		};
	}

	public Vector2i ghostScatterTargetTile(byte ghostID) {
		return switch (ghostID) {
		case Ghost.ID_RED_GHOST -> v2i(25, 0);
		case Ghost.ID_PINK_GHOST -> v2i(2, 0);
		case Ghost.ID_CYAN_GHOST -> v2i(27, 34);
		case Ghost.ID_ORANGE_GHOST -> v2i(0, 34);
		default -> throw new IllegalGhostIDException(ghostID);
		};
	}

	public Direction ghostInitialDirection(byte ghostID) {
		return switch (ghostID) {
		case Ghost.ID_RED_GHOST -> Direction.LEFT;
		case Ghost.ID_PINK_GHOST -> Direction.DOWN;
		case Ghost.ID_CYAN_GHOST -> Direction.UP;
		case Ghost.ID_ORANGE_GHOST -> Direction.UP;
		default -> throw new IllegalGhostIDException(ghostID);
		};
	}

	/**
	 * @param ghost a ghost
	 * @return relative speed of ghost when hunting
	 */
	public float huntingSpeed(Ghost ghost) {
		if (world.isTunnel(ghost.tile())) {
			return ghostSpeedTunnel;
		} else if (ghost.id() == ID_RED_GHOST && cruiseElroyState == 1) {
			return elroy1Speed;
		} else if (ghost.id() == ID_RED_GHOST && cruiseElroyState == 2) {
			return elroy2Speed;
		} else {
			return ghostSpeed;
		}
	}

	private void checkIfBlinkyBecomesCruiseElroy() {
		var foodRemaining = world.uneatenFoodCount();
		if (foodRemaining == elroy1DotsLeft) {
			setCruiseElroyState(1);
		} else if (foodRemaining == elroy2DotsLeft) {
			setCruiseElroyState(2);
		}
	}

	private void checkIfGhostCanGetUnlocked() {
		checkIfGhostCanLeaveHouse().ifPresent(unlock -> {
			memo.unlockedGhost = Optional.of(unlock.ghost());
			memo.unlockReason = unlock.reason();
			LOG.trace("%s unlocked: %s", unlock.ghost().name(), unlock.reason());
			if (unlock.ghost().id() == ID_ORANGE_GHOST && cruiseElroyState < 0) {
				// Blinky's "cruise elroy" state is re-enabled when orange ghost is unlocked
				setCruiseElroyStateEnabled(true);
			}
		});
	}

	private void findEdibleGhosts() {
		memo.edibleGhosts = ghosts(FRIGHTENED).filter(pac::sameTile).toList();
	}

	/**
	 * Called by cheat action only.
	 */
	public void killAllHuntingAndFrightenedGhosts() {
		memo.edibleGhosts = ghosts(HUNTING_PAC, FRIGHTENED).toList();
		numGhostsKilledByEnergizer = 0;
		killEdibleGhosts();
	}

	public void killEdibleGhosts() {
		if (!memo.edibleGhosts.isEmpty()) {
			memo.edibleGhosts.forEach(this::killGhost);
			numGhostsKilledInLevel += memo.edibleGhosts.size();
			if (numGhostsKilledInLevel == 16) {
				game.scorePoints(GameModel.POINTS_ALL_GHOSTS_KILLED);
				LOG.trace("All ghosts killed at level %d, %s wins %d points", number, pac.name(),
						GameModel.POINTS_ALL_GHOSTS_KILLED);
			}
		}
	}

	private void killGhost(Ghost ghost) {
		ghost.setKilledIndex(numGhostsKilledByEnergizer);
		ghost.enterStateEaten();
		numGhostsKilledByEnergizer += 1;
		memo.killedGhosts.add(ghost);
		int points = GameModel.POINTS_GHOSTS_SEQUENCE[ghost.killedIndex()];
		game.scorePoints(points);
		LOG.trace("%s killed at tile %s, %s wins %d points", ghost.name(), ghost.tile(), pac.name(), points);
	}

	// Pac-Man

	public boolean pacKilled() {
		return memo.pacKilled;
	}

	private void checkIfPacManGetsKilled() {
		if (game.isImmune()) {
			return;
		}
		memo.pacKilled = ghosts(HUNTING_PAC).anyMatch(pac::sameTile);
	}

	public void onPacKilled() {
		stopHunting();
		pac.die();
		resetGlobalDotCounterAndSetEnabled(true);
		setCruiseElroyStateEnabled(false);
		LOG.info("%s died at tile %s", pac.name(), pac.tile());
	}

	private void checkPacPower() {
		memo.pacPowerFading = pac.powerTimer().remaining() == GameModel.TICKS_PAC_POWER_FADES;
		memo.pacPowerLost = pac.powerTimer().hasExpired();
		if (memo.pacPowerGained) {
			stopHunting();
			pac.powerTimer().restartSeconds(pacPowerSeconds);
			LOG.info("%s power starting, duration %d ticks", pac.name(), pac.powerTimer().duration());
			ghosts(HUNTING_PAC).forEach(Ghost::enterStateFrightened);
			ghosts(FRIGHTENED).forEach(Ghost::reverseAsSoonAsPossible);
			publishGameEventOfType(GameEventType.PAC_GETS_POWER);
			publishSoundEvent(GameModel.SE_PACMAN_POWER_STARTS);
		} else if (memo.pacPowerFading) {
			publishGameEventOfType(GameEventType.PAC_STARTS_LOSING_POWER);
		} else if (memo.pacPowerLost) {
			LOG.info("%s power ends, timer: %s", pac.name(), pac.powerTimer());
			huntingTimer.start();
			LOG.info("Hunting timer restarted");
			pac.powerTimer().stop();
			pac.powerTimer().resetIndefinitely();
			ghosts(FRIGHTENED).forEach(Ghost::enterStateHuntingPac);
			publishGameEventOfType(GameEventType.PAC_LOSES_POWER);
			publishSoundEvent(GameModel.SE_PACMAN_POWER_ENDS);
		}
	}

	// Food

	public boolean completed() {
		return memo.lastFoodFound;
	}

	private void checkIfPacFoundFood() {
		var tile = pac.tile();
		if (world.containsFood(tile)) {
			world.removeFood(tile);
			memo.foodFoundTile = Optional.of(tile);
			memo.energizerFound = world.isEnergizerTile(tile);
			memo.lastFoodFound = world.uneatenFoodCount() == 0;
			memo.pacPowerGained = memo.energizerFound && pacPowerSeconds > 0;
			memo.bonusReached = game.isFirstBonusReached() || game.isSecondBonusReached();
			pac.endStarving();
			if (memo.energizerFound) {
				numGhostsKilledByEnergizer = 0;
				pac.rest(GameModel.RESTING_TICKS_ENERGIZER);
				game.scorePoints(GameModel.POINTS_ENERGIZER);
			} else {
				pac.rest(GameModel.RESTING_TICKS_NORMAL_PELLET);
				game.scorePoints(GameModel.POINTS_NORMAL_PELLET);
			}
			checkIfBlinkyBecomesCruiseElroy();
			updateGhostDotCounters();
			publishGameEvent(GameEventType.PAC_FINDS_FOOD, tile);
			publishSoundEvent(GameModel.SE_PACMAN_FOUND_FOOD);
		} else {
			pac.starve();
		}
		if (memo.bonusReached) {
			game.onBonusReached();
		}
	}

	/* --- Ghosthouse control rules, see Pac-Man dossier --- */

	private long pacStarvingTicksLimit;
	private byte[] globalGhostDotLimits;
	private byte[] privateGhostDotLimits;
	private int[] ghostDotCounters;
	private int globalDotCounter;
	private boolean globalDotCounterEnabled;

	private void defineGhostHouseRules() {
		pacStarvingTicksLimit = number < 5 ? 4 * GameModel.FPS : 3 * GameModel.FPS;
		globalGhostDotLimits = new byte[] { -1, 7, 17, -1 };
		privateGhostDotLimits = switch (number) {
		case 1 -> new byte[] { 0, 0, 30, 60 };
		case 2 -> new byte[] { 0, 0, 0, 50 };
		default -> new byte[] { 0, 0, 0, 0 };
		};
		ghostDotCounters = new int[] { 0, 0, 0, 0 };
		globalDotCounter = 0;
		globalDotCounterEnabled = false;
	}

	private void resetGlobalDotCounterAndSetEnabled(boolean enabled) {
		globalDotCounter = 0;
		globalDotCounterEnabled = enabled;
		LOG.trace("Global dot counter reset to 0 and %s", enabled ? "enabled" : "disabled");
	}

	private void updateGhostDotCounters() {
		if (globalDotCounterEnabled) {
			if (ghost(ID_ORANGE_GHOST).is(LOCKED) && globalDotCounter == 32) {
				LOG.trace("%s inside house when counter reached 32", ghost(ID_ORANGE_GHOST).name());
				resetGlobalDotCounterAndSetEnabled(false);
			} else {
				globalDotCounter++;
				LOG.trace("Global dot counter = %d", globalDotCounter);
			}
		} else {
			ghosts(LOCKED).filter(ghost -> world.ghostHouse().contains(ghost.tile())).findFirst()
					.ifPresent(this::increaseGhostDotCounter);
		}
	}

	private void increaseGhostDotCounter(Ghost ghost) {
		ghostDotCounters[ghost.id()]++;
		LOG.trace("%s dot counter = %d", ghost.name(), ghostDotCounters[ghost.id()]);
	}

	private Optional<GhostUnlockResult> checkIfGhostCanLeaveHouse() {
		var ghost = ghosts(LOCKED).findFirst().orElse(null);
		if (ghost == null) {
			return Optional.empty();
		}
		if (!world.ghostHouse().contains(ghost.tile())) {
			return unlockGhost(ghost, "Already outside house");
		}
		var id = ghost.id();
		// check private dot counter
		if (!globalDotCounterEnabled && ghostDotCounters[id] >= privateGhostDotLimits[id]) {
			return unlockGhost(ghost, "Private dot counter at limit (%d)", privateGhostDotLimits[id]);
		}
		// check global dot counter
		var globalDotLimit = globalGhostDotLimits[id] == -1 ? Integer.MAX_VALUE : globalGhostDotLimits[id];
		if (globalDotCounter >= globalDotLimit) {
			return unlockGhost(ghost, "Global dot counter at limit (%d)", globalDotLimit);
		}
		// check Pac-Man starving time
		if (pac.starvingTicks() >= pacStarvingTicksLimit) {
			pac.endStarving();
			LOG.trace("Pac-Man starving timer reset to 0");
			return unlockGhost(ghost, "%s reached starving limit (%d ticks)", pac.name(), pacStarvingTicksLimit);
		}
		return Optional.empty();
	}

	private Optional<GhostUnlockResult> unlockGhost(Ghost ghost, String reason, Object... args) {
		if (!world.ghostHouse().contains(ghost.tile())) {
			ghost.setMoveAndWishDir(LEFT);
			ghost.enterStateHuntingPac();
		} else {
			ghost.enterStateLeavingHouse(this);
		}
		return Optional.of(new GhostUnlockResult(ghost, reason.formatted(args)));
	}
}
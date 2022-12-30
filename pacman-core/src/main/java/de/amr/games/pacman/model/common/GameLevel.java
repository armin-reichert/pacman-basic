/*
MIT License

Copyright (c) 2022 Armin Reichert

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

import static de.amr.games.pacman.model.common.actors.Ghost.ID_CYAN_GHOST;
import static de.amr.games.pacman.model.common.actors.Ghost.ID_ORANGE_GHOST;
import static de.amr.games.pacman.model.common.actors.Ghost.ID_PINK_GHOST;
import static de.amr.games.pacman.model.common.actors.Ghost.ID_RED_GHOST;
import static de.amr.games.pacman.model.common.actors.GhostState.FRIGHTENED;
import static de.amr.games.pacman.model.common.actors.GhostState.HUNTING_PAC;
import static de.amr.games.pacman.model.common.actors.GhostState.LEAVING_HOUSE;
import static de.amr.games.pacman.model.common.actors.GhostState.LOCKED;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.event.GameEvents;
import de.amr.games.pacman.lib.anim.Pulse;
import de.amr.games.pacman.lib.math.Vector2i;
import de.amr.games.pacman.lib.steering.Direction;
import de.amr.games.pacman.lib.timer.TickTimer;
import de.amr.games.pacman.model.common.actors.AnimKeys;
import de.amr.games.pacman.model.common.actors.Bonus;
import de.amr.games.pacman.model.common.actors.Creature;
import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.model.common.actors.GhostState;
import de.amr.games.pacman.model.common.actors.Pac;
import de.amr.games.pacman.model.common.world.ArcadeWorld;
import de.amr.games.pacman.model.common.world.World;

/**
 * @author Armin Reichert
 */
public class GameLevel {

	private static final Logger LOGGER = LogManager.getFormatterLogger();

	public record Parameters(
	//@formatter:off
		/** Relative player speed in this level. */
		float playerSpeed,
		/** Relative ghost speed in this level. */
		float ghostSpeed,
		/** Relative ghost speed when inside tunnel in this level. */
		float ghostSpeedTunnel,
		/** Number of pellets left before player becomes "Cruise Elroy" with severity 1. */
		int elroy1DotsLeft,
		/** Relative speed of player being "Cruise Elroy" at severity 1. */
		float elroy1Speed,
		/** Number of pellets left before player becomes "Cruise Elroy" with severity 2. */
		int elroy2DotsLeft,
		/** Relative speed of player being "Cruise Elroy" with severity 2. */
		float elroy2Speed,
		/** Relative speed of player in power mode. */
		float playerSpeedPowered,
		/** Relative speed of frightened ghost. */
		float ghostSpeedFrightened,
		/** Number of seconds Pac-Man gets power int this level. */
		int pacPowerSeconds,
		/** Number of maze flashes at end of this level. */
		int numFlashes,
		/** Number of intermission scene played after this level (1, 2, 3, 0 = no intermission). */
		int intermissionNumber,
		/** Index into array of hunting times. */
		int huntingTimesIndex)
	//@formatter:on
	{
		public static Parameters createFromData(byte[] data) {
			//@formatter:off
			float playerSpeed          = data[0] / 100f;
			float ghostSpeed           = data[1] / 100f;
			float ghostSpeedTunnel     = data[2] / 100f;
			byte elroy1DotsLeft        = data[3];
			float elroy1Speed          = data[4] / 100f;
			byte elroy2DotsLeft        = data[5];
			float elroy2Speed          = data[6] / 100f;
			float playerSpeedPowered   = data[7] / 100f;
			float ghostSpeedFrightened = data[8] / 100f;
			byte pacPowerSeconds       = data[9];
			byte numFlashes            = data[10];
			byte intermissionNumber    = data[11];
			byte huntingTimesIndex     = data[12]; 
			//@formatter:on
			return new Parameters(playerSpeed, ghostSpeed, ghostSpeedTunnel, elroy1DotsLeft, elroy1Speed, elroy2DotsLeft,
					elroy2Speed, playerSpeedPowered, ghostSpeedFrightened, pacPowerSeconds, numFlashes, intermissionNumber,
					huntingTimesIndex);
		}
	}

	protected static int checkGhostID(int id) {
		if (id < 0 || id > 3) {
			throw new IllegalArgumentException("Illegal ghost ID: %d".formatted(id));
		}
		return id;
	}

	private final GameModel game;
	private final int number;
	private final Pac pac;
	private final Ghost[] theGhosts;
	private final World world;
	private final Pulse energizerPulse;
	private final Bonus bonus;
	private final TickTimer huntingTimer;
	private final GhostHouseRules houseRules;
	private final Parameters params;

	private final Memory memo = new Memory();
	private int huntingPhase;
	private int numGhostsKilledInLevel;
	private int numGhostsKilledByEnergizer;
	private byte cruiseElroyState;

	public GameLevel(GameModel game, int number, Pac pac, Ghost[] theGhosts, World world, Bonus bonus,
			GhostHouseRules houseRules, Parameters params) {
		this.game = game;
		this.number = number;
		this.pac = pac;
		this.theGhosts = theGhosts;
		this.world = world;
		this.energizerPulse = new Pulse(10, true);
		this.bonus = bonus;
		this.huntingTimer = new TickTimer("HuntingTimer-level-%d".formatted(number));
		this.houseRules = houseRules;
		this.params = params;
	}

	/** @return Level number, starting with 1. */
	public int number() {
		return number;
	}

	public GameModel game() {
		return game;
	}

	public World world() {
		return world;
	}

	public Pulse energizerPulse() {
		return energizerPulse;
	}

	/**
	 * @param id ghost ID, one of {@link Ghost#ID_RED_GHOST}, {@link Ghost#ID_PINK_GHOST}, {@value Ghost#ID_CYAN_GHOST},
	 *           {@link Ghost#ID_ORANGE_GHOST}
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
		if (world instanceof ArcadeWorld arcadeWorld) {
			boolean blocked = dir == Direction.UP && ghost.is(HUNTING_PAC)
					&& arcadeWorld.upwardBlockedTiles().contains(ghost.tile());
			return !blocked;
		}
		return true;
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

	public Bonus bonus() {
		return bonus;
	}

	public TickTimer huntingTimer() {
		return huntingTimer;
	}

	public Parameters params() {
		return params;
	}

	/**
	 * Remembers what happens during a tick.
	 * 
	 * @return the memo
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
		LOGGER.trace("Cruise Elroy state set to %d", cruiseElroyState);
	}

	private void setCruiseElroyStateEnabled(boolean enabled) {
		if (enabled && cruiseElroyState < 0 || !enabled && cruiseElroyState > 0) {
			cruiseElroyState = (byte) (-cruiseElroyState);
			LOGGER.trace("Cruise Elroy state set to %d", cruiseElroyState);
		}
	}

	public int numGhostsKilledInLevel() {
		return numGhostsKilledInLevel;
	}

	public void setNumGhostsKilledInLevel(int number) {
		this.numGhostsKilledInLevel = number;
	}

	public int numGhostsKilledByEnergizer() {
		return numGhostsKilledByEnergizer;
	}

	public void setNumGhostsKilledByEnergizer(int number) {
		this.numGhostsKilledByEnergizer = number;
	}

	public void enter() {
		LOGGER.trace("Enter level %d (%s)", number, game.variant());
		world.assignGhostPositions(theGhosts);
		houseRules.resetPrivateGhostDotCounters();
		setCruiseElroyState(0);
		letsGetReadyToRumbleAndShowGuys(false);
	}

	public void update() {
		pac.update(this);
		checkIfGhostCanGetUnlocked();
		ghosts().forEach(ghost -> ghost.update(this));
		bonus.update(this);
		energizerPulse.animate();
		updateHunting();
	}

	public void exit() {
		LOGGER.trace("Exit level %d (%s)", number, game.variant());
		pac.rest(Integer.MAX_VALUE);
		pac.selectAndResetAnimation(AnimKeys.PAC_MUNCHING);
		ghosts().forEach(Ghost::hide);
		bonus.setInactive();
		energizerPulse.reset();
		huntingTimer.stop();
	}

	/**
	 * Hunting happens in different phases. Phases 0, 2, 4, 6 are scattering phases where the ghosts target for their
	 * respective corners and circle around the walls in their corner, phases 1, 3, 5, 7 are chasing phases where the
	 * ghosts attack Pac-Man.
	 * 
	 * @param phase hunting phase (0..7)
	 */
	public void startHuntingPhase(int phase) {
		if (phase < 0 || phase > 7) {
			throw new IllegalArgumentException("Hunting phase must be 0..7, but is " + phase);
		}
		this.huntingPhase = phase;
		huntingTimer.reset(huntingTicks(phase));
		huntingTimer.start();
		LOGGER.info("Started hunting phase %d (%s). %s", phase, currentHuntingPhaseName(), huntingTimer);
	}

	private long huntingTicks(int phase) {
		var durations = GameModel.HUNTING_DURATIONS[params.huntingTimesIndex];
		return durations[phase] == -1 ? TickTimer.INDEFINITE : durations[phase];
	}

	/**
	 * Advances the current hunting phase and enters the next phase when the current phase ends. On every change between
	 * phases, the living ghosts outside of the ghost house reverse their move direction.
	 */
	private void updateHunting() {
		huntingTimer.advance();
		if (huntingTimer.hasExpired()) {
			startHuntingPhase(huntingPhase + 1);
			// locked and house-leaving ghost will reverse as soon as he has left the house
			ghosts(HUNTING_PAC, LOCKED, LEAVING_HOUSE).forEach(Ghost::reverseDirectionASAP);
		}
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
		return huntingPhase % 2 == 0 ? OptionalInt.of(huntingPhase / 2) : OptionalInt.empty();
	}

	/**
	 * @return (optional) number of current chasing phase <code>(0-3)</code>
	 */
	public OptionalInt chasingPhase() {
		return huntingPhase % 2 == 1 ? OptionalInt.of(huntingPhase / 2) : OptionalInt.empty();
	}

	public String currentHuntingPhaseName() {
		return huntingPhase % 2 == 0 ? "Scattering" : "Chasing";
	}

	/**
	 * Pac-Man and the ghosts are placed at their initial positions and locks them. Also the power timer and energizers
	 * are reset.
	 * 
	 * @param guysVisible if the guys are visible when ready
	 */
	public void letsGetReadyToRumbleAndShowGuys(boolean guysVisible) {
		pac.reset();
		pac.setPosition(world.pacStartPosition());
		pac.setMoveAndWishDir(Direction.LEFT);
		var initialDirs = List.of(Direction.LEFT, Direction.DOWN, Direction.UP, Direction.UP);
		ghosts().forEach(ghost -> {
			ghost.reset();
			ghost.setPosition(ghost.homePosition());
			ghost.setMoveAndWishDir(initialDirs.get(ghost.id()));
			ghost.enterStateLocked();
		});
		guys().forEach(guy -> guy.setVisible(guysVisible));
		bonus.setInactive();
		energizerPulse().reset();
	}

	private void checkIfBlinkyBecomesCruiseElroy() {
		var foodRemaining = world.foodRemaining();
		if (foodRemaining == params.elroy1DotsLeft()) {
			setCruiseElroyState(1);
		} else if (foodRemaining == params.elroy2DotsLeft()) {
			setCruiseElroyState(2);
		}
	}

	private void checkIfGhostCanGetUnlocked() {
		houseRules.checkIfGhostUnlocked(this).ifPresent(unlock -> {
			memo.unlockedGhost = Optional.of(unlock.ghost());
			memo.unlockReason = unlock.reason();
			LOGGER.trace("Unlocked %s: %s", unlock.ghost().name(), unlock.reason());
			if (unlock.ghost().id() == ID_ORANGE_GHOST && cruiseElroyState < 0) {
				// Blinky's "cruise elroy" state is re-enabled when orange ghost is unlocked
				setCruiseElroyStateEnabled(true);
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
		setNumGhostsKilledByEnergizer(0);
		killGhosts(prey);
	}

	private void killGhosts(List<Ghost> prey) {
		prey.forEach(this::killGhost);
		setNumGhostsKilledInLevel(numGhostsKilledInLevel() + prey.size());
		if (numGhostsKilledInLevel() == 16) {
			game.scorePoints(GameModel.POINTS_ALL_GHOSTS_KILLED);
			LOGGER.trace("All ghosts killed at level %d, %s wins %d points", number, pac.name(),
					GameModel.POINTS_ALL_GHOSTS_KILLED);
		}
	}

	private void killGhost(Ghost ghost) {
		ghost.setKilledIndex(numGhostsKilledByEnergizer());
		ghost.enterStateEaten();
		setNumGhostsKilledByEnergizer(numGhostsKilledByEnergizer() + 1);
		memo.killedGhosts.add(ghost);
		int points = GameModel.POINTS_GHOSTS_SEQUENCE[ghost.killedIndex()];
		game.scorePoints(points);
		LOGGER.trace("%s killed at tile %s, %s wins %d points", ghost.name(), ghost.tile(), pac.name(), points);
	}

	// Pac-Man

	private void onPacMeetsKiller() {
		pac.kill();
		houseRules.resetGlobalDotCounterAndSetEnabled(true);
		setCruiseElroyStateEnabled(false);
		LOGGER.trace("%s died at tile %s", pac.name(), pac.tile());
	}

	private void onPacPowerBegin() {
		LOGGER.trace("%s power begins", pac.name());
		huntingTimer().stop();
		pac.powerTimer().restartSeconds(params().pacPowerSeconds());
		LOGGER.trace("Timer started: %s", pac.powerTimer());
		ghosts(HUNTING_PAC).forEach(Ghost::enterStateFrightened);
		ghosts(FRIGHTENED).forEach(Ghost::reverseDirectionASAP);
		GameEvents.publish(GameEventType.PAC_GETS_POWER, pac.tile());
	}

	private void onPacPowerEnd() {
		LOGGER.trace("%s power ends", pac.name());
		huntingTimer().start();
		pac.powerTimer().stop();
		pac.powerTimer().resetIndefinitely();
		LOGGER.trace("Timer stopped: %s", pac.powerTimer());
		ghosts(FRIGHTENED).forEach(Ghost::enterStateHuntingPac);
		GameEvents.publish(GameEventType.PAC_LOSES_POWER, pac.tile());
	}

	// Food

	public void checkIfPacFindsFood() {
		var tile = pac.tile();
		if (world.containsFood(tile)) {
			memo.foodFoundTile = Optional.of(tile);
			memo.lastFoodFound = world.foodRemaining() == 1;
			memo.energizerFound = world.isEnergizerTile(tile);
			memo.pacPowered = memo.energizerFound && params().pacPowerSeconds() > 0;
			memo.bonusReached = world.eatenFoodCount() == GameModel.PELLETS_EATEN_BONUS1
					|| world.eatenFoodCount() == GameModel.PELLETS_EATEN_BONUS2;
			onFoodFound(tile);
		} else {
			pac.starve();
		}
		if (memo.bonusReached) {
			game.onBonusReached();
		}
	}

	private void onFoodFound(Vector2i tile) {
		world.removeFood(tile);
		pac.endStarving();
		if (memo.energizerFound) {
			setNumGhostsKilledByEnergizer(0);
			pac.rest(GameModel.RESTING_TICKS_ENERGIZER);
			game.scorePoints(GameModel.POINTS_ENERGIZER);
		} else {
			pac.rest(GameModel.RESTING_TICKS_NORMAL_PELLET);
			game.scorePoints(GameModel.POINTS_NORMAL_PELLET);
		}
		checkIfBlinkyBecomesCruiseElroy();
		houseRules.updateGhostDotCounters(this);
		GameEvents.publish(GameEventType.PAC_FINDS_FOOD, tile);
	}
}
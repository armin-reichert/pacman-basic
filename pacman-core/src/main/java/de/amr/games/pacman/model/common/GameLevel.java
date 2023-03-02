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

import static de.amr.games.pacman.event.GameEvents.publishGameEvent;
import static de.amr.games.pacman.event.GameEvents.publishGameEventOfType;
import static de.amr.games.pacman.event.GameEvents.publishSoundEvent;
import static de.amr.games.pacman.lib.steering.Direction.UP;
import static de.amr.games.pacman.model.common.GameModel.checkGhostID;
import static de.amr.games.pacman.model.common.actors.Ghost.ID_CYAN_GHOST;
import static de.amr.games.pacman.model.common.actors.Ghost.ID_ORANGE_GHOST;
import static de.amr.games.pacman.model.common.actors.Ghost.ID_PINK_GHOST;
import static de.amr.games.pacman.model.common.actors.Ghost.ID_RED_GHOST;
import static de.amr.games.pacman.model.common.actors.GhostState.FRIGHTENED;
import static de.amr.games.pacman.model.common.actors.GhostState.HUNTING_PAC;
import static de.amr.games.pacman.model.common.actors.GhostState.LEAVING_HOUSE;
import static de.amr.games.pacman.model.common.actors.GhostState.LOCKED;
import static java.util.function.Predicate.not;

import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.amr.games.pacman.controller.common.Steering;
import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.lib.anim.AnimKeys;
import de.amr.games.pacman.lib.anim.EntityAnimation;
import de.amr.games.pacman.lib.math.Vector2i;
import de.amr.games.pacman.lib.steering.Direction;
import de.amr.games.pacman.lib.timer.TickTimer;
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

	private static final Logger LOG = LogManager.getFormatterLogger();

	/**
	 * Simulates the overflow bug from the original Arcade version.
	 * 
	 * @param guy a creature
	 * @param n   number of tiles
	 * @return tile that is located given number of tiles ahead creature (towards move direction). In case creature looks
	 *         up, additional n tiles are added towards left which simulates an overflow error in the Arcade game version
	 */
	protected static Vector2i tilesAhead(Creature guy, int n) {
		var ahead = guy.tile().plus(guy.moveDir().vector().scaled(n));
		return guy.moveDir() == UP ? ahead.minus(n, 0) : ahead;
	}

	/**
	 * Individual level parameters.
	 */
	public record Parameters(
	//@formatter:off
		/** Relative Pac-Man speed in this level. */
		float pacSpeed,
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
		/** Relative speed of Pac-Man in power mode. */
		float pacSpeedPowered,
		/** Relative speed of frightened ghost. */
		float ghostSpeedFrightened,
		/** Number of seconds Pac-Man gets power int this level. */
		int pacPowerSeconds,
		/** Number of maze flashes at end of this level. */
		int numFlashes,
		/** Number of intermission scene played after this level (1, 2, 3, 0 = no intermission). */
		int intermissionNumber)
	//@formatter:on
	{
		public static Parameters createFromData(byte[] data) {
			//@formatter:off
			float pacSpeed             = data[0] / 100f;
			float ghostSpeed           = data[1] / 100f;
			float ghostSpeedTunnel     = data[2] / 100f;
			byte elroy1DotsLeft        = data[3];
			float elroy1Speed          = data[4] / 100f;
			byte elroy2DotsLeft        = data[5];
			float elroy2Speed          = data[6] / 100f;
			float pacSpeedPowered      = data[7] / 100f;
			float ghostSpeedFrightened = data[8] / 100f;
			byte pacPowerSeconds       = data[9];
			byte numFlashes            = data[10];
			byte intermissionNumber    = data[11];
			//@formatter:on
			return new Parameters(pacSpeed, ghostSpeed, ghostSpeedTunnel, elroy1DotsLeft, elroy1Speed, elroy2DotsLeft,
					elroy2Speed, pacSpeedPowered, ghostSpeedFrightened, pacPowerSeconds, numFlashes, intermissionNumber);
		}
	}

	private final GameModel game;
	private final int number;
	private final TickTimer huntingTimer = new TickTimer("HuntingTimer");
	private final Memory memo = new Memory();
	private final World world;
	private final Pac pac;
	private final Ghost[] ghosts;
	private final Bonus bonus;
	private final Parameters params;
	private final int[] huntingDurations;
	private final GhostHouseRules houseRules;
	private Steering pacSteering;
	private int huntingPhase;
	private int numGhostsKilledInLevel;
	private int numGhostsKilledByEnergizer;
	private byte cruiseElroyState;

	public GameLevel(GameModel game, int number) {
		this.game = Objects.requireNonNull(game);
		this.number = GameModel.checkLevelNumber(number);
		world = game.createWorld(number);
		pac = game.createPac();
		ghosts = game.createGhosts();
		bonus = game.createBonus(number);
		houseRules = game.createHouseRules(number);
		huntingDurations = game.huntingDurations(number);
		params = game.levelParameters(number);

		// Red ghost attacks Pac-Man directly
		ghost(ID_RED_GHOST).setChasingTarget(pac::tile);
		// Pink ghost ambushes Pac-Man
		ghost(ID_PINK_GHOST).setChasingTarget(() -> tilesAhead(pac, 4));
		// Cyan ghost attacks from opposite side than red ghost
		ghost(ID_CYAN_GHOST).setChasingTarget(() -> tilesAhead(pac, 2).scaled(2).minus(ghost(ID_RED_GHOST).tile()));
		// Orange ghost attacks directly but retreats if too near
		ghost(ID_ORANGE_GHOST).setChasingTarget( //
				() -> ghost(ID_ORANGE_GHOST).tile().euclideanDistance(pac.tile()) < 8 ? //
						world.ghostScatterTargetTile(ID_ORANGE_GHOST) : pac.tile());

		LOG.trace("Game level %d created. (%s)", number, game.variant());
	}

	public void update() {
		memo.forgetEverything(); // ich scholze jetzt
		world.animation(ArcadeWorld.ENERGIZER_PULSE).ifPresent(EntityAnimation::animate);
		pac.update(this);
		checkIfGhostCanGetUnlocked();
		ghosts().forEach(ghost -> ghost.update(this));
		updateHunting();
		bonus.update(this);
		checkIfPacFoundFood();
		checkPacPower();
		checkIfPacManGetsKilled();
		findEdibleGhosts();
	}

	public void exit() {
		LOG.trace("Exit level %d (%s)", number, game.variant());
		pac.rest(Pac.REST_INDEFINITE);
		pac.selectAndResetAnimation(AnimKeys.PAC_MUNCHING);
		ghosts().forEach(Ghost::hide);
		bonus.setInactive();
		world.animation(ArcadeWorld.ENERGIZER_PULSE).ifPresent(EntityAnimation::reset);
		huntingTimer.stop();
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
		return ghosts[checkGhostID(id)];
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

	public Parameters params() {
		return params;
	}

	public TickTimer huntingTimer() {
		return huntingTimer;
	}

	public GhostHouseRules houseRules() {
		return houseRules;
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
		Objects.requireNonNull(ghost);
		Objects.requireNonNull(dir);
		if (world instanceof ArcadeWorld arcadeWorld) {
			boolean blocked = dir == Direction.UP && ghost.is(HUNTING_PAC)
					&& arcadeWorld.upwardBlockedTiles().contains(ghost.tile());
			return !blocked;
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
	public void startHuntingPhase(int phase) {
		if (phase < 0 || phase > 7) {
			throw new IllegalArgumentException("Hunting phase must be 0..7, but is " + phase);
		}
		this.huntingPhase = phase;
		huntingTimer.reset(huntingTicks(phase));
		huntingTimer.start();
		LOG.info("Hunting phase %d (%s) starts. %s", phase, currentHuntingPhaseName(), huntingTimer);
	}

	private long huntingTicks(int phase) {
		return huntingDurations[phase] == -1 ? TickTimer.INDEFINITE : huntingDurations[phase];
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
	 * Pac-Man and the ghosts are placed at their initial positions and locked. Also the bonus, Pac-Man power timer and
	 * energizer pulse are reset.
	 * 
	 * @param guysVisible if the guys are made visible
	 */
	public void letsGetReadyToRumbleAndShowGuys(boolean guysVisible) {
		pac.reset();
		pac.setPosition(world.pacInitialPosition());
		pac.setMoveAndWishDir(world.pacInitialDirection());
		pac.setVisible(guysVisible);
		ghosts().forEach(ghost -> {
			ghost.reset();
			ghost.setPosition(world.ghostInitialPosition(ghost.id()));
			ghost.setMoveAndWishDir(world.ghostInitialDirection(ghost.id()));
			ghost.setVisible(guysVisible);
			ghost.enterStateLocked();
		});
		bonus.setInactive();
		world.animation(ArcadeWorld.ENERGIZER_PULSE).ifPresent(EntityAnimation::reset);
	}

	/**
	 * @param ghost a ghost
	 * @return relative speed of ghost when hunting
	 */
	public float huntingSpeed(Ghost ghost) {
		if (world.isTunnel(ghost.tile())) {
			return params.ghostSpeedTunnel();
		} else if (ghost.id() == ID_RED_GHOST && cruiseElroyState == 1) {
			return params.elroy1Speed();
		} else if (ghost.id() == ID_RED_GHOST && cruiseElroyState == 2) {
			return params.elroy2Speed();
		} else {
			return params.ghostSpeed();
		}
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
			LOG.trace("Unlocked %s: %s", unlock.ghost().name(), unlock.reason());
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
		memo.pacKilled = pac.isMeetingKiller(this);
	}

	public void onPacKilled() {
		pac.die();
		houseRules.resetGlobalDotCounterAndSetEnabled(true);
		setCruiseElroyStateEnabled(false);
		LOG.trace("%s died at tile %s", pac.name(), pac.tile());
	}

	private void checkPacPower() {
		memo.pacPowerFading = pac.powerTimer().remaining() == GameModel.TICKS_PAC_POWER_FADES;
		memo.pacPowerLost = pac.powerTimer().hasExpired();
		if (memo.pacPowerGained) {
			onPacPowerStarts();
			publishGameEventOfType(GameEventType.PAC_GETS_POWER);
			publishSoundEvent("pacman_power_starts");
		} else if (memo.pacPowerFading) {
			publishGameEventOfType(GameEventType.PAC_STARTS_LOSING_POWER);
		} else if (memo.pacPowerLost) {
			onPacPowerEnds();
			publishGameEventOfType(GameEventType.PAC_LOSES_POWER);
			publishSoundEvent("pacman_power_ends");
		}
	}

	private void onPacPowerStarts() {
		LOG.trace("%s power begins", pac.name());
		huntingTimer.stop();
		pac.powerTimer().restartSeconds(params().pacPowerSeconds());
		LOG.trace("Timer started: %s", pac.powerTimer());
		ghosts(HUNTING_PAC).forEach(Ghost::enterStateFrightened);
		ghosts(FRIGHTENED).forEach(Ghost::reverseDirectionASAP);
	}

	private void onPacPowerEnds() {
		LOG.trace("%s power ends", pac.name());
		huntingTimer.start();
		pac.powerTimer().stop();
		pac.powerTimer().resetIndefinitely();
		LOG.trace("Timer stopped: %s", pac.powerTimer());
		ghosts(FRIGHTENED).forEach(Ghost::enterStateHuntingPac);
	}

	// Food

	public boolean completed() {
		return memo.lastFoodFound;
	}

	public void checkIfPacFoundFood() {
		var tile = pac.tile();
		if (world.containsFood(tile)) {
			memo.foodFoundTile = Optional.of(tile);
			memo.lastFoodFound = world.foodRemaining() == 1;
			memo.energizerFound = world.isEnergizerTile(tile);
			memo.pacPowerGained = memo.energizerFound && params().pacPowerSeconds() > 0;
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
			numGhostsKilledByEnergizer = 0;
			pac.rest(GameModel.RESTING_TICKS_ENERGIZER);
			game.scorePoints(GameModel.POINTS_ENERGIZER);
		} else {
			pac.rest(GameModel.RESTING_TICKS_NORMAL_PELLET);
			game.scorePoints(GameModel.POINTS_NORMAL_PELLET);
		}
		checkIfBlinkyBecomesCruiseElroy();
		houseRules.updateGhostDotCounters(this);
		publishGameEvent(GameEventType.PAC_FINDS_FOOD, tile);
		publishSoundEvent("pacman_found_food");
	}

	/**
	 * Called by cheat action.
	 */
	public void removeAllPellets() {
		world.tiles().filter(not(world::isEnergizerTile)).forEach(world::removeFood);
		publishGameEventOfType(GameEventType.PAC_FINDS_FOOD);
	}
}
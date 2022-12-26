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

import static de.amr.games.pacman.model.common.actors.Ghost.ID_ORANGE_GHOST;
import static de.amr.games.pacman.model.common.actors.Ghost.ID_RED_GHOST;
import static de.amr.games.pacman.model.common.actors.GhostState.FRIGHTENED;
import static de.amr.games.pacman.model.common.actors.GhostState.HUNTING_PAC;
import static de.amr.games.pacman.model.common.actors.GhostState.LEAVING_HOUSE;
import static de.amr.games.pacman.model.common.actors.GhostState.LOCKED;

import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.event.GameEvents;
import de.amr.games.pacman.lib.anim.Pulse;
import de.amr.games.pacman.lib.math.Vector2i;
import de.amr.games.pacman.lib.steering.Direction;
import de.amr.games.pacman.lib.timer.TickTimer;
import de.amr.games.pacman.model.common.actors.Bonus;
import de.amr.games.pacman.model.common.actors.Creature;
import de.amr.games.pacman.model.common.actors.Ghost;
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
		int numFlashes)
	//@formatter:on
	{
	}

	/** Remembers what happens during a tick. */
	public final Memory memo = new Memory();

	private final int number;
	private final GameModel game;
	private final World world;
	private final Pulse energizerPulse;
	private final Bonus bonus;
	private final TickTimer huntingTimer;
	private final int[] huntingDurations;
	private final GhostHouseRules houseRules;
	private final Parameters params;
	private int huntingPhase;
	private int numGhostsKilledInLevel;
	private int numGhostsKilledByEnergizer;

	public GameLevel(int levelNumber, GameModel game, World world, Bonus bonus, int[] huntingDurations,
			GhostHouseRules houseRules, byte[] data) {
		this.number = levelNumber;
		this.game = game;
		this.world = world;
		this.energizerPulse = new Pulse(10, true);
		this.bonus = bonus;
		this.huntingDurations = huntingDurations;
		this.huntingTimer = new TickTimer("HuntingTimer-level-%d".formatted(levelNumber));
		this.houseRules = houseRules;

		//@formatter:off
		float playerSpeed          = percentage(data[0]);
		float ghostSpeed           = percentage(data[1]);
		float ghostSpeedTunnel     = percentage(data[2]);
		int elroy1DotsLeft         = data[3];
		float elroy1Speed          = percentage(data[4]);
		int elroy2DotsLeft         = data[5];
		float elroy2Speed          = percentage(data[6]);
		float playerSpeedPowered   = percentage(data[7]);
		float ghostSpeedFrightened = percentage(data[8]);
		int pacPowerSeconds        = data[9];
		int numFlashes             = data[10];
		//@formatter:on

		params = new Parameters(playerSpeed, ghostSpeed, ghostSpeedTunnel, elroy1DotsLeft, elroy1Speed, elroy2DotsLeft,
				elroy2Speed, playerSpeedPowered, ghostSpeedFrightened, pacPowerSeconds, numFlashes);
	}

	/** Number of level, starts with 1. */
	public int number() {
		return number;
	}

	public GameModel game() {
		return game;
	}

	/** World used in this level. */
	public World world() {
		return world;
	}

	public Pulse energizerPulse() {
		return energizerPulse;
	}

	/** Bonus used in this level. */
	public Bonus bonus() {
		return bonus;
	}

	public TickTimer huntingTimer() {
		return huntingTimer;
	}

	/** Ghost house rules in this level */
	public GhostHouseRules houseRules() {
		return houseRules;
	}

	/** Parameters in this level */
	public Parameters params() {
		return params;
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

	private static float percentage(int value) {
		return value / 100f;
	}

	public void update() {
		game.pac.update(this);
		checkIfGhostCanGetUnlocked();
		game.ghosts().forEach(ghost -> ghost.update(this));
		bonus.update(this);
		energizerPulse.animate();
		advanceHunting(game);
	}

	public void enter() {
		LOGGER.trace("Enter level %d (%s)", number, game.variant());
		world.assignGhostPositions(game.theGhosts);
		houseRules().resetPrivateGhostDotCounters();
		letsGetReadyToRumble();
	}

	public void exit() {
		LOGGER.trace("Exit level %d (%s)", number, game.variant());
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
		var ticks = huntingDurations[phase] == -1 ? TickTimer.INDEFINITE : huntingDurations[phase];
		LOGGER.info("Start hunting phase %d (%s). %s", phase, currentHuntingPhaseName(), huntingTimer);
		huntingTimer.reset(ticks);
		huntingTimer.start();
	}

	/**
	 * Advances the current hunting phase and enters the next phase when the current phase ends. On every change between
	 * phases, the living ghosts outside of the ghost house reverse their move direction.
	 */
	private void advanceHunting(GameModel game) {
		huntingTimer.advance();
		if (huntingTimer.hasExpired()) {
			startHuntingPhase(huntingPhase + 1);
			// locked and house-leaving ghost will reverse as soon as he has left the house
			game.ghosts(HUNTING_PAC, LOCKED, LEAVING_HOUSE).forEach(Ghost::reverseDirectionASAP);
		}
	}

	/**
	 * @return number of current phase <code>(0-7)
	 */
	public int huntingPhase() {
		return huntingPhase;
	}

	/**
	 * @return number of current scattering phase <code>(0-4)</code> or <code>-1</code> if currently chasing
	 */
	public int scatterPhaseIndex() {
		return huntingPhase % 2 == 0 ? huntingPhase / 2 : -1;
	}

	/**
	 * @return number of current chasing phase <code>(0-4)</code> or <code>-1</code> if currently scattering
	 */
	public int chasingPhaseIndex() {
		return huntingPhase % 2 == 1 ? huntingPhase / 2 : -1;
	}

	public String currentHuntingPhaseName() {
		return huntingPhase % 2 == 0 ? "Scattering" : "Chasing";
	}

	public boolean inChasingPhase() {
		return chasingPhaseIndex() != -1;
	}

	public boolean inScatterPhase() {
		return scatterPhaseIndex() != -1;
	}

	/**
	 * Sets the game state to be ready for playing. Pac-Man and the ghosts are placed at their initial positions, made
	 * visible and their state is initialized. Also the power timer and energizers are reset.
	 */
	public void letsGetReadyToRumble() {
		game.pac.reset();
		game.pac.setPosition(world.pacStartPosition());
		game.pac.setMoveAndWishDir(Direction.LEFT);
		var initialDirs = List.of(Direction.LEFT, Direction.DOWN, Direction.UP, Direction.UP);
		game.ghosts().forEach(ghost -> {
			ghost.reset();
			ghost.setPosition(ghost.homePosition());
			ghost.setMoveAndWishDir(initialDirs.get(ghost.id()));
			ghost.enterStateLocked();
		});
		game.guys().forEach(Creature::hide);
		bonus.setInactive();
		energizerPulse().reset();
	}

	public void checkIfGhostBecomesCruiseElroy(Ghost ghost) {
		var foodRemaining = world.foodRemaining();
		if (foodRemaining == params.elroy1DotsLeft()) {
			ghost.setCruiseElroyState(1);
		} else if (foodRemaining == params.elroy2DotsLeft()) {
			ghost.setCruiseElroyState(2);
		}
	}

	private void checkIfGhostCanGetUnlocked() {
		Ghost redGhost = game.ghost(ID_RED_GHOST);
		houseRules().checkIfGhostUnlocked(this).ifPresent(unlock -> {
			memo.unlockedGhost = Optional.of(unlock.ghost());
			memo.unlockReason = unlock.reason();
			LOGGER.trace("Unlocked %s: %s", unlock.ghost().name(), unlock.reason());
			if (unlock.ghost().id() == ID_ORANGE_GHOST && redGhost.cruiseElroyState() < 0) {
				// Blinky's "cruise elroy" state is re-enabled when orange ghost is unlocked
				redGhost.setCruiseElroyStateEnabled(true);
			}
		});
	}

	public void checkHowTheGuysAreDoing() {
		if (memo.pacPowered) {
			onPacPowerBegin();
		}

		memo.pacMetKiller = game.pac.isMeetingKiller(this);
		if (memo.pacMetKiller) {
			onPacMeetsKiller();
			return; // enter new game state
		}

		memo.edibleGhosts = game.ghosts(FRIGHTENED).filter(game.pac::sameTile).toList();
		if (!memo.edibleGhosts.isEmpty()) {
			killGhosts(memo.edibleGhosts);
			memo.ghostsKilled = true;
			return; // enter new game state
		}

		memo.pacPowerFading = game.pac.powerTimer().remaining() == game.pac.powerFadingTicks();
		memo.pacPowerLost = game.pac.powerTimer().hasExpired();
		if (memo.pacPowerFading) {
			GameEvents.publish(GameEventType.PAC_STARTS_LOSING_POWER, game.pac.tile());
		}
		if (memo.pacPowerLost) {
			onPacPowerEnd();
		}
	}

	public void killAllPossibleGhosts() {
		var prey = game.ghosts(HUNTING_PAC, FRIGHTENED).toList();
		setNumGhostsKilledByEnergizer(0);
		killGhosts(prey);
	}

	private void killGhosts(List<Ghost> prey) {
		prey.forEach(this::killGhost);
		setNumGhostsKilledInLevel(numGhostsKilledInLevel() + prey.size());
		if (numGhostsKilledInLevel() == 16) {
			game.scorePoints(GameModel.POINTS_ALL_GHOSTS_KILLED);
			LOGGER.trace("All ghosts killed at level %d, %s wins %d points", number, game.pac.name(),
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
		LOGGER.trace("%s killed at tile %s, %s wins %d points", ghost.name(), ghost.tile(), game.pac.name(), points);
	}

	// Pac-Man

	private void onPacMeetsKiller() {
		game.pac.kill();
		houseRules().resetGlobalDotCounterAndSetEnabled(true);
		game.ghost(ID_RED_GHOST).setCruiseElroyStateEnabled(false);
		LOGGER.trace("%s died at tile %s", game.pac.name(), game.pac.tile());
	}

	private void onPacPowerBegin() {
		LOGGER.trace("%s power begins", game.pac.name());
		huntingTimer().stop();
		game.pac.powerTimer().restartSeconds(params().pacPowerSeconds());
		LOGGER.trace("Timer started: %s", game.pac.powerTimer());
		game.ghosts(HUNTING_PAC).forEach(ghost -> ghost.enterStateFrightened());
		game.ghosts(FRIGHTENED).forEach(Ghost::reverseDirectionASAP);
		GameEvents.publish(GameEventType.PAC_GETS_POWER, game.pac.tile());
	}

	private void onPacPowerEnd() {
		LOGGER.trace("%s power ends", game.pac.name());
		huntingTimer().start();
		game.pac.powerTimer().stop();
		game.pac.powerTimer().resetIndefinitely();
		LOGGER.trace("Timer stopped: %s", game.pac.powerTimer());
		game.ghosts(FRIGHTENED).forEach(ghost -> ghost.enterStateHuntingPac());
		GameEvents.publish(GameEventType.PAC_LOSES_POWER, game.pac.tile());
	}

	// Food

	public void checkIfPacFindsFood() {
		var tile = game.pac.tile();
		if (world.containsFood(tile)) {
			memo.foodFoundTile = Optional.of(tile);
			memo.lastFoodFound = world.foodRemaining() == 1;
			memo.energizerFound = world.isEnergizerTile(tile);
			memo.pacPowered = memo.energizerFound && params().pacPowerSeconds() > 0;
			memo.bonusReached = world.eatenFoodCount() == GameModel.PELLETS_EATEN_BONUS1
					|| world.eatenFoodCount() == GameModel.PELLETS_EATEN_BONUS2;
			onFoodFound(tile);
		} else {
			game.pac.starve();
		}
	}

	private void onFoodFound(Vector2i tile) {
		world.removeFood(tile);
		game.pac.endStarving();
		if (memo.energizerFound) {
			setNumGhostsKilledByEnergizer(0);
			game.pac.rest(GameModel.RESTING_TICKS_ENERGIZER);
			game.scorePoints(GameModel.POINTS_ENERGIZER);
		} else {
			game.pac.rest(GameModel.RESTING_TICKS_NORMAL_PELLET);
			game.scorePoints(GameModel.POINTS_NORMAL_PELLET);
		}
		if (memo.bonusReached) {
			game.onBonusReached(bonus);
		}
		checkIfGhostBecomesCruiseElroy(game.ghost(ID_RED_GHOST));
		houseRules().updateGhostDotCounters(this);
		GameEvents.publish(GameEventType.PAC_FINDS_FOOD, tile);
	}

}
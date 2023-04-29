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

package de.amr.games.pacman.model;

import static de.amr.games.pacman.lib.Globals.checkGameNotNull;
import static de.amr.games.pacman.lib.Globals.checkGhostID;
import static de.amr.games.pacman.lib.Globals.checkLevelNumber;
import static de.amr.games.pacman.lib.Globals.checkNotNull;
import static de.amr.games.pacman.lib.Globals.isEven;
import static de.amr.games.pacman.lib.Globals.isOdd;
import static de.amr.games.pacman.lib.Globals.percent;
import static de.amr.games.pacman.lib.Globals.v2i;
import static de.amr.games.pacman.model.actors.GhostState.FRIGHTENED;
import static de.amr.games.pacman.model.actors.GhostState.HUNTING_PAC;
import static de.amr.games.pacman.model.actors.GhostState.LEAVING_HOUSE;
import static de.amr.games.pacman.model.actors.GhostState.LOCKED;
import static de.amr.games.pacman.model.world.World.halfTileRightOf;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Stream;

import org.tinylog.Logger;

import de.amr.games.pacman.controller.Steering;
import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.event.GameEvents;
import de.amr.games.pacman.lib.anim.Animated;
import de.amr.games.pacman.lib.math.Vector2i;
import de.amr.games.pacman.lib.steering.Direction;
import de.amr.games.pacman.lib.timer.TickTimer;
import de.amr.games.pacman.model.actors.Creature;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.GhostState;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.model.world.World;

/**
 * @author Armin Reichert
 */
public class GameLevel {

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

	private final BonusManagement bonusManagement;

	private final GhostHouseManagement ghostHouseManagement;

	/** 1-based level number. */
	private final int number;

	private final boolean demoLevel;

	private final TickTimer huntingTimer = new TickTimer("HuntingTimer");

	/** Memorizes what happens during a frame. */
	private final Memory memo = new Memory();

	private final World world;

	private final Pac pac;

	private final Ghost[] ghosts;

	private Steering pacSteering;

	private int huntingPhase;

	private int numGhostsKilledInLevel;

	private int numGhostsKilledByEnergizer;

	private byte cruiseElroyState;

	public GameLevel(GameModel game, World world, int number, byte[] data, boolean demoLevel) {
		checkGameNotNull(game);
		checkNotNull(world);
		checkLevelNumber(number);

		this.game = game;
		this.world = world;
		this.number = number;
		this.demoLevel = demoLevel;

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

		pac = new Pac(game.variant() == GameVariant.MS_PACMAN ? "Ms. Pac-Man" : "Pac-Man");

		ghosts = new Ghost[] { //
				new Ghost(GameModel.RED_GHOST, "Blinky"), //
				new Ghost(GameModel.PINK_GHOST, "Pinky"), //
				new Ghost(GameModel.CYAN_GHOST, "Inky"), //
				new Ghost(GameModel.ORANGE_GHOST, game.variant() == GameVariant.MS_PACMAN ? "Sue" : "Clyde") //
		};

		// Blinky: attacks Pac-Man directly
		ghosts[GameModel.RED_GHOST].setInitialDirection(Direction.LEFT);
		ghosts[GameModel.RED_GHOST].setInitialPosition(world.house().door().entryPosition());
		ghosts[GameModel.RED_GHOST].setRevivalPosition(world.house().seatPosition(1));
		ghosts[GameModel.RED_GHOST].setScatterTile(v2i(25, 0));
		ghosts[GameModel.RED_GHOST].setChasingTarget(pac::tile);

		// Pinky: ambushes Pac-Man
		ghosts[GameModel.PINK_GHOST].setInitialDirection(Direction.DOWN);
		ghosts[GameModel.PINK_GHOST].setInitialPosition(world.house().seatPosition(1));
		ghosts[GameModel.PINK_GHOST].setRevivalPosition(world.house().seatPosition(1));
		ghosts[GameModel.PINK_GHOST].setScatterTile(v2i(2, 0));
		ghosts[GameModel.PINK_GHOST].setChasingTarget(() -> pac.tilesAheadBuggy(4));

		// Inky: attacks from opposite side as Blinky
		ghosts[GameModel.CYAN_GHOST].setInitialDirection(Direction.UP);
		ghosts[GameModel.CYAN_GHOST].setInitialPosition(world.house().seatPosition(0));
		ghosts[GameModel.CYAN_GHOST].setRevivalPosition(world.house().seatPosition(0));
		ghosts[GameModel.CYAN_GHOST].setScatterTile(v2i(27, 34));
		ghosts[GameModel.CYAN_GHOST]
				.setChasingTarget(() -> pac.tilesAheadBuggy(2).scaled(2).minus(ghosts[GameModel.RED_GHOST].tile()));

		// Clyde/Sue: attacks directly but retreats if Pac is near
		ghosts[GameModel.ORANGE_GHOST].setInitialDirection(Direction.UP);
		ghosts[GameModel.ORANGE_GHOST].setInitialPosition(world.house().seatPosition(2));
		ghosts[GameModel.ORANGE_GHOST].setRevivalPosition(world.house().seatPosition(2));
		ghosts[GameModel.ORANGE_GHOST].setScatterTile(v2i(0, 34));
		ghosts[GameModel.ORANGE_GHOST]
				.setChasingTarget(() -> ghosts[GameModel.ORANGE_GHOST].tile().euclideanDistance(pac.tile()) < 8 //
						? ghosts[GameModel.ORANGE_GHOST].scatterTile()
						: pac.tile());

		bonusManagement = new BonusManagement(this);
		bonusManagement.onLevelStart();

		ghostHouseManagement = new GhostHouseManagement(this);

		Logger.trace("Game level {} created. ({})", number, game.variant());
	}

	public void exit() {
		Logger.trace("Exit level {} ({})", number, game.variant());
		pac.rest(Pac.REST_FOREVER);
		pac.selectAndResetAnimation(GameModel.AK_PAC_MUNCHING);
		ghosts().forEach(Ghost::hide);
		bonusManagement.onLevelEnd();
		world.animation(GameModel.AK_MAZE_ENERGIZER_BLINKING).ifPresent(Animated::reset);
		stopHuntingTimer();
	}

	public GameModel game() {
		return game;
	}

	public byte symbol() {
		return bonusManagement.bonusInfo(0).symbol();
	}

	public BonusManagement bonusManagement() {
		return bonusManagement;
	}

	public TickTimer huntingTimer() {
		return huntingTimer;
	}

	public boolean isDemoLevel() {
		return demoLevel;
	}

	/** @return level number, starting with 1. */
	public int number() {
		return number;
	}

	public World world() {
		return world;
	}

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
	 * @param id ghost ID, one of {@link GameModel#RED_GHOST}, {@link GameModel#PINK_GHOST},
	 *           {@value GameModel#CYAN_GHOST}, {@link GameModel#ORANGE_GHOST}
	 * @return the ghost with the given ID
	 */
	public Ghost ghost(byte id) {
		checkGhostID(id);
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
		return Stream.of(pac, ghosts[GameModel.RED_GHOST], ghosts[GameModel.PINK_GHOST], ghosts[GameModel.CYAN_GHOST],
				ghosts[GameModel.ORANGE_GHOST]);
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
		Logger.trace("Cruise Elroy state set to {}", cruiseElroyState);
	}

	private void setCruiseElroyStateEnabled(boolean enabled) {
		if (enabled && cruiseElroyState < 0 || !enabled && cruiseElroyState > 0) {
			cruiseElroyState = (byte) (-cruiseElroyState);
			Logger.trace("Cruise Elroy state set to {}", cruiseElroyState);
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
		checkNotNull(ghost);
		checkNotNull(dir);
		if (upwardsBlockedTiles().isEmpty()) {
			return true;
		}
		// In the Pac-Man game, hunting ghosts cannot move upwards at specific tiles
		boolean upwardsBlocked = upwardsBlockedTiles().contains(ghost.tile());
		return dir != Direction.UP || !ghost.is(HUNTING_PAC) || !upwardsBlocked;
	}

	public List<Vector2i> upwardsBlockedTiles() {
		return switch (game.variant()) {
		case MS_PACMAN -> Collections.emptyList();
		case PACMAN -> GameModel.PACMAN_RED_ZONE;
		default -> throw new IllegalGameVariantException(game.variant());
		};
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
		var durations = game.huntingDurations(number);
		var ticks = durations[phase] == -1 ? TickTimer.INDEFINITE : durations[phase];
		huntingTimer.reset(ticks);
		huntingTimer.start();
		Logger.info("Hunting phase {} ({}, {} ticks / {} seconds) started. {}", phase, currentHuntingPhaseName(),
				huntingTimer.duration(), (float) huntingTimer.duration() / GameModel.FPS, huntingTimer);
	}

	private void stopHuntingTimer() {
		huntingTimer.stop();
		Logger.info("Hunting timer stopped");
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
	 * Specifies the hunting behavior of the given ghost.
	 * 
	 * @param ghost one of the guys
	 */
	public void doGhostHuntingAction(Ghost ghost) {
		boolean cruiseElroy = ghost.id() == GameModel.RED_GHOST && cruiseElroyState > 0;
		switch (game.variant()) {
		case MS_PACMAN -> {
			/*
			 * In Ms. Pac-Man, Blinky and Pinky move randomly during the *first* hunting/scatter phase. Some say, the original
			 * intention had been to randomize the scatter target of *all* ghosts in Ms. Pac-Man but because of a bug, only
			 * the scatter target of Blinky and Pinky would have been affected. Who knows?
			 */
			if (scatterPhase().isPresent() && (ghost.id() == GameModel.RED_GHOST || ghost.id() == GameModel.PINK_GHOST)) {
				ghost.roam(this); // not sure
			} else if (chasingPhase().isPresent() || cruiseElroy) {
				ghost.chase(this);
			} else {
				ghost.scatter(this);
			}
		}
		case PACMAN -> {
			if (chasingPhase().isPresent() || cruiseElroy) {
				ghost.chase(this);
			} else {
				ghost.scatter(this);
			}
		}
		default -> throw new IllegalGameVariantException(game.variant());
		}
	}

	/**
	 * @return (optional) number of current scattering phase <code>(0-3)</code>
	 */
	public OptionalInt scatterPhase() {
		return isEven(huntingPhase) ? OptionalInt.of(huntingPhase / 2) : OptionalInt.empty();
	}

	/**
	 * @return (optional) number of current chasing phase <code>(0-3)</code>
	 */
	public OptionalInt chasingPhase() {
		return isOdd(huntingPhase) ? OptionalInt.of(huntingPhase / 2) : OptionalInt.empty();
	}

	public String currentHuntingPhaseName() {
		return isEven(huntingPhase) ? "Scattering" : "Chasing";
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
			ghost.setPosition(ghost.initialPosition());
			ghost.setMoveAndWishDir(ghost.initialDirection());
			ghost.setVisible(guysVisible);
			ghost.enterStateLocked();
		});
		world.animation(GameModel.AK_MAZE_ENERGIZER_BLINKING).ifPresent(Animated::reset);
	}

	/**
	 * @param ghost a ghost
	 * @return relative speed of ghost when hunting
	 */
	public float huntingSpeed(Ghost ghost) {
		if (world.isTunnel(ghost.tile())) {
			return ghostSpeedTunnel;
		} else if (ghost.id() == GameModel.RED_GHOST && cruiseElroyState == 1) {
			return elroy1Speed;
		} else if (ghost.id() == GameModel.RED_GHOST && cruiseElroyState == 2) {
			return elroy2Speed;
		} else {
			return ghostSpeed;
		}
	}

	/*
	 * 
	 * This is the main logic of the game.
	 *
	 */

	private void collectInformation() {
		memo.forgetEverything(); // Ich scholze jetzt

		var pacTile = pac.tile();

		// Food information
		if (world.containsFood(pacTile)) {
			memo.foodFoundTile = Optional.of(pacTile);
			memo.energizerFound = world.isEnergizerTile(pacTile);
		}

		// Pac power
		if (memo.energizerFound && pacPowerSeconds > 0) {
			memo.pacPowerStarts = true;
			memo.pacPowerActive = true;
		} else {
			memo.pacPowerFading = pac.powerTimer().remaining() == GameModel.PAC_POWER_FADES_TICKS;
			memo.pacPowerLost = pac.powerTimer().hasExpired();
			memo.pacPowerActive = pac.powerTimer().isRunning();
		}
	}

	public void update() {
		collectInformation();

		// Food
		if (memo.foodFoundTile.isPresent()) {
			var foodTile = memo.foodFoundTile.get();
			world.removeFood(foodTile);
			pac.endStarving();
			if (bonusManagement.isFirstBonusReached()) {
				memo.bonusReachedIndex = 0;
			} else if (bonusManagement.isSecondBonusReached()) {
				memo.bonusReachedIndex = 1;
			}
			if (memo.energizerFound) {
				numGhostsKilledByEnergizer = 0;
				pac.rest(GameModel.RESTING_TICKS_ENERGIZER);
				game.scorePoints(GameModel.POINTS_ENERGIZER);
			} else {
				pac.rest(GameModel.RESTING_TICKS_NORMAL_PELLET);
				game.scorePoints(GameModel.POINTS_NORMAL_PELLET);
			}
			ghostHouseManagement.update();
			GameEvents.publishGameEvent(GameEventType.PAC_FINDS_FOOD, foodTile);
			GameEvents.publishSoundEvent(GameModel.SE_PACMAN_FOUND_FOOD);
		} else {
			pac.starve();
		}

		if (isCompleted()) {
			logMemo();
			return;
		}

		// Bonus?
		if (memo.bonusReachedIndex != -1) {
			bonusManagement.handleBonusReached(memo.bonusReachedIndex);
		}

		// Pac power state changes
		if (memo.pacPowerStarts) {
			pac.powerTimer().restartSeconds(pacPowerSeconds);
			Logger.info("{} power starting, duration {} ticks", pac.name(), pac.powerTimer().duration());
			ghosts(HUNTING_PAC).forEach(Ghost::enterStateFrightened);
			ghosts(FRIGHTENED).forEach(Ghost::reverseAsSoonAsPossible);
			GameEvents.publishGameEventOfType(GameEventType.PAC_GETS_POWER);
			GameEvents.publishSoundEvent(GameModel.SE_PACMAN_POWER_STARTS);
		} else if (memo.pacPowerFading) {
			GameEvents.publishGameEventOfType(GameEventType.PAC_STARTS_LOSING_POWER);
		} else if (memo.pacPowerLost) {
			Logger.info("{} power ends, timer: {}", pac.name(), pac.powerTimer());
			huntingTimer.start();
			Logger.info("Hunting timer restarted");
			pac.powerTimer().stop();
			pac.powerTimer().resetIndefinitely();
			ghosts(FRIGHTENED).forEach(Ghost::enterStateHuntingPac);
			GameEvents.publishGameEventOfType(GameEventType.PAC_LOSES_POWER);
			GameEvents.publishSoundEvent(GameModel.SE_PACMAN_POWER_ENDS);
		}

		checkIfGhostCanGetUnlocked();

		// Cruise Elroy
		if (world.uneatenFoodCount() == elroy1DotsLeft) {
			setCruiseElroyState(1);
		} else if (world.uneatenFoodCount() == elroy2DotsLeft) {
			setCruiseElroyState(2);
		}

		// Who must die?
		memo.pacPrey = ghosts(FRIGHTENED).filter(pac::sameTile).toList();
		memo.pacKilled = !game.isImmune() && ghosts(HUNTING_PAC).anyMatch(pac::sameTile);

		// Update world and guys
		world.animation(GameModel.AK_MAZE_ENERGIZER_BLINKING).ifPresent(Animated::animate);
		pac.update(this);
		ghosts().forEach(ghost -> ghost.update(this));
		bonusManagement.updateBonus();

		// Update hunting timer
		if (memo.pacPowerStarts || memo.pacKilled) {
			stopHuntingTimer();
		} else {
			boolean huntingPhaseChange = updateHuntingTimer();
			if (huntingPhaseChange) {
				ghosts(HUNTING_PAC, LOCKED, LEAVING_HOUSE).forEach(Ghost::reverseAsSoonAsPossible);
			}
		}
		logMemo();
	}

	private void logMemo() {
		var memoText = memo.toString();
		if (!memoText.isBlank()) {
			Logger.trace(memo);
		}
	}

	/**
	 * Called by cheat action only.
	 */
	public void killAllHuntingAndFrightenedGhosts() {
		memo.pacPrey = ghosts(HUNTING_PAC, FRIGHTENED).toList();
		numGhostsKilledByEnergizer = 0;
		killEdibleGhosts();
	}

	public void killEdibleGhosts() {
		if (!memo.pacPrey.isEmpty()) {
			memo.pacPrey.forEach(this::killGhost);
			numGhostsKilledInLevel += memo.pacPrey.size();
			if (numGhostsKilledInLevel == 16) {
				game.scorePoints(GameModel.POINTS_ALL_GHOSTS_KILLED_IN_LEVEL);
				Logger.trace("All ghosts killed at level {}, {} wins {} points", number, pac.name(),
						GameModel.POINTS_ALL_GHOSTS_KILLED_IN_LEVEL);
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
		Logger.trace("{} killed at tile {}, {} wins {} points", ghost.name(), ghost.tile(), pac.name(), points);
	}

	// Pac-Man

	public boolean pacKilled() {
		return memo.pacKilled;
	}

	public void onPacKilled() {
		pac.killed();
		ghostHouseManagement.onPacKilled();
		setCruiseElroyStateEnabled(false);
		Logger.info("{} died at tile {}", pac.name(), pac.tile());
	}

	public boolean isCompleted() {
		return world.uneatenFoodCount() == 0;
	}

	private void checkIfGhostCanGetUnlocked() {
		ghostHouseManagement.checkIfNextGhostCanLeaveHouse().ifPresent(unlockInfo -> {
			var ghost = unlockInfo.ghost();
			ghost.leaveHouse(this);
			if (ghost.id() == GameModel.ORANGE_GHOST && cruiseElroyState < 0) {
				// Blinky's "cruise elroy" state is re-enabled when orange ghost is unlocked
				setCruiseElroyStateEnabled(true);
			}
			Logger.info("{} unlocked: {}", unlockInfo.ghost().name(), unlockInfo.reason());
		});
	}
}
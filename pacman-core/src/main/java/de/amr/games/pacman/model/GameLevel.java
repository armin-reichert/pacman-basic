/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.controller.Steering;
import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.lib.*;
import de.amr.games.pacman.model.actors.*;
import de.amr.games.pacman.model.world.House;
import de.amr.games.pacman.model.world.World;
import org.tinylog.Logger;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.amr.games.pacman.lib.Direction.LEFT;
import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.lib.NavigationPoint.np;
import static de.amr.games.pacman.model.GameModel.*;
import static de.amr.games.pacman.model.actors.GhostState.*;
import static de.amr.games.pacman.model.world.World.halfTileRightOf;
import static de.amr.games.pacman.model.world.World.tileAt;

/**
 * @author Armin Reichert
 */
public class GameLevel {

	/** Relative Pac-Man speed (percentage of base speed). */
	public final float pacSpeed;

	/** Relative ghost speed when hunting or scattering. */
	public final float ghostSpeed;

	/** Relative ghost speed inside tunnel. */
	public final float ghostSpeedTunnel;

	/** Number of pellets left when Blinky becomes "Cruise Elroy" grade 1. */
	public final byte elroy1DotsLeft;

	/** Relative speed of Blinky being "Cruise Elroy" grade 1. */
	public final float elroy1Speed;

	/** Number of pellets left when Blinky becomes "Cruise Elroy" grade 2. */
	public final byte elroy2DotsLeft;

	/** Relative speed of Blinky being "Cruise Elroy" grade 2. */
	public final float elroy2Speed;

	/** Relative speed of Pac-Man in power mode. */
	public final float pacSpeedPowered;

	/** Relative speed of frightened ghost. */
	public final float ghostSpeedFrightened;

	/** Number of seconds Pac-Man gets power. */
	public final byte pacPowerSeconds;

	/** Number of maze flashes at end of this level. */
	public final byte numFlashes;

	/** Number of intermission scene played after this level (1, 2, 3, 0 = no intermission). */
	public final byte intermissionNumber;

	private final GameModel game;

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
		checkNotNull(data);

		this.game      = game;
		this.world     = world;
		this.number    = number;
		this.demoLevel = demoLevel;

		pacSpeed             = percent(data[0]);
		ghostSpeed           = percent(data[1]);
		ghostSpeedTunnel     = percent(data[2]);
		elroy1DotsLeft       = data[3];
		elroy1Speed          = percent(data[4]);
		elroy2DotsLeft       = data[5];
		elroy2Speed          = percent(data[6]);
		pacSpeedPowered      = percent(data[7]);
		ghostSpeedFrightened = percent(data[8]);
		pacPowerSeconds      = data[9];
		numFlashes           = data[10];
		intermissionNumber   = data[11];

		final boolean msPacManGame = game.variant() == GameVariant.MS_PACMAN;
		final House house = world.house();

		pac = new Pac(msPacManGame ? "Ms. Pac-Man" : "Pac-Man");
		ghosts = new Ghost[] {
			new Ghost(RED_GHOST,  "Blinky"),
			new Ghost(PINK_GHOST, "Pinky"),
			new Ghost(CYAN_GHOST, "Inky"),
			new Ghost(ORANGE_GHOST, msPacManGame ? "Sue" : "Clyde")
		};

		guys().forEach(guy -> guy.setLevel(this));

		// Blinky: attacks Pac-Man directly
		ghosts[RED_GHOST].setInitialDirection(Direction.LEFT);
		ghosts[RED_GHOST].setInitialPosition(house.door().entryPosition());
		ghosts[RED_GHOST].setRevivalPosition(house.getSeat("middle"));
		ghosts[RED_GHOST].setScatterTile(v2i(25, 0));
		ghosts[RED_GHOST].setChasingTarget(pac::tile);

		// Pinky: ambushes Pac-Man
		ghosts[PINK_GHOST].setInitialDirection(Direction.DOWN);
		ghosts[PINK_GHOST].setInitialPosition(house.getSeat("middle"));
		ghosts[PINK_GHOST].setRevivalPosition(house.getSeat("middle"));
		ghosts[PINK_GHOST].setScatterTile(v2i(2, 0));
		ghosts[PINK_GHOST].setChasingTarget(() -> pac.tilesAheadBuggy(4));

		// Inky: attacks from opposite side as Blinky
		ghosts[CYAN_GHOST].setInitialDirection(Direction.UP);
		ghosts[CYAN_GHOST].setInitialPosition(house.getSeat("left"));
		ghosts[CYAN_GHOST].setRevivalPosition(house.getSeat("left"));
		ghosts[CYAN_GHOST].setScatterTile(v2i(27, 34));
		ghosts[CYAN_GHOST].setChasingTarget(() -> pac.tilesAheadBuggy(2).scaled(2).minus(ghosts[RED_GHOST].tile()));

		// Clyde/Sue: attacks directly but retreats if Pac is near
		ghosts[ORANGE_GHOST].setInitialDirection(Direction.UP);
		ghosts[ORANGE_GHOST].setInitialPosition(house.getSeat("right"));
		ghosts[ORANGE_GHOST].setRevivalPosition(house.getSeat("right"));
		ghosts[ORANGE_GHOST].setScatterTile(v2i(0, 34));
		ghosts[ORANGE_GHOST].setChasingTarget(() -> ghosts[ORANGE_GHOST].tile().euclideanDistance(pac.tile()) < 8 //
				? ghosts[ORANGE_GHOST].scatterTile()
				: pac.tile());

		bonusSymbols = new byte[2];
		bonusSymbols[0] = nextBonusSymbol();
		bonusSymbols[1] = nextBonusSymbol();

		ghostHouseManagement = new GhostHouseManagement(number);

		Logger.trace("Game level {} created. ({})", number, game.variant());
	}

	public void end() {
		Logger.trace("Level {} ({}) ends", number, game.variant());
		pac.rest(Pac.REST_FOREVER);
		pac.selectAnimation(PacAnimations.MUNCHING);
		ghosts().forEach(Ghost::hide);
		deactivateBonus();
		world.mazeFlashing().reset();
		stopHuntingTimer();
	}

	public GameModel game() {
		return game;
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
		return Stream.of(pac, ghosts[RED_GHOST], ghosts[PINK_GHOST], ghosts[CYAN_GHOST], ghosts[ORANGE_GHOST]);
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
	public void setCruiseElroyState(byte cruiseElroyState) {
		if (cruiseElroyState < -2 || cruiseElroyState > 2) {
			throw new IllegalArgumentException(
					"Cruise Elroy state must be one of -2, -1, 0, 1, 2, but is " + cruiseElroyState);
		}
		this.cruiseElroyState = cruiseElroyState;
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
		switch (game.variant()) {
		case MS_PACMAN:
			return Collections.emptyList();
		case PACMAN:
			return GameModel.PACMAN_RED_ZONE;
		default:
			throw new IllegalGameVariantException(game.variant());
		}
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
	 * phases, the living ghosts outside the ghost house reverse their move direction.
	 * 
	 * @return if new hunting phase has been started
	 */
	private boolean updateHuntingTimer() {
		if (huntingTimer.hasExpired()) {
			startHunting(huntingPhase + 1);
			return true;
		}
		huntingTimer.advance();
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
		boolean cruiseElroy = ghost.id() == RED_GHOST && cruiseElroyState > 0;
		switch (game.variant()) {
		case MS_PACMAN: {
			/*
			 * In Ms. Pac-Man, Blinky and Pinky move randomly during the *first* hunting/scatter phase. Some say, the original
			 * intention had been to randomize the scatter target of *all* ghosts in Ms. Pac-Man but because of a bug, only
			 * the scatter target of Blinky and Pinky would have been affected. Who knows?
			 */
			if (scatterPhase().isPresent() && (ghost.id() == RED_GHOST || ghost.id() == PINK_GHOST)) {
				ghost.roam();
			} else if (chasingPhase().isPresent() || cruiseElroy) {
				ghost.chase();
			} else {
				ghost.scatter();
			}
			break;
		}
		case PACMAN: {
			if (chasingPhase().isPresent() || cruiseElroy) {
				ghost.chase();
			} else {
				ghost.scatter();
			}
			break;
		}
		default:
			throw new IllegalGameVariantException(game.variant());
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
	 * Pac-Man and the ghosts are placed at their initial positions and locked. The bonus, Pac-Man power timer and
	 * energizer pulse are reset too. All guys are hidden initially.
	 */
	public void letsGetReadyToRumble() {
		pac.reset();
		pac.setPosition(halfTileRightOf(13, 26));
		pac.setMoveAndWishDir(Direction.LEFT);
		pac.setVisible(false);
		pac.stopAnimation();
		pac.resetAnimation();
		ghosts().forEach(ghost -> {
			ghost.reset();
			ghost.setPosition(ghost.initialPosition());
			ghost.setMoveAndWishDir(ghost.initialDirection());
			ghost.setVisible(false);
			ghost.enterStateLocked();
			ghost.stopAnimation();
			ghost.resetAnimation();
		});
		world.mazeFlashing().reset();
		world.energizerBlinking().reset();
	}

	/**
	 * @param ghost a ghost
	 * @return relative speed of ghost
	 */
	public float huntingSpeed(Ghost ghost) {
		if (world.isTunnel(ghost.tile())) {
			return ghostSpeedTunnel;
		}
		if (ghost.id() == RED_GHOST && cruiseElroyState == 1) {
			return elroy1Speed;
		}
		if (ghost.id() == RED_GHOST && cruiseElroyState == 2) {
			return elroy2Speed;
		}
		return ghostSpeed;
	}

	/* --- Here comes the main logic of the game. --- */

	private void collectInformation() {
		memo.forgetEverything(); // Ich scholze jetzt
		var pacTile = pac.tile();
		if (world.hasFoodAt(pacTile)) {
			memo.foodFoundTile  = Optional.of(pacTile);
			memo.energizerFound = world.isEnergizerTile(pacTile);
		}
		if (memo.energizerFound && pacPowerSeconds > 0) {
			memo.pacPowerStarts = true;
			memo.pacPowerActive = true;
		} else {
			memo.pacPowerFading = pac.powerTimer().remaining() == GameModel.PAC_POWER_FADES_TICKS;
			memo.pacPowerLost   = pac.powerTimer().hasExpired();
			memo.pacPowerActive = pac.powerTimer().isRunning();
		}
		// Who must die?
		memo.pacPrey = ghosts(FRIGHTENED).filter(pac::sameTile).collect(Collectors.toList());
		memo.pacKilled = !GameController.it().isImmune() && ghosts(HUNTING_PAC).anyMatch(pac::sameTile);
	}

	private void handleFoodFound(Vector2i foodTile) {
		world.removeFood(foodTile);
		if (isFirstBonusReached()) {
			memo.bonusReachedIndex = 0;
		} else if (isSecondBonusReached()) {
			memo.bonusReachedIndex = 1;
		}
		if (memo.energizerFound) {
			numGhostsKilledByEnergizer = 0;
			pac.rest(GameModel.RESTING_TICKS_ENERGIZER);
			int points = GameModel.POINTS_ENERGIZER;
			game.scorePoints(points);
			Logger.info("Scored {} points for eating energizer", points);
		} else {
			pac.rest(GameModel.RESTING_TICKS_NORMAL_PELLET);
			game.scorePoints(GameModel.POINTS_NORMAL_PELLET);
		}
		ghostHouseManagement.update(this);
		if (world.uneatenFoodCount() == elroy1DotsLeft) {
			setCruiseElroyState((byte) 1);
		} else if (world.uneatenFoodCount() == elroy2DotsLeft) {
			setCruiseElroyState((byte) 2);
		}
		GameController.it().publishGameEvent(GameEventType.PAC_FOUND_FOOD, foodTile);
	}

	private void handlePacPowerStarts() {
		pac.powerTimer().restartSeconds(pacPowerSeconds);
		Logger.info("{} power starting, duration {} ticks", pac.name(), pac.powerTimer().duration());
		ghosts(HUNTING_PAC).forEach(Ghost::enterStateFrightened);
		ghosts(FRIGHTENED).forEach(Ghost::reverseAsSoonAsPossible);
		GameController.it().publishGameEvent(GameEventType.PAC_GETS_POWER);
	}

	private void handlePacPowerLost() {
		Logger.info("{} power ends, timer: {}", pac.name(), pac.powerTimer());
		pac.powerTimer().stop();
		pac.powerTimer().resetIndefinitely();
		huntingTimer.start();
		Logger.info("Hunting timer restarted");
		ghosts(FRIGHTENED).forEach(Ghost::enterStateHuntingPac);
		GameController.it().publishGameEvent(GameEventType.PAC_LOST_POWER);
	}

	public void update() {
		collectInformation();

		// Food found?
		if (memo.foodFoundTile.isPresent()) {
			pac.endStarving();
			handleFoodFound(memo.foodFoundTile.get());
		} else {
			pac.starve();
		}

		// Level complete?
		if (isCompleted()) {
			logMemo();
			return;
		}

		// Bonus?
		if (memo.bonusReachedIndex != -1) {
			handleBonusReached(memo.bonusReachedIndex);
		}

		// Pac power state changed?
		if (memo.pacPowerStarts) {
			handlePacPowerStarts();
		} else if (memo.pacPowerFading) {
			GameController.it().publishGameEvent(GameEventType.PAC_STARTS_LOSING_POWER);
		} else if (memo.pacPowerLost) {
			handlePacPowerLost();
		}

		// Update world and guys
		world.mazeFlashing().tick();
		unlockGhost();
		pac.update();
		ghosts().forEach(Ghost::update);
		if (bonus != null) {
			bonus.update(this);
		}

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
		memo.pacPrey = ghosts(HUNTING_PAC, FRIGHTENED).collect(Collectors.toList());
		numGhostsKilledByEnergizer = 0;
		killEdibleGhosts();
	}

	public void killEdibleGhosts() {
		if (!memo.pacPrey.isEmpty()) {
			memo.pacPrey.forEach(this::killGhost);
			numGhostsKilledInLevel += memo.pacPrey.size();
			if (numGhostsKilledInLevel == 16) {
				int points = GameModel.POINTS_ALL_GHOSTS_KILLED_IN_LEVEL;
				game.scorePoints(points);
				Logger.info("Scored {} points for killing all ghosts at level {}", points, number);
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
		Logger.info("Scored {} points for killing {} at tile {}", points, ghost.name(), ghost.tile());
	}

	// Pac-Man

	public boolean isPacKilled() {
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

	private void unlockGhost() {
		ghostHouseManagement.checkIfNextGhostCanLeaveHouse(this).ifPresent(unlocked -> {
			var ghost = unlocked.ghost();
			if (ghost.insideHouse()) {
				ghost.enterStateLeavingHouse();
			} else {
				ghost.setMoveAndWishDir(LEFT);
				ghost.enterStateHuntingPac();
			}
			if (ghost.id() == ORANGE_GHOST && cruiseElroyState < 0) {
				// Blinky's "cruise elroy" state is re-enabled when orange ghost is unlocked
				setCruiseElroyStateEnabled(true);
			}
			Logger.trace("{} unlocked: {}", unlocked.ghost().name(), unlocked.reason());
		});
	}

	// Bonus Management

	private final byte[] bonusSymbols;
	private Bonus bonus;

	private byte nextBonusSymbol() {
		if (game.variant() == GameVariant.MS_PACMAN) {
			return nextMsPacManBonusSymbol();
		}
		// In the Pac-Man game, each level has a single bonus symbol appearing twice
		switch (number) {
			case 1:  return GameModel.PACMAN_CHERRIES;
			case 2:  return GameModel.PACMAN_STRAWBERRY;
			case 3:
			case 4:  return GameModel.PACMAN_PEACH;
			case 5:
			case 6:  return GameModel.PACMAN_APPLE;
			case 7:
			case 8:  return GameModel.PACMAN_GRAPES;
			case 9:
			case 10: return GameModel.PACMAN_GALAXIAN;
			case 11:
			case 12: return GameModel.PACMAN_BELL;
			default: return GameModel.PACMAN_KEY;
		}
	}

	/**
	 * (From Reddit user <em>damselindis</em>, see
	 * <a href="https://www.reddit.com/r/Pacman/comments/12q4ny3/is_anyone_able_to_explain_the_ai_behind_the/">Reddit</a>)
	 * <p>
	 * The exact fruit mechanics are as follows: After 64 dots are consumed, the game spawns the first fruit of the level.
	 * After 176 dots are consumed, the game attempts to spawn the second fruit of the level. If the first fruit is still
	 * present in the level when (or eaten very shortly before) the 176th dot is consumed, the second fruit will not
	 * spawn. Dying while a fruit is on screen causes it to immediately disappear and never return.
	 * <p>
	 * The type of fruit is determined by the level count - levels 1-7 will always have two cherries, two strawberries,
	 * etc. until two bananas on level 7. On level 8 and beyond, the fruit type is randomly selected using the weights in
	 * the following table:
	 *
	 * <table>
	 * <tr>
	 * <th>Cherry
	 * <th>Strawberry
	 * <th>Peach
	 * <th>Pretzel
	 * <th>Apple
	 * <th>Pear
	 * <th>Banana
	 * </tr>
	 * <tr>
	 * <td>5/32
	 * <td>5/32
	 * <td>5/32
	 * <td>5/32
	 * <td>4/32
	 * <td>4/32
	 * <td>4/32
	 * </tr>
	 * </table>
	 */
	private byte nextMsPacManBonusSymbol() {
		switch (number) {
			case 1: return GameModel.MS_PACMAN_CHERRIES;
			case 2: return GameModel.MS_PACMAN_STRAWBERRY;
			case 3: return GameModel.MS_PACMAN_ORANGE;
			case 4: return GameModel.MS_PACMAN_PRETZEL;
			case 5: return GameModel.MS_PACMAN_APPLE;
			case 6: return GameModel.MS_PACMAN_PEAR;
			case 7: return GameModel.MS_PACMAN_BANANA;
			default:
				int random = randomInt(0, 320);
				if (random < 50)  return GameModel.MS_PACMAN_CHERRIES;
				if (random < 100) return GameModel.MS_PACMAN_STRAWBERRY;
				if (random < 150) return GameModel.MS_PACMAN_ORANGE;
				if (random < 200) return GameModel.MS_PACMAN_PRETZEL;
				if (random < 240) return GameModel.MS_PACMAN_APPLE;
				if (random < 280) return GameModel.MS_PACMAN_PEAR;
				else              return GameModel.MS_PACMAN_BANANA;
		}
	}

	public boolean isFirstBonusReached() {
		switch (game.variant()) {
			case MS_PACMAN:
				return world().eatenFoodCount() == 64;
			case PACMAN:
				return world().eatenFoodCount() == 70;
			default:
				throw new IllegalGameVariantException(game.variant());
		}
	}

	public boolean isSecondBonusReached() {
		switch (game.variant()) {
			case MS_PACMAN:
				return world().eatenFoodCount() == 176;
			case PACMAN:
				return world().eatenFoodCount() == 170;
			default:
				throw new IllegalGameVariantException(game.variant());
		}
	}

	public Optional<Bonus> bonus() {
		return Optional.ofNullable(bonus);
	}

	public byte bonusSymbol(int index) {
		return bonusSymbols[index];
	}

	public void deactivateBonus() {
		if (bonus != null) {
			bonus.setInactive();
		}
	}

	/**
	 * Handles bonus achievement (public access for unit tests and level test).
	 *
	 * @param bonusIndex bonus index (0 or 1).
	 */
	public void handleBonusReached(int bonusIndex) {
		switch (game.variant()) {
			case MS_PACMAN: {
				if (bonusIndex == 1 && bonus != null && bonus.state() != Bonus.STATE_INACTIVE) {
					Logger.info("First bonus still active, skip second one");
					return;
				}
				byte symbol = bonusSymbols[bonusIndex];
				bonus = createMovingBonus(symbol, GameModel.BONUS_VALUES_MS_PACMAN[symbol] * 100, RND.nextBoolean());
				bonus.setEdible(TickTimer.INDEFINITE);
				GameController.it().publishGameEvent(GameEventType.BONUS_ACTIVATED, bonus.entity().tile());
				break;
			}
			case PACMAN: {
				byte symbol = bonusSymbols[bonusIndex];
				bonus = new StaticBonus(symbol, GameModel.BONUS_VALUES_PACMAN[symbol] * 100);
				bonus.entity().setPosition(GameModel.BONUS_POSITION_PACMAN);
				int ticks = randomInt(9 * FPS, 10 * FPS); // between 9 and 10 seconds
				bonus.setEdible(ticks);
				GameController.it().publishGameEvent(GameEventType.BONUS_ACTIVATED, bonus.entity().tile());
				break;
			}
			default:
				throw new IllegalGameVariantException(game.variant());
		}
	}

	/**
	 * The moving bonus enters the world at a random portal, walks to the house entry, takes a tour around the house and
	 * finally leaves the world through a random portal on the opposite side of the world.
	 * <p>
	 * TODO: This is not the exact behavior as in the original Arcade game.
	 **/
	private Bonus createMovingBonus(byte symbol, int points, boolean leftToRight) {
		var houseHeight    = world.house().size().y();
		var houseEntryTile = tileAt(world.house().door().entryPosition());
		var portals        = world.portals();
		var entryPortal    = portals.get(RND.nextInt(portals.size()));
		var exitPortal     = portals.get(RND.nextInt(portals.size()));
		var startPoint     = leftToRight ? np(entryPortal.leftTunnelEnd())
				                             : np(entryPortal.rightTunnelEnd());
		var exitPoint      = leftToRight ? np(exitPortal.rightTunnelEnd().plus(1, 0))
				                             : np(exitPortal.leftTunnelEnd().minus(1, 0));

		var route = new ArrayList<NavigationPoint>();
		route.add(startPoint);
		route.add(np(houseEntryTile));
		route.add(np(houseEntryTile.plus(0, houseHeight + 1)));
		route.add(np(houseEntryTile));
		route.add(exitPoint);
		route.trimToSize();

		var movingBonus = new MovingBonus(symbol, points);
		movingBonus.setLevel(this);
		movingBonus.setRoute(route, leftToRight);
		Logger.info("Moving bonus created, route: {} ({})",	route, leftToRight ? "left to right" : "right to left");
		return movingBonus;
	}
}
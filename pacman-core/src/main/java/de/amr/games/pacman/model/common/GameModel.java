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
import static de.amr.games.pacman.model.common.GameVariant.MS_PACMAN;
import static de.amr.games.pacman.model.common.Ghost.CYAN_GHOST;
import static de.amr.games.pacman.model.common.Ghost.ORANGE_GHOST;
import static de.amr.games.pacman.model.common.Ghost.PINK_GHOST;
import static de.amr.games.pacman.model.common.Ghost.RED_GHOST;
import static de.amr.games.pacman.model.common.GhostState.DEAD;
import static de.amr.games.pacman.model.common.GhostState.FRIGHTENED;
import static de.amr.games.pacman.model.common.GhostState.HUNTING_PAC;
import static de.amr.games.pacman.model.common.GhostState.LEAVING_HOUSE;
import static de.amr.games.pacman.model.common.GhostState.LOCKED;
import static de.amr.games.pacman.model.common.world.World.HTS;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Stream;

import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.event.GameEventListener;
import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Hiscore;
import de.amr.games.pacman.lib.TickTimer;
import de.amr.games.pacman.lib.V2d;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.world.World;
import de.amr.games.pacman.model.pacman.Bonus;

/**
 * Common part of the Pac-Man and Ms. Pac-Man game models.
 * 
 * @author Armin Reichert
 */
public abstract class GameModel {

	public static boolean logPublishEvents = false;

	/** Speed in pixels/tick at 100%. */
	public static final double BASE_SPEED = 1.25;

	private static final long[][] HUNTING_TIMES = { //
			{ 7 * 60, 20 * 60, 7 * 60, 20 * 60, 5 * 60, 20 * 60, 5 * 60, TickTimer.INDEFINITE },
			{ 7 * 60, 20 * 60, 7 * 60, 20 * 60, 5 * 60, 1033 * 60, 1, TickTimer.INDEFINITE },
			{ 5 * 60, 20 * 60, 5 * 60, 20 * 60, 5 * 60, 1037 * 60, 1, TickTimer.INDEFINITE } };

	/** 1-based level number */
	public int levelNumber;

	/** 1-based maze number */
	public int mazeNumber;

	/** 1-based map number (some mazes share the same map) */
	public int mapNumber;

	/** World of current level. */
	public World world;

	/** Number of running intermissions scene in test mode. */
	public int intermissionTestNumber;

	/** The player, Pac-Man or Ms. Pac-Man. */
	public Pac player;

	/** The four ghosts in order RED, PINK, CYAN, ORANGE. */
	public Ghost[] ghosts;

	/** The bonus entity. */
	public Bonus bonus;

	/** Current level-specific data. */
	public GameLevel level;

	/** Number of player lives when the game starts. */
	public int initialLives = 3;

	/** Game score. */
	public int score;

	/** Value of a simple pellet. */
	public int pelletValue = 10;

	/** Value of an energizer pellet. */
	public int energizerValue = 50;

	/** Bounty for eating the next ghost. */
	public int ghostBounty;

	/** Bounty for eating the first ghost after Pac-Man entered power mode. */
	public int firstGhostBounty = 200;

	/** List of collected level symbols. */
	public List<Integer> levelCounter = new ArrayList<>();

	/** Counter used by ghost house logic. */
	public int globalDotCounter;

	/** Enabled state of the counter used by ghost house logic. */
	public boolean globalDotCounterEnabled;

	/** Level at which current high score has been reached. */
	public int highscoreLevel;

	/** Points scored at current high score. */
	public int highscorePoints;

	/** High score file of current game variant. */
	public File hiscoreFile;

	private final Collection<GameEventListener> subscribers = new ConcurrentLinkedQueue<>();

	/*
	 * Eventing.
	 */

	private boolean eventingEnabled;

	public void setEventingEnabled(boolean eventingEnabled) {
		this.eventingEnabled = eventingEnabled;
	}

	public void addEventListener(GameEventListener subscriber) {
		subscribers.add(subscriber);
	}

	public void removeEventListener(GameEventListener subscriber) {
		subscribers.remove(subscriber);
	}

	public void publishEvent(GameEvent gameEvent) {
		if (eventingEnabled) {
			if (logPublishEvents) {
				log("%s: publish event: '%s'", getClass().getSimpleName(), gameEvent);
			}
			subscribers.forEach(subscriber -> subscriber.onGameEvent(gameEvent));
		}
	}

	public void publishEvent(GameEventType info, V2i tile) {
		publishEvent(new GameEvent(this, info, null, tile));
	}

	public void reset() {
		score = 0;
		player.lives = initialLives;
		levelCounter.clear();
		setLevel(1);
		Hiscore highscore = new Hiscore(this);
		highscore.load();
		highscoreLevel = highscore.level;
		highscorePoints = highscore.points;
	}

	public void resetGuys() {
		player.placeAt(world.playerHomeTile(), HTS, 0);
		player.setMoveDir(world.playerStartDirection());
		player.setWishDir(world.playerStartDirection());
		player.show();
		player.velocity = V2d.NULL;
		player.targetTile = null; // used in autopilot mode
		player.stuck = false;
		player.forcedOnTrack = true;
		player.killed = false;
		player.restingTicksLeft = 0;
		player.starvingTicks = 0;
		player.powerTimer.setDurationIndefinite();

		for (Ghost ghost : ghosts) {
			ghost.placeAt(ghost.homeTile, HTS, 0);
			ghost.setMoveDir(world.ghostStartDirection(ghost.id));
			ghost.setWishDir(world.ghostStartDirection(ghost.id));
			ghost.show();
			ghost.velocity = V2d.NULL;
			ghost.targetTile = null;
			ghost.stuck = false;
			// if ghost home is outside of house (red ghost), ghost is forced on track initially
			ghost.forcedOnTrack = !world.ghostHouse().contains(ghost.homeTile);
			ghost.state = GhostState.LOCKED;
			ghost.bounty = 0;
			// these values are reset only when a level is started:
			// ghost.dotCounter = 0;
			// ghost.elroyMode = 0;
		}

		bonus.init();
	}

	protected void initGhosts(int levelNumber, World world, Ghost[] ghosts) {
		for (Ghost ghost : ghosts) {
			ghost.world = world;
			ghost.dotCounter = 0;
			ghost.elroy = 0;
		}

		ghosts[RED_GHOST].homeTile = world.ghostHouse().leftEntry();
		ghosts[RED_GHOST].revivalTile = world.ghostHouse().seatCenter;
		ghosts[RED_GHOST].globalDotLimit = Integer.MAX_VALUE;
		ghosts[RED_GHOST].privateDotLimit = 0;

		ghosts[PINK_GHOST].homeTile = world.ghostHouse().seatCenter;
		ghosts[PINK_GHOST].revivalTile = world.ghostHouse().seatCenter;
		ghosts[PINK_GHOST].globalDotLimit = 7;
		ghosts[PINK_GHOST].privateDotLimit = 0;

		ghosts[CYAN_GHOST].homeTile = world.ghostHouse().seatLeft;
		ghosts[CYAN_GHOST].revivalTile = world.ghostHouse().seatLeft;
		ghosts[CYAN_GHOST].globalDotLimit = 17;
		ghosts[CYAN_GHOST].privateDotLimit = levelNumber == 1 ? 30 : 0;

		ghosts[ORANGE_GHOST].homeTile = world.ghostHouse().seatRight;
		ghosts[ORANGE_GHOST].revivalTile = world.ghostHouse().seatRight;
		ghosts[ORANGE_GHOST].globalDotLimit = Integer.MAX_VALUE;
		ghosts[ORANGE_GHOST].privateDotLimit = levelNumber == 1 ? 60 : levelNumber == 2 ? 50 : 0;
	}

	protected void createGhosts(String redName, String pinkName, String cyanName, String orangeName) {
		ghosts = new Ghost[] { //
				new Ghost(RED_GHOST, redName), //
				new Ghost(PINK_GHOST, pinkName), //
				new Ghost(CYAN_GHOST, cyanName), //
				new Ghost(ORANGE_GHOST, orangeName) //
		};

		// Red ghost chases Pac-Man directly
		ghosts[RED_GHOST].fnChasingTargetTile = player::tile;

		// Pink ghost's target is two tiles ahead of Pac-Man (simulate overflow bug when player moves up)
		ghosts[PINK_GHOST].fnChasingTargetTile = () -> player.moveDir() != Direction.UP //
				? player.tilesAhead(4)
				: player.tilesAhead(4).plus(-4, 0);

		// For cyan ghost's target, see Pac-Man dossier (simulate overflow bug when player moves up)
		ghosts[CYAN_GHOST].fnChasingTargetTile = () -> player.moveDir() != Direction.UP //
				? player.tilesAhead(2).scaled(2).minus(ghosts[RED_GHOST].tile())
				: player.tilesAhead(2).plus(-2, 0).scaled(2).minus(ghosts[RED_GHOST].tile());

		// Orange ghost's target is either Pac-Man tile or its scatter tile at the lower left maze corner
		ghosts[ORANGE_GHOST].fnChasingTargetTile = () -> ghosts[ORANGE_GHOST].tile().euclideanDistance(player.tile()) < 8
				? world.ghostScatterTile(ORANGE_GHOST)
				: player.tile();
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

	/**
	 * @param phase hunting phase (0, ... 7)
	 * @return hunting (scattering or chasing) ticks for current level and given phase
	 */
	public long huntingPhaseTicks(int phase) {
		if (phase < 0 || phase > 7) {
			throw new IllegalArgumentException("Hunting phase must be 0..7, but is " + phase);
		}
		return switch (levelNumber) {
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

	/**
	 * @param symbolID bonus symbol identifier
	 * @return value of this bonus symbol
	 */
	public abstract int bonusValue(int symbolID);

	/**
	 * @return number of ticks the bonus is active
	 */
	public abstract long bonusActivationTicks();

	// Game logic

	/**
	 * @return <code>true</code> if player power just expired
	 */
	public boolean updatePlayer() {
		boolean lostPower = false;
		if (player.restingTicksLeft > 0) {
			player.restingTicksLeft--;
		} else {
			player.setSpeed(player.powerTimer.isRunning() ? level.playerSpeedPowered : level.playerSpeed);
			player.tryMoving();
		}
		switch (player.powerTimer.state()) {
		case RUNNING -> {
			player.powerTimer.advance();
			if (player.powerTimer.remaining() == sec_to_ticks(1)) {
				// TODO not sure exactly how long the player is losing power
				publishEvent(GameEventType.PLAYER_STARTED_LOSING_POWER, player.tile());
			}
		}
		case EXPIRED -> {
			lostPower = true;
			log("%s lost power, timer=%s", player.name, player.powerTimer);
			ghosts(FRIGHTENED).forEach(ghost -> ghost.state = HUNTING_PAC);
			player.powerTimer.setDurationIndefinite(); // TODO needed?
			publishEvent(GameEventType.PLAYER_LOST_POWER, player.tile());
		}
		default -> {
		}
		}
		return lostPower;
	}

	public void updateGhosts(GameVariant gameVariant, int huntingPhase) {
		ghosts().forEach(ghost -> updateGhost(ghost, gameVariant, huntingPhase));
		Ghost released = releaseLockedGhosts();
		if (released != null) {
			publishEvent(new GameEvent(this, GameEventType.GHOST_STARTED_LEAVING_HOUSE, released, released.tile()));
		}
	}

	/**
	 * Updates ghost's speed and behavior depending on its current state.
	 * 
	 * TODO: I am not sure about the exact speed values as the Pac-Man dossier has no info on this. Any help is
	 * appreciated!
	 * 
	 * @param ghost ghost to update
	 */
	public void updateGhost(Ghost ghost, GameVariant gameVariant, int huntingPhase) {
		switch (ghost.state) {

		case LOCKED -> {
			if (ghost.atGhostHouseDoor(world.ghostHouse())) {
				ghost.setSpeed(0);
			} else {
				ghost.setSpeed(level.ghostSpeed / 2);
				ghost.bounce(world.ghostHouse());
			}
		}

		case ENTERING_HOUSE -> {
			ghost.setSpeed(level.ghostSpeed * 2);
			boolean reachedRevivalTile = ghost.enterHouse(world.ghostHouse());
			if (reachedRevivalTile) {
				publishEvent(new GameEvent(this, GameEventType.GHOST_REVIVED, ghost, ghost.tile()));
				publishEvent(new GameEvent(this, GameEventType.GHOST_STARTED_LEAVING_HOUSE, ghost, ghost.tile()));
			}
		}

		case LEAVING_HOUSE -> {
			ghost.setSpeed(level.ghostSpeed / 2);
			boolean leftHouse = ghost.leaveHouse(world.ghostHouse());
			if (leftHouse) {
				publishEvent(new GameEvent(this, GameEventType.GHOST_FINISHED_LEAVING_HOUSE, ghost, ghost.tile()));
			}
		}

		case FRIGHTENED -> {
			if (world.isTunnel(ghost.tile())) {
				ghost.setSpeed(level.ghostSpeedTunnel);
				ghost.tryMoving();
			} else {
				ghost.setSpeed(level.ghostSpeedFrightened);
				ghost.roam();
			}
		}

		case HUNTING_PAC -> {
			if (world.isTunnel(ghost.tile())) {
				ghost.setSpeed(level.ghostSpeedTunnel);
			} else if (ghost.elroy == 1) {
				ghost.setSpeed(level.elroy1Speed);
			} else if (ghost.elroy == 2) {
				ghost.setSpeed(level.elroy2Speed);
			} else {
				ghost.setSpeed(level.ghostSpeed);
			}

			/*
			 * In Ms. Pac-Man, Blinky and Pinky move randomly during the *first* scatter phase. Some say, the original
			 * intention had been to randomize the scatter target of *all* ghosts in Ms. Pac-Man but because of a bug, only
			 * the scatter target of Blinky and Pinky would have been affected. Who knows?
			 */
			if (gameVariant == MS_PACMAN && huntingPhase == 0 && (ghost.id == RED_GHOST || ghost.id == PINK_GHOST)) {
				ghost.roam();
			} else if (HuntingTimer.isScatteringPhase(huntingPhase) && ghost.elroy == 0) {
				ghost.scatter();
			} else {
				ghost.chase();
			}
		}

		case DEAD -> {
			ghost.setSpeed(level.ghostSpeed * 2);
			boolean reachedHouse = ghost.returnHome(world.ghostHouse());
			if (reachedHouse) {
				publishEvent(new GameEvent(this, GameEventType.GHOST_ENTERED_HOUSE, ghost, ghost.tile()));
			}
		}

		default -> throw new IllegalArgumentException("Illegal ghost state: " + ghost.state);
		}
	}

	/**
	 * @param points points to score
	 * @return <code>true</code> if extra life has been achieved
	 */
	public boolean score(int points) {
		int oldscore = score;
		score += points;
		if (score > highscorePoints) {
			highscorePoints = score;
			highscoreLevel = levelNumber;
		}
		if (oldscore < 10000 && score >= 10000) {
			player.lives++;
			return true;
		}
		return false;
	}

	/**
	 * @param tile
	 * @return <code>true</code> if energizer was eaten on given tile
	 */
	public boolean checkFood(V2i tile) {
		if (!world.containsFood(tile)) {
			player.starvingTicks++;
			return false;
		}
		publishEvent(GameEventType.PLAYER_FOUND_FOOD, tile);
		boolean extraLife = false;
		boolean energizerEaten = false;
		if (world.isEnergizerTile(tile)) {
			extraLife = eatEnergizer(tile);
			energizerEaten = true;
			if (level.ghostFrightenedSeconds > 0) {
				publishEvent(GameEventType.PLAYER_GOT_POWER, tile);
			}
		} else {
			extraLife = eatPellet(tile);
		}
		if (extraLife) {
			log("Extra life. Player has %d lives now", player.lives);
			publishEvent(GameEventType.PLAYER_GOT_EXTRA_LIFE, null);
		}
		if (checkBonusAwarded()) {
			publishEvent(GameEventType.BONUS_ACTIVATED, bonus.tile());
		}
		return energizerEaten;
	}

	private boolean eatEnergizer(V2i tile) {
		world.removeFood(tile);
		ghostBounty = firstGhostBounty;
		player.starvingTicks = 0;
		player.restingTicksLeft = 3;
		if (level.ghostFrightenedSeconds > 0) {
			ghosts(HUNTING_PAC).forEach(ghost -> {
				ghost.state = FRIGHTENED;
				ghost.forceTurningBack();
			});
			player.powerTimer.setDurationSeconds(level.ghostFrightenedSeconds).start();
			log("%s got power, timer=%s", player.name, player.powerTimer);
		}
		checkElroy();
		updateGhostDotCounters();
		return score(energizerValue);
	}

	private boolean eatPellet(V2i tile) {
		world.removeFood(tile);
		player.starvingTicks = 0;
		player.restingTicksLeft = 1;
		checkElroy();
		updateGhostDotCounters();
		return score(pelletValue);
	}

	public boolean checkKillGhosts() {
		Ghost[] prey = ghosts(FRIGHTENED).filter(player::meets).toArray(Ghost[]::new);
		Stream.of(prey).forEach(this::killGhost);
		return prey.length > 0;
	}

	/**
	 * Killing ghosts wins 200, 400, 800, 1600 points in order when using the same energizer power. If all 16 ghosts on a
	 * level are killed, additonal 12000 points are rewarded.
	 */
	public void killGhost(Ghost ghost) {
		ghost.state = DEAD;
		ghost.targetTile = world.ghostHouse().leftEntry();
		ghost.bounty = ghostBounty;
		ghostBounty *= 2;
		level.numGhostsKilled++;
		score(ghost.bounty);
		if (level.numGhostsKilled == 16) {
			score(12000);
		}
		log("Ghost %s killed at tile %s, Pac-Man wins %d points", ghost.name, ghost.tile(), ghost.bounty);
	}

	public boolean checkKillPlayer(boolean immune) {
		if (player.powerTimer.isRunning()) {
			return false;
		}
		if (immune) {
			return false;
		}
		Optional<Ghost> killer = ghosts(HUNTING_PAC).filter(player::meets).findAny();
		killer.ifPresent(ghost -> {
			player.killed = true;
			log("%s got killed by %s at tile %s", player.name, ghost.name, player.tile());
			// Elroy mode of red ghost gets disabled when player is killed
			Ghost redGhost = ghosts[RED_GHOST];
			if (redGhost.elroy > 0) {
				redGhost.elroy = -redGhost.elroy; // negative value means "disabled"
				log("Elroy mode %d for %s has been disabled", redGhost.elroy, redGhost.name);
			}
			// reset and disable global dot counter (used by ghost house logic)
			globalDotCounter = 0;
			globalDotCounterEnabled = true;
			log("Global dot counter got reset and enabled");
		});
		return killer.isPresent();
	}

	private boolean checkElroy() {
		if (world.foodRemaining() == level.elroy1DotsLeft) {
			ghosts[RED_GHOST].elroy = 1;
			log("Blinky becomes Cruise Elroy 1");
			return true;
		} else if (world.foodRemaining() == level.elroy2DotsLeft) {
			ghosts[RED_GHOST].elroy = 2;
			log("Blinky becomes Cruise Elroy 2");
			return true;
		}
		return false;
	}

	public boolean checkBonusAwarded() {
		if (world.isBonusReached()) {
			bonus.activate(level.bonusSymbol, bonusValue(bonus.symbol));
			bonus.timer = bonusActivationTicks();
			log("Bonus id=%d, value=%d activated for %d ticks", bonus.symbol, bonus.points, bonus.timer);
			return true;
		}
		return false;
	}

	public void updateBonus() {
		switch (bonus.state) {
		case EDIBLE -> {
			if (player.meets(bonus)) {
				log("%s found bonus id=%d of value %d", player.name, bonus.symbol, bonus.points);
				bonus.eat();
				bonus.timer = sec_to_ticks(2);
				boolean extraLife = score(bonus.points);
				if (extraLife) {
					log("Extra life. Player has %d lives now", player.lives);
					publishEvent(GameEventType.PLAYER_GOT_EXTRA_LIFE, null);
				}
				publishEvent(GameEventType.BONUS_EATEN, bonus.tile());
			} else {
				bonus.update();
				if (bonus.timer == 0) {
					log("Bonus id=%d expired", bonus.symbol);
					publishEvent(GameEventType.BONUS_EXPIRED, bonus.tile());
				}
			}
		}
		case EATEN -> {
			bonus.update();
			if (bonus.timer == 0) {
				publishEvent(GameEventType.BONUS_EXPIRED, bonus.tile());
			}
		}
		default -> {
			// INACTIVE
		}
		}
	}

	// Ghost house rules, see Pac-Man dossier

	public Ghost releaseLockedGhosts() {
		if (ghosts[RED_GHOST].is(LOCKED)) {
			ghosts[RED_GHOST].state = HUNTING_PAC;
		}
		Optional<Ghost> nextToRelease = preferredLockedGhostInHouse();
		if (nextToRelease.isPresent()) {
			Ghost ghost = nextToRelease.get();
			if (globalDotCounterEnabled && globalDotCounter >= ghost.globalDotLimit) {
				return releaseGhost(ghost, "Global dot counter reached limit (%d)", ghost.globalDotLimit);
			} else if (!globalDotCounterEnabled && ghost.dotCounter >= ghost.privateDotLimit) {
				return releaseGhost(ghost, "Private dot counter reached limit (%d)", ghost.privateDotLimit);
			} else if (player.starvingTicks >= player.starvingTimeLimit) {
				int starved = player.starvingTicks;
				player.starvingTicks = 0;
				return releaseGhost(ghost, "%s reached starving limit (%d ticks)", player.name, starved);
			}
		}
		return null;
	}

	private Ghost releaseGhost(Ghost ghost, String reason, Object... args) {
		if (ghost.id == ORANGE_GHOST && ghosts[RED_GHOST].elroy < 0) {
			ghosts[RED_GHOST].elroy = -ghosts[RED_GHOST].elroy; // resume Elroy mode
			log("%s Elroy mode %d resumed", ghosts[RED_GHOST].name, ghosts[RED_GHOST].elroy);
		}
		ghost.state = LEAVING_HOUSE;
		log("Ghost %s released: %s", ghost.name, String.format(reason, args));
		return ghost;
	}

	private Optional<Ghost> preferredLockedGhostInHouse() {
		return Stream.of(Ghost.PINK_GHOST, CYAN_GHOST, ORANGE_GHOST).map(id -> ghosts[id]).filter(ghost -> ghost.is(LOCKED))
				.findFirst();
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
			preferredLockedGhostInHouse().ifPresent(ghost -> ++ghost.dotCounter);
		}
	}
}
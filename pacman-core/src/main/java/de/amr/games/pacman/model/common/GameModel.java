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
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.event.GameEventSupport;
import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Hiscore;
import de.amr.games.pacman.lib.TickTimer;
import de.amr.games.pacman.lib.TickTimer.TickTimerState;
import de.amr.games.pacman.lib.V2d;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.world.GhostHouse;

/**
 * Common part of the Pac-Man and Ms. Pac-Man game models.
 * 
 * @author Armin Reichert
 */
public abstract class GameModel {

	/** Speed in pixels/tick at 100%. */
	public static final double BASE_SPEED = 1.25;

	private static final long[][] HUNTING_TIMES = {
	//@formatter:off
		{ 7*60, 20*60, 7*60, 20*60, 5*60,   20*60, 5*60, TickTimer.INDEFINITE },
		{ 7*60, 20*60, 7*60, 20*60, 5*60, 1033*60,    1, TickTimer.INDEFINITE },
		{ 5*60, 20*60, 5*60, 20*60, 5*60, 1037*60,    1, TickTimer.INDEFINITE }
	//@formatter:on
	};

	/** Current level-specific data. */
	public GameLevel level;

	/** Number of running intermissions scene in test mode. */
	public int intermissionTestNumber;

	/** The player, Pac-Man or Ms. Pac-Man. */
	public Pac player;

	/** The four ghosts in order RED, PINK, CYAN, ORANGE. */
	public Ghost[] ghosts;

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

	public final GameEventSupport eventSupport = new GameEventSupport(this);

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
		player.placeAt(level.world.playerHomeTile(), HTS, 0);
		player.setBothDirs(level.world.playerStartDir());
		player.show();
		player.velocity = V2d.NULL;
		player.targetTile = null; // used in autopilot mode
		player.stuck = false;
		player.killed = false;
		player.restingTicksLeft = 0;
		player.starvingTicks = 0;
		player.powerTimer.setDurationIndefinite();

		for (Ghost ghost : ghosts) {
			ghost.placeAt(ghost.homeTile, HTS, 0);
			ghost.setBothDirs(level.world.ghostStartDir(ghost.id));
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

	protected void initGhosts(int levelNumber, Ghost[] ghosts, GhostHouse house) {
		for (Ghost ghost : ghosts) {
			ghost.dotCounter = 0;
			ghost.elroy = 0;
		}

		ghosts[RED_GHOST].homeTile = house.entry();
		ghosts[RED_GHOST].revivalTile = house.seatMiddle();

		ghosts[PINK_GHOST].homeTile = house.seatMiddle();
		ghosts[PINK_GHOST].revivalTile = house.seatMiddle();

		ghosts[CYAN_GHOST].homeTile = house.seatLeft();
		ghosts[CYAN_GHOST].revivalTile = house.seatLeft();

		ghosts[ORANGE_GHOST].homeTile = house.seatRight();
		ghosts[ORANGE_GHOST].revivalTile = house.seatRight();
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
				? level.world.ghostScatterTile(ORANGE_GHOST)
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

	/**
	 * @param symbolID bonus symbol identifier
	 * @return value of this bonus symbol
	 */
	public abstract int bonusValue(int symbolID);

	// Game logic

	public void movePlayer() {
		if (player.restingTicksLeft > 0) {
			player.restingTicksLeft--;
		} else {
			player.setSpeed(player.powerTimer.isRunning() ? level.playerSpeedPowered : level.playerSpeed, BASE_SPEED);
			player.tryMoving(level.world);
		}
	}

	/**
	 * @return if the player lost power in this frame
	 */
	public boolean updatePlayerPower() {
		if (player.powerTimer.state() == TickTimerState.RUNNING) {
			player.powerTimer.advance();
		} else if (player.powerTimer.state() == TickTimerState.EXPIRED) {
			player.powerTimer.setDurationIndefinite(); // now in state READY
			return true;
		}
		return false;
	}

	/**
	 * @param points points to score
	 */
	protected void score(int points) {
		int oldscore = score;
		score += points;
		if (score > highscorePoints) {
			highscorePoints = score;
			highscoreLevel = level.number;
		}
		if (oldscore < 10000 && score >= 10000) {
			player.lives++;
			eventSupport.publish(new GameEvent(this, GameEventType.PLAYER_GETS_EXTRA_LIFE, null, player.tile()));
		}
	}

	/**
	 * @param tile
	 * @return <code>true</code> if energizer was eaten on given tile
	 */
	public boolean checkFood(V2i tile) {
		if (!level.world.containsFood(tile)) {
			player.starvingTicks++;
			return false;
		}
		eventSupport.publish(GameEventType.PLAYER_FINDS_FOOD, tile);
		boolean energizerEaten = false;
		if (level.world.isEnergizerTile(tile)) {
			eatEnergizer(tile);
			energizerEaten = true;
			if (level.ghostFrightenedSeconds > 0) {
				eventSupport.publish(GameEventType.PLAYER_GETS_POWER, tile);
			}
		} else {
			eatPellet(tile);
		}
		if (checkBonusAwarded()) {
			eventSupport.publish(GameEventType.BONUS_GETS_ACTIVE, level.world.bonusTile());
		}
		return energizerEaten;
	}

	private void eatEnergizer(V2i tile) {
		level.world.removeFood(tile);
		ghostBounty = firstGhostBounty;
		player.starvingTicks = 0;
		player.restingTicksLeft = 3;
		if (level.ghostFrightenedSeconds > 0) {
			ghosts(HUNTING_PAC).forEach(ghost -> {
				ghost.state = FRIGHTENED;
				ghost.forceTurningBack(level.world);
			});
			player.powerTimer.setDurationSeconds(level.ghostFrightenedSeconds).start();
			log("%s got power, timer=%s", player.name, player.powerTimer);
		}
		checkElroy();
		updateGhostDotCounters();
		score(energizerValue);
	}

	private void eatPellet(V2i tile) {
		level.world.removeFood(tile);
		player.starvingTicks = 0;
		player.restingTicksLeft = 1;
		checkElroy();
		updateGhostDotCounters();
		score(pelletValue);
	}

	public boolean checkKillGhosts() {
		Ghost[] prey = ghosts(FRIGHTENED).filter(player::sameTile).toArray(Ghost[]::new);
		Stream.of(prey).forEach(this::killGhost);
		return prey.length > 0;
	}

	/**
	 * Killing ghosts wins 200, 400, 800, 1600 points in order when using the same energizer power. If all 16 ghosts on a
	 * level are killed, additonal 12000 points are rewarded.
	 */
	public void killGhost(Ghost ghost) {
		ghost.state = DEAD;
		ghost.targetTile = level.world.ghostHouse().entry();
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
		Optional<Ghost> killer = ghosts(HUNTING_PAC).filter(player::sameTile).findAny();
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
		if (level.world.foodRemaining() == level.elroy1DotsLeft) {
			ghosts[RED_GHOST].elroy = 1;
			log("Blinky becomes Cruise Elroy 1");
			return true;
		} else if (level.world.foodRemaining() == level.elroy2DotsLeft) {
			ghosts[RED_GHOST].elroy = 2;
			log("Blinky becomes Cruise Elroy 2");
			return true;
		}
		return false;
	}

	// Bonus stuff

	public abstract Bonus bonus();

	public abstract boolean checkBonusAwarded();

	public abstract void updateBonus();

	// Ghost house rules, see Pac-Man dossier

	public Ghost releaseLockedGhosts() {
		if (ghosts[RED_GHOST].is(LOCKED)) {
			ghosts[RED_GHOST].state = HUNTING_PAC;
		}
		Optional<Ghost> nextToRelease = preferredLockedGhostInHouse();
		if (nextToRelease.isPresent()) {
			Ghost ghost = nextToRelease.get();
			if (globalDotCounterEnabled && globalDotCounter >= level.globalDotLimits[ghost.id]) {
				return releaseGhost(ghost, "Global dot counter reached limit (%d)", level.globalDotLimits[ghost.id]);
			} else if (!globalDotCounterEnabled && ghost.dotCounter >= level.privateDotLimits[ghost.id]) {
				return releaseGhost(ghost, "Private dot counter reached limit (%d)", level.privateDotLimits[ghost.id]);
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
		return Stream.of(PINK_GHOST, CYAN_GHOST, ORANGE_GHOST) //
				.map(id -> ghosts[id]).filter(ghost -> ghost.is(LOCKED)).findFirst();
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
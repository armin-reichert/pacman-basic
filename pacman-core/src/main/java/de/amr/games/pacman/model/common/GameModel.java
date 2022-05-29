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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.event.GameEventSupport;
import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.event.ScatterPhaseStartsEvent;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Hiscore;
import de.amr.games.pacman.lib.TickTimer;
import de.amr.games.pacman.lib.V2d;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.actors.Bonus;
import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.model.common.actors.GhostState;
import de.amr.games.pacman.model.common.actors.Pac;

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
	public static final int EXTRA_LIFE_POINTS = 10_000;
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

	/** Number of lives remainingr. */
	public int lives;

	/** Game score. */
	public int score;

	/** Bounty for eating the next ghost. */
	public int ghostBounty;

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

	/** Number of current intermission scene in test mode. */
	public int intermissionTestNumber;

	public final GameEventSupport eventSupport = new GameEventSupport(this);

	public abstract File highscoreFile();

	public GameModel(GameVariant gameVariant, Pac player, Ghost... ghosts) {
		if (ghosts.length != 4) {
			throw new IllegalArgumentException("We need exactly 4 ghosts in order RED, PINK, CYAN, ORANGE");
		}
		this.variant = gameVariant;
		this.player = player;
		this.ghosts = ghosts;
	}

	public void reset() {
		score = 0;
		lives = INITIAL_LIFES;
		levelCounter.clear();
		setLevel(1);
		Hiscore highscore = new Hiscore(this);
		highscore.load();
		highscoreLevel = highscore.level;
		highscorePoints = highscore.points;
	}

	public void resetGuys() {
		player.placeAt(level.world.playerHomeTile(), HTS, 0);
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

	/**
	 * @param points points to score
	 */
	public void score(int points) {
		int oldscore = score;
		score += points;
		if (score > highscorePoints) {
			highscorePoints = score;
			highscoreLevel = level.number;
		}
		if (oldscore < EXTRA_LIFE_POINTS && score >= EXTRA_LIFE_POINTS) {
			lives++;
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
		boolean energizerEaten = false;
		level.world.removeFood(tile);
		player.starvingTicks = 0;
		eventSupport.publish(GameEventType.PLAYER_FINDS_FOOD, tile);
		if (level.world.isEnergizerTile(tile)) {
			energizerEaten = true;
			score(ENERGIZER_VALUE);
			ghostBounty = FIRST_GHOST_BOUNTY;
			player.restingCountdown = ENERGIZER_RESTING_TICKS;
			if (level.ghostFrightenedSeconds > 0) {
				ghosts(HUNTING_PAC).forEach(ghost -> {
					ghost.state = FRIGHTENED;
					ghost.forceTurningBack(level.world);
				});
				player.powerTimer.setDurationSeconds(level.ghostFrightenedSeconds).start();
				log("%s power timer started: %s", player.name, player.powerTimer);
				eventSupport.publish(GameEventType.PLAYER_GETS_POWER, tile);
			}
		} else {
			player.restingCountdown = PELLET_RESTING_TICKS;
			score(PELLET_VALUE);
		}
		if (checkBonusAwarded()) {
			eventSupport.publish(GameEventType.BONUS_GETS_ACTIVE, bonus().tile());
		}
		ghosts[RED_GHOST].checkCruiseElroyStart(level);
		updateGhostDotCounters();
		return energizerEaten;
	}

	/**
	 * Killing ghosts wins 200, 400, 800, 1600 points in order when using the same energizer power. If all 16 ghosts on a
	 * level are killed, additonal 12000 points are rewarded.
	 */
	public boolean checkKillGhosts() {
		var prey = ghosts(FRIGHTENED).filter(player::sameTile).toArray(Ghost[]::new);
		if (prey.length > 0) {
			killGhosts(prey);
			return true;
		}
		return false;
	}

	// This is public because cheat method in game controller calls it
	public void killGhosts(Ghost[] prey) {
		Stream.of(prey).forEach(this::killGhost);
		level.numGhostsKilled += prey.length;
		if (level.numGhostsKilled == 16) {
			log("All ghosts killed at level %d, Pac-Man wins additional %d points", level.number, ALL_GHOSTS_KILLED_POINTS);
			score(ALL_GHOSTS_KILLED_POINTS);
		}
	}

	private void killGhost(Ghost ghost) {
		ghost.state = DEAD;
		ghost.targetTile = level.world.ghostHouse().entry();
		ghost.bounty = ghostBounty;
		ghostBounty *= 2;
		score(ghost.bounty);
		log("Ghost %s killed at tile %s, Pac-Man wins %d points", ghost.name, ghost.tile(), ghost.bounty);
	}

	public boolean checkKillPlayer() {
		Optional<Ghost> killer = ghosts(HUNTING_PAC).filter(player::sameTile).findAny();
		killer.ifPresent(ghost -> {
			log("%s got killed by %s at tile %s", player.name, ghost.name, player.tile());
			player.killed = true;
			ghosts[RED_GHOST].checkCruiseElroyStop();
			// reset and disable global dot counter (see Pac-Man dossier)
			globalDotCounter = 0;
			globalDotCounterEnabled = true;
			log("Global dot counter got reset and enabled");
		});
		return killer.isPresent();
	}

	// Bonus stuff

	public abstract Bonus bonus();

	public abstract boolean checkBonusAwarded();

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
			} else if (player.starvingTicks >= level.pacStarvingTimeLimit) {
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
		return Stream.of(PINK_GHOST, CYAN_GHOST, ORANGE_GHOST).map(id -> ghosts[id]).filter(g -> g.is(LOCKED)).findFirst();
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
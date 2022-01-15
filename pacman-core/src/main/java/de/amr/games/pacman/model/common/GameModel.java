/*
MIT License

Copyright (c) 2021 Armin Reichert

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
import static de.amr.games.pacman.model.world.PacManGameWorld.HTS;

import java.io.File;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.stream.Stream;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Hiscore;
import de.amr.games.pacman.lib.TickTimer;
import de.amr.games.pacman.lib.V2d;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.pacman.entities.Bonus;
import de.amr.games.pacman.model.world.PacManGameWorld;

/**
 * Common part of the Pac-Man and Ms. Pac-Man game models.
 * 
 * @author Armin Reichert
 */
public abstract class GameModel {

	public static final int RED_GHOST = 0, PINK_GHOST = 1, CYAN_GHOST = 2, ORANGE_GHOST = 3;

	/** Speed in pixels / tick at 100%. */
	public static double BASE_SPEED = 1.25;

	private static float percent(Object intValue) {
		return ((int) intValue) / 100f;
	}

	//@formatter:off
	public final long[][] HUNTING_PHASE_TICKS = {
	  // scatter  chase   scatter  chase  scatter  chase    scatter  chase
	   { 7*60,    20*60,  7*60,    20*60, 5*60,      20*60, 5*60,    TickTimer.INDEFINITE },
	   { 7*60,    20*60,  7*60,    20*60, 5*60,    1033*60,    1,    TickTimer.INDEFINITE },
	   { 5*60,    20*60,  5*60,    20*60, 5*60,    1037*60,    1,    TickTimer.INDEFINITE },
	};
	//@formatter:on

	public long getHuntingPhaseDuration(int phase) {
		int row = levelNumber == 1 ? 0 : levelNumber <= 4 ? 1 : 2;
		return HUNTING_PHASE_TICKS[row][phase];
	}

	/** 1-based level number */
	public int levelNumber;

	/** 1-based maze number */
	public int mazeNumber;

	/** 1-based map number */
	public int mapNumber;

	/** World of current level. */
	public PacManGameWorld world;

	/** Bonus symbol of current level. */
	public String bonusSymbol;

	/** Relative player speed at current level. */
	public float playerSpeed;

	/** Relative ghost speed at current level. */
	public float ghostSpeed;

	/** Relative ghost speed when inside tunnel at current level. */
	public float ghostSpeedTunnel;

	/** Number of pellets left before player becomes "Cruise Elroy" at severity 1. */
	public int elroy1DotsLeft;

	/** Relative speed of player being "Cruise Elroy" at severity 1. */
	public float elroy1Speed;

	/** Number of pellets left before player becomes "Cruise Elroy" at severity 2. */
	public int elroy2DotsLeft;

	/** Relative speed of player being "Cruise Elroy" at severity 2. */
	public float elroy2Speed;

	/** Relative speed of player in power mode. */
	public float playerSpeedPowered;

	/** Relative speed of frightened ghost. */
	public float ghostSpeedFrightened;

	/** Number of seconds ghost are frightened at current level. */
	public int ghostFrightenedSeconds;

	/** Number of maze flashes at end of current level. */
	public int numFlashes;

	/** Set of tile indices with eaten food. */
	private BitSet eaten;

	/** Total number of pellets at current level. */
	public int totalFoodCount;

	/** Total number of pellets remaining at current level. */
	public int foodRemaining;

	/** The player, Pac-Man or Ms. Pac-Man. */
	public Pac player;

	/** The four ghosts in order RED, PINK, CYAN, ORANGE. */
	public Ghost[] ghosts;

	/** The bonus entity. */
	public Bonus bonus;

	/** Number of player lives when the game starts. */
	public int initialLives;

	/** Game score. */
	public int score;

	/** Number of ghosts killed at the current level. */
	public int numGhostsKilled;

	/** Value of a simple pellet. */
	public int pelletValue;

	/** Value of an energizer pellet. */
	public int energizerValue;

	/** Bounty for eating the next ghost. */
	public int ghostBounty;

	/** Bounty for eating the first ghost after Pac-Man entered power mode. */
	public int firstGhostBounty;

	/** List of collected level symbols. */
	public List<String> levelCounter = new ArrayList<>();

	/** Counter used by ghost house logic. */
	public int globalDotCounter;

	/** Enabled state of the counter used by ghost house logic. */
	public boolean globalDotCounterEnabled;

	/** Level at which current high score has been reached. */
	public int hiscoreLevel;

	/** Points scored at current high score. */
	public int hiscorePoints;

	/** High score file of current game variant. */
	public File hiscorePath;

	/**
	 * Initializes model for given game level.
	 * 
	 * @param levelNumber 1-based level number
	 */
	public abstract void enterLevel(int levelNumber);

	/**
	 * @param levelNumber game level number
	 * @return 1-based intermission (cut scene) number that is played after given level or <code>0</code> if no
	 *         intermission is played after given level.
	 */
	public abstract int intermissionNumber(int levelNumber);

	/**
	 * @param symbolName bonus symbol identifier
	 * @return value of this bonus symbol
	 */
	public abstract int bonusValue(String symbolName);

	protected GameModel() {
		initialLives = 3;
		pelletValue = 10;
		energizerValue = 50;
		firstGhostBounty = 200;
	}

	public void reset() {
		score = 0;
		player.lives = initialLives;
		levelCounter.clear();
		Hiscore hiscore = new Hiscore(hiscorePath).load();
		hiscoreLevel = hiscore.level;
		hiscorePoints = hiscore.points;
		enterLevel(1);
	}

	public void resetGuys() {
		player.placeAt(world.playerHomeTile(), HTS, 0);
		player.setDir(world.playerStartDirection());
		player.setWishDir(world.playerStartDirection());
		player.hide();
		player.velocity = V2d.NULL;
		player.targetTile = null; // used in autopilot mode
		player.stuck = false;
		player.forcedOnTrack = true;
		player.dead = false;
		player.restingTicksLeft = 0;
		player.starvingTicks = 0;
		player.powerTimer.resetIndefinite();

		for (Ghost ghost : ghosts) {
			ghost.placeAt(ghost.homeTile, HTS, 0);
			ghost.setDir(world.ghostStartDirection(ghost.id));
			ghost.setWishDir(world.ghostStartDirection(ghost.id));
			ghost.hide();
			ghost.velocity = V2d.NULL;
			ghost.targetTile = null;
			ghost.stuck = false;
			// if ghost home is located outside of house, he must be on track initially
			boolean ghostHomeOutsideOfHouse = !world.ghostHouse().contains(ghost.homeTile);
			ghost.forced = ghostHomeOutsideOfHouse;
			ghost.forcedOnTrack = ghostHomeOutsideOfHouse;
			ghost.state = GhostState.LOCKED;
			ghost.bounty = 0;
			// these are reset only when level is started:
			// ghost.dotCounter = 0;
			// ghost.elroyMode = 0;
		}

		bonus.init();
	}

	protected abstract Object[] levelData(int levelNumber);

	protected void loadLevel(int levelNumber) {
		Object[] levelData = levelData(levelNumber);
		bonusSymbol = (String) levelData[0];
		playerSpeed = percent(levelData[1]);
		ghostSpeed = percent(levelData[2]);
		ghostSpeedTunnel = percent(levelData[3]);
		elroy1DotsLeft = (int) levelData[4];
		elroy1Speed = percent(levelData[5]);
		elroy2DotsLeft = (int) levelData[6];
		elroy2Speed = percent(levelData[7]);
		playerSpeedPowered = percent(levelData[8]);
		ghostSpeedFrightened = percent(levelData[9]);
		ghostFrightenedSeconds = (int) levelData[10];
		numFlashes = (int) levelData[11];
		totalFoodCount = (int) world.tiles().filter(world::isFoodTile).count();
		foodRemaining = totalFoodCount;
		eaten = new BitSet();

		long energizerCount = world.tiles().filter(world::isEnergizerTile).count();
		log("Level %d loaded. Total food: %d (%d pellets, %d energizers)", levelNumber, totalFoodCount,
				totalFoodCount - energizerCount, energizerCount);
	}

	protected Ghost[] createGhosts(String redGhostName, String pinkGhostName, String cyanGhostName,
			String orangeGhostName) {

		Ghost redGhost = new Ghost(RED_GHOST, redGhostName);
		Ghost pinkGhost = new Ghost(PINK_GHOST, pinkGhostName);
		Ghost cyanGhost = new Ghost(CYAN_GHOST, cyanGhostName);
		Ghost orangeGhost = new Ghost(ORANGE_GHOST, orangeGhostName);

		// Red ghost chases Pac-Man directly
		redGhost.fnChasingTargetTile = player::tile;

		// Pink ghost's target is two tiles ahead of Pac-Man (simulate overflow bug when player looks up)
		pinkGhost.fnChasingTargetTile = () -> player.dir() == Direction.UP ? player.tilesAhead(4).plus(-4, 0)
				: player.tilesAhead(4);

		// For cyan ghost's target, see Pac-Man dossier (simulate overflow bug when player looks up)
		cyanGhost.fnChasingTargetTile = () -> player.dir() == Direction.UP
				? player.tilesAhead(2).plus(-2, 0).scaled(2).minus(redGhost.tile())
				: player.tilesAhead(2).scaled(2).minus(redGhost.tile());

		// Orange ghost's target is either Pac-Man tile or scatter tile #3 at the lower left maze corner
		orangeGhost.fnChasingTargetTile = () -> orangeGhost.tile().euclideanDistance(player.tile()) < 8
				? world.ghostScatterTile(3)
				: player.tile();

		return Stream.of(redGhost, pinkGhost, cyanGhost, orangeGhost).toArray(Ghost[]::new);
	}

	public String levelSymbol(int levelNumber) {
		return levelCounter.get(levelNumber - 1);
	}

	public Stream<Ghost> ghosts() {
		return Stream.of(ghosts);
	}

	public Stream<Ghost> ghosts(GhostState state) {
		return ghosts().filter(ghost -> ghost.state == state);
	}

	public Ghost ghost(int id) {
		return ghosts[id];
	}

	public void hideGhosts() {
		for (Ghost ghost : ghosts) {
			ghost.hide();
		}
	}

	public void showGhosts() {
		for (Ghost ghost : ghosts) {
			ghost.show();
		}
	}

	public void resetGhostBounty() {
		ghostBounty = firstGhostBounty;
	}

	public void increaseGhostBounty() {
		ghostBounty *= 2;
	}

	public boolean isBonusReached() {
		return eatenFoodCount() == world.pelletsToEatForBonus(0) || eatenFoodCount() == world.pelletsToEatForBonus(1);
	}

	public void saveHiscore() {
		Hiscore hiscore = new Hiscore(hiscorePath).load();
		if (hiscorePoints > hiscore.points) {
			hiscore.points = hiscorePoints;
			hiscore.level = hiscoreLevel;
			hiscore.save();
			log("New hiscore: %d points in level %d.", hiscore.points, hiscore.level);
		}
	}

	public int eatenFoodCount() {
		return totalFoodCount - foodRemaining;
	}

	public boolean isFoodEaten(V2i tile) {
		return eaten.get(world.index(tile));
	}

	public boolean containsFood(V2i tile) {
		return world.isFoodTile(tile) && !isFoodEaten(tile);
	}

	public void removeFood(V2i tile) {
		if (containsFood(tile)) {
			eaten.set(world.index(tile));
			--foodRemaining;
		}
	}
}
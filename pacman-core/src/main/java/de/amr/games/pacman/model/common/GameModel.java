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
import java.util.OptionalInt;
import java.util.stream.Stream;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Hiscore;
import de.amr.games.pacman.lib.V2d;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.pacman.entities.Bonus;
import de.amr.games.pacman.model.world.PacManGameWorld;

/**
 * Common part of all game models.
 * 
 * @author Armin Reichert
 */
public abstract class GameModel {

	public static final int RED_GHOST = 0, PINK_GHOST = 1, CYAN_GHOST = 2, ORANGE_GHOST = 3;

	private static float percent(Object value) {
		return ((int) value) / 100f;
	}

	//@formatter:off
	public final int[][] HUNTING_PHASE_TICKS = {
  //  scatter  chase   scatter  chase  scatter  chase    scatter  chase
		{ 7*60,    20*60,  7*60,    20*60, 5*60,      20*60, 5*60,    Integer.MAX_VALUE },
		{ 7*60,    20*60,  7*60,    20*60, 5*60,    1033*60,    1,    Integer.MAX_VALUE },
		{ 5*60,    20*60,  5*60,    20*60, 5*60,    1037*60,    1,    Integer.MAX_VALUE },
	};
	//@formatter:on

	/** 1-based level number */
	public int levelNumber;

	/** 1-based maze number */
	public int mazeNumber;

	/** 1-based map number */
	public int mapNumber;

	public PacManGameWorld world;

	public String bonusSymbol;

	public float playerSpeed;

	public float ghostSpeed;

	public float ghostSpeedTunnel;

	public int elroy1DotsLeft;

	public float elroy1Speed;

	public int elroy2DotsLeft;

	public float elroy2Speed;

	public float playerSpeedPowered;

	public float ghostSpeedFrightened;

	public int ghostFrightenedSeconds;

	public int numFlashes;

	// food
	private BitSet eaten;

	public int totalFoodCount;

	public int energizerCount;

	public int foodRemaining;

	public Pac player;

	public Ghost[] ghosts;

	public Bonus bonus;

	public int initialLives;

	public int lives;

	public int score;

	public int numGhostsKilled;

	public int hiscoreLevel;

	public int hiscorePoints;

	public String hiscoreFilename;

	public int pelletValue;

	public int energizerValue;

	public int ghostBounty;

	public int firstGhostBounty;

	public List<String> levelCounter = new ArrayList<>();

	public int globalDotCounter;

	public boolean globalDotCounterEnabled;

	/**
	 * Enters given game level.
	 * 
	 * @param levelNumber 1-based level number
	 */
	public abstract void enterLevel(int levelNumber);

	/**
	 * @param symbolName bonus symbol identifier
	 * @return value of this bonus symbol
	 */
	public abstract int bonusValue(String symbolName);

	protected GameModel(String hiscoreFilename) {
		this.hiscoreFilename = hiscoreFilename;
		initialLives = 3;
		pelletValue = 10;
		energizerValue = 50;
		firstGhostBounty = 200;
	}

	public void reset() {
		score = 0;
		lives = initialLives;
		levelCounter.clear();
		Hiscore hiscore = loadHiscore();
		hiscoreLevel = hiscore.level;
		hiscorePoints = hiscore.points;
		enterLevel(1);
	}

	public void resetGuys() {
		player.placeAt(world.playerHomeTile(), HTS, 0);
		player.setDir(world.playerStartDirection());
		player.setWishDir(world.playerStartDirection());
		player.visible = true;
		player.velocity = V2d.NULL;
		player.targetTile = null; // used in autopilot mode
		player.stuck = false;
		player.forcedOnTrack = true;
		player.dead = false;
		player.restingTicksLeft = 0;
		player.starvingTicks = 0;
		player.powerTimer.reset();

		for (Ghost ghost : ghosts) {
			ghost.placeAt(ghost.homeTile, HTS, 0);
			ghost.setDir(world.ghostStartDirection(ghost.id));
			ghost.setWishDir(world.ghostStartDirection(ghost.id));
			ghost.visible = true;
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
		energizerCount = (int) world.tiles().filter(world::isEnergizerTile).count();
		foodRemaining = totalFoodCount;
		eaten = new BitSet();

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

	public OptionalInt intermissionAfterLevel(int levelNumber) {
		switch (levelNumber) {
		case 2:
			return OptionalInt.of(1);
		case 5:
			return OptionalInt.of(2);
		case 9:
		case 13:
		case 17:
			return OptionalInt.of(3);
		default:
			return OptionalInt.empty();
		}
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

	public void resetGhostBounty() {
		ghostBounty = firstGhostBounty;
	}

	public void increaseGhostBounty() {
		ghostBounty *= 2;
	}

	public boolean isBonusReached() {
		return eatenFoodCount() == world.pelletsToEatForBonus(0) || eatenFoodCount() == world.pelletsToEatForBonus(1);
	}

	public long getHuntingPhaseDuration(int phase) {
		int row = levelNumber == 1 ? 0 : levelNumber <= 4 ? 1 : 2;
		return HUNTING_PHASE_TICKS[row][phase];
	}

	public void saveHiscore() {
		Hiscore hiscore = loadHiscore();
		if (hiscorePoints > hiscore.points) {
			hiscore.points = hiscorePoints;
			hiscore.level = hiscoreLevel;
			hiscore.save();
			log("New hiscore: %d points in level %d.", hiscore.points, hiscore.level);
		}
	}

	private Hiscore loadHiscore() {
		File dir = new File(System.getProperty("user.home"));
		Hiscore hiscore = new Hiscore(new File(dir, hiscoreFilename));
		hiscore.load();
		return hiscore;
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
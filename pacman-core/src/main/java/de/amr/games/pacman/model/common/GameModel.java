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
import static de.amr.games.pacman.model.world.World.HTS;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Hiscore;
import de.amr.games.pacman.lib.TickTimer;
import de.amr.games.pacman.lib.V2d;
import de.amr.games.pacman.model.pacman.entities.Bonus;
import de.amr.games.pacman.model.world.World;

/**
 * Common part of the Pac-Man and Ms. Pac-Man game models.
 * 
 * @author Armin Reichert
 */
public abstract class GameModel {

	/** Ghost ID. */
	public static final int RED_GHOST = 0, PINK_GHOST = 1, CYAN_GHOST = 2, ORANGE_GHOST = 3;

	/** Speed in pixels/tick at 100%. */
	public static final double BASE_SPEED = 1.25;

	/** 1-based level number */
	public int levelNumber;

	/** 1-based maze number */
	public int mazeNumber;

	/** World of current level. */
	public World world;

	/** The hunting phase. Values: 0, 2, 4, 6 = "scattering", 1, 3, 5, 7 = "chasing". */
	public int huntingPhase;

	/** Tells if the current hunting phase is "scattering". */
	public boolean inScatteringPhase() {
		return huntingPhase % 2 == 0;
	}

	/** The durations of the hunting phases. */
	//@formatter:off
	public long[][] huntingPhaseDurationsTable = new long[][] {
	  // scatter  chase   scatter  chase  scatter  chase    scatter  chase
	   { 7*60,    20*60,  7*60,    20*60, 5*60,      20*60, 5*60,    TickTimer.INDEFINITE },
	   { 7*60,    20*60,  7*60,    20*60, 5*60,    1033*60,    1,    TickTimer.INDEFINITE },
	   { 5*60,    20*60,  5*60,    20*60, 5*60,    1037*60,    1,    TickTimer.INDEFINITE },
	};
	//@formatter:on

	/** The currently active row in the {@link #huntingPhaseDurationsTable}. */
	public long[] huntingPhaseDurations;

	/** Bonus symbol of current level. */
	public int bonusSymbol;

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
	public List<Integer> levelCounter = new ArrayList<>();

	/** Counter used by ghost house logic. */
	public int globalDotCounter;

	/** Enabled state of the counter used by ghost house logic. */
	public boolean globalDotCounterEnabled;

	/** Level at which current high score has been reached. */
	public int hiscoreLevel;

	/** Points scored at current high score. */
	public int hiscorePoints;

	/** High score file of current game variant. */
	public File hiscoreFile;

	/**
	 * Initializes model for given game level.
	 * 
	 * @param levelNumber 1-based level number
	 */
	public abstract void setLevel(int levelNumber);

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
		Hiscore hiscore = new Hiscore(hiscoreFile).load();
		hiscoreLevel = hiscore.level;
		hiscorePoints = hiscore.points;
		setLevel(1);
	}

	public void resetGuys() {
		player.placeAt(world.playerHomeTile(), HTS, 0);
		player.setMoveDir(world.playerStartDirection());
		player.setWishDir(world.playerStartDirection());
		player.hide();
		player.velocity = V2d.NULL;
		player.targetTile = null; // used in autopilot mode
		player.stuck = false;
		player.forcedOnTrack = true;
		player.killed = false;
		player.restingTicksLeft = 0;
		player.starvingTicks = 0;
		player.powerTimer.setIndefinite();

		for (Ghost ghost : ghosts) {
			ghost.placeAt(ghost.homeTile, HTS, 0);
			ghost.setMoveDir(world.ghostStartDirection(ghost.id));
			ghost.setWishDir(world.ghostStartDirection(ghost.id));
			ghost.hide();
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

	/**
	 * @param percentValue percentage value
	 * @return corresponding fraction, e.g. {@code 0.8} for {@code 80%}
	 */
	private float fraction(Object percentValue) {
		return ((Integer) percentValue) / 100f;
	}

	protected void initLevel(int levelNumber, Object[] data) {
		this.levelNumber = levelNumber;
		bonusSymbol = (int) data[0];
		playerSpeed = fraction(data[1]);
		ghostSpeed = fraction(data[2]);
		ghostSpeedTunnel = fraction(data[3]);
		elroy1DotsLeft = (int) data[4];
		elroy1Speed = fraction(data[5]);
		elroy2DotsLeft = (int) data[6];
		elroy2Speed = fraction(data[7]);
		playerSpeedPowered = fraction(data[8]);
		ghostSpeedFrightened = fraction(data[9]);
		ghostFrightenedSeconds = (int) data[10];
		numFlashes = (int) data[11];
	}

	protected Ghost[] createGhosts(String redGhostName, String pinkGhostName, String cyanGhostName,
			String orangeGhostName) {

		Ghost redGhost = new Ghost(RED_GHOST, redGhostName);
		Ghost pinkGhost = new Ghost(PINK_GHOST, pinkGhostName);
		Ghost cyanGhost = new Ghost(CYAN_GHOST, cyanGhostName);
		Ghost orangeGhost = new Ghost(ORANGE_GHOST, orangeGhostName);

		// Red ghost chases Pac-Man directly
		redGhost.fnChasingTargetTile = player::tile;

		// Pink ghost's target is two tiles ahead of Pac-Man (simulate overflow bug when player moves up)
		pinkGhost.fnChasingTargetTile = () -> player.moveDir() != Direction.UP //
				? player.tilesAhead(4)
				: player.tilesAhead(4).plus(-4, 0);

		// For cyan ghost's target, see Pac-Man dossier (simulate overflow bug when player moves up)
		cyanGhost.fnChasingTargetTile = () -> player.moveDir() != Direction.UP //
				? player.tilesAhead(2).scaled(2).minus(redGhost.tile())
				: player.tilesAhead(2).plus(-2, 0).scaled(2).minus(redGhost.tile());

		// Orange ghost's target is either Pac-Man tile or scatter tile #3 at the lower left maze corner
		orangeGhost.fnChasingTargetTile = () -> orangeGhost.tile().euclideanDistance(player.tile()) < 8
				? world.ghostScatterTile(3)
				: player.tile();

		return Stream.of(redGhost, pinkGhost, cyanGhost, orangeGhost).toArray(Ghost[]::new);
	}

	protected void resetGhosts(World world) {
		for (Ghost ghost : ghosts) {
			ghost.world = world;
			ghost.dotCounter = 0;
			ghost.elroy = 0;
		}

		ghosts[RED_GHOST].homeTile = world.ghostHouse().entryTile();
		ghosts[RED_GHOST].revivalTile = world.ghostHouse().seat(1);
		ghosts[RED_GHOST].globalDotLimit = Integer.MAX_VALUE;
		ghosts[RED_GHOST].privateDotLimit = 0;

		ghosts[PINK_GHOST].homeTile = world.ghostHouse().seat(1);
		ghosts[PINK_GHOST].revivalTile = world.ghostHouse().seat(1);
		ghosts[PINK_GHOST].globalDotLimit = 7;
		ghosts[PINK_GHOST].privateDotLimit = 0;

		ghosts[CYAN_GHOST].homeTile = world.ghostHouse().seat(0);
		ghosts[CYAN_GHOST].revivalTile = world.ghostHouse().seat(0);
		ghosts[CYAN_GHOST].globalDotLimit = 17;
		ghosts[CYAN_GHOST].privateDotLimit = levelNumber == 1 ? 30 : 0;

		ghosts[ORANGE_GHOST].homeTile = world.ghostHouse().seat(2);
		ghosts[ORANGE_GHOST].revivalTile = world.ghostHouse().seat(2);
		ghosts[ORANGE_GHOST].globalDotLimit = Integer.MAX_VALUE;
		ghosts[ORANGE_GHOST].privateDotLimit = levelNumber == 1 ? 60 : levelNumber == 2 ? 50 : 0;
	}

	public int levelSymbol(int levelNumber) {
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

	public void showGuys() {
		showGhosts();
		player.show();
	}

	public void hideGuys() {
		hideGhosts();
		player.hide();
	}

	public boolean isBonusReached() {
		return world.eatenFoodCount() == world.pelletsToEatForBonus(0)
				|| world.eatenFoodCount() == world.pelletsToEatForBonus(1);
	}

	public abstract long bonusActivationTicks();

	public void saveHiscore() {
		Hiscore hiscore = new Hiscore(hiscoreFile).load();
		if (hiscorePoints > hiscore.points) {
			hiscore.points = hiscorePoints;
			hiscore.level = hiscoreLevel;
			hiscore.save();
			log("New hiscore: %d points in level %d.", hiscore.points, hiscore.level);
		}
	}
}
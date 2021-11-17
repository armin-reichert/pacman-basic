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
import java.util.List;
import java.util.OptionalInt;
import java.util.stream.Stream;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Hiscore;
import de.amr.games.pacman.lib.V2d;
import de.amr.games.pacman.model.pacman.entities.Bonus;
import de.amr.games.pacman.model.world.PacManGameWorld;

/**
 * Common part of game model for all variants.
 * 
 * @author Armin Reichert
 */
public abstract class AbstractGameModel implements PacManGameModel {

	//@formatter:off
	protected final int[][] HUNTING_PHASE_TICKS = {
		{ 7 * 60, 20 * 60, 7 * 60, 20 * 60, 5 * 60,   20 * 60,  5 * 60, Integer.MAX_VALUE },
		{ 7 * 60, 20 * 60, 7 * 60, 20 * 60, 5 * 60, 1033 * 60,       1, Integer.MAX_VALUE },
		{ 5 * 60, 20 * 60, 5 * 60, 20 * 60, 5 * 60, 1037 * 60,       1, Integer.MAX_VALUE },
	};
	//@formatter:on

	protected GameLevel level;
	protected Pac player;
	protected Ghost[] ghosts;
	protected Bonus bonus;
	protected int initialLives;
	protected int lives;
	protected int score;
	protected int hiscoreLevel;
	protected int hiscorePoints;
	protected int pelletValue;
	protected int energizerValue;
	protected int ghostBounty;
	protected int firstGhostBounty;
	protected List<String> levelCounter = new ArrayList<>();
	protected int dotCounter;
	protected boolean dotCounterEnabled;

	public AbstractGameModel() {
		initialLives = 3;
		pelletValue = 10;
		energizerValue = 50;
		firstGhostBounty = 200;
	}

	protected abstract String hiscoreFilename();

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
				? level.world.ghostScatterTile(3)
				: player.tile();

		return Stream.of(redGhost, pinkGhost, cyanGhost, orangeGhost).toArray(Ghost[]::new);
	}

	@Override
	public GameLevel level() {
		return level;
	}

	@Override
	public String levelSymbol(int levelNumber) {
		return levelCounter.get(levelNumber - 1);
	}

	@Override
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

	@Override
	public int lives() {
		return lives;
	}

	@Override
	public void changeLivesBy(int delta) {
		lives = Math.max(0, lives + delta);
	}

	@Override
	public int pelletValue() {
		return pelletValue;
	}

	@Override
	public int energizerValue() {
		return energizerValue;
	}

	@Override
	public int score() {
		return score;
	}

	@Override
	public void addScore(int points) {
		score += points;
	}

	@Override
	public int hiscorePoints() {
		return hiscorePoints;
	}

	@Override
	public void setHiscorePoints(int points) {
		hiscorePoints = points;
	}

	@Override
	public int hiscoreLevel() {
		return hiscoreLevel;
	}

	@Override
	public void setHiscoreLevel(int number) {
		hiscoreLevel = number;
	}

	@Override
	public Pac player() {
		return player;
	}

	@Override
	public Stream<Ghost> ghosts() {
		return Stream.of(ghosts);
	}

	@Override
	public Stream<Ghost> ghosts(GhostState state) {
		return ghosts().filter(ghost -> ghost.state == state);
	}

	@Override
	public Ghost ghost(int id) {
		return ghosts[id];
	}

	@Override
	public int getNextGhostBounty() {
		return ghostBounty;
	}

	@Override
	public void resetGhostBounty() {
		ghostBounty = firstGhostBounty;
	}

	@Override
	public void increaseNextGhostBounty() {
		ghostBounty *= 2;
	}

	@Override
	public Bonus bonus() {
		return bonus;
	}

	@Override
	public boolean isBonusReached() {
		return level.eatenFoodCount() == level.world.pelletsToEatForBonus(0)
				|| level.eatenFoodCount() == level.world.pelletsToEatForBonus(1);
	}

	@Override
	public void resetGuys() {
		final PacManGameWorld world = level.world;

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

	@Override
	public void reset() {
		score = 0;
		lives = initialLives;
		levelCounter.clear();
		Hiscore hiscore = loadHiscore();
		hiscoreLevel = hiscore.level;
		hiscorePoints = hiscore.points;
		enterLevel(1);
	}

	@Override
	public long getHuntingPhaseDuration(int phase) {
		int row = level.number == 1 ? 0 : level.number <= 4 ? 1 : 2;
		return HUNTING_PHASE_TICKS[row][phase];
	}

	@Override
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
		Hiscore hiscore = new Hiscore(new File(dir, hiscoreFilename()));
		hiscore.load();
		return hiscore;
	}

	@Override
	public int globalDotCounter() {
		return dotCounter;
	}

	@Override
	public void setGlobalDotCounter(int globalDotCounter) {
		this.dotCounter = globalDotCounter;
	}

	@Override
	public boolean isGlobalDotCounterEnabled() {
		return dotCounterEnabled;
	}

	@Override
	public void enableGlobalDotCounter(boolean enable) {
		dotCounterEnabled = enable;
	}
}
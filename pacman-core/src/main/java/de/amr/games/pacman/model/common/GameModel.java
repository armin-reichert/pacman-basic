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

import static de.amr.games.pacman.lib.Direction.LEFT;
import static de.amr.games.pacman.lib.Direction.UP;
import static de.amr.games.pacman.model.common.actors.Ghost.ID_CYAN_GHOST;
import static de.amr.games.pacman.model.common.actors.Ghost.ID_ORANGE_GHOST;
import static de.amr.games.pacman.model.common.actors.Ghost.ID_PINK_GHOST;
import static de.amr.games.pacman.model.common.actors.Ghost.ID_RED_GHOST;
import static de.amr.games.pacman.model.common.actors.GhostState.FRIGHTENED;
import static de.amr.games.pacman.model.common.actors.GhostState.HUNTING_PAC;
import static de.amr.games.pacman.model.common.actors.GhostState.LEAVING_HOUSE;
import static de.amr.games.pacman.model.common.actors.GhostState.LOCKED;
import static de.amr.games.pacman.model.common.world.World.HTS;
import static de.amr.games.pacman.model.common.world.World.TS;

import java.io.File;
import java.time.LocalDate;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.event.GameEvents;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.TickTimer;
import de.amr.games.pacman.lib.V2d;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.lib.animation.EntityAnimation;
import de.amr.games.pacman.lib.animation.EntityAnimationSet;
import de.amr.games.pacman.lib.animation.SingleEntityAnimation;
import de.amr.games.pacman.model.common.actors.AnimKeys;
import de.amr.games.pacman.model.common.actors.Bonus;
import de.amr.games.pacman.model.common.actors.Creature;
import de.amr.games.pacman.model.common.actors.Entity;
import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.model.common.actors.GhostState;
import de.amr.games.pacman.model.common.actors.Pac;
import de.amr.games.pacman.model.common.world.ArcadeGhostHouse;
import de.amr.games.pacman.model.common.world.ArcadeWorld;

/**
 * Common part of the Pac-Man and Ms. Pac-Man game models.
 * 
 * @author Armin Reichert
 */
public abstract class GameModel {

	private static final Logger LOGGER = LogManager.getFormatterLogger();

	/** Speed in pixels/tick at 100%. */
	public static final double BASE_SPEED = 1.25;

	/** Game loop speed in ticks/sec. */
	public static final int FPS = 60;

	protected static final long[][] HUNTING_TIMES = {
	//@formatter:off
	{ 7*FPS, 20*FPS, 7*FPS, 20*FPS, 5*FPS,   20*FPS, 5*FPS, TickTimer.INDEFINITE },
	{ 7*FPS, 20*FPS, 7*FPS, 20*FPS, 5*FPS, 1033*FPS,     1, TickTimer.INDEFINITE },
	{ 5*FPS, 20*FPS, 5*FPS, 20*FPS, 5*FPS, 1037*FPS,     1, TickTimer.INDEFINITE }
	//@formatter:on
	};

	public static final int MAX_CREDIT = 99;
	public static final int PELLET_VALUE = 10;
	public static final int PELLET_RESTING_TICKS = 1;
	public static final int ENERGIZER_VALUE = 50;
	public static final int ENERGIZER_RESTING_TICKS = 3;
	public static final int INITIAL_LIVES = 3;
	public static final int ALL_GHOSTS_KILLED_POINTS = 12_000;
	public static final int EXTRA_LIFE = 10_000;

	// not sure exactly how long Pac-Man is losing power
	public static final int PAC_POWER_FADING_TICKS = 2 * FPS;

	public static final double GHOST_SPEED_HOUSE = 0.5; // unsure
	public static final double GHOST_SPEED_RETURNING = 2.0; // unsure

	protected static final int[] GHOST_VALUES = { 200, 400, 800, 1600 };

	/** Credit for playing. */
	protected int credit;

	/** Tells if the game play is active. */
	protected boolean playing;

	/** Pac-Man or Ms. Pac-Man. */
	protected final Pac pac;

	/** Controls the time Pac has power. */
	protected final TickTimer powerTimer = new TickTimer("Pac-power-timer", FPS);

	/** Tells if Pac-Man can be killed by ghosts. */
	protected boolean immune;

	/** If Pac-Man is controlled by autopilot. */
	protected boolean autoControlled;

	/** The ghosts in order RED, PINK, CYAN, ORANGE. */
	protected final Ghost[] theGhosts;

	/** "Cruise Elroy" state. Values: <code>0, 1, 2, -1, -2 (0= "off", negative value = "disabled")</code>. */
	protected byte cruiseElroyState;

	/** The position of the ghosts when the game starts. */
	protected final V2d[] ghostHomePosition = new V2d[4];

	/** The tiles inside the house where the ghosts get revived. Amen. */
	protected final V2d[] ghostRevivalPosition = new V2d[4];

	/** The (unreachable) tiles in the corners targeted during the scatter phase. */
	protected final V2i[] ghostScatterTile = new V2i[4];

	/** Timer used to control hunting phases. */
	protected final HuntingTimer huntingTimer = new HuntingTimer();

	/** Current level. */
	protected GameLevel level;

	/** Number of lives remaining. */
	protected int lives;

	/** If lives or one less is displayed in lives counter. */
	protected boolean livesOneLessShown;

	/** Number of ghosts killed at the current level. */
	protected int numGhostsKilledInLevel;

	/** Number of ghosts killed by current energizer. */
	protected int ghostsKilledByEnergizer;

	/** List of collected level symbols. */
	protected final LevelCounter levelCounter = new LevelCounter();

	/** Energizer animation. */
	public final SingleEntityAnimation<Boolean> energizerPulse = SingleEntityAnimation.pulse(10);

	/** Counters used by ghost house logic. */
	protected final int[] ghostDotCounter = new int[4];

	/** Counter used by ghost house logic. */
	protected int globalDotCounter;

	/** Enabled state of the counter used by ghost house logic. */
	protected boolean globalDotCounterEnabled;

	/** Max number of clock ticks Pac can be starving until ghost gets unlocked. */
	protected int pacStarvingTimeLimit;

	protected byte[] globalDotLimits;

	protected byte[] privateDotLimits;

	private final Score gameScore = new Score("SCORE");

	private final Score highScore = new Score("HIGH SCORE");

	protected File highScoreFile;

	protected boolean scoresEnabled;

	/** Stores what happened during the last tick. */
	public final Memory memo = new Memory();

	protected GameModel(String pacName, String redGhostName, String pinkGhostName, String cyanGhostName,
			String orangeGhostName) {

		pac = new Pac(pacName);

		var redGhost = new Ghost(ID_RED_GHOST, redGhostName);
		var pinkGhost = new Ghost(ID_PINK_GHOST, pinkGhostName);
		var cyanGhost = new Ghost(ID_CYAN_GHOST, cyanGhostName);
		var orangeGhost = new Ghost(ID_ORANGE_GHOST, orangeGhostName);
		theGhosts = new Ghost[] { redGhost, pinkGhost, cyanGhost, orangeGhost };

		// The "AI" which defines the different ghost characters
		redGhost.setChasingTarget(pac::tile);
		pinkGhost.setChasingTarget(() -> tilesAhead(pac, 4));
		cyanGhost.setChasingTarget(() -> tilesAhead(pac, 2).scaled(2).minus(redGhost.tile()));
		orangeGhost.setChasingTarget(
				() -> orangeGhost.tile().euclideanDistance(pac.tile()) < 8 ? ghostScatterTile[ID_ORANGE_GHOST] : pac.tile());
	}

	public abstract GameVariant variant();

	public boolean isPlaying() {
		return playing;
	}

	public void setPlaying(boolean playing) {
		this.playing = playing;
	}

	public LevelCounter levelCounter() {
		return levelCounter;
	}

	public Score gameScore() {
		return gameScore;
	}

	public Score highScore() {
		return highScore;
	}

	public File highScoreFile() {
		return highScoreFile;
	}

	public void setHiscoreFile(File file) {
		this.highScoreFile = file;
		ScoreManager.loadScore(highScore, file);
	}

	public void enableScores(boolean enabled) {
		this.scoresEnabled = enabled;
	}

	public void scorePoints(int points) {
		if (!scoresEnabled) {
			return;
		}
		int scoreBeforeAddingPoints = gameScore.points;
		gameScore.points += points;
		if (gameScore.points > highScore.points) {
			highScore.points = gameScore.points;
			highScore.levelNumber = level.number();
			highScore.date = LocalDate.now();
		}
		if (scoreBeforeAddingPoints < GameModel.EXTRA_LIFE && gameScore.points >= GameModel.EXTRA_LIFE) {
			lives++;
			GameEvents.publish(new GameEvent(this, GameEventType.PLAYER_GETS_EXTRA_LIFE, null, pac.tile()));
		}
	}

	// simulates the overflow bug from the original Arcade version
	private static V2i tilesAhead(Creature guy, int n) {
		var ahead = guy.tile().plus(guy.moveDir().vec.scaled(n));
		return guy.moveDir() == UP ? ahead.minus(n, 0) : ahead;
	}

	public int credit() {
		return credit;
	}

	public void setCredit(int credit) {
		if (0 <= credit && credit <= MAX_CREDIT) {
			this.credit = credit;
		}
	}

	public boolean addCredit() {
		if (credit < MAX_CREDIT) {
			++credit;
			return true;
		}
		return false;
	}

	public void consumeCredit() {
		if (credit > 0) {
			--credit;
		}
	}

	public boolean hasCredit() {
		return credit > 0;
	}

	public void reset() {
		globalDotCounter = 0;
		globalDotCounterEnabled = false;
		playing = false;
		lives = INITIAL_LIVES;
		livesOneLessShown = false;
		gameScore.reset();
		ScoreManager.loadScore(highScore, highScoreFile);
	}

	public GameLevel level() {
		return level;
	}

	public void startLevel() {
		levelCounter.addSymbol(level.bonusIndex());
		resetGuys();
		guys().forEach(Entity::hide);
	}

	public void endLevel() {
		huntingTimer.stop();
		bonus().setInactive();
		pac.setAbsSpeed(0);
		pac.animationSet().ifPresent(EntityAnimationSet::reset);
		ghosts().forEach(Ghost::hide);
		energizerPulse.reset();
	}

	protected void initGhosts() {
		for (var ghost : theGhosts) {
			ghostDotCounter[ghost.id] = 0;
		}
		cruiseElroyState = 0;
		if (level.world() instanceof ArcadeWorld) {
			ghostHomePosition[ID_RED_GHOST] = halfTileRightOf(ArcadeGhostHouse.ENTRY_TILE);
			ghostRevivalPosition[ID_RED_GHOST] = halfTileRightOf(ArcadeGhostHouse.SEAT_TILE_CENTER);
			ghostScatterTile[ID_RED_GHOST] = ArcadeWorld.SCATTER_TILE_NE;

			ghostHomePosition[ID_PINK_GHOST] = halfTileRightOf(ArcadeGhostHouse.SEAT_TILE_CENTER);
			ghostRevivalPosition[ID_PINK_GHOST] = halfTileRightOf(ArcadeGhostHouse.SEAT_TILE_CENTER);
			ghostScatterTile[ID_PINK_GHOST] = ArcadeWorld.SCATTER_TILE_NW;

			ghostHomePosition[ID_CYAN_GHOST] = halfTileRightOf(ArcadeGhostHouse.SEAT_TILE_LEFT);
			ghostRevivalPosition[ID_CYAN_GHOST] = halfTileRightOf(ArcadeGhostHouse.SEAT_TILE_LEFT);
			ghostScatterTile[ID_CYAN_GHOST] = ArcadeWorld.SCATTER_TILE_SE;

			ghostHomePosition[ID_ORANGE_GHOST] = halfTileRightOf(ArcadeGhostHouse.SEAT_TILE_RIGHT);
			ghostRevivalPosition[ID_ORANGE_GHOST] = halfTileRightOf(ArcadeGhostHouse.SEAT_TILE_RIGHT);
			ghostScatterTile[ID_ORANGE_GHOST] = ArcadeWorld.SCATTER_TILE_SW;
		}
	}

	private static V2d halfTileRightOf(V2i tile) {
		return tile.scaled(TS).toDoubleVec().plus(HTS, 0);
	}

	public void resetGuys() {
		powerTimer.reset(0);
		energizerPulse.reset();
		pac.reset();
		pac.setPosition(level.world().pacStartPosition());
		pac.setMoveAndWishDir(Direction.LEFT);
		pac.show();
		pac.selectAndEnsureRunningAnimation(AnimKeys.PAC_MUNCHING).ifPresent(EntityAnimation::reset);
		ghosts().forEach(ghost -> {
			ghost.reset();
			ghost.setMoveAndWishDir(switch (ghost.id) {
			case Ghost.ID_RED_GHOST -> Direction.LEFT;
			case Ghost.ID_PINK_GHOST -> Direction.DOWN;
			case Ghost.ID_CYAN_GHOST, Ghost.ID_ORANGE_GHOST -> Direction.UP;
			default -> throw new IllegalArgumentException("Ghost ID: " + ghost.id);
			});
			ghost.setPosition(ghostHomePosition[ghost.id]);
			ghost.show();
			ghost.enterStateLocked();
		});
	}

	/**
	 * @return Pac-Man and the ghosts in order RED, PINK, CYAN, ORANGE
	 */
	public Stream<Creature> guys() {
		return Stream.of(pac, theGhosts[ID_RED_GHOST], theGhosts[ID_PINK_GHOST], theGhosts[ID_CYAN_GHOST],
				theGhosts[ID_ORANGE_GHOST]);
	}

	/**
	 * @return Pac-Man or Ms. Pac-Man
	 */
	public Pac pac() {
		return pac;
	}

	public boolean isAutoControlled() {
		return autoControlled;
	}

	public void setAutoControlled(boolean autoControlled) {
		this.autoControlled = autoControlled;
	}

	public boolean isImmune() {
		return immune;
	}

	public void setImmune(boolean immune) {
		this.immune = immune;
	}

	public int lives() {
		return lives;
	}

	public void setLives(int lives) {
		this.lives = lives;
	}

	public boolean isLivesOneLessShown() {
		return livesOneLessShown;
	}

	public void setLivesOneLessShown(boolean livesOneLessShown) {
		this.livesOneLessShown = livesOneLessShown;
	}

	/**
	 * @param id ghost ID (0, 1, 2, 3)
	 * @return the ghost with the given ID
	 */
	public Ghost ghost(int id) {
		checkGhostID(id);
		return theGhosts[id];
	}

	private static void checkGhostID(int id) {
		if (id < 0 || id > 3) {
			throw new IllegalArgumentException("Illegal ghost ID: %d".formatted(id));
		}
	}

	/**
	 * @param states states specifying which ghosts are returned
	 * @return all ghosts which are in any of the given states or all ghosts, if no states are specified
	 */
	public Stream<Ghost> ghosts(GhostState... states) {
		// when no state is given, return all ghosts (Ghost.is() would return no ghosts!)
		if (states.length > 0) {
			return Stream.of(theGhosts).filter(ghost -> ghost.is(states));
		}
		return Stream.of(theGhosts);
	}

	/**
	 * @param id ghost ID (0, 1, 2, 3)
	 * @return home position of ghost
	 */
	public V2d ghostHomePosition(int id) {
		checkGhostID(id);
		return ghostHomePosition[id];
	}

	/**
	 * @param id ghost ID (0, 1, 2, 3)
	 * @return revival position of ghost
	 */
	public V2d ghostRevivalPosition(int id) {
		checkGhostID(id);
		return ghostRevivalPosition[id];
	}

	/**
	 * @param id ghost ID (0, 1, 2, 3)
	 * @return scatter tile of ghost
	 */
	public V2i ghostScatterTile(int id) {
		checkGhostID(id);
		return ghostScatterTile[id];
	}

	/**
	 * @param ghost a ghost
	 * @param dir   a direction
	 * @return tells if the ghost can currently move towards the given direction
	 */
	public boolean isGhostAllowedMoving(Ghost ghost, Direction dir) {
		return true;
	}

	/**
	 * @param ghostKillIndex index telling when a ghost was killed using the same energizer (0..4)
	 * @return value of killed ghost (200, 400, 800, 1600)
	 */
	public int ghostValue(int ghostKillIndex) {
		return GHOST_VALUES[ghostKillIndex];
	}

	/**
	 * @return Blinky's current "cruise elroy" state. Values are 0 (no cruise elroy), 1, 2, -1, -2, wherer negative values
	 *         mean "disabled".
	 */
	public int blinkyCruiseElroyState() {
		return cruiseElroyState;
	}

	/**
	 * Initializes the model for given game level.
	 * 
	 * @param levelNumber 1-based level number
	 */
	public abstract void setLevel(int levelNumber);

	// Hunting

	public HuntingTimer huntingTimer() {
		return huntingTimer;
	}

	/**
	 * Hunting happens in different phases. Phases 0, 2, 4, 6 are scattering phases where the ghosts target for their
	 * respective corners and circle around the walls in their corner, phases 1, 3, 5, 7 are chasing phases where the
	 * ghosts attack Pac-Man.
	 * 
	 * @param phase hunting phase (0..7)
	 */
	public void startHuntingPhase(int phase) {
		huntingTimer.startPhase(phase, huntingPhaseTicks(phase));
	}

	/**
	 * Advances the current hunting phase and enters the next phase when the current phase ends. On every change between
	 * phases, the living ghosts outside of the ghosthouse reverse their move direction.
	 */
	private void advanceHunting() {
		huntingTimer.advance();
		if (huntingTimer.hasExpired()) {
			startHuntingPhase(huntingTimer.phase() + 1);
			ghosts(HUNTING_PAC, FRIGHTENED, LOCKED, LEAVING_HOUSE).forEach(Ghost::forceTurningBack);
		}
	}

	/**
	 * @param phase hunting phase (0, ... 7)
	 * @return hunting (scattering or chasing) ticks for current level and given phase
	 */
	private long huntingPhaseTicks(int phase) {
		if (phase < 0 || phase > 7) {
			throw new IllegalArgumentException("Hunting phase must be 0..7, but is " + phase);
		}
		return switch (level.number()) {
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

	public TickTimer powerTimer() {
		return powerTimer;
	}

	// Bonus stuff

	public abstract Bonus bonus();

	protected abstract void onBonusReached();

	// Game logic

	public void update() {
		pac.update(this);
		updateGhosts();
		bonus().update(this);
		advanceHunting();
		energizerPulse.advance();
		powerTimer.advance();
	}

	public void whatHappenedWithFood() {
		checkFoodFound();
		if (memo.foodFound) {
			onFoodFound();
			if (memo.bonusReached) {
				onBonusReached();
			}
		} else {
			pac.starve();
		}
	}

	public void whatHappenedWithTheGuys() {
		if (memo.pacGotPower) {
			onPacGetsPower();
		}

		memo.pacMetKiller = isPacMeetingKiller();
		if (memo.pacMetKiller) {
			onPacMetKiller();
			return; // enter new game state
		}

		checkEdibleGhosts();
		if (memo.edibleGhosts.length > 0) {
			killGhosts(memo.edibleGhosts);
			memo.ghostsKilled = true;
			return; // enter new game state
		}
		checkPacPower();
		if (memo.pacPowerFading) {
			GameEvents.publish(GameEventType.PAC_STARTS_LOSING_POWER, pac.tile());
		}
		if (memo.pacPowerLost) {
			onPacPowerLost();
		}
	}

	private boolean isPacMeetingKiller() {
		return !immune && !powerTimer.isRunning() && ghosts(HUNTING_PAC).anyMatch(pac::sameTile);
	}

	private void onPacMetKiller() {
		pac.die();
		var redGhost = theGhosts[ID_RED_GHOST];
		if (cruiseElroyState > 0) {
			LOGGER.info("Cruise Elroy mode %d for %s disabled", cruiseElroyState, redGhost.name());
			cruiseElroyState = (byte) -cruiseElroyState; // negative value means "disabled"
		}
		globalDotCounter = 0;
		globalDotCounterEnabled = true;
		LOGGER.info("Global dot counter got reset and enabled because %s died", pac.name());
	}

	private void checkEdibleGhosts() {
		memo.edibleGhosts = ghosts(FRIGHTENED).filter(pac::sameTile).toArray(Ghost[]::new);
	}

	/**
	 * Cheat.
	 */
	public void killAllPossibleGhosts() {
		var prey = ghosts(GhostState.HUNTING_PAC, GhostState.FRIGHTENED).toArray(Ghost[]::new);
		ghostsKilledByEnergizer = 0;
		killGhosts(prey);
	}

	private void killGhosts(Ghost[] prey) {
		Stream.of(prey).forEach(this::killGhost);
		numGhostsKilledInLevel += prey.length;
		if (numGhostsKilledInLevel == 16) {
			LOGGER.info("All ghosts killed at level %d, Pac-Man wins additional %d points", level.number(),
					ALL_GHOSTS_KILLED_POINTS);
			scorePoints(ALL_GHOSTS_KILLED_POINTS);
		}
	}

	private void killGhost(Ghost ghost) {
		memo.killedGhosts.add(ghost);
		ghost.killedIndex = ghostsKilledByEnergizer;
		ghostsKilledByEnergizer++;
		ghost.enterStateEaten(this);
		int value = ghostValue(ghost.killedIndex);
		scorePoints(value);
		LOGGER.info("Ghost %s killed at tile %s, Pac-Man wins %d points", ghost.name(), ghost.tile(), value);
	}

	private void startPowerTimer(double seconds) {
		powerTimer.resetSeconds(seconds);
		powerTimer.start();
		LOGGER.info("Power timer started: %s", powerTimer);
	}

	private void checkPacPower() {
		memo.pacPowerFading = powerTimer.remaining() == PAC_POWER_FADING_TICKS;
		memo.pacPowerLost = powerTimer.hasExpired();
	}

	public boolean isPacPowerFading() {
		return powerTimer.isRunning() && powerTimer.remaining() <= PAC_POWER_FADING_TICKS;
	}

	private void onPacPowerLost() {
		LOGGER.info("%s lost power, timer=%s", pac.name(), powerTimer);
		// leave state EXPIRED to avoid repetitions:
		powerTimer.resetIndefinitely();
		huntingTimer.start();
		ghosts(FRIGHTENED).forEach(ghost -> ghost.enterStateHuntingPac(this));
		GameEvents.publish(GameEventType.PAC_LOSES_POWER, pac.tile());
	}

	private void checkFoodFound() {
		if (level.world().containsFood(pac.tile())) {
			memo.foodFound = true;
			memo.allFoodEaten = level.world().foodRemaining() == 1;
			if (level.world().isEnergizerTile(pac.tile())) {
				memo.energizerFound = true;
				if (level.ghostFrightenedSeconds() > 0) {
					memo.pacGotPower = true;
				}
			}
			memo.bonusReached = level.world().eatenFoodCount() == 70 || level.world().eatenFoodCount() == 170;
		}
	}

	private void onFoodFound() {
		if (memo.energizerFound) {
			ghostsKilledByEnergizer = 0;
			eatFood(ENERGIZER_VALUE, ENERGIZER_RESTING_TICKS);
		} else {
			eatFood(PELLET_VALUE, PELLET_RESTING_TICKS);
		}
	}

	private void eatFood(int value, int restingTicks) {
		pac.endStarving();
		pac.rest(restingTicks);
		level.world().removeFood(pac.tile());
		checkIfRedGhostBecomesCruiseElroy();
		updateGhostDotCounters();
		scorePoints(value);
		GameEvents.publish(GameEventType.PAC_FINDS_FOOD, pac.tile());
	}

	private void checkIfRedGhostBecomesCruiseElroy() {
		var redGhost = theGhosts[ID_RED_GHOST];
		var foodRemaining = level.world().foodRemaining();
		if (foodRemaining == level.elroy1DotsLeft()) {
			cruiseElroyState = 1;
			LOGGER.info("%s becomes Cruise Elroy 1", redGhost.name());
		} else if (foodRemaining == level.elroy2DotsLeft()) {
			cruiseElroyState = 2;
			LOGGER.info("%s becomes Cruise Elroy 2", redGhost.name());
		}
	}

	private void onPacGetsPower() {
		huntingTimer.stop();
		startPowerTimer(level.ghostFrightenedSeconds());
		ghosts(HUNTING_PAC).forEach(ghost -> {
			ghost.enterStateFrightened(this);
			ghost.forceTurningBack();
		});
		GameEvents.publish(GameEventType.PAC_GETS_POWER, pac.tile());
	}

	// Ghosts

	private void updateGhosts() {
		checkGhostCanBeUnlocked(memo);
		memo.unlockedGhost.ifPresent(ghost -> {
			unlockGhost(ghost, memo.unlockReason);
			GameEvents.publish(new GameEvent(this, GameEventType.GHOST_STARTS_LEAVING_HOUSE, ghost, ghost.tile()));
		});
		ghosts().forEach(ghost -> ghost.update(this));
	}

	// Ghost house rules, see Pac-Man dossier

	private void checkGhostCanBeUnlocked(Memory memo) {
		ghosts(LOCKED).findFirst().ifPresent(ghost -> {
			if (ghost.id == ID_RED_GHOST) {
				memo.unlockedGhost = Optional.of(ghost);
				memo.unlockReason = "Blinky is unlocked immediately";
				return;
			}
			// first check private dot counter
			if (!globalDotCounterEnabled && ghostDotCounter[ghost.id] >= privateDotLimits[ghost.id]) {
				memo.unlockedGhost = Optional.of(ghost);
				memo.unlockReason = "Private dot counter at limit (%d)".formatted(privateDotLimits[ghost.id]);
				return;
			}
			// check global dot counter
			var globalDotLimit = globalDotLimits[ghost.id] == -1 ? Integer.MAX_VALUE : globalDotLimits[ghost.id];
			if (globalDotCounter >= globalDotLimit) {
				memo.unlockedGhost = Optional.of(ghost);
				memo.unlockReason = "Global dot counter at limit (%d)".formatted(globalDotLimit);
			} else if (pac.starvingTime() >= pacStarvingTimeLimit) {
				memo.unlockedGhost = Optional.of(ghost);
				memo.unlockReason = "%s at starving limit (%d ticks)".formatted(pac.name(), pacStarvingTimeLimit);
				pac.endStarving();
			}
		});
	}

	private void unlockGhost(Ghost unlockedGhost, String reason) {
		LOGGER.info("Unlock ghost %s (%s)", unlockedGhost.name(), reason);
		var redGhost = theGhosts[ID_RED_GHOST];
		if (unlockedGhost.id == ID_ORANGE_GHOST && cruiseElroyState < 0) {
			cruiseElroyState = (byte) -cruiseElroyState; // resume Elroy mode
			LOGGER.info("%s Elroy mode %d resumed", redGhost.name(), cruiseElroyState);
		}
		if (unlockedGhost == redGhost) {
			unlockedGhost.setMoveAndWishDir(LEFT);
			unlockedGhost.enterStateHuntingPac(this);
		} else {
			unlockedGhost.enterStateLeavingHouse(this);
		}
	}

	private void updateGhostDotCounters() {
		if (globalDotCounterEnabled) {
			if (theGhosts[ID_ORANGE_GHOST].is(LOCKED) && globalDotCounter == 32) {
				globalDotCounterEnabled = false;
				globalDotCounter = 0;
				LOGGER.info("Global dot counter disabled and reset, Clyde was in house when counter reached 32");
			} else {
				globalDotCounter++;
			}
		} else {
			ghosts(LOCKED).filter(ghost -> ghost.id != ID_RED_GHOST).findFirst()
					.ifPresent(ghost -> ++ghostDotCounter[ghost.id]);
		}
	}
}
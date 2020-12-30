package de.amr.games.pacman.core;

import static de.amr.games.pacman.core.Creature.offset;
import static de.amr.games.pacman.core.Creature.tile;
import static de.amr.games.pacman.core.GameState.CHANGING_LEVEL;
import static de.amr.games.pacman.core.GameState.GAME_OVER;
import static de.amr.games.pacman.core.GameState.GHOST_DYING;
import static de.amr.games.pacman.core.GameState.HUNTING;
import static de.amr.games.pacman.core.GameState.INTRO;
import static de.amr.games.pacman.core.GameState.PACMAN_DYING;
import static de.amr.games.pacman.core.GameState.READY;
import static de.amr.games.pacman.core.Ghost.BLINKY;
import static de.amr.games.pacman.core.Ghost.CLYDE;
import static de.amr.games.pacman.core.Ghost.INKY;
import static de.amr.games.pacman.core.Ghost.PINKY;
import static de.amr.games.pacman.core.World.HTS;
import static de.amr.games.pacman.lib.Direction.DOWN;
import static de.amr.games.pacman.lib.Direction.LEFT;
import static de.amr.games.pacman.lib.Direction.RIGHT;
import static de.amr.games.pacman.lib.Direction.UP;
import static de.amr.games.pacman.lib.Functions.differsAtMost;
import static de.amr.games.pacman.lib.Logging.log;
import static java.lang.Math.abs;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.amr.games.pacman.lib.Clock;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Hiscore;
import de.amr.games.pacman.lib.V2f;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.ui.PacManGameUI;
import de.amr.games.pacman.ui.Sound;

/**
 * Pac-Man game with original "AI", levels, timers.
 * 
 * @author Armin Reichert
 * 
 * @see <a href="https://pacman.holenet.info">Jamey Pittman: The Pac-Man Dossier
 * @see <a href="https://gameinternals.com/understanding-pac-man-ghost-behavior">Chad Birch:
 *      Understanding ghost behavior
 */
public class Game {

	public static final byte CHERRIES = 0, STRAWBERRY = 1, PEACH = 2, APPLE = 3, GRAPES = 4, GALAXIAN = 5, BELL = 6,
			KEY = 7;

	/*@formatter:off*/
	static final Level[] LEVELS = {
		/* 1*/ new Level(CHERRIES,   100,  80, 75, 40,  20,  80, 10,  85,  90, 50, 6, 5),
		/* 2*/ new Level(STRAWBERRY, 300,  90, 85, 45,  30,  90, 15,  95,  95, 55, 5, 5),
		/* 3*/ new Level(PEACH,      500,  90, 85, 45,  40,  90, 20,  95,  95, 55, 4, 5),
		/* 4*/ new Level(PEACH,      500,  90, 85, 50,  40, 100, 20,  95,  95, 55, 3, 5),
		/* 5*/ new Level(APPLE,      700, 100, 95, 50,  40, 100, 20, 105, 100, 60, 2, 5),
		/* 6*/ new Level(APPLE,      700, 100, 95, 50,  50, 100, 25, 105, 100, 60, 5, 5),
		/* 7*/ new Level(GRAPES,    1000, 100, 95, 50,  50, 100, 25, 105, 100, 60, 2, 5),
		/* 8*/ new Level(GRAPES,    1000, 100, 95, 50,  50, 100, 25, 105, 100, 60, 2, 5),
		/* 9*/ new Level(GALAXIAN,  2000, 100, 95, 50,  60, 100, 30, 105, 100, 60, 1, 3),
		/*10*/ new Level(GALAXIAN,  2000, 100, 95, 50,  60, 100, 30, 105, 100, 60, 5, 5),
		/*11*/ new Level(BELL,      3000, 100, 95, 50,  60, 100, 30, 105, 100, 60, 2, 5),
		/*12*/ new Level(BELL,      3000, 100, 95, 50,  80, 100, 40, 105, 100, 60, 1, 3),
		/*13*/ new Level(KEY,       5000, 100, 95, 50,  80, 100, 40, 105, 100, 60, 1, 3),
		/*14*/ new Level(KEY,       5000, 100, 95, 50,  80, 100, 40, 105, 100, 60, 3, 5),
		/*15*/ new Level(KEY,       5000, 100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3),
		/*16*/ new Level(KEY,       5000, 100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3),
		/*17*/ new Level(KEY,       5000, 100, 95, 50, 100, 100, 50, 105,   0,  0, 0, 0),
		/*18*/ new Level(KEY,       5000, 100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3),
		/*19*/ new Level(KEY,       5000, 100, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0),
		/*20*/ new Level(KEY,       5000, 100, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0),
		/*21*/ new Level(KEY,       5000,  90, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0)
	};
	/*@formatter:on*/

	static final Direction[] DIRECTION_PRIORITY = { UP, LEFT, DOWN, RIGHT };

	public final Clock clock;
	public final Random rnd;
	public final World world;
	public final PacMan pacMan;
	public final Ghost[] ghosts;
	public final Hiscore hiscore;
	public PacManGameUI ui;

	public boolean gamePaused;
	public boolean gameStarted;

	public GameState state;
	public GameState previousState;
	public short level;
	public byte huntingPhase;
	public byte lives;
	public int score;
	public short ghostBounty;
	public byte ghostsKilledInLevel;
	public byte mazeFlashesRemaining;
	public long bonusAvailableTicks;
	public long bonusConsumedTicks;
	public short globalDotCounter;
	public boolean globalDotCounterEnabled;

	public boolean autoPilot;

	public Game() {
		clock = new Clock();
		rnd = new Random();
		world = new World();
		hiscore = new Hiscore();
		pacMan = new PacMan(world.pacManHome);
		/*@formatter:off*/
		ghosts = new Ghost[] {
			new Ghost(BLINKY, world.houseEntry,  world.upperRightScatterTile),
			new Ghost(PINKY,  world.houseCenter, world.upperLeftScatterTile), 
			new Ghost(INKY,   world.houseLeft,   world.lowerRightScatterTile),
			new Ghost(CLYDE,  world.houseRight,  world.lowerLeftScatterTile) 
		};
		/*@formatter:on*/
	}

	public void start() {
		reset();
		ui.show();
		enterIntroState();
		new Thread(this::gameLoop, "GameLoop").start();
	}

	private void gameLoop() {
		do {
			clock.tick(() -> {
				readInput();
				updateState();
				ui.render();
			});
		} while (true);
	}

	public void exit() {
		if (hiscore.changed) {
			hiscore.save();
		}
		log("Game exits.");
	}

	public Level level(int level) {
		int index = level <= 21 ? level - 1 : 20;
		return LEVELS[index];
	}

	public Level level() {
		return level(level);
	}

	private void readInput() {
		if (ui.keyPressed("p")) {
			gamePaused = !gamePaused;
		}
		if (ui.keyPressed("s")) {
			clock.targetFrequency = clock.targetFrequency == 60 ? 30 : 60;
		}
		if (ui.keyPressed("d")) {
			ui.setDebugMode(!ui.isDebugMode());
		}
		if (ui.keyPressed("a")) {
			autoPilot = !autoPilot;
		}
	}

	private void reset() {
		gameStarted = false;
		score = 0;
		lives = 3;
		hiscore.load();
		startLevel(1); // first level is 1
	}

	private void startLevel(int levelNumber) {
		level = (short) levelNumber;
		huntingPhase = 0;
		mazeFlashesRemaining = 0;
		ghostBounty = 200;
		ghostsKilledInLevel = 0;
		bonusAvailableTicks = 0;
		bonusConsumedTicks = 0;
		for (GameState state : GameState.values()) {
			state.setDuration(0);
		}
		for (Ghost ghost : ghosts) {
			ghost.dotCounter = 0;
		}
		ghosts[BLINKY].elroyMode = 0;
		world.restoreFood();
	}

	private void resetGuys() {
		pacMan.visible = true;
		pacMan.speed = 0;
		pacMan.dir = pacMan.wishDir = RIGHT;
		pacMan.changedTile = true;
		pacMan.couldMove = true;
		pacMan.forcedOnTrack = true;
		pacMan.forcedTurningBack = false;
		pacMan.dead = false;
		pacMan.powerTicksLeft = 0;
		pacMan.restingTicksLeft = 0;
		pacMan.starvingTicks = 0;
		pacMan.collapsingTicksLeft = 0;
		pacMan.placeAt(pacMan.homeTile.x, pacMan.homeTile.y, HTS, 0);

		for (Ghost ghost : ghosts) {
			ghost.visible = true;
			ghost.speed = 0;
			ghost.targetTile = null;
			ghost.changedTile = true;
			ghost.couldMove = true;
			ghost.forcedTurningBack = false;
			ghost.forcedOnTrack = ghost.id == BLINKY;
			ghost.dead = false;
			ghost.frightened = false;
			ghost.locked = true;
			ghost.enteringHouse = false;
			ghost.leavingHouse = false;
			ghost.bounty = 0;
//			ghost.dotCounter = 0;
//			ghost.elroyMode = 0;
			ghost.placeAt(ghost.homeTile.x, ghost.homeTile.y, HTS, 0);
		}
		ghosts[BLINKY].dir = ghosts[BLINKY].wishDir = LEFT;
		ghosts[PINKY].dir = ghosts[PINKY].wishDir = DOWN;
		ghosts[INKY].dir = ghosts[INKY].wishDir = UP;
		ghosts[CLYDE].dir = ghosts[CLYDE].wishDir = UP;

		bonusAvailableTicks = 0;
		bonusConsumedTicks = 0;
	}

	// BEGIN STATE-MACHINE

	public String ticksDescription(long ticks) {
		return ticks == Long.MAX_VALUE ? "indefinite time" : ticks + " ticks";
	}

	public String stateDescription() {
		if (state == HUNTING) {
			String phaseName = inChasingPhase() ? "Chasing" : "Scattering";
			int phase = huntingPhase / 2;
			return String.format("%s-%s (%d of 4)", state, phaseName, phase + 1);
		}
		return state.name();
	}

	private void enterState(GameState newState, long ticks) {
		state = newState;
		state.setDuration(ticks);
		logStateEntry();
	}

	private void logStateEntry() {
		log("Enter state '%s' for %s", stateDescription(), ticksDescription(state.duration()));
	}

	private void logStateExit() {
		log("Exit state '%s'", stateDescription());
	}

	private void updateState() {
		if (gamePaused) {
			return;
		}
		switch (state) {
		case INTRO:
			runIntroState();
			break;
		case READY:
			runReadyState();
			break;
		case HUNTING:
			runHuntingState();
			break;
		case CHANGING_LEVEL:
			runChangingLevelState();
			break;
		case PACMAN_DYING:
			runPacManDyingState();
			break;
		case GHOST_DYING:
			runGhostDyingState();
			break;
		case GAME_OVER:
			runGameOverState();
			break;
		default:
			throw new IllegalStateException("Illegal state: " + state);
		}
	}

	// INTRO

	private void enterIntroState() {
		enterState(INTRO, Long.MAX_VALUE);
		ui.startIntroScene();
	}

	private void runIntroState() {
		if (ui.anyKeyPressed()) {
			exitIntroState();
			enterReadyState();
			return;
		}
		state.tick();
	}

	private void exitIntroState() {
		ui.endIntroScene();
		logStateExit();
	}

	// READY

	private void enterReadyState() {
		enterState(READY, clock.sec(gameStarted ? 0.5 : 4.5));
		if (!gameStarted) {
			ui.playSound(Sound.GAME_READY);
			gameStarted = true;
		}
		resetGuys();
	}

	private void runReadyState() {
		if (state.expired()) {
			exitReadyState();
			enterHuntingState();
			return;
		}
		for (Ghost ghost : ghosts) {
			if (ghost.id != BLINKY) {
				letGhostBounce(ghost);
			}
		}
		state.tick();
	}

	private void exitReadyState() {
		logStateExit();
	}

	// HUNTING

	static final short[][] HUNTING_PHASE_DURATION = {
		//@formatter:off
		{ 7, 20, 7, 20, 5,   20,  5, Short.MAX_VALUE },
		{ 7, 20, 7, 20, 5, 1033, -1, Short.MAX_VALUE },
		{ 5, 20, 5, 20, 5, 1037, -1, Short.MAX_VALUE },
		//@formatter:on
	};

	private long huntingPhaseDuration(int phase) {
		int index = level == 1 ? 0 : level <= 4 ? 1 : 2;
		return huntingTicks(HUNTING_PHASE_DURATION[index][phase]);
	}

	private long huntingTicks(short duration) {
		if (duration == -1) {
			return 1; // 1 tick
		}
		if (duration == Short.MAX_VALUE) {
			return Long.MAX_VALUE;
		}
		return clock.sec(duration);
	}

	private boolean inScatteringPhase() {
		return huntingPhase % 2 == 0;
	}

	private boolean inChasingPhase() {
		return huntingPhase % 2 != 0;
	}

	private void enterNextHuntingPhase() {
		huntingPhase++;
		state.setDuration(huntingPhaseDuration(huntingPhase));
		forceHuntingGhostsTurningBack();
		if (inScatteringPhase()) {
			if (huntingPhase >= 2) {
				ui.stopSound(siren(huntingPhase - 2));
			}
			ui.loopSound(siren(huntingPhase));
		}
		log("Game state updated to '%s' for %s", stateDescription(), ticksDescription(state.ticksRemaining()));
	}

	private static Sound siren(int huntingPhase) {
		switch (huntingPhase / 2) {
		case 0:
			return Sound.SIREN_1;
		case 1:
			return Sound.SIREN_2;
		case 2:
			return Sound.SIREN_3;
		case 3:
			return Sound.SIREN_4;
		default:
			throw new IllegalArgumentException("Illegal hunting phase: " + huntingPhase);
		}
	}

	private void enterHuntingState() {
		huntingPhase = 0;
		enterState(HUNTING, huntingPhaseDuration(huntingPhase));
		ui.loopSound(siren(huntingPhase));
	}

	private void runHuntingState() {

		// Cheats
		if (ui.keyPressed("e")) {
			eatAllNormalPellets();
		}
		if (ui.keyPressed("x")) {
			killAllGhosts();
			exitHuntingState();
			enterGhostDyingState();
			return;
		}
		if (ui.keyPressed("l")) {
			if (lives < Byte.MAX_VALUE) {
				lives++;
			}
		}

		if (state.expired()) {
			enterNextHuntingPhase();
		}

		if (world.foodRemaining == 0) {
			exitHuntingState();
			enterChangingLevelState();
			return;
		}

		boolean ghostCollision = checkPacManGhostCollision();
		if (ghostCollision) {
			exitHuntingState();
			if (pacMan.dead) {
				enterPacManDyingState();
			} else {
				enterGhostDyingState();
			}
			return;
		}

		checkPacManFindsFood();
		checkPacManFindsBonus();

		updatePacMan();
		for (Ghost ghost : ghosts) {
			updateGhost(ghost);
		}
		updateBonus();

		if (pacMan.powerTicksLeft == 0) {
			state.tick();
		}
	}

	private void exitHuntingState() {
		logStateExit();
	}

	// PACMAN_DYING

	private void enterPacManDyingState() {
		enterState(PACMAN_DYING, clock.sec(6));
		pacMan.speed = 0;
		for (Ghost ghost : ghosts) {
			ghost.speed = 0;
		}
		ui.stopAllSounds();
	}

	private void runPacManDyingState() {
		if (state.expired()) {
			exitPacManDyingState();
			if (lives > 0) {
				enterReadyState();
				return;
			} else {
				enterGameOverState();
				return;
			}
		}
		if (state.running() == clock.sec(1.5)) {
			for (Ghost ghost : ghosts) {
				ghost.visible = false;
			}
		}
		if (state.running() == clock.sec(2.5)) {
			pacMan.collapsingTicksLeft = 88;
			ui.playSound(Sound.PACMAN_DEATH);
		}
		if (pacMan.collapsingTicksLeft > 1) {
			pacMan.collapsingTicksLeft--;
		}
		state.tick();
	}

	private void exitPacManDyingState() {
		if (!autoPilot) {
			lives -= 1;
		}
		pacMan.collapsingTicksLeft = 0;
		for (Ghost ghost : ghosts) {
			ghost.visible = true;
		}
		logStateExit();
	}

	// GHOST_DYING

	private void enterGhostDyingState() {
		previousState = state;
		enterState(GHOST_DYING, clock.sec(1));
		pacMan.visible = false;
		ui.playSound(Sound.GHOST_DEATH);
	}

	private void runGhostDyingState() {
		if (state.expired()) {
			exitGhostDyingState();
			state = previousState;
			log("Resume state '%s'", state);
			return;
		}
		for (Ghost ghost : ghosts) {
			if (ghost.dead && ghost.bounty == 0) {
				updateGhost(ghost);
			}
		}
		state.tick();
	}

	private void exitGhostDyingState() {
		for (Ghost ghost : ghosts) {
			if (ghost.dead && ghost.bounty > 0) {
				ghost.bounty = 0;
			}
		}
		pacMan.visible = true;
		ui.loopSound(Sound.RETREATING);
		logStateExit();
	}

	// CHANGING_LEVEL

	private void enterChangingLevelState() {
		enterState(CHANGING_LEVEL, clock.sec(level().numFlashes + 2));
		for (Ghost ghost : ghosts) {
			ghost.frightened = false;
			ghost.dead = false;
			ghost.speed = 0;
		}
		pacMan.speed = 0;
		ui.stopAllSounds();
	}

	private void runChangingLevelState() {
		if (state.expired()) {
			exitChangingLevelState();
			enterReadyState();
			return;
		}
		if (state.running() == clock.sec(1)) {
			for (Ghost ghost : ghosts) {
				ghost.visible = false;
			}
		}
		if (state.running() == clock.sec(2)) {
			mazeFlashesRemaining = level().numFlashes;
		}
		state.tick();
	}

	private void exitChangingLevelState() {
		log("Level %d complete, entering level %d", level, level + 1);
		startLevel(++level);
		logStateExit();
	}

	// GAME_OVER

	private void enterGameOverState() {
		enterState(GAME_OVER, clock.sec(30));
		for (Ghost ghost : ghosts) {
			ghost.speed = 0;
		}
		pacMan.speed = 0;
		if (hiscore.changed) {
			hiscore.save();
		}
	}

	private void runGameOverState() {
		if (state.expired() || ui.anyKeyPressed()) {
			exitGameOverState();
			enterIntroState();
			return;
		}
		state.tick();
	}

	private void exitGameOverState() {
		reset();
		logStateExit();
	}

	// END STATE-MACHINE

	private void updatePacMan() {
		if (pacMan.restingTicksLeft == 0) {
			updatePacManDirection();
			tryMoving(pacMan);
		} else {
			pacMan.restingTicksLeft--;
		}
		if (pacMan.powerTicksLeft > 0) {
			pacMan.powerTicksLeft--;
			if (pacMan.powerTicksLeft == 0) {
				for (Ghost ghost : ghosts) {
					ghost.frightened = false;
				}
				ui.stopSound(Sound.PACMAN_POWER);
			}
		}
	}

	private void updatePacManDirection() {
		if (autoPilot) {
			controlPacManAutomatically();
		} else {
			controlPacManByKeys();
		}
	}

	private void controlPacManByKeys() {
		if (ui.keyPressed("left")) {
			pacMan.wishDir = LEFT;
		}
		if (ui.keyPressed("right")) {
			pacMan.wishDir = RIGHT;
		}
		if (ui.keyPressed("up")) {
			pacMan.wishDir = UP;
		}
		if (ui.keyPressed("down")) {
			pacMan.wishDir = DOWN;
		}
	}

	private void controlPacManAutomatically() {
		if (!pacMan.couldMove || world.isIntersectionTile(pacMan.tile().x, pacMan.tile().y)) {
			pacMan.wishDir = randomMoveDir(pacMan);
		}
	}

	private boolean checkPacManGhostCollision() {
		Ghost collidingGhost = Stream.of(ghosts).filter(ghost -> !ghost.dead)
				.filter(ghost -> pacMan.tile().equals(ghost.tile())).findAny().orElse(null);
		if (collidingGhost == null) {
			return false;
		}
		if (collidingGhost.frightened) {
			killGhost(collidingGhost);
			return true;
		}
		resetAndEnableGlobalDotCounter();
		byte elroyMode = ghosts[BLINKY].elroyMode;
		if (elroyMode > 0) {
			ghosts[BLINKY].elroyMode = (byte) -elroyMode; // negative value means "disabled"
			log("Blinky Elroy mode %d disabled", elroyMode);
		}
		pacMan.dead = true;
		log("Pac-Man killed by %s at tile %s", collidingGhost.name(), collidingGhost.tile());
		return true;
	}

	private void checkPacManFindsBonus() {
		V2i tile = pacMan.tile();
		if (!world.isBonusTile(tile.x, tile.y) || bonusAvailableTicks == 0) {
			return;
		}
		bonusAvailableTicks = 0;
		bonusConsumedTicks = clock.sec(2);
		score(level().bonusPoints);
		ui.playSound(Sound.EAT_BONUS);
		log("Pac-Man found bonus (id=%d) of value %d", level().bonusSymbol, level().bonusPoints);
	}

	private void checkPacManFindsFood() {
		V2i tile = pacMan.tile();
		if (world.isFoodTile(tile.x, tile.y) && !world.foodRemoved(tile.x, tile.y)) {
			onPacManFoundFood(tile);
			ui.playSound(Sound.MUNCH);
		} else {
			onPacManStarved();
		}
	}

	private void onPacManStarved() {
		pacMan.starvingTicks++;
		if (pacMan.starvingTicks >= pacManStarvingTimeLimit()) {
			preferredLockedGhost().ifPresent(ghost -> {
				releaseGhost(ghost, "Pac-Man has been starving for %d ticks", pacMan.starvingTicks);
				pacMan.starvingTicks = 0;
			});
		}
	}

	private void onPacManFoundFood(V2i tile) {
		pacMan.starvingTicks = 0;
		world.removeFood(tile.x, tile.y);
		if (world.isEnergizerTile(tile.x, tile.y)) {
			score(50);
			givePacManPower();
			ghostBounty = 200;
			pacMan.restingTicksLeft = 3;
		} else {
			score(10);
			pacMan.restingTicksLeft = 1;
		}
		// bonus score reached?
		int eaten = world.totalFoodCount - world.foodRemaining;
		if (bonusAvailableTicks == 0 && (eaten == 70 || eaten == 170)) {
			bonusAvailableTicks = clock.sec(9 + rnd.nextFloat());
		}
		updateGhostDotCounters();
		mayBeEnterElroyMode();
	}

	private void givePacManPower() {
		int powerSeconds = level().ghostFrightenedSeconds;
		pacMan.powerTicksLeft = clock.sec(powerSeconds);
		if (powerSeconds > 0) {
			log("Pac-Man got power for %d seconds", powerSeconds);
			for (Ghost ghost : ghosts) {
				if (isGhostHunting(ghost)) {
					ghost.frightened = true;
				}
			}
			forceHuntingGhostsTurningBack();
			ui.loopSound(Sound.PACMAN_POWER);
		}
	}

	private void updateGhostDotCounters() {
		if (globalDotCounterEnabled) {
			if (ghosts[CLYDE].locked && globalDotCounter == 32) {
				globalDotCounterEnabled = false;
				globalDotCounter = 0;
				log("Global dot counter disabled and reset, Clyde in house when counter reached 32");
			} else {
				++globalDotCounter;
			}
		} else {
			preferredLockedGhost().ifPresent(ghost -> {
				ghost.dotCounter++;
			});
		}
	}

	private void mayBeEnterElroyMode() {
		if (world.foodRemaining == level().elroy1DotsLeft) {
			ghosts[BLINKY].elroyMode = 1;
			log("Blinky becomes Elroy 1");
		} else if (world.foodRemaining == level().elroy2DotsLeft) {
			ghosts[BLINKY].elroyMode = 2;
			log("Blinky becomes Elroy 2");
		}
	}

	private void killGhost(Ghost ghost) {
		ghost.frightened = false;
		ghost.dead = true;
		ghost.targetTile = world.houseEntry;
		ghost.bounty = ghostBounty;
		score(ghost.bounty);
		ghostsKilledInLevel++;
		if (ghostsKilledInLevel == 16) {
			score(12000);
		}
		ghostBounty *= 2;
		log("Ghost %s killed at tile %s, Pac-Man wins %d points", ghost.name(), ghost.tile(), ghost.bounty);
	}

	private void updateBonus() {
		// bonus active and not consumed
		if (bonusAvailableTicks > 0) {
			--bonusAvailableTicks;
		}
		// bonus active and consumed
		if (bonusConsumedTicks > 0) {
			--bonusConsumedTicks;
		}
	}

	private void updateGhost(Ghost ghost) {
		if (ghost.locked) {
			if (ghost.id != BLINKY) {
				tryReleasingGhost(ghost);
				letGhostBounce(ghost);
			} else {
				ghost.locked = false;
			}
		} else if (ghost.enteringHouse) {
			letGhostEnterHouse(ghost);
		} else if (ghost.leavingHouse) {
			letGhostLeaveHouse(ghost);
		} else if (ghost.dead) {
			letGhostReturnHome(ghost);
		} else if (state == HUNTING) {
			letGhostHuntPacMan(ghost);
		}
	}

	private void tryReleasingGhost(Ghost ghost) {
		if (globalDotCounterEnabled && globalDotCounter >= ghostGlobalDotLimit(ghost)) {
			releaseGhost(ghost, "Global dot counter (%d) reached limit (%d)", globalDotCounter, ghostGlobalDotLimit(ghost));
		} else if (!globalDotCounterEnabled && ghost.dotCounter >= ghostPrivateDotLimit(ghost)) {
			releaseGhost(ghost, "%s's dot counter (%d) reached limit (%d)", ghost.name(), ghost.dotCounter,
					ghostPrivateDotLimit(ghost));
		}
	}

	private void releaseGhost(Ghost ghost, String reason, Object... args) {
		ghost.locked = false;
		ghost.leavingHouse = true;
		if (ghost.id == CLYDE && ghosts[BLINKY].elroyMode < 0) {
			ghosts[BLINKY].elroyMode = (byte) -ghosts[BLINKY].elroyMode; // resume Elroy mode
			log("Blinky Elroy mode %d resumed", ghosts[BLINKY].elroyMode);
		}
		log("Ghost %s released: %s", ghost.name(), String.format(reason, args));
	}

	private Optional<Ghost> preferredLockedGhost() {
		return Stream.of(ghosts[PINKY], ghosts[INKY], ghosts[CLYDE]).filter(ghost -> ghost.locked).findFirst();
	}

	private int ghostPrivateDotLimit(Ghost ghost) {
		if (ghost.id == INKY) {
			return level == 1 ? 30 : 0;
		}
		if (ghost.id == CLYDE) {
			return level == 1 ? 60 : level == 2 ? 50 : 0;
		}
		return 0;
	}

	private int ghostGlobalDotLimit(Ghost ghost) {
		return ghost.id == PINKY ? 7 : ghost.id == INKY ? 17 : Integer.MAX_VALUE;
	}

	private void resetAndEnableGlobalDotCounter() {
		globalDotCounter = 0;
		globalDotCounterEnabled = true;
		log("Global dot counter reset and enabled");
	}

	private int pacManStarvingTimeLimit() {
		return level < 5 ? clock.sec(4) : clock.sec(3);
	}

	private V2i ghostChasingTarget(Ghost ghost) {
		switch (ghost.id) {
		case BLINKY: {
			return pacMan.tile();
		}
		case PINKY: {
			V2i pacManAhead4 = pacMan.tile().sum(pacMan.dir.vec.scaled(4));
			if (pacMan.dir == UP) {
				// simulate overflow bug when Pac-Man is looking UP
				pacManAhead4 = pacManAhead4.sum(LEFT.vec.scaled(4));
			}
			return pacManAhead4;
		}
		case INKY: {
			V2i pacManAhead2 = pacMan.tile().sum(pacMan.dir.vec.scaled(2));
			if (pacMan.dir == UP) {
				// simulate overflow bug when Pac-Man is looking UP
				pacManAhead2 = pacManAhead2.sum(LEFT.vec.scaled(2));
			}
			return ghosts[BLINKY].tile().scaled(-1).sum(pacManAhead2.scaled(2));
		}
		case CLYDE: {
			return ghost.tile().distance(pacMan.tile()) < 8 ? ghost.scatterTile : pacMan.tile();
		}
		default:
			throw new IllegalArgumentException("Unknown ghost id: " + ghost.id);
		}
	}

	private void letGhostBounce(Ghost ghost) {
		tryMoving(ghost);
		if (!ghost.couldMove) {
			ghost.wishDir = ghost.dir.opposite();
		}
	}

	private void letGhostHuntPacMan(Ghost ghost) {
		ghost.targetTile = inChasingPhase() || ghost.id == BLINKY && ghost.elroyMode > 0 ? ghostChasingTarget(ghost)
				: ghost.scatterTile;
		letGhostHeadForTargetTile(ghost);
	}

	private void letGhostHeadForTargetTile(Ghost ghost) {
		newGhostWishDir(ghost).ifPresent(dir -> ghost.wishDir = dir);
		tryMoving(ghost);
	}

	private void letGhostReturnHome(Ghost ghost) {
		if (atGhostHouseDoor(ghost)) {
			ghost.setOffset(HTS, 0);
			ghost.dir = ghost.wishDir = DOWN;
			ghost.forcedOnTrack = false;
			ghost.enteringHouse = true;
			ghost.targetTile = ghost.id == BLINKY ? world.houseCenter : ghost.homeTile;
			return;
		}
		letGhostHeadForTargetTile(ghost);
	}

	private boolean atGhostHouseDoor(Creature guy) {
		return guy.at(world.houseEntry) && differsAtMost(guy.offset().x, HTS, 2);
	}

	private void letGhostEnterHouse(Ghost ghost) {
		V2f offset = ghost.offset();
		// target reached?
		if (ghost.at(ghost.targetTile) && offset.y >= 0) {
			ghost.wishDir = ghost.dir.opposite();
			ghost.dead = false;
			ghost.enteringHouse = false;
			ghost.leavingHouse = true;
			if (Arrays.stream(ghosts).noneMatch(g -> g.dead)) {
				ui.stopSound(Sound.RETREATING);
			}
			return;
		}
		// move sidewards towards target tile?
		if (ghost.at(world.houseCenter) && offset.y >= 0) {
			ghost.wishDir = ghost.targetTile.x < world.houseCenter.x ? LEFT : RIGHT;
		}
		ghost.couldMove = tryMoving(ghost, ghost.wishDir);
	}

	private void letGhostLeaveHouse(Ghost ghost) {
		V2f offset = ghost.offset();
		// house left?
		if (ghost.at(world.houseEntry) && differsAtMost(offset.y, 0, 1)) {
			ghost.setOffset(HTS, 0);
			ghost.dir = ghost.wishDir = LEFT;
			ghost.forcedOnTrack = true;
			ghost.leavingHouse = false;
			return;
		}
		// center of house reached?
		if (ghost.at(world.houseCenter) && differsAtMost(offset.x, 3, 1)) {
			ghost.setOffset(HTS, 0);
			ghost.wishDir = UP;
			tryMoving(ghost);
			return;
		}
		// keep bouncing until ghost can move towards middle of house
		if (ghost.wishDir == UP || ghost.wishDir == DOWN) {
			if (ghost.at(ghost.homeTile)) {
				ghost.setOffset(HTS, 0);
				ghost.wishDir = ghost.homeTile.x < world.houseCenter.x ? RIGHT : LEFT;
			} else {
				letGhostBounce(ghost);
			}
			return;
		}
		tryMoving(ghost);
	}

	private Optional<Direction> newGhostWishDir(Ghost ghost) {
		if (!ghost.changedTile) {
			return Optional.empty();
		}
		if (ghost.forcedTurningBack) {
			ghost.forcedTurningBack = false;
			return Optional.of(ghost.dir.opposite());
		}
		V2i tile = ghost.tile();
		if (world.isPortalTile(tile.x, tile.y)) {
			return Optional.empty();
		}
		if (ghost.frightened && world.isIntersectionTile(tile.x, tile.y)) {
			return Optional.of(randomMoveDir(ghost));
		}
		return ghostTargetDirection(ghost);
	}

	private Optional<Direction> ghostTargetDirection(Ghost ghost) {
		double minDist = Double.MAX_VALUE;
		Direction minDistDir = null;
		for (Direction dir : DIRECTION_PRIORITY) {
			if (dir == ghost.dir.opposite()) {
				continue;
			}
			V2i neighbor = ghost.tile().sum(dir.vec);
			if (!canAccessTile(ghost, neighbor.x, neighbor.y)) {
				continue;
			}
			if (dir == UP && world.isUpwardsBlocked(neighbor.x, neighbor.y) && !ghost.frightened && !ghost.dead) {
				continue;
			}
			double dist = neighbor.distance(ghost.targetTile);
			if (dist < minDist) {
				minDist = dist;
				minDistDir = dir;
			}
		}
		return Optional.ofNullable(minDistDir);
	}

	private boolean isGhostHunting(Ghost ghost) {
		return !ghost.dead && !ghost.locked && !ghost.enteringHouse && !ghost.leavingHouse && !ghost.frightened;
	}

	private void forceHuntingGhostsTurningBack() {
		for (Ghost ghost : ghosts) {
			if (isGhostHunting(ghost)) {
				ghost.forcedTurningBack = true;
			}
		}
	}

	private void tryMoving(Creature guy) {
		if (guy.at(world.portalRight) && guy.dir == RIGHT) {
			guy.placeAt(world.portalLeft.x, world.portalLeft.y, 0, 0);
			return;
		}
		if (guy.at(world.portalLeft) && guy.dir == LEFT) {
			guy.placeAt(world.portalRight.x, world.portalRight.y, 0, 0);
			return;
		}
		guy.couldMove = tryMoving(guy, guy.wishDir);
		if (guy.couldMove) {
			guy.dir = guy.wishDir;
		} else {
			guy.couldMove = tryMoving(guy, guy.dir);
		}
	}

	private boolean tryMoving(Creature guy, Direction dir) {
		guy.updateSpeed(world, level());
		// 100% speed corresponds to 1.25 pixels/tick (75px/sec)
		float pixelSpeed = guy.speed * 1.25f;

		V2i tile = guy.tile();
		V2f offset = guy.offset();
		V2i neighbor = tile.sum(dir.vec);

		// check if guy can change its direction now
		if (guy.forcedOnTrack && canAccessTile(guy, neighbor.x, neighbor.y)) {
			if (dir == LEFT || dir == RIGHT) {
				if (abs(offset.y) > pixelSpeed) {
					return false;
				}
				guy.setOffset(offset.x, 0);
			} else if (dir == UP || dir == DOWN) {
				if (abs(offset.x) > pixelSpeed) {
					return false;
				}
				guy.setOffset(0, offset.y);
			}
		}

		V2f velocity = new V2f(dir.vec).scaled(pixelSpeed);
		V2f newPosition = guy.position.sum(velocity);
		V2i newTile = tile(newPosition);
		V2f newOffset = offset(newPosition);

		// block moving into inaccessible tile
		if (!canAccessTile(guy, newTile.x, newTile.y)) {
			return false;
		}

		// align with edge of inaccessible neighbor
		if (!canAccessTile(guy, neighbor.x, neighbor.y)) {
			if (dir == RIGHT && newOffset.x > 0 || dir == LEFT && newOffset.x < 0) {
				guy.setOffset(0, offset.y);
				return false;
			}
			if (dir == DOWN && newOffset.y > 0 || dir == UP && newOffset.y < 0) {
				guy.setOffset(offset.x, 0);
				return false;
			}
		}

		guy.placeAt(newTile.x, newTile.y, newOffset.x, newOffset.y);
		guy.changedTile = !guy.at(tile);
		return true;
	}

	private boolean canAccessTile(Creature guy, int x, int y) {
		if (world.isPortalTile(x, y)) {
			return true;
		}
		if (world.isGhostHouseDoor(x, y)) {
			return guy instanceof Ghost && (((Ghost) guy).enteringHouse || ((Ghost) guy).leavingHouse);
		}
		return !world.isWall(x, y);
	}

	private Direction randomMoveDir(Creature guy) {
		//@formatter:off
		List<Direction> dirs = Stream.of(Direction.values())
			.filter(dir -> dir != guy.dir.opposite())
			.filter(dir -> {
				V2i neighbor = guy.tile().sum(dir.vec);
				return world.isAccessibleTile(neighbor.x, neighbor.y);
			})
			.collect(Collectors.toList());
		//@formatter:on
		return dirs.get(rnd.nextInt(dirs.size()));
	}

	// Score

	private void score(int points) {
		int oldscore = score;
		score += points;
		if (oldscore < 10000 && score >= 10000) {
			lives++;
			ui.playSound(Sound.EXTRA_LIFE);
		}
		hiscore.update(score, level);
	}

	// Cheats

	private void eatAllNormalPellets() {
		for (int x = 0; x < world.size.x; ++x) {
			for (int y = 0; y < world.size.y; ++y) {
				if (world.isFoodTile(x, y) && !world.foodRemoved(x, y) && !world.isEnergizerTile(x, y)) {
					world.removeFood(x, y);
				}
			}
		}
	}

	private void killAllGhosts() {
		ghostBounty = 200;
		for (Ghost ghost : ghosts) {
			if (isGhostHunting(ghost) || ghost.frightened) {
				killGhost(ghost);
			}
		}
	}
}
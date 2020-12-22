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
import static de.amr.games.pacman.core.World.HOUSE_CENTER;
import static de.amr.games.pacman.core.World.HOUSE_ENTRY;
import static de.amr.games.pacman.core.World.HOUSE_LEFT;
import static de.amr.games.pacman.core.World.HOUSE_RIGHT;
import static de.amr.games.pacman.core.World.HTS;
import static de.amr.games.pacman.core.World.LOWER_LEFT_CORNER;
import static de.amr.games.pacman.core.World.LOWER_RIGHT_CORNER;
import static de.amr.games.pacman.core.World.PACMAN_HOME;
import static de.amr.games.pacman.core.World.PORTAL_LEFT;
import static de.amr.games.pacman.core.World.PORTAL_RIGHT;
import static de.amr.games.pacman.core.World.TOTAL_FOOD_COUNT;
import static de.amr.games.pacman.core.World.UPPER_LEFT_CORNER;
import static de.amr.games.pacman.core.World.UPPER_RIGHT_CORNER;
import static de.amr.games.pacman.core.World.WORLD_TILES;
import static de.amr.games.pacman.lib.Direction.DOWN;
import static de.amr.games.pacman.lib.Direction.LEFT;
import static de.amr.games.pacman.lib.Direction.RIGHT;
import static de.amr.games.pacman.lib.Direction.UP;
import static de.amr.games.pacman.lib.Logging.log;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.amr.games.pacman.lib.Clock;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Functions;
import de.amr.games.pacman.lib.Hiscore;
import de.amr.games.pacman.lib.V2f;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.ui.PacManGameUI;
import de.amr.games.pacman.ui.Sound;

/**
 * Pac-Man game with original "AI".
 * 
 * @author Armin Reichert
 * 
 * @see https://gameinternals.com/understanding-pac-man-ghost-behavior
 * @see https://pacman.holenet.info
 * @see https://www.gamasutra.com/view/feature/132330/the_pacman_dossier.php
 */
public class Game {

	public static final int CHERRIES = 0, STRAWBERRY = 1, PEACH = 2, APPLE = 3, GRAPES = 4, GALAXIAN = 5, BELL = 6,
			KEY = 7;

	static final Level[] LEVELS = {
	/*@formatter:off*/
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
	/*@formatter:on*/
	};

	static final Direction[] DIRECTION_PRIORITY = { UP, LEFT, DOWN, RIGHT };

	static final int[] GHOST_UNLOCK_ORDER = { PINKY, INKY, CLYDE };

	public final World world;
	public final PacMan pacMan;
	public final Ghost[] ghosts;
	public final Hiscore hiscore;
	public final Clock clock;
	public final Random rnd;
	public PacManGameUI ui;

	public boolean paused;

	public GameState state;
	public GameState previousState;
	public boolean gameStarted;
	public int level;
	public int huntingPhase;
	public int lives;
	public int score;
	public int ghostBounty;
	public int ghostsKilledInLevel;
	public int mazeFlashesRemaining;
	public long bonusAvailableTimer;
	public long bonusConsumedTimer;

	public int globalDotCounter;
	public boolean globalDotCounterEnabled;

	public Game() {
		clock = new Clock();
		rnd = new Random();
		world = new World();
		hiscore = new Hiscore();
		pacMan = new PacMan(PACMAN_HOME);
		/*@formatter:off*/
		ghosts = new Ghost[] {
			new Ghost(BLINKY, HOUSE_ENTRY,  UPPER_RIGHT_CORNER),
			new Ghost(PINKY,  HOUSE_CENTER, UPPER_LEFT_CORNER), 
			new Ghost(INKY,   HOUSE_LEFT,   LOWER_RIGHT_CORNER),
			new Ghost(CLYDE,  HOUSE_RIGHT,  LOWER_LEFT_CORNER) 
		};
		/*@formatter:on*/
	}

	private void run() {
		reset();
		ui.show();
		enterIntroState();
		while (true) {
			clock.tick(() -> {
				readInput();
				updateState();
				ui.render();
			});
		}
	}

	public void start() {
		new Thread(this::run, "GameLoop").start();
	}

	public void exit() {
		if (hiscore.changed) {
			hiscore.save();
		}
	}

	public Level level(int level) {
		int index = level <= 21 ? level - 1 : 20;
		return LEVELS[index];
	}

	public Level level() {
		return level(level);
	}

	private void reset() {
		gameStarted = false;
		score = 0;
		lives = 3;
		hiscore.load();
		startLevel(1); // level numbering starts with 1!
	}

	private void startLevel(int levelNumber) {
		level = levelNumber;
		huntingPhase = 0;
		mazeFlashesRemaining = 0;
		ghostBounty = 200;
		ghostsKilledInLevel = 0;
		bonusAvailableTimer = 0;
		bonusConsumedTimer = 0;
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

		bonusAvailableTimer = 0;
		bonusConsumedTimer = 0;
	}

	private void readInput() {

		if (ui.keyPressed("p")) {
			paused = !paused;
		}
		if (ui.keyPressed("s")) {
			clock.targetFPS = clock.targetFPS == 60 ? 30 : 60;
		}
		if (ui.keyPressed("d")) {
			ui.setDebugMode(!ui.isDebugMode());
		}

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

		// cheats
		if (ui.keyPressed("e") && state == HUNTING) {
			eatAllNormalPellets();
		}
		if (ui.keyPressed("x") && state == HUNTING) {
			killAllGhosts();
		}
		if (ui.keyPressed("plus") && state == READY) {
			startLevel(++level);
			enterReadyState();
		}
		if (ui.keyPressed("l") && state == READY) {
			lives++;
		}
	}

	// BEGIN STATE-MACHINE

	public String stateDescription() {
		if (state == HUNTING) {
			int step = huntingPhase / 2;
			return String.format(inChasingPhase() ? "%s-chasing-%d" : "%s-scattering-%d", state, step);
		}
		return state.name();
	}

	private void enterState(GameState newState, long duration) {
		state = newState;
		state.setDuration(duration);
		String durationText = duration == Long.MAX_VALUE ? "indefinite time" : duration + " ticks";
		log("Game entered state %s for %s", stateDescription(), durationText);
	}

	private void updateState() {
		if (paused) {
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
		if (ui.keyPressed("space")) {
			exitIntroState();
			enterReadyState();
			return;
		}
		state.tick();
	}

	private void exitIntroState() {
		ui.endIntroScene();
	}

	// READY

	private void enterReadyState() {
		enterState(READY, clock.sec(gameStarted ? 1 : 4.5));
		HUNTING.setDuration(0);
		huntingPhase = 0;
		resetGuys();
	}

	private void runReadyState() {
		if (state.expired()) {
			exitReadyState();
			enterHuntingState();
			return;
		}
		if (state.running() == clock.sec(0.25)) {
			if (!gameStarted) {
				ui.playSound(Sound.GAME_READY);
				gameStarted = true;
			} else {
				ui.playSound(Sound.CREDIT);
			}
		}
		if (state.running() > clock.sec(0.5)) {
			for (Ghost ghost : ghosts) {
				if (ghost.id != BLINKY) {
					letGhostBounce(ghost);
				}
			}
		}
		state.tick();
	}

	private void exitReadyState() {
		maybeReleaseGhost(ghosts[BLINKY]);
	}

	// HUNTING

	static final short[][] HUNTING_PHASE_DURATION = {
		//@formatter:off
		{ 7, 20, 7, 20, 5,   20,  5, Short.MAX_VALUE },
		{ 7, 20, 7, 20, 5, 1033, -1, Short.MAX_VALUE },
		{ 5, 20, 5, 20, 5, 1037, -1, Short.MAX_VALUE },
		//@formatter:on
	};

	private boolean inScatteringPhase() {
		return huntingPhase % 2 == 0;
	}

	private boolean inChasingPhase() {
		return huntingPhase % 2 != 0;
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

	private long currentHuntingPhaseDuration() {
		int index = level == 1 ? 0 : level <= 4 ? 1 : 2;
		return huntingTicks(HUNTING_PHASE_DURATION[index][huntingPhase]);
	}

	private void enterNextHuntingPhase() {
		huntingPhase++;
		state.setDuration(currentHuntingPhaseDuration());
		forceGhostsTurningBack();
		log("Game state updated to %s for %d ticks", stateDescription(), state.ticksRemaining());
		if (inScatteringPhase()) {
			if (huntingPhase >= 2) {
				ui.stopSound(siren(huntingPhase - 2));
			}
			ui.loopSound(siren(huntingPhase));
		}
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
		enterState(HUNTING, currentHuntingPhaseDuration());
		ui.loopSound(siren(huntingPhase));
	}

	private void runHuntingState() {
		Ghost ghostColliding = checkGhostCollision();
		if (ghostColliding != null) {
			if (ghostColliding.frightened) {
				killGhost(ghostColliding);
				exitHuntingState();
				enterGhostDyingState();
				return;
			}
			if (pacMan.powerTicksLeft == 0) {
				log("Pac-Man killed by %s at tile %s", ghostColliding.name(), ghostColliding.tile());
				pacMan.dead = true;
				resetAndEnableGlobalDotCounter();
				if (ghosts[BLINKY].elroyMode > 0) {
					log("Blinky Elroy mode %d disabled", ghosts[BLINKY].elroyMode);
					ghosts[BLINKY].elroyMode = (byte) -ghosts[BLINKY].elroyMode; // disabled
				}
				exitHuntingState();
				enterPacManDyingState();
				return;
			}
		}
		if (world.foodRemaining == 0) {
			exitHuntingState();
			enterChangingLevelState();
			return;
		}
		checkPacManFoundFood();
		checkPacManFoundBonus();
		updatePacMan();
		for (Ghost ghost : ghosts) {
			updateGhost(ghost);
		}
		updateBonus();

		if (pacMan.powerTicksLeft == 0) {
			state.tick();
		}
		if (state.expired()) {
			enterNextHuntingPhase();
		}
	}

	private void exitHuntingState() {
	}

	// PACMAN_DYING

	private void enterPacManDyingState() {
		enterState(PACMAN_DYING, clock.sec(6));
		pacMan.speed = 0;
		ui.stopAllSounds();
	}

	private void runPacManDyingState() {
		if (state.expired()) {
			exitPacManDyingState();
			if (lives > 0) {
				enterReadyState();
			} else {
				enterGameOverState();
			}
			return;
		}
		if (state.ticksRemaining() == clock.sec(4.5)) {
			for (Ghost ghost : ghosts) {
				ghost.visible = false;
			}
		}
		if (state.ticksRemaining() == clock.sec(3.5)) {
			pacMan.collapsingTicksLeft = 88;
			ui.playSound(Sound.PACMAN_DEATH);
		}
		if (pacMan.collapsingTicksLeft > 1) { // display Pac-Man completely collapsed till end of state
			pacMan.collapsingTicksLeft--;
		}
		state.tick();
	}

	private void exitPacManDyingState() {
		lives -= 1;
		pacMan.collapsingTicksLeft = 0;
		for (Ghost ghost : ghosts) {
			ghost.visible = true;
		}
	}

	// GHOST_DYING

	private void enterGhostDyingState() {
		previousState = state;
		enterState(GHOST_DYING, clock.sec(0.75));
		pacMan.visible = false;
		ui.playSound(Sound.GHOST_DEATH);
	}

	private void runGhostDyingState() {
		if (state.expired()) {
			exitGhostDyingState();
			state = previousState;
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
			log("Level %d complete, entering level %d", level, level + 1);
			startLevel(++level);
			enterReadyState();
			return;
		}
		if (state.ticksRemaining() == clock.sec(level().numFlashes + 1)) {
			for (Ghost ghost : ghosts) {
				ghost.visible = false;
			}
		}
		if (state.ticksRemaining() == clock.sec(level().numFlashes)) {
			mazeFlashesRemaining = level().numFlashes;
		}
		state.tick();
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
		if (state.expired() || ui.keyPressed("space")) {
			exitGameOverState();
			enterIntroState();
			return;
		}
		state.tick();
	}

	private void exitGameOverState() {
		reset();
		log("Left game over state");
	}

	// END STATE-MACHINE

	private void updatePacMan() {
		if (pacMan.restingTicksLeft == 0) {
			tryMoving(pacMan);
		} else {
			pacMan.restingTicksLeft--;
		}
		updatePacManPower();
	}

	private Ghost checkGhostCollision() {
		V2i tile = pacMan.tile();
		for (Ghost ghost : ghosts) {
			if (!ghost.dead && tile.equals(ghost.tile())) {
				return ghost;
			}
		}
		return null;
	}

	private void checkPacManFoundBonus() {
		V2i tile = pacMan.tile();
		if (!world.isBonusTile(tile.x, tile.y) || bonusAvailableTimer == 0) {
			return;
		}
		bonusAvailableTimer = 0;
		bonusConsumedTimer = clock.sec(2);
		score(level().bonusPoints);
		ui.playSound(Sound.EAT_BONUS);
		log("Pac-Man found bonus (id=%d) of value %d", level().bonusSymbol, level().bonusPoints);
	}

	private void checkPacManFoundFood() {
		V2i tile = pacMan.tile();
		if (world.isFoodTile(tile.x, tile.y) && !world.foodEatenAt(tile.x, tile.y)) {
			onPacManFoundFood(tile);
		} else {
			onPacManStarved();
		}
	}

	private void onPacManStarved() {
		pacMan.starvingTicks++;
		if (pacMan.starvingTicks >= starvingTimeLimit()) {
			preferredLockedGhost().ifPresent(ghost -> {
				releaseGhost(ghost, "Pac-Man has been starving for %d ticks", pacMan.starvingTicks);
				pacMan.starvingTicks = 0;
			});
		}
	}

	private void onPacManFoundFood(V2i tile) {
		ui.playSound(Sound.MUNCH, false);
		world.eatFood(tile.x, tile.y);
		pacMan.starvingTicks = 0;
		updateGhostDotCounters();
		mayBeEnterElroyMode();
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
		int eaten = TOTAL_FOOD_COUNT - world.foodRemaining;
		if (bonusAvailableTimer == 0 && (eaten == 70 || eaten == 170)) {
			bonusAvailableTimer = clock.sec(9 + rnd.nextFloat());
		}
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
			forceGhostsTurningBack();
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

	private void updatePacManPower() {
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

	private void killGhost(Ghost ghost) {
		ghostsKilledInLevel++;
		ghost.dead = true;
		ghost.frightened = false;
		ghost.targetTile = HOUSE_ENTRY;
		ghost.bounty = ghostBounty;
		log("Ghost %s killed at tile %s, Pac-Man wins %d points", ghost.name(), ghost.tile(), ghost.bounty);
		score(ghost.bounty);
		if (ghostsKilledInLevel == 16) {
			score(12000);
		}
		ghostBounty *= 2;
	}

	private void updateBonus() {
		// bonus active and not consumed
		if (bonusAvailableTimer > 0) {
			--bonusAvailableTimer;
		}
		// bonus active and consumed
		if (bonusConsumedTimer > 0) {
			--bonusConsumedTimer;
		}
	}

	private void updateGhost(Ghost ghost) {
		if (ghost.locked) {
			maybeReleaseGhost(ghost);
		} else if (ghost.enteringHouse) {
			letGhostEnterHouse(ghost);
		} else if (ghost.leavingHouse) {
			letGhostLeaveHouse(ghost);
		} else if (ghost.dead) {
			letGhostReturnHome(ghost);
		} else if (state == HUNTING) {
			updateGhostTargetTile(ghost);
			letGhostHeadForTargetTile(ghost);
		}
	}

	private void maybeReleaseGhost(Ghost ghost) {
		if (ghost.id == BLINKY) {
			ghost.locked = false;
			return;
		}
		if (globalDotCounterEnabled && globalDotCounter >= globalDotLimit(ghost)) {
			releaseGhost(ghost, "Global dot counter is %d", globalDotCounter);
		} else if (!globalDotCounterEnabled && ghost.dotCounter >= privateDotLimit(ghost)) {
			releaseGhost(ghost, "%s's dot counter is %d", ghost.name(), ghost.dotCounter);
		} else {
			letGhostBounce(ghost);
		}
	}

	private void releaseGhost(Ghost ghost, String reason, Object... args) {
		ghost.locked = false;
		ghost.leavingHouse = true;
		if (ghost.id == CLYDE && ghosts[BLINKY].elroyMode < 0) {
			ghosts[BLINKY].elroyMode = (byte) -ghosts[BLINKY].elroyMode; // resume Elroy mode
			log("Blinky Elroy mode %d resumed", ghosts[BLINKY].elroyMode);
		}
		log("Ghost %s unlocked: %s", ghost.name(), String.format(reason, args));
	}

	private void updateGhostTargetTile(Ghost ghost) {
		boolean chasing = huntingPhase % 2 != 0;
		ghost.targetTile = chasing ? currentChasingTarget(ghost) : ghost.scatterTile;
		if (ghost == ghosts[BLINKY] && ghost.elroyMode > 0) {
			ghost.targetTile = pacMan.tile();
		}
	}

	private Optional<Ghost> preferredLockedGhost() {
		for (int ghostId : GHOST_UNLOCK_ORDER) {
			if (ghosts[ghostId].locked) {
				return Optional.of(ghosts[ghostId]);
			}
		}
		return Optional.empty();
	}

	/**
	 * @param ghost ghost
	 * @return dot limit for ghost at current game level
	 * 
	 * @see https://pacman.holenet.info/#CH2_Home_Sweet_Home
	 */
	private int privateDotLimit(Ghost ghost) {
		if (ghost.id == INKY) {
			return level == 1 ? 30 : 0;
		}
		if (ghost.id == CLYDE) {
			return level == 1 ? 60 : level == 2 ? 50 : 0;
		}
		return 0;
	}

	private int globalDotLimit(Ghost ghost) {
		return ghost.id == PINKY ? 7 : ghost.id == INKY ? 17 : Integer.MAX_VALUE;
	}

	private void resetAndEnableGlobalDotCounter() {
		globalDotCounter = 0;
		globalDotCounterEnabled = true;
		log("Global dot counter reset and enabled");
	}

	private int starvingTimeLimit() {
		return level < 5 ? clock.sec(4) : clock.sec(3);
	}

	private V2i currentChasingTarget(Ghost ghost) {
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

	private void letGhostHeadForTargetTile(Ghost ghost) {
		newWishDir(ghost).ifPresent(dir -> ghost.wishDir = dir);
		tryMoving(ghost);
	}

	private void letGhostReturnHome(Ghost ghost) {
		if (atGhostHouseDoor(ghost)) {
			ghost.setOffset(HTS, 0);
			ghost.dir = ghost.wishDir = DOWN;
			ghost.forcedOnTrack = false;
			ghost.enteringHouse = true;
			ghost.targetTile = ghost.id == BLINKY ? HOUSE_CENTER : ghost.homeTile;
			return;
		}
		letGhostHeadForTargetTile(ghost);
	}

	private boolean atGhostHouseDoor(Creature guy) {
		return guy.at(HOUSE_ENTRY) && Functions.differsAtMost(guy.offset().x, HTS, 2);
	}

	private boolean isGhostHunting(Ghost ghost) {
		return !ghost.dead && !ghost.locked && !ghost.enteringHouse && !ghost.leavingHouse && !ghost.frightened;
	}

	private void letGhostEnterHouse(Ghost ghost) {
		V2f offset = ghost.offset();
		// target reached?
		if (ghost.at(ghost.targetTile) && offset.y >= 0) {
			ghost.wishDir = ghost.dir.opposite();
			ghost.dead = false;
			ghost.enteringHouse = false;
			ghost.leavingHouse = true;
			return;
		}
		// move sidewards towards target tile?
		if (ghost.at(HOUSE_CENTER) && offset.y >= 0) {
			ghost.wishDir = ghost.targetTile.x < HOUSE_CENTER.x ? LEFT : RIGHT;
		}
		ghost.couldMove = tryMoving(ghost, ghost.wishDir);
	}

	private void letGhostLeaveHouse(Ghost ghost) {
		V2f offset = ghost.offset();
		// house left?
		if (ghost.at(HOUSE_ENTRY) && Functions.differsAtMost(offset.y, 0, 1)) {
			ghost.setOffset(HTS, 0);
			ghost.dir = ghost.wishDir = LEFT;
			ghost.forcedOnTrack = true;
			ghost.leavingHouse = false;
			return;
		}
		// center of house reached?
		if (ghost.at(HOUSE_CENTER) && Functions.differsAtMost(offset.x, 3, 1)) {
			ghost.setOffset(HTS, 0);
			ghost.wishDir = UP;
			tryMoving(ghost);
			return;
		}
		// keep bouncing until ghost can move towards middle of house
		if (ghost.wishDir == UP || ghost.wishDir == DOWN) {
			if (ghost.at(ghost.homeTile)) {
				ghost.setOffset(HTS, 0);
				ghost.wishDir = ghost.homeTile.x < HOUSE_CENTER.x ? RIGHT : LEFT;
			} else {
				letGhostBounce(ghost);
			}
			return;
		}
		tryMoving(ghost);
	}

	private Optional<Direction> newWishDir(Ghost ghost) {
		if (!ghost.changedTile) {
			return Optional.empty();
		}
		if (ghost.forcedTurningBack) {
			ghost.forcedTurningBack = false;
			return Optional.of(ghost.wishDir.opposite());
		}
		V2i tile = ghost.tile();
		if (world.isPortalTile(tile.x, tile.y)) {
			return Optional.empty();
		}
		if (ghost.frightened && world.isIntersectionTile(tile.x, tile.y)) {
			return Optional.of(randomMoveDir(ghost));
		}
		return bestDirection(ghost);
	}

	private Optional<Direction> bestDirection(Ghost ghost) {
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
			if (dir == UP && world.isUpwardsBlocked(neighbor.x, neighbor.y)) {
				if (!ghost.frightened && !ghost.dead) {
					continue;
				}
			}
			double dist = neighbor.distance(ghost.targetTile);
			if (dist < minDist) {
				minDistDir = dir;
				minDist = dist;
			}
		}
		return Optional.ofNullable(minDistDir);
	}

	private void forceGhostsTurningBack() {
		for (Ghost ghost : ghosts) {
			if (isGhostHunting(ghost)) {
				ghost.forcedTurningBack = true;
			}
		}
	}

	private void letGhostBounce(Ghost ghost) {
		tryMoving(ghost);
		if (!ghost.couldMove) {
			ghost.wishDir = ghost.wishDir.opposite();
		}
	}

	private void tryMoving(Creature guy) {
		guy.updateSpeed(world, level());
		if (guy.speed == 0) {
			return;
		}

		// entering portal?
		if (guy.at(PORTAL_RIGHT) && guy.dir == RIGHT) {
			guy.placeAt(PORTAL_LEFT.x, PORTAL_LEFT.y, 0, 0);
			return;
		}
		if (guy.at(PORTAL_LEFT) && guy.dir == LEFT) {
			guy.placeAt(PORTAL_RIGHT.x, PORTAL_RIGHT.y, 0, 0);
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
		V2i tile = guy.tile();
		V2f offset = guy.offset();
		V2i neighbor = tile.sum(dir.vec);
		// 100% speed corresponds to 1.25 pixels/tick
		float pixelsMoving = guy.speed * 1.25f;

		// check if guy can take the given direction at its current position
		if (guy.forcedOnTrack && canAccessTile(guy, neighbor.x, neighbor.y)) {
			if (dir == LEFT || dir == RIGHT) {
				if (Math.abs(offset.y) > pixelsMoving) {
					return false;
				}
				guy.setOffset(offset.x, 0);
			} else if (dir == UP || dir == DOWN) {
				if (Math.abs(offset.x) > pixelsMoving) {
					return false;
				}
				guy.setOffset(0, offset.y);
			}
		}

		V2f velocity = new V2f(dir.vec).scaled(pixelsMoving);
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
		return world.inMapRange(x, y) && !world.isWall(x, y);
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
		return dirs.get(new Random().nextInt(dirs.size()));
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
		for (int x = 0; x < WORLD_TILES.x; ++x) {
			for (int y = 0; y < WORLD_TILES.y; ++y) {
				if (world.isFoodTile(x, y) && !world.foodEatenAt(x, y) && !world.isEnergizerTile(x, y)) {
					world.eatFood(x, y);
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
		enterGhostDyingState();
	}
}
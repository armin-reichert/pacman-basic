package de.amr.games.pacman.core;

import static de.amr.games.pacman.core.Creature.offset;
import static de.amr.games.pacman.core.Creature.tile;
import static de.amr.games.pacman.core.PacManGameState.CHANGING_LEVEL;
import static de.amr.games.pacman.core.PacManGameState.GAME_OVER;
import static de.amr.games.pacman.core.PacManGameState.GHOST_DYING;
import static de.amr.games.pacman.core.PacManGameState.HUNTING;
import static de.amr.games.pacman.core.PacManGameState.INTRO;
import static de.amr.games.pacman.core.PacManGameState.PACMAN_DYING;
import static de.amr.games.pacman.core.PacManGameState.READY;
import static de.amr.games.pacman.core.PacManGameWorld.BLINKY;
import static de.amr.games.pacman.core.PacManGameWorld.CLYDE;
import static de.amr.games.pacman.core.PacManGameWorld.HTS;
import static de.amr.games.pacman.core.PacManGameWorld.INKY;
import static de.amr.games.pacman.core.PacManGameWorld.PINKY;
import static de.amr.games.pacman.core.PacManGameWorld.t;
import static de.amr.games.pacman.lib.Direction.DOWN;
import static de.amr.games.pacman.lib.Direction.LEFT;
import static de.amr.games.pacman.lib.Direction.RIGHT;
import static de.amr.games.pacman.lib.Direction.UP;
import static de.amr.games.pacman.lib.Functions.differsAtMost;
import static de.amr.games.pacman.lib.Logging.log;
import static java.lang.Math.abs;

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
 * <p>
 * Still missing:
 * <ul>
 * <li>Pac-Man "cornering"</li>
 * <li>Intermission scenes</li>
 * <li>Multiple players</li>
 * </ul>
 * 
 * @author Armin Reichert
 * 
 * @see <a href="https://pacman.holenet.info">Jamey Pittman: The Pac-Man Dossier
 * @see <a href="https://gameinternals.com/understanding-pac-man-ghost-behavior">Chad Birch:
 *      Understanding ghost behavior
 */
public class PacManGame {

	public static final byte CHERRIES = 0, STRAWBERRY = 1, PEACH = 2, APPLE = 3, GRAPES = 4, GALAXIAN = 5, BELL = 6,
			KEY = 7;

	/*@formatter:off*/
	static final PacManGameLevel[] LEVELS = {
	/* 1*/ new PacManGameLevel(CHERRIES,   100,  80, 75, 40,  20,  80, 10,  85,  90, 50, 6, 5),
	/* 2*/ new PacManGameLevel(STRAWBERRY, 300,  90, 85, 45,  30,  90, 15,  95,  95, 55, 5, 5),
	/* 3*/ new PacManGameLevel(PEACH,      500,  90, 85, 45,  40,  90, 20,  95,  95, 55, 4, 5),
	/* 4*/ new PacManGameLevel(PEACH,      500,  90, 85, 45,  40,  90, 20,  95,  95, 55, 3, 5),
	/* 5*/ new PacManGameLevel(APPLE,      700, 100, 95, 50,  40, 100, 20, 105, 100, 60, 2, 5),
	/* 6*/ new PacManGameLevel(APPLE,      700, 100, 95, 50,  50, 100, 25, 105, 100, 60, 5, 5),
	/* 7*/ new PacManGameLevel(GRAPES,    1000, 100, 95, 50,  50, 100, 25, 105, 100, 60, 2, 5),
	/* 8*/ new PacManGameLevel(GRAPES,    1000, 100, 95, 50,  50, 100, 25, 105, 100, 60, 2, 5),
	/* 9*/ new PacManGameLevel(GALAXIAN,  2000, 100, 95, 50,  60, 100, 30, 105, 100, 60, 1, 3),
	/*10*/ new PacManGameLevel(GALAXIAN,  2000, 100, 95, 50,  60, 100, 30, 105, 100, 60, 5, 5),
	/*11*/ new PacManGameLevel(BELL,      3000, 100, 95, 50,  60, 100, 30, 105, 100, 60, 2, 5),
	/*12*/ new PacManGameLevel(BELL,      3000, 100, 95, 50,  80, 100, 40, 105, 100, 60, 1, 3),
	/*13*/ new PacManGameLevel(KEY,       5000, 100, 95, 50,  80, 100, 40, 105, 100, 60, 1, 3),
	/*14*/ new PacManGameLevel(KEY,       5000, 100, 95, 50,  80, 100, 40, 105, 100, 60, 3, 5),
	/*15*/ new PacManGameLevel(KEY,       5000, 100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3),
	/*16*/ new PacManGameLevel(KEY,       5000, 100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3),
	/*17*/ new PacManGameLevel(KEY,       5000, 100, 95, 50, 100, 100, 50, 105,   0,  0, 0, 0),
	/*18*/ new PacManGameLevel(KEY,       5000, 100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3),
	/*19*/ new PacManGameLevel(KEY,       5000, 100, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0),
	/*20*/ new PacManGameLevel(KEY,       5000, 100, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0),
	/*21*/ new PacManGameLevel(KEY,       5000,  90, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0)
	};
	/*@formatter:on*/

	static final Direction[] DIRECTION_PRIORITY = { UP, LEFT, DOWN, RIGHT };

	public final Clock clock;
	public final Random rnd;
	public final Hiscore hiscore;

	public PacManGameWorld world;
	public PacMan pacMan;
	public Ghost[] ghosts;
	public PacManGameUI ui;

	public boolean gamePaused;
	public boolean gameStarted;

	public PacManGameState state;
	public PacManGameState previousState;
	public short levelNumber;
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

	public boolean autopilotMode;
	public final Autopilot autopilot;

	public PacManGame() {
		clock = new Clock();
		rnd = new Random();
		hiscore = new Hiscore();
		autopilot = new Autopilot();
		pacMan = new PacMan();
		ghosts = new Ghost[] { new Ghost(0), new Ghost(1), new Ghost(2), new Ghost(3) };
	}

	public void setWorld(PacManGameWorld world) {
		this.world = world;
		pacMan.name = "Pac-Man";
		pacMan.homeTile = world.pacManHome();
		ghosts[BLINKY].name = "Blinky";
		ghosts[BLINKY].homeTile = world.houseEntry();
		ghosts[BLINKY].scatterTile = world.scatterTileTopRight();
		ghosts[PINKY].name = "Pinky";
		ghosts[PINKY].homeTile = world.houseCenter();
		ghosts[PINKY].scatterTile = world.scatterTileTopLeft();
		ghosts[INKY].name = "Inky";
		ghosts[INKY].homeTile = world.houseLeft();
		ghosts[INKY].scatterTile = world.scatterTileBottomRight();
		ghosts[CLYDE].name = "Clyde";
		ghosts[CLYDE].homeTile = world.houseRight();
		ghosts[CLYDE].scatterTile = world.scatterTileBottomLeft();
	}

	public void start() {
		reset();
		ui.show();
		enterIntroState();
		log("Enter state '%s' for %s", stateDescription(), ticksDescription(state.duration()));
		new Thread(this::loop, "GameLoop").start();
	}

	private void loop() {
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

	public static PacManGameLevel level(int levelNumber) {
		return LEVELS[levelNumber <= 21 ? levelNumber - 1 : 20];
	}

	public PacManGameLevel level() {
		return level(levelNumber);
	}

	private void readInput() {
		if (ui.keyPressed("p")) {
			gamePaused = !gamePaused;
		}
		if (ui.keyPressed("s")) {
			clock.targetFrequency = clock.targetFrequency == 60 ? 30 : 60;
			log("Clock frequency changed to %d Hz", clock.targetFrequency);
		}
		if (ui.keyPressed("f")) {
			clock.targetFrequency = clock.targetFrequency == 60 ? 120 : 60;
			log("Clock frequency changed to %d Hz", clock.targetFrequency);
		}
		if (ui.keyPressed("d")) {
			ui.setDebugMode(!ui.isDebugMode());
			log("UI debug mode is %s", ui.isDebugMode() ? "on" : "off");
		}
		if (ui.keyPressed("a")) {
			autopilotMode = !autopilotMode;
			log("Pac-Man autopilot mode is %s", autopilotMode ? "on" : "off");
		}
	}

	private void reset() {
		gameStarted = false;
		score = 0;
		lives = 3;
		hiscore.load();
		setLevel(1);
	}

	private void setLevel(int number) {
		levelNumber = (short) number;
		huntingPhase = 0;
		mazeFlashesRemaining = 0;
		ghostBounty = 200;
		ghostsKilledInLevel = 0;
		bonusAvailableTicks = 0;
		bonusConsumedTicks = 0;
		for (Ghost ghost : ghosts) {
			ghost.dotCounter = 0;
			ghost.elroyMode = 0;
		}
		world.restoreFood();
	}

	private void resetGuys() {
		pacMan.visible = true;
		pacMan.speed = 0;
		pacMan.targetTile = null; // used in autopilot mode
		pacMan.changedTile = true;
		pacMan.couldMove = true;
		pacMan.forcedOnTrack = true;
		pacMan.dead = false;
		pacMan.powerTicksLeft = 0;
		pacMan.restingTicksLeft = 0;
		pacMan.starvingTicks = 0;
		pacMan.collapsingTicksLeft = 0;
		pacMan.placeAt(pacMan.homeTile.x, pacMan.homeTile.y, HTS, 0);
		pacMan.dir = pacMan.wishDir = RIGHT;

		for (Ghost ghost : ghosts) {
			ghost.visible = true;
			ghost.speed = 0;
			ghost.targetTile = null;
			ghost.changedTile = true;
			ghost.couldMove = true;
			ghost.forcedDirection = false;
			ghost.forcedOnTrack = ghost.id == BLINKY;
			ghost.dead = false;
			ghost.frightened = false;
			ghost.locked = true;
			ghost.enteringHouse = false;
			ghost.leavingHouse = false;
			ghost.bounty = 0;
			ghost.placeAt(ghost.homeTile.x, ghost.homeTile.y, HTS, 0);
			ghost.dir = ghost.wishDir = ghost.id == BLINKY ? LEFT : ghost.id == PINKY ? DOWN : UP;
			// these are only reset when entering level:
//		ghost.dotCounter = 0;
//		ghost.elroyMode = 0;
		}
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

	private PacManGameState changeState(Runnable oldStateExit, Runnable newStateEntry, Runnable action) {
		log("Exit state '%s'", stateDescription());
		oldStateExit.run();
		if (action != null) {
			action.run();
		}
		newStateEntry.run();
		log("Enter state '%s' for %s", stateDescription(), ticksDescription(state.duration()));
		return state;
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
		state = INTRO;
		state.setDuration(Long.MAX_VALUE);
		ui.startIntroScene();
	}

	private PacManGameState runIntroState() {
		if (ui.anyKeyPressed()) {
			return changeState(this::exitIntroState, this::enterReadyState, null);
		}
		return state.tick();
	}

	private void exitIntroState() {
		ui.endIntroScene();
	}

	// READY

	private void enterReadyState() {
		state = READY;
		if (!gameStarted) {
			state.setDuration(clock.sec(4.5));
			ui.playSound(Sound.GAME_READY);
			gameStarted = true;
		} else {
			state.setDuration(clock.sec(0.5));
		}
		resetGuys();
		bonusAvailableTicks = bonusConsumedTicks = 0;
	}

	private PacManGameState runReadyState() {
		if (state.expired()) {
			return changeState(this::exitReadyState, this::enterHuntingState, null);
		}
		for (Ghost ghost : ghosts) {
			if (ghost.id != BLINKY) {
				letGhostBounce(ghost);
			}
		}
		return state.tick();
	}

	private void exitReadyState() {
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
		int row = levelNumber == 1 ? 0 : levelNumber <= 4 ? 1 : 2;
		return huntingTicks(HUNTING_PHASE_DURATION[row][phase]);
	}

	private long huntingTicks(short duration) {
		if (duration == -1) {
			return 1; // -1 means a single tick
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

	private void startHuntingPhase(int phase) {
		huntingPhase = (byte) phase;
		state.setDuration(huntingPhaseDuration(huntingPhase));
		if (inScatteringPhase()) {
			if (huntingPhase >= 2) {
				ui.stopSound(siren(huntingPhase - 2));
			}
			ui.loopSound(siren(huntingPhase)); // TODO not clear when which siren should play
		}
		log("Hunting phase %d started, state is now %s", phase, stateDescription());
	}

	private void enterHuntingState() {
		state = HUNTING;
		startHuntingPhase(0);
	}

	private PacManGameState runHuntingState() {

		// Cheats
		if (ui.keyPressed("e")) {
			eatAllNormalPellets();
		}
		if (ui.keyPressed("x")) {
			return changeState(this::exitHuntingState, this::enterGhostDyingState, this::killAllGhosts);
		}
		if (ui.keyPressed("l")) {
			if (lives < Byte.MAX_VALUE) {
				lives++;
			}
		}

		if (state.expired()) {
			startHuntingPhase(++huntingPhase);
			forceHuntingGhostsTurningBack();
		}

		if (world.foodRemaining() == 0) {
			return changeState(this::exitHuntingState, this::enterChangingLevelState, null);
		}

		updatePacMan();
		for (Ghost ghost : ghosts) {
			updateGhost(ghost);
		}
		updateBonus();

		checkPacManFindsFood();
		checkPacManFindsBonus();

		Ghost collidingGhost = ghostCollidingWithPacMan();
		if (collidingGhost != null && collidingGhost.frightened) {
			return changeState(this::exitHuntingState, this::enterGhostDyingState, () -> killGhost(collidingGhost));
		}
		if (collidingGhost != null && !collidingGhost.frightened) {
			return changeState(this::exitHuntingState, this::enterPacManDyingState, () -> killPacMan(collidingGhost));
		}

		return pacMan.powerTicksLeft == 0 ? state.tick() : state;
	}

	private void exitHuntingState() {
	}

	// PACMAN_DYING

	private void enterPacManDyingState() {
		state = PACMAN_DYING;
		state.setDuration(clock.sec(6));
		pacMan.speed = 0;
		for (Ghost ghost : ghosts) {
			ghost.speed = 0;
		}
		ui.stopAllSounds();
	}

	private PacManGameState runPacManDyingState() {
		if (state.expired()) {
			if (lives > 0) {
				return changeState(this::exitPacManDyingState, this::enterReadyState, null);
			} else {
				return changeState(this::exitPacManDyingState, this::enterGameOverState, null);
			}
		}
		if (state.running(clock.sec(1.5))) {
			for (Ghost ghost : ghosts) {
				ghost.visible = false;
			}
		}
		if (state.running(clock.sec(2.5))) {
			pacMan.collapsingTicksLeft = 88;
			ui.playSound(Sound.PACMAN_DEATH);
		}
		if (pacMan.collapsingTicksLeft > 1) {
			// count down until 1 such that animation stays at last frame until state expires
			pacMan.collapsingTicksLeft--;
		}
		return state.tick();
	}

	private void exitPacManDyingState() {
		lives -= autopilotMode ? 0 : 1;
		pacMan.collapsingTicksLeft = 0;
		for (Ghost ghost : ghosts) {
			ghost.visible = true;
		}
	}

	// GHOST_DYING

	private void enterGhostDyingState() {
		previousState = state;
		state = GHOST_DYING;
		state.setDuration(clock.sec(1)); // TODO correct?
		pacMan.visible = false;
		ui.playSound(Sound.GHOST_DEATH);
	}

	private PacManGameState runGhostDyingState() {
		if (state.expired()) {
			return changeState(this::exitGhostDyingState, () -> state = previousState, () -> log("Resume state '%s'", state));
		}
		for (Ghost ghost : ghosts) {
			if (ghost.dead && ghost.bounty == 0) {
				updateGhost(ghost);
			}
		}
		return state.tick();
	}

	private void exitGhostDyingState() {
		for (Ghost ghost : ghosts) {
			if (ghost.dead && ghost.bounty > 0) {
				ghost.bounty = 0;
			}
		}
		pacMan.visible = true;
		ui.loopSound(Sound.RETREATING);
	}

	// CHANGING_LEVEL

	private void enterChangingLevelState() {
		state = CHANGING_LEVEL;
		state.setDuration(clock.sec(level().numFlashes + 2));
		for (Ghost ghost : ghosts) {
			ghost.frightened = false;
			ghost.dead = false;
			ghost.speed = 0;
		}
		pacMan.speed = 0;
		ui.stopAllSounds();
	}

	private PacManGameState runChangingLevelState() {
		if (state.expired()) {
			return changeState(this::exitChangingLevelState, this::enterReadyState, null);
		}
		if (state.running() == clock.sec(1)) {
			for (Ghost ghost : ghosts) {
				ghost.visible = false;
			}
		}
		if (state.running() == clock.sec(2)) {
			mazeFlashesRemaining = level().numFlashes;
		}
		return state.tick();
	}

	private void exitChangingLevelState() {
		log("Level %d complete, entering level %d", levelNumber, levelNumber + 1);
		setLevel(++levelNumber);
	}

	// GAME_OVER

	private void enterGameOverState() {
		state = GAME_OVER;
		state.setDuration(clock.sec(30));
		for (Ghost ghost : ghosts) {
			ghost.speed = 0;
		}
		pacMan.speed = 0;
		if (hiscore.changed) {
			hiscore.save();
		}
	}

	private PacManGameState runGameOverState() {
		if (state.expired() || ui.anyKeyPressed()) {
			return changeState(this::exitGameOverState, this::enterIntroState, null);
		}
		return state.tick();
	}

	private void exitGameOverState() {
		reset();
	}

	// END STATE-MACHINE

	private void updatePacMan() {
		if (pacMan.restingTicksLeft == 0) {
			updatePacManDirection();
			pacMan.speed = pacMan.powerTicksLeft == 0 ? level().pacManSpeed : level().pacManSpeedPowered;
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
		if (autopilotMode) {
			autopilot.controlPacMan(this);
		} else {
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
	}

	private Ghost ghostCollidingWithPacMan() {
		return Stream.of(ghosts).filter(ghost -> !ghost.dead).filter(ghost -> ghost.tile().equals(pacMan.tile())).findAny()
				.orElse(null);
	}

	private void killPacMan(Ghost killer) {
		pacMan.dead = true;
		log("Pac-Man killed by %s at tile %s", killer.name, killer.tile());
		resetAndEnableGlobalDotCounter();
		byte elroyMode = ghosts[BLINKY].elroyMode;
		if (elroyMode > 0) {
			ghosts[BLINKY].elroyMode = (byte) -elroyMode; // negative value means "disabled"
			log("Blinky Elroy mode %d disabled", elroyMode);
		}
	}

	private void checkPacManFindsBonus() {
		if (bonusAvailableTicks > 0 && world.bonusTile().equals(pacMan.tile())) {
			bonusAvailableTicks = 0;
			bonusConsumedTicks = clock.sec(2);
			score(level().bonusPoints);
			ui.playSound(Sound.EAT_BONUS);
			log("Pac-Man found bonus (%d) of value %d", level().bonusSymbol, level().bonusPoints);
		}
	}

	private void checkPacManFindsFood() {
		V2i pacManLocation = pacMan.tile();
		if (world.containsFood(pacManLocation.x, pacManLocation.y)) {
			onPacManFoundFood(pacManLocation);
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

	private void onPacManFoundFood(V2i foodLocation) {
		if (world.isEnergizerTile(foodLocation.x, foodLocation.y)) {
			score(50);
			givePacManPower();
			pacMan.restingTicksLeft = 3;
			ghostBounty = 200;
		} else {
			score(10);
			pacMan.restingTicksLeft = 1;
		}
		pacMan.starvingTicks = 0;
		world.removeFood(foodLocation.x, foodLocation.y);
		if (world.foodRemaining() == level().elroy1DotsLeft) {
			ghosts[BLINKY].elroyMode = 1;
			log("Blinky becomes Cruise Elroy 1");
		} else if (world.foodRemaining() == level().elroy2DotsLeft) {
			ghosts[BLINKY].elroyMode = 2;
			log("Blinky becomes Cruise Elroy 2");
		}
		int eaten = world.eatenFoodCount();
		if (eaten == 70 || eaten == 170) {
			bonusAvailableTicks = clock.sec(9 + rnd.nextFloat());
		}
		updateGhostDotCounters();
		ui.playSound(Sound.MUNCH);
	}

	private void givePacManPower() {
		int seconds = level().ghostFrightenedSeconds;
		pacMan.powerTicksLeft = clock.sec(seconds);
		if (seconds > 0) {
			log("Pac-Man got power for %d seconds", seconds);
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

	private void killGhost(Ghost ghost) {
		ghost.frightened = false;
		ghost.dead = true;
		ghost.speed = 2 * level().ghostSpeed; // TODO correct?
		ghost.targetTile = world.houseEntry();
		ghost.bounty = ghostBounty;
		score(ghost.bounty);
		ghostsKilledInLevel++;
		if (ghostsKilledInLevel == 16) {
			score(12000);
		}
		ghostBounty *= 2;
		log("Ghost %s killed at tile %s, Pac-Man wins %d points", ghost.name, ghost.tile(), ghost.bounty);
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
			letGhostHunt(ghost);
		}
	}

	private void tryReleasingGhost(Ghost ghost) {
		if (globalDotCounterEnabled && globalDotCounter >= ghostGlobalDotLimit(ghost)) {
			releaseGhost(ghost, "Global dot counter (%d) reached limit (%d)", globalDotCounter, ghostGlobalDotLimit(ghost));
		} else if (!globalDotCounterEnabled && ghost.dotCounter >= ghostPrivateDotLimit(ghost)) {
			releaseGhost(ghost, "%s's dot counter (%d) reached limit (%d)", ghost.name, ghost.dotCounter,
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
		log("Ghost %s released: %s", ghost.name, String.format(reason, args));
	}

	private Optional<Ghost> preferredLockedGhost() {
		return Stream.of(ghosts[PINKY], ghosts[INKY], ghosts[CLYDE]).filter(ghost -> ghost.locked).findFirst();
	}

	private int ghostPrivateDotLimit(Ghost ghost) {
		if (ghost.id == INKY) {
			return levelNumber == 1 ? 30 : 0;
		}
		if (ghost.id == CLYDE) {
			return levelNumber == 1 ? 60 : levelNumber == 2 ? 50 : 0;
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
		return levelNumber < 5 ? clock.sec(4) : clock.sec(3);
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
			return ghost.tile().euclideanDistance(pacMan.tile()) < 8 ? ghost.scatterTile : pacMan.tile();
		}
		default:
			throw new IllegalArgumentException("Unknown ghost id: " + ghost.id);
		}
	}

	private void letGhostBounce(Ghost ghost) {
		int ceiling = t(world.houseCenter().y - 1) + 3, ground = t(world.houseCenter().y) + 4;
		if (ghost.position.y <= ceiling || ghost.position.y >= ground) {
			ghost.wishDir = ghost.dir.opposite();
		}
		ghost.speed = level().ghostSpeedTunnel; // TODO correct?
		tryMoving(ghost);
	}

	private void letGhostHunt(Ghost ghost) {
		V2i tile = ghost.tile();
		if (world.isTunnel(tile.x, tile.y)) {
			ghost.speed = level().ghostSpeedTunnel;
		} else if (ghost.frightened) {
			ghost.speed = level().ghostSpeedFrightened;
		} else if (ghost.id == BLINKY && ghost.elroyMode == 1) {
			ghost.speed = level().elroy1Speed;
		} else if (ghost.id == BLINKY && ghost.elroyMode == 2) {
			ghost.speed = level().elroy2Speed;
		} else {
			ghost.speed = level().ghostSpeed;
		}
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
			ghost.targetTile = ghost.id == BLINKY ? world.houseCenter() : ghost.homeTile;
			return;
		}
		letGhostHeadForTargetTile(ghost);
	}

	private void letGhostEnterHouse(Ghost ghost) {
		V2i ghostLocation = ghost.tile();
		V2f offset = ghost.offset();
		// Target reached? Revive and start leaving house.
		if (ghostLocation.equals(ghost.targetTile) && offset.y >= 0) {
			ghost.dead = false;
			ghost.enteringHouse = false;
			ghost.leavingHouse = true;
			ghost.speed = level().ghostSpeed; // TODO correct?
			ghost.wishDir = ghost.dir.opposite();
			if (Stream.of(ghosts).noneMatch(g -> g.dead)) {
				ui.stopSound(Sound.RETREATING);
			}
			return;
		}
		// House center reached? Move sidewards towards target tile
		if (ghostLocation.equals(world.houseCenter()) && offset.y >= 0) {
			ghost.wishDir = ghost.targetTile.x < world.houseCenter().x ? LEFT : RIGHT;
		}
		ghost.couldMove = tryMoving(ghost, ghost.wishDir);
	}

	private void letGhostLeaveHouse(Ghost ghost) {
		V2i ghostLocation = ghost.tile();
		V2f offset = ghost.offset();
		// leaving house complete?
		if (ghostLocation.equals(world.houseEntry()) && differsAtMost(offset.y, 0, 1)) {
			ghost.setOffset(HTS, 0);
			ghost.dir = ghost.wishDir = LEFT;
			ghost.forcedOnTrack = true;
			ghost.leavingHouse = false;
			return;
		}
		// at house middle and rising?
		if (ghostLocation.x == world.houseCenter().x && differsAtMost(offset.x, 3, 1)) {
			ghost.setOffset(HTS, offset.y);
			ghost.wishDir = UP;
			ghost.couldMove = tryMoving(ghost, ghost.wishDir);
			return;
		}
		// move sidewards towards center
		if (ghostLocation.x == ghost.homeTile.x && differsAtMost(offset.y, 0, 1)) {
			ghost.wishDir = ghost.homeTile.x < world.houseCenter().x ? RIGHT : LEFT;
			ghost.couldMove = tryMoving(ghost, ghost.wishDir);
			return;
		}
		// keep bouncing until ghost can move towards center
		letGhostBounce(ghost);
	}

	private boolean atGhostHouseDoor(Creature guy) {
		return guy.tile().equals(world.houseEntry()) && differsAtMost(guy.offset().x, HTS, 2);
	}

	private Optional<Direction> newGhostWishDir(Ghost ghost) {
		if (ghost.couldMove && !ghost.changedTile) {
			return Optional.empty();
		}
		if (ghost.forcedDirection) {
			ghost.forcedDirection = false;
			return Optional.of(ghost.wishDir);
		}
		V2i ghostLocation = ghost.tile();
		if (world.isPortal(ghostLocation.x, ghostLocation.y)) {
			return Optional.empty();
		}
		if (ghost.frightened && world.isIntersection(ghostLocation.x, ghostLocation.y)) {
			return Optional.of(randomPossibleMoveDir(ghost));
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
			if (dir == UP && world.isUpwardsBlocked(neighbor.x, neighbor.y) && !(ghost.frightened || ghost.dead)) {
				continue;
			}
			double dist = neighbor.euclideanDistance(ghost.targetTile);
			if (dist < minDist) {
				minDist = dist;
				minDistDir = dir;
			}
		}
		return Optional.ofNullable(minDistDir);
	}

	boolean isGhostHunting(Ghost ghost) {
		return !(ghost.dead || ghost.locked || ghost.enteringHouse || ghost.leavingHouse || ghost.frightened);
	}

	private void forceHuntingGhostsTurningBack() {
		for (Ghost ghost : ghosts) {
			if (isGhostHunting(ghost)) {
				ghost.wishDir = ghost.dir.opposite();
				ghost.forcedDirection = true;
			}
		}
	}

	private void tryMoving(Creature guy) {
		V2i guyLocation = guy.tile();
		if (guyLocation.equals(world.portalRight()) && guy.dir == RIGHT) {
			guy.placeAt(world.portalLeft().x, world.portalLeft().y, 0, 0);
			return;
		}
		if (guyLocation.equals(world.portalLeft()) && guy.dir == LEFT) {
			guy.placeAt(world.portalRight().x, world.portalRight().y, 0, 0);
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
		// 100% speed corresponds to 1.25 pixels/tick (75px/sec)
		float pixels = guy.speed * 1.25f;

		V2i guyLocationBeforeMove = guy.tile();
		V2f offset = guy.offset();
		V2i neighbor = guyLocationBeforeMove.sum(dir.vec);

		// check if guy can change its direction now
		if (guy.forcedOnTrack && canAccessTile(guy, neighbor.x, neighbor.y)) {
			if (dir == LEFT || dir == RIGHT) {
				if (abs(offset.y) > pixels) {
					return false;
				}
				guy.setOffset(offset.x, 0);
			} else if (dir == UP || dir == DOWN) {
				if (abs(offset.x) > pixels) {
					return false;
				}
				guy.setOffset(0, offset.y);
			}
		}

		V2f velocity = new V2f(dir.vec).scaled(pixels);
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
		guy.changedTile = !guy.tile().equals(guyLocationBeforeMove);
		return true;
	}

	boolean canAccessTile(Creature guy, int x, int y) {
		if (world.isPortal(x, y)) {
			return true;
		}
		if (world.isGhostHouseDoor(x, y)) {
			return guy instanceof Ghost && (((Ghost) guy).enteringHouse || ((Ghost) guy).leavingHouse);
		}
		return world.inMapRange(x, y) && !world.isWall(x, y);
	}

	private Direction randomPossibleMoveDir(Creature guy) {
		//@formatter:off
		List<Direction> dirs = Stream.of(Direction.values())
			.filter(dir -> dir != guy.dir.opposite())
			.filter(dir -> {
				V2i neighbor = guy.tile().sum(dir.vec);
				return world.isAccessible(neighbor.x, neighbor.y);
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
		hiscore.update(score, levelNumber);
	}

	// Cheats

	private void eatAllNormalPellets() {
		for (int x = 0; x < world.size().x; ++x) {
			for (int y = 0; y < world.size().y; ++y) {
				if (world.containsFood(x, y) && !world.isEnergizerTile(x, y)) {
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
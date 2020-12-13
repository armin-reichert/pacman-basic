package de.amr.games.pacman.core;

import static de.amr.games.pacman.core.PacManGameState.CHANGING_LEVEL;
import static de.amr.games.pacman.core.PacManGameState.GAME_OVER;
import static de.amr.games.pacman.core.PacManGameState.GHOST_DYING;
import static de.amr.games.pacman.core.PacManGameState.HUNTING;
import static de.amr.games.pacman.core.PacManGameState.INTRO;
import static de.amr.games.pacman.core.PacManGameState.PACMAN_DYING;
import static de.amr.games.pacman.core.PacManGameState.READY;
import static de.amr.games.pacman.core.PacManGameWorld.HOUSE_CENTER;
import static de.amr.games.pacman.core.PacManGameWorld.HOUSE_ENTRY;
import static de.amr.games.pacman.core.PacManGameWorld.HOUSE_LEFT;
import static de.amr.games.pacman.core.PacManGameWorld.HOUSE_RIGHT;
import static de.amr.games.pacman.core.PacManGameWorld.HTS;
import static de.amr.games.pacman.core.PacManGameWorld.LOWER_LEFT_CORNER;
import static de.amr.games.pacman.core.PacManGameWorld.LOWER_RIGHT_CORNER;
import static de.amr.games.pacman.core.PacManGameWorld.PACMAN_HOME;
import static de.amr.games.pacman.core.PacManGameWorld.PORTAL_LEFT;
import static de.amr.games.pacman.core.PacManGameWorld.PORTAL_RIGHT;
import static de.amr.games.pacman.core.PacManGameWorld.TOTAL_FOOD_COUNT;
import static de.amr.games.pacman.core.PacManGameWorld.UPPER_LEFT_CORNER;
import static de.amr.games.pacman.core.PacManGameWorld.UPPER_RIGHT_CORNER;
import static de.amr.games.pacman.core.PacManGameWorld.WORLD_HEIGHT_TILES;
import static de.amr.games.pacman.core.PacManGameWorld.WORLD_WIDTH_TILES;
import static de.amr.games.pacman.entities.Creature.offset;
import static de.amr.games.pacman.entities.Creature.tile;
import static de.amr.games.pacman.entities.Ghost.BLINKY;
import static de.amr.games.pacman.entities.Ghost.CLYDE;
import static de.amr.games.pacman.entities.Ghost.INKY;
import static de.amr.games.pacman.entities.Ghost.PINKY;
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

import de.amr.games.pacman.entities.Creature;
import de.amr.games.pacman.entities.Ghost;
import de.amr.games.pacman.entities.PacMan;
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
public class PacManGame implements Runnable {

	static final PacManGameLevel[] LEVELS = {
	/*@formatter:off*/
		new PacManGameLevel("Cherries",   100,  80, 75, 40,  20,  80, 10,  85,  90, 50, 6, 5),
		new PacManGameLevel("Strawberry", 300,  90, 85, 45,  30,  90, 15,  95,  95, 55, 5, 5),
		new PacManGameLevel("Peach",      500,  90, 85, 45,  40,  90, 20,  95,  95, 55, 4, 5),
		new PacManGameLevel("Peach",      500,  90, 85, 50,  40, 100, 20,  95,  95, 55, 3, 5),
		new PacManGameLevel("Apple",      700, 100, 95, 50,  40, 100, 20, 105, 100, 60, 2, 5),
		new PacManGameLevel("Apple",      700, 100, 95, 50,  50, 100, 25, 105, 100, 60, 5, 5),
		new PacManGameLevel("Grapes",    1000, 100, 95, 50,  50, 100, 25, 105, 100, 60, 2, 5),
		new PacManGameLevel("Grapes",    1000, 100, 95, 50,  50, 100, 25, 105, 100, 60, 2, 5),
		new PacManGameLevel("Galaxian",  2000, 100, 95, 50,  60, 100, 30, 105, 100, 60, 1, 3),
		new PacManGameLevel("Galaxian",  2000, 100, 95, 50,  60, 100, 30, 105, 100, 60, 5, 5),
		new PacManGameLevel("Bell",      3000, 100, 95, 50,  60, 100, 30, 105, 100, 60, 2, 5),
		new PacManGameLevel("Bell",      3000, 100, 95, 50,  80, 100, 40, 105, 100, 60, 1, 3),
		new PacManGameLevel("Key",       5000, 100, 95, 50,  80, 100, 40, 105, 100, 60, 1, 3),
		new PacManGameLevel("Key",       5000, 100, 95, 50,  80, 100, 40, 105, 100, 60, 3, 5),
		new PacManGameLevel("Key",       5000, 100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3),
		new PacManGameLevel("Key",       5000, 100, 95, 50, 100, 100, 50, 105,   0,  0, 1, 3),
		new PacManGameLevel("Key",       5000, 100, 95, 50, 100, 100, 50, 105, 100, 60, 0, 0),
		new PacManGameLevel("Key",       5000, 100, 95, 50, 100, 100, 50, 105,   0,  0, 1, 0),
		new PacManGameLevel("Key",       5000, 100, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0),
		new PacManGameLevel("Key",       5000, 100, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0),
		new PacManGameLevel("Key",       5000,  90, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0)
	/*@formatter:on*/
	};

	static final Direction[] DIRECTION_PRIORITY = { UP, LEFT, DOWN, RIGHT };

	static final int[] GHOST_UNLOCK_ORDER = { PINKY, INKY, CLYDE };

	public final PacManGameWorld world;
	public final PacMan pacMan;
	public final Ghost[] ghosts;
	public final Hiscore hiscore;
	public final Clock clock;
	public PacManGameUI ui;

	public boolean paused;

	public PacManGameState state;
	public PacManGameState previousState;
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

	public PacManGame() {
		clock = new Clock();
		world = new PacManGameWorld();
		hiscore = new Hiscore();
		pacMan = new PacMan("Pac-Man", PACMAN_HOME);
		ghosts = new Ghost[4];
		ghosts[BLINKY] = new Ghost("Blinky", HOUSE_ENTRY, UPPER_RIGHT_CORNER);
		ghosts[PINKY] = new Ghost("Pinky", HOUSE_CENTER, UPPER_LEFT_CORNER);
		ghosts[INKY] = new Ghost("Inky", HOUSE_LEFT, LOWER_RIGHT_CORNER);
		ghosts[CLYDE] = new Ghost("Clyde", HOUSE_RIGHT, LOWER_LEFT_CORNER);
	}

	@Override
	public void run() {
		reset();
		enterIntroState();
		ui.show();
		while (true) {
			clock.tick(() -> {
				readInput();
				updateState();
				ui.render();
			});
		}
	}

	public void exit() {
		if (hiscore.changed) {
			hiscore.save();
		}
	}

	public PacManGameLevel level(int level) {
		int index = level <= 21 ? level - 1 : 20;
		return LEVELS[index];
	}

	public PacManGameLevel level() {
		return level(level);
	}

	private void reset() {
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
		for (PacManGameState state : PacManGameState.values()) {
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
		pacMan.powerTicks = 0;
		pacMan.restingTicks = 0;
		pacMan.starvingTicks = 0;
		pacMan.placeAt(pacMan.homeTile.x, pacMan.homeTile.y, HTS, 0);

		for (Ghost ghost : ghosts) {
			ghost.visible = true;
			ghost.speed = 0;
			ghost.targetTile = null;
			ghost.changedTile = true;
			ghost.couldMove = true;
			ghost.forcedTurningBack = false;
			ghost.forcedOnTrack = ghost == ghosts[BLINKY];
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
			boolean chasing = huntingPhase % 2 == 1;
			int step = huntingPhase / 2;
			return String.format(chasing ? "%s-chasing-%d" : "%s-scattering-%d", state, step);
		}
		return state.name();
	}

	private void enterState(PacManGameState newState, long duration) {
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
		enterState(READY, clock.sec(6));
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
		if (state.ticksRemaining() == clock.sec(5)) {
			ui.playSound(Sound.GAME_READY);
		}
		if (state.ticksRemaining() <= clock.sec(5)) {
			letGhostBounce(ghosts[INKY]);
			letGhostBounce(ghosts[PINKY]);
			letGhostBounce(ghosts[CLYDE]);
		}
		state.tick();
	}

	private void exitReadyState() {
		ghosts[BLINKY].locked = false;
	}

	// HUNTING

	static final short[][] HUNTING_PHASE_DURATION = {
		//@formatter:off
		{ 7, 20, 7, 20, 5,   20,  5, Short.MAX_VALUE },
		{ 7, 20, 7, 20, 5, 1033, -1, Short.MAX_VALUE },
		{ 5,  5, 5,  5, 5, 1037, -1, Short.MAX_VALUE },
		//@formatter:on
	};

	private long ticks(short duration) {
		if (duration == -1) {
			return 1; // 1 tick
		}
		if (duration == Short.MAX_VALUE) {
			return Long.MAX_VALUE;
		}
		return clock.sec(duration);
	}

	private long huntingPhaseDuration() {
		int index = level == 1 ? 0 : level <= 4 ? 1 : 2;
		return ticks(HUNTING_PHASE_DURATION[index][huntingPhase]);
	}

	private void enterNextHuntingPhase() {
		huntingPhase++;
		state.setDuration(huntingPhaseDuration());
		forceGhostsTurningBack();
		log("Game state updated to %s for %d ticks", stateDescription(), state.ticksRemaining());
		boolean chasing = huntingPhase % 2 == 1;
		if (chasing) {
			ui.loopSound(Sound.SIREN);
		} else {
			ui.stopSound(Sound.SIREN);
		}
	}

	private void enterHuntingState() {
		huntingPhase = 0;
		enterState(HUNTING, huntingPhaseDuration());
	}

	private void runHuntingState() {
		if (pacMan.dead) {
			exitHuntingState();
			enterPacManDyingState();
			return;
		}
		if (world.foodRemaining == 0) {
			exitHuntingState();
			enterChangingLevelState();
			return;
		}
		if (state.expired()) {
			enterNextHuntingPhase();
		}
		updatePacMan();
		for (Ghost ghost : ghosts) {
			updateGhost(ghost);
		}
		updateBonus();

		if (pacMan.powerTicks == 0) {
			state.tick();
		}
	}

	private void exitHuntingState() {
		ui.stopSound(Sound.SIREN);
	}

	// PACMAN_DYING

	private void enterPacManDyingState() {
		// 11 animation frames, 8 ticks each, 2 seconds before animation, 2 seconds after
		enterState(PACMAN_DYING, clock.sec(2) + 88 + clock.sec(2));
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
		if (state.ticksRemaining() == clock.sec(2.5f) + 88) {
			for (Ghost ghost : ghosts) {
				ghost.visible = false;
			}
		}
		if (state.ticksRemaining() == clock.sec(2.5f) + 40) {
			ui.playSound(Sound.PACMAN_DEATH);
		}
		state.tick();
	}

	private void exitPacManDyingState() {
		lives -= 1;
		for (Ghost ghost : ghosts) {
			ghost.visible = true;
		}
	}

	// GHOST_DYING

	private void enterGhostDyingState() {
		previousState = state;
		enterState(GHOST_DYING, clock.sec(0.5f));
		pacMan.visible = false;
		ui.playSound(Sound.GHOST_DEATH);
	}

	private void runGhostDyingState() {
		if (state.expired()) {
			exitGhostDyingState();
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
		state = previousState;
	}

	// CHANGING_LEVEL

	private void enterChangingLevelState() {
		enterState(CHANGING_LEVEL, clock.sec(4 + level().numFlashes));
		mazeFlashesRemaining = level().numFlashes;
		for (Ghost ghost : ghosts) {
			ghost.frightened = false;
			ghost.dead = false;
			ghost.speed = 0;
		}
		pacMan.speed = 0;
	}

	private void runChangingLevelState() {
		if (state.expired()) {
			log("Level %d complete, entering level %d", level, level + 1);
			startLevel(++level);
			enterReadyState();
			return;
		}
		if (state.ticksRemaining() == clock.sec(2 + level().numFlashes)) {
			for (Ghost ghost : ghosts) {
				ghost.visible = false;
			}
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
		V2i tile = pacMan.tile();
		if (pacMan.restingTicks == 0) {
			tryMoving(pacMan);
		} else {
			pacMan.restingTicks--;
		}
		updatePacManPower();
		checkPacManFoundFood(tile);
		checkPacManFoundBonus(tile);

		// ghost(s) at current tile?
		for (Ghost ghost : ghosts) {
			if (!tile.equals(ghost.tile())) {
				continue;
			}
			if (ghost.dead) {
				continue;
			}
			if (ghost.frightened) {
				killGhost(ghost);
				enterGhostDyingState();
				return;
			}
			if (pacMan.powerTicks == 0) {
				pacMan.dead = true;
				log("Pac-Man killed by %s at tile %s", ghost.name, ghost.tile());
				globalDotCounter = 0;
				globalDotCounterEnabled = true;
				log("Global dot counter reset and enabled");
				if (ghosts[BLINKY].elroyMode > 0) {
					log("Blinky Elroy mode %d disabled", ghosts[BLINKY].elroyMode);
					ghosts[BLINKY].elroyMode = (byte) -ghosts[BLINKY].elroyMode; // disabled
				}
				return;
			}
		}
	}

	private void checkPacManFoundBonus(V2i tile) {
		if (bonusAvailableTimer > 0 && world.isBonusTile(tile.x, tile.y)) {
			bonusAvailableTimer = 0;
			bonusConsumedTimer = clock.sec(2);
			score(level().bonusPoints);
			ui.playSound(Sound.EAT_BONUS);
			log("Pac-Man found bonus %s of value %d", level().bonusSymbol, level().bonusPoints);
		}
	}

	private void checkPacManFoundFood(V2i tile) {
		if (world.isFoodTile(tile.x, tile.y) && !world.hasEatenFood(tile.x, tile.y)) {
			onPacManFoundFood(tile);
		} else {
			onPacManStarved();
		}
	}

	private void onPacManStarved() {
		pacMan.starvingTicks++;
		if (pacMan.starvingTicks >= starvingTimeLimit()) {
			preferredLockedGhost().ifPresent(ghost -> {
				unlockGhost(ghost, "Pac-Man starving for %d ticks", pacMan.starvingTicks);
				pacMan.starvingTicks = 0;
			});
		}
	}

	private void onPacManFoundFood(V2i tile) {
		ui.playSound(Sound.CHOMP, false);
		world.eatFood(tile.x, tile.y);
		pacMan.starvingTicks = 0;
		updateGhostDotCounters();
		mayBeEnterElroyMode();
		if (world.isEnergizerTile(tile.x, tile.y)) {
			pacMan.restingTicks = 3;
			score(50);
			int powerSeconds = level().ghostFrightenedSeconds;
			pacMan.powerTicks = clock.sec(powerSeconds);
			if (powerSeconds > 0) {
				log("Pac-Man got power for %d seconds", powerSeconds);
				for (Ghost ghost : ghosts) {
					if (!ghost.dead && !ghost.locked) {
						ghost.frightened = true;
					}
				}
				forceGhostsTurningBack();
				ui.stopSound(Sound.SIREN);
				ui.loopSound(Sound.PACMAN_POWER);
			}
			ghostBounty = 200;
		} else {
			pacMan.restingTicks = 1;
			score(10);
		}
		// bonus score reached?
		int eaten = TOTAL_FOOD_COUNT - world.foodRemaining;
		if (bonusAvailableTimer == 0 && (eaten == 70 || eaten == 170)) {
			bonusAvailableTimer = clock.sec(9 + new Random().nextFloat());
		}
	}

	private void updateGhostDotCounters() {
		if (globalDotCounterEnabled) {
			if (ghosts[CLYDE].locked && globalDotCounter == 32) {
				globalDotCounterEnabled = false;
				globalDotCounter = 0;
				log("Global dot counter disabled, Clyde in house when counter reached 32");
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
		if (pacMan.powerTicks > 0) {
			pacMan.powerTicks--;
			if (pacMan.powerTicks == 0) {
				for (Ghost ghost : ghosts) {
					ghost.frightened = false;
				}
				ui.stopSound(Sound.PACMAN_POWER);
				ui.loopSound(Sound.SIREN);
			}
		}
	}

	private void killGhost(Ghost ghost) {
		ghostsKilledInLevel++;
		ghost.dead = true;
		ghost.frightened = false;
		ghost.targetTile = HOUSE_ENTRY;
		ghost.bounty = ghostBounty;
		log("Ghost %s killed at tile %s, Pac-Man wins %d points", ghost.name, ghost.tile(), ghost.bounty);
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
			if (ghost != ghosts[BLINKY]) {
				if (globalDotCounterEnabled && globalDotCounter >= globalDotLimit(ghost)) {
					unlockGhost(ghost, "Global dot counter is %d", globalDotCounter);
				} else if (!globalDotCounterEnabled && ghost.dotCounter >= privateDotLimit(ghost)) {
					unlockGhost(ghost, "%s's dot counter is %d", ghost.name, ghost.dotCounter);
				} else {
					letGhostBounce(ghost);
				}
			}
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

	private void updateGhostTargetTile(Ghost ghost) {
		boolean chasing = huntingPhase % 2 != 0;
		ghost.targetTile = chasing ? currentChasingTarget(ghost) : ghost.scatterTile;
		if (ghost == ghosts[BLINKY] && ghost.elroyMode > 0) {
			ghost.targetTile = pacMan.tile();
		}
	}

	private void unlockGhost(Ghost ghost, String reason, Object... args) {
		ghost.locked = false;
		if (ghost != ghosts[BLINKY]) {
			ghost.leavingHouse = true;
			if (ghost == ghosts[CLYDE] && ghosts[BLINKY].elroyMode < 0) {
				ghosts[BLINKY].elroyMode = (byte) -ghosts[BLINKY].elroyMode;
				log("Blinky Elroy mode %d resumed", ghosts[BLINKY].elroyMode);
			}
		}
		log("Ghost %s unlocked: %s", ghost.name, String.format(reason, args));
	}

	private Optional<Ghost> preferredLockedGhost() {
		for (int ghost : GHOST_UNLOCK_ORDER) {
			if (ghosts[ghost].locked) {
				return Optional.of(ghosts[ghost]);
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
		if (ghost == ghosts[INKY]) {
			return level == 1 ? 30 : 0;
		}
		if (ghost == ghosts[CLYDE]) {
			return level == 1 ? 60 : level == 2 ? 50 : 0;
		}
		return 0;
	}

	private int globalDotLimit(Ghost ghost) {
		if (ghost == ghosts[PINKY]) {
			return 7;
		}
		if (ghost == ghosts[INKY]) {
			return 17;
		}
		return Integer.MAX_VALUE;
	}

	private int starvingTimeLimit() {
		return level < 5 ? clock.sec(4) : clock.sec(3);
	}

	private V2i currentChasingTarget(Ghost ghost) {
		V2i pacManTile = pacMan.tile();
		switch (ghost.name) {
		case "Blinky": {
			return pacManTile;
		}
		case "Pinky": {
			V2i pacManTileAhead4 = pacManTile.sum(pacMan.dir.vec.scaled(4));
			if (pacMan.dir == UP) {
				// simulate overflow bug when Pac-Man is looking UP
				pacManTileAhead4 = pacManTileAhead4.sum(LEFT.vec.scaled(4));
			}
			return pacManTileAhead4;
		}
		case "Inky": {
			V2i pacManTileAhead2 = pacManTile.sum(pacMan.dir.vec.scaled(2));
			if (pacMan.dir == UP) {
				// simulate overflow bug when Pac-Man is looking UP
				pacManTileAhead2 = pacManTileAhead2.sum(LEFT.vec.scaled(2));
			}
			return ghosts[BLINKY].tile().scaled(-1).sum(pacManTileAhead2.scaled(2));
		}
		case "Clyde": {
			return ghosts[CLYDE].tile().distance(pacManTile) < 8 ? ghosts[CLYDE].scatterTile : pacManTile;
		}
		default:
			throw new IllegalArgumentException("Unknown ghost name: " + ghost.name);
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
			ghost.targetTile = ghost == ghosts[BLINKY] ? HOUSE_CENTER : ghost.homeTile;
			return;
		}
		letGhostHeadForTargetTile(ghost);
	}

	private boolean atGhostHouseDoor(Creature guy) {
		return guy.at(HOUSE_ENTRY) && Functions.differsAtMost(guy.offset().x, HTS, 2);
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

	private void setSpeed(Creature guy) {
		if (guy.name.equals("Pac-Man")) {
			guy.speed = level().pacManSpeed;
		} else {
			setGhostSpeed((Ghost) guy);
		}
	}

	private void setGhostSpeed(Ghost ghost) {
		if (ghost.leavingHouse || (ghost.locked && ghost != ghosts[BLINKY])) {
			ghost.speed = 0.75f * level().ghostSpeed;
		} else if (ghost.dead) {
			ghost.speed = 2f * level().ghostSpeed;
		} else if (world.isInsideTunnel(ghost.tile().x, ghost.tile().y)) {
			ghost.speed = level().ghostSpeedTunnel;
		} else if (ghost.frightened) {
			ghost.speed = level().ghostSpeedFrightened;
		} else if (ghost.elroyMode == 1) {
			ghost.speed = level().elroy1Speed;
		} else if (ghost.elroyMode == 2) {
			ghost.speed = level().elroy2Speed;
		} else {
			ghost.speed = level().ghostSpeed;
		}
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
		// use direction to neighbor with minimal distance to target
		double minDist = Double.MAX_VALUE;
		Direction minDistDir = null;
		for (Direction dir : DIRECTION_PRIORITY) {
			if (dir == ghost.dir.opposite()) {
				continue;
			}
			V2i neighbor = tile.sum(dir.vec);
			if (!canAccessTile(ghost, neighbor.x, neighbor.y)) {
				continue;
			}
			if (dir == UP && !ghost.dead && world.isUpwardsBlocked(neighbor.x, neighbor.y)) {
				continue;
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
			if (!ghost.dead && !ghost.enteringHouse && !ghost.leavingHouse) {
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
		setSpeed(guy);
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
		for (int x = 0; x < WORLD_WIDTH_TILES; ++x) {
			for (int y = 0; y < WORLD_HEIGHT_TILES; ++y) {
				if (world.isFoodTile(x, y) && !world.hasEatenFood(x, y) && !world.isEnergizerTile(x, y)) {
					world.eatFood(x, y);
				}
			}
		}
	}

	private void killAllGhosts() {
		ghostBounty = 200;
		for (Ghost ghost : ghosts) {
			if (!ghost.dead) {
				killGhost(ghost);
			}
		}
		enterGhostDyingState();
	}

}
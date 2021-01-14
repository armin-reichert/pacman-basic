package de.amr.games.pacman.core;

import static de.amr.games.pacman.core.GameVariant.CLASSIC;
import static de.amr.games.pacman.core.GameVariant.MS_PACMAN;
import static de.amr.games.pacman.core.PacManGameState.CHANGING_LEVEL;
import static de.amr.games.pacman.core.PacManGameState.GAME_OVER;
import static de.amr.games.pacman.core.PacManGameState.GHOST_DYING;
import static de.amr.games.pacman.core.PacManGameState.HUNTING;
import static de.amr.games.pacman.core.PacManGameState.INTRO;
import static de.amr.games.pacman.core.PacManGameState.PACMAN_DYING;
import static de.amr.games.pacman.core.PacManGameState.READY;
import static de.amr.games.pacman.lib.Direction.DOWN;
import static de.amr.games.pacman.lib.Direction.LEFT;
import static de.amr.games.pacman.lib.Direction.RIGHT;
import static de.amr.games.pacman.lib.Direction.UP;
import static de.amr.games.pacman.lib.Functions.differsAtMost;
import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.world.PacManClassicWorld.BLINKY;
import static de.amr.games.pacman.world.PacManClassicWorld.CLYDE;
import static de.amr.games.pacman.world.PacManClassicWorld.INKY;
import static de.amr.games.pacman.world.PacManClassicWorld.PINKY;
import static de.amr.games.pacman.world.PacManGameWorld.HTS;
import static de.amr.games.pacman.world.PacManGameWorld.offset;
import static de.amr.games.pacman.world.PacManGameWorld.t;
import static de.amr.games.pacman.world.PacManGameWorld.tile;
import static java.lang.Math.abs;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.amr.games.pacman.creatures.Bonus;
import de.amr.games.pacman.creatures.Creature;
import de.amr.games.pacman.creatures.Ghost;
import de.amr.games.pacman.creatures.Ghost.GhostState;
import de.amr.games.pacman.creatures.Pac;
import de.amr.games.pacman.lib.Clock;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Hiscore;
import de.amr.games.pacman.lib.V2f;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.ui.api.PacManGameSound;
import de.amr.games.pacman.ui.api.PacManGameUI;
import de.amr.games.pacman.world.MsPacManWorld;
import de.amr.games.pacman.world.PacManClassicWorld;
import de.amr.games.pacman.world.PacManGameWorld;

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
public class PacManGame extends Thread {

	static final ResourceBundle TEXTS = ResourceBundle.getBundle("localization.translation");

	public final Clock clock;
	public final Random rnd;
	public final Hiscore hiscore;

	public final GameVariant variant;
	public final PacManGameWorld world;
	public final Pac pac;
	public final Ghost[] ghosts;
	public final Bonus bonus;

	public PacManGameUI ui;

	public boolean gamePaused;
	public boolean gameStarted;

	public PacManGameState state, stateBefore;
	public PacManGameLevel level;
	public short levelNumber;
	public byte lives;
	public int score;
	public byte huntingPhase;
	public short ghostBounty;
	public byte ghostsKilledInLevel;
	public byte mazeFlashesRemaining;
	public short globalDotCounter;
	public boolean globalDotCounterEnabled;

	public boolean autopilotEnabled;
	public final Autopilot autopilot;

	public PacManGame(GameVariant variant) {
		super("Pac-Man-" + variant);

		clock = new Clock();
		rnd = new Random();
		hiscore = new Hiscore();
		autopilot = new Autopilot();
		pac = new Pac();
		ghosts = new Ghost[] { new Ghost(0), new Ghost(1), new Ghost(2), new Ghost(3) };
		bonus = new Bonus();

		this.variant = variant;
		world = variant == CLASSIC ? new PacManClassicWorld() : new MsPacManWorld();
		pac.name = world.pacName();
		for (Ghost ghost : ghosts) {
			ghost.name = world.ghostName(ghost.id);
		}
	}

	@Override
	public void run() {
		reset();
		enterIntroState();
		log("Game starts. Entering state '%s' for %s", stateDescription(), ticksDescription(state.duration()));
		while (true) {
			clock.tick(this::step);
		}
	}

	private void step() {
		readInput();
		updateState();
		ui.render();
	}

	public void exit() {
		if (hiscore.changed) {
			hiscore.save(hiscoreFile(variant));
		}
		log("Game exits.");
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
			autopilotEnabled = !autopilotEnabled;
			log("Pac-Man autopilot mode is %s", autopilotEnabled ? "on" : "off");
		}
		if (ui.keyPressed("escape")) {
			ui.stopAllSounds();
			reset();
			enterIntroState();
		}
	}

	private void reset() {
		gameStarted = false;
		score = 0;
		lives = 3;
		setLevel(1);
		hiscore.load(hiscoreFile(variant));
		if (ui != null) {
			ui.clearMessage();
		}
	}

	private void setLevel(int number) {
		levelNumber = (short) number;
		level = world.levelData(levelNumber);
		world.setLevel(levelNumber);
		huntingPhase = 0;
		mazeFlashesRemaining = 0;
		ghostBounty = 200;
		ghostsKilledInLevel = 0;
		bonus.availableTicks = 0;
		bonus.consumedTicks = 0;
		for (Ghost ghost : ghosts) {
			ghost.dotCounter = 0;
			ghost.elroyMode = 0;
		}
	}

	private void resetGuys() {
		pac.placeAt(world.pacHome(), HTS, 0);
		pac.dir = pac.wishDir = world.pacStartDirection();
		pac.visible = true;
		pac.speed = 0;
		pac.targetTile = null; // used in autopilot mode
		pac.couldMove = true;
		pac.forcedOnTrack = true;
		pac.dead = false;
		pac.powerTicksLeft = 0;
		pac.restingTicksLeft = 0;
		pac.starvingTicks = 0;
		pac.collapsingTicksLeft = 0;

		for (Ghost ghost : ghosts) {
			ghost.placeAt(world.ghostHome(ghost.id), HTS, 0);
			ghost.dir = ghost.wishDir = world.ghostStartDirection(ghost.id);
			ghost.visible = true;
			ghost.speed = 0;
			ghost.targetTile = null;
			ghost.couldMove = true;
			ghost.forcedDirection = false;
			ghost.forcedOnTrack = ghost.id == BLINKY;
			ghost.state = GhostState.LOCKED;
			ghost.bounty = 0;
			// these are only reset when entering level:
//		ghost.dotCounter = 0;
//		ghost.elroyMode = 0;
		}

		bonus.visible = false;
		bonus.speed = 0;
		bonus.changedTile = true;
		bonus.couldMove = true;
		bonus.forcedOnTrack = true;
		bonus.availableTicks = 0;
		bonus.consumedTicks = 0;
	}

	// BEGIN STATE-MACHINE

	public String stateDescription() {
		if (state == HUNTING) {
			String phaseName = inChasingPhase() ? "Chasing" : "Scattering";
			int phase = huntingPhase / 2;
			return String.format("%s-%s (%d of 4)", state, phaseName, phase + 1);
		}
		return state.name();
	}

	private String ticksDescription(long ticks) {
		return ticks == Long.MAX_VALUE ? "indefinite time" : ticks + " ticks";
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
	}

	private PacManGameState runIntroState() {
		if (ui.keyPressed("v")) {
			toggleVariant();
			return state;
		}
		if (ui.anyKeyPressed()) {
			return changeState(this::exitIntroState, this::enterReadyState, null);
		}
		return state.tick();
	}

	private void toggleVariant() {
		PacManGame game = new PacManGame(variant == CLASSIC ? MS_PACMAN : CLASSIC);
		ui.setGame(game);
		game.start();
		try {
			join();
		} catch (InterruptedException x) {
			x.printStackTrace();
		}
	}

	private void exitIntroState() {
	}

	// READY

	private void enterReadyState() {
		state = READY;
		if (!gameStarted) {
			state.setDuration(clock.sec(4.5));
			ui.playSound(PacManGameSound.GAME_READY);
			gameStarted = true;
		} else {
			state.setDuration(clock.sec(0.5));
		}
		resetGuys();
		ui.showMessage(TEXTS.getString("READY"), false);
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
		ui.clearMessage();
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

	private static PacManGameSound siren(int huntingPhase) {
		switch (huntingPhase / 2) {
		case 0:
			return PacManGameSound.SIREN_1;
		case 1:
			return PacManGameSound.SIREN_2;
		case 2:
			return PacManGameSound.SIREN_3;
		case 3:
			return PacManGameSound.SIREN_4;
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
		if (ui.keyPressed("n")) {
			return changeState(this::exitHuntingState, this::enterChangingLevelState, null);
		}

		if (state.expired()) {
			startHuntingPhase(++huntingPhase);
			forceHuntingGhostsTurningBack();
		}

		if (world.foodRemaining() == 0) {
			return changeState(this::exitHuntingState, this::enterChangingLevelState, null);
		}

		updatePac();
		for (Ghost ghost : ghosts) {
			updateGhost(ghost);
		}
		updateBonus();

		checkPacFindsFood();
		checkPacFindsBonus();

		Ghost collidingGhost = ghostCollidingWithPac();
		if (collidingGhost != null && collidingGhost.state == GhostState.FRIGHTENED) {
			return changeState(this::exitHuntingState, this::enterGhostDyingState, () -> ghostKilled(collidingGhost));
		}
		if (collidingGhost != null && collidingGhost.state != GhostState.FRIGHTENED) {
			return changeState(this::exitHuntingState, this::enterPacManDyingState, () -> pacKilled(collidingGhost));
		}

		return pac.powerTicksLeft == 0 ? state.tick() : state;
	}

	private void exitHuntingState() {
	}

	// PACMAN_DYING

	private void enterPacManDyingState() {
		state = PACMAN_DYING;
		state.setDuration(clock.sec(6));
		pac.speed = 0;
		for (Ghost ghost : ghosts) {
			ghost.speed = 0;
			ghost.state = GhostState.HUNTING;
		}
		bonus.availableTicks = bonus.consumedTicks = 0;
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
			pac.collapsingTicksLeft = clock.sec(1.5); // TODO correct?
			ui.playSound(PacManGameSound.PACMAN_DEATH);
		}
		if (pac.collapsingTicksLeft > 1) {
			// count down until 1 such that animation stays at last frame until state expires
			pac.collapsingTicksLeft--;
		}
		return state.tick();
	}

	private void exitPacManDyingState() {
		lives -= autopilotEnabled ? 0 : 1;
		pac.collapsingTicksLeft = 0;
		for (Ghost ghost : ghosts) {
			ghost.visible = true;
		}
	}

	// GHOST_DYING

	private void enterGhostDyingState() {
		stateBefore = state;
		state = GHOST_DYING;
		state.setDuration(clock.sec(1)); // TODO correct?
		pac.visible = false;
		ui.playSound(PacManGameSound.GHOST_DEATH);
	}

	private PacManGameState runGhostDyingState() {
		if (state.expired()) {
			return changeState(this::exitGhostDyingState, () -> state = stateBefore, () -> log("Resume state '%s'", state));
		}
		for (Ghost ghost : ghosts) {
			if (ghost.state == GhostState.DEAD && ghost.bounty == 0) {
				updateGhost(ghost);
			}
		}
		return state.tick();
	}

	private void exitGhostDyingState() {
		for (Ghost ghost : ghosts) {
			if (ghost.state == GhostState.DEAD && ghost.bounty > 0) {
				ghost.bounty = 0;
			}
		}
		pac.visible = true;
		ui.loopSound(PacManGameSound.RETREATING);
	}

	// CHANGING_LEVEL

	private void enterChangingLevelState() {
		state = CHANGING_LEVEL;
		state.setDuration(clock.sec(level.numFlashes + 2));
		for (Ghost ghost : ghosts) {
			ghost.state = GhostState.LOCKED;
			ghost.speed = 0;
		}
		pac.speed = 0;
		bonus.availableTicks = bonus.consumedTicks = 0;
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
			mazeFlashesRemaining = level.numFlashes;
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
		pac.speed = 0;
		if (hiscore.changed) {
			hiscore.save(hiscoreFile(variant));
		}
		ui.showMessage(TEXTS.getString("GAME_OVER"), true);
	}

	private PacManGameState runGameOverState() {
		if (state.expired() || ui.anyKeyPressed()) {
			return changeState(this::exitGameOverState, this::enterIntroState, this::reset);
		}
		return state.tick();
	}

	private void exitGameOverState() {
	}

	// END STATE-MACHINE

	private void updatePac() {
		if (pac.restingTicksLeft == 0) {
			updatePacDirection();
			pac.speed = pac.powerTicksLeft == 0 ? level.pacSpeed : level.pacSpeedPowered;
			tryMoving(pac);
		} else {
			pac.restingTicksLeft--;
		}
		if (pac.powerTicksLeft > 0) {
			pac.powerTicksLeft--;
			if (pac.powerTicksLeft == 0) {
				for (Ghost ghost : ghosts) {
					if (ghost.state == GhostState.FRIGHTENED) {
						ghost.state = GhostState.HUNTING;
					}
					;
				}
				ui.stopSound(PacManGameSound.PACMAN_POWER);
			}
		}
	}

	private void updatePacDirection() {
		if (autopilotEnabled) {
			autopilot.controlPac(this);
		} else {
			if (ui.keyPressed("left")) {
				pac.wishDir = LEFT;
			}
			if (ui.keyPressed("right")) {
				pac.wishDir = RIGHT;
			}
			if (ui.keyPressed("up")) {
				pac.wishDir = UP;
			}
			if (ui.keyPressed("down")) {
				pac.wishDir = DOWN;
			}
		}
	}

	private void checkPacFindsBonus() {
		if (bonus.availableTicks > 0 && pac.tile().equals(bonus.tile())) {
			bonus.availableTicks = 0;
			bonus.consumedTicks = clock.sec(2);
			bonus.speed = 0;
			score(level.bonusPoints);
			ui.playSound(PacManGameSound.EAT_BONUS);
			log("Pac-Man found bonus (%d) of value %d", level.bonusSymbol, level.bonusPoints);
		}
	}

	private void checkPacFindsFood() {
		V2i pacLocation = pac.tile();
		if (world.containsFood(pacLocation.x, pacLocation.y)) {
			pacFoundFood(pacLocation);
		} else {
			pacStarved();
		}
	}

	private void checkBonusActivation() {
		int eaten = world.eatenFoodCount();
		if (eaten == 70 || eaten == 170) {
			bonus.visible = true;
			if (variant == CLASSIC) {
				bonus.availableTicks = clock.sec(9 + rnd.nextFloat());
				bonus.placeAt(PacManClassicWorld.BONUS_TILE, HTS, 0);
			} else if (variant == MS_PACMAN) {
				bonus.availableTicks = Long.MAX_VALUE; // TODO is there a timeout?
				int portal = rnd.nextInt(world.numPortals());
				boolean entersMazeFromLeft = rnd.nextBoolean();
				V2i startTile = entersMazeFromLeft ? world.portalLeft(portal) : world.portalRight(portal);
				bonus.placeAt(startTile, 0, 0);
				bonus.targetDirection = entersMazeFromLeft ? RIGHT : LEFT;
				bonus.dir = bonus.wishDir = bonus.targetDirection;
				bonus.couldMove = true;
				bonus.speed = 0.5f;
			}
		}
	}

	private void checkElroyActivation() {
		if (world.foodRemaining() == level.elroy1DotsLeft) {
			ghosts[BLINKY].elroyMode = 1;
			log("Blinky becomes Cruise Elroy 1");
		} else if (world.foodRemaining() == level.elroy2DotsLeft) {
			ghosts[BLINKY].elroyMode = 2;
			log("Blinky becomes Cruise Elroy 2");
		}
	}

	private void pacStarved() {
		pac.starvingTicks++;
		if (pac.starvingTicks >= pacStarvingTimeLimit()) {
			preferredLockedGhost().ifPresent(ghost -> {
				releaseGhost(ghost, "%s has been starving for %d ticks", pac.name, pac.starvingTicks);
				pac.starvingTicks = 0;
			});
		}
	}

	private int pacStarvingTimeLimit() {
		return levelNumber < 5 ? clock.sec(4) : clock.sec(3);
	}

	private void pacFoundFood(V2i foodLocation) {
		if (world.isEnergizerTile(foodLocation.x, foodLocation.y)) {
			score(50);
			pacGetsPower();
			pac.restingTicksLeft = 3;
			ghostBounty = 200;
		} else {
			score(10);
			pac.restingTicksLeft = 1;
		}
		pac.starvingTicks = 0;
		world.removeFood(foodLocation.x, foodLocation.y);
		checkElroyActivation();
		checkBonusActivation();
		updateGhostDotCounters();
		ui.playSound(PacManGameSound.MUNCH);
	}

	private void pacKilled(Ghost killer) {
		pac.dead = true;
		log("%s killed by %s at tile %s", pac.name, killer.name, killer.tile());
		resetAndEnableGlobalDotCounter();
		byte elroyMode = ghosts[BLINKY].elroyMode;
		if (elroyMode > 0) {
			ghosts[BLINKY].elroyMode = (byte) -elroyMode; // negative value means "disabled"
			log("Blinky Elroy mode %d disabled", elroyMode);
		}
	}

	private void pacGetsPower() {
		int seconds = level.ghostFrightenedSeconds;
		pac.powerTicksLeft = clock.sec(seconds);
		if (seconds > 0) {
			log("Pac-Man got power for %d seconds", seconds);
			for (Ghost ghost : ghosts) {
				if (isGhostHunting(ghost)) {
					ghost.state = GhostState.FRIGHTENED;
				}
			}
			forceHuntingGhostsTurningBack();
			ui.loopSound(PacManGameSound.PACMAN_POWER);
		}
	}

	private Ghost ghostCollidingWithPac() {
		return Stream.of(ghosts).filter(ghost -> ghost.state != GhostState.DEAD)
				.filter(ghost -> ghost.tile().equals(pac.tile())).findAny().orElse(null);
	}

	private void updateGhostDotCounters() {
		if (globalDotCounterEnabled) {
			if (ghosts[CLYDE].state == GhostState.LOCKED && globalDotCounter == 32) {
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

	private void updateBonus() {

		if (bonus.availableTicks > 0) {
			--bonus.availableTicks;
			if (bonus.speed > 0) {
				V2i bonusLocation = bonus.tile();
				if (world.isPortal(bonusLocation.x, bonusLocation.y)) {
					bonus.availableTicks = 0;
					bonus.visible = false;
					return;
				}
				if (!bonus.couldMove || world.isIntersection(bonusLocation.x, bonusLocation.y)) {
					List<Direction> dirs = possibleMoveDirections(bonus);
					if (dirs.size() > 1) {
						// give random movement a bias towards the target direction
						dirs.remove(bonus.targetDirection.opposite());
					}
					bonus.wishDir = dirs.get(rnd.nextInt(dirs.size()));
				}
				tryMoving(bonus);
			}
		}

		if (bonus.consumedTicks > 0) {
			--bonus.consumedTicks;
			if (bonus.consumedTicks == 0) {
				bonus.visible = false;
			}
		}
	}

	private void updateGhost(Ghost ghost) {
		switch (ghost.state) {
		case LOCKED:
			if (ghost.id != BLINKY) {
				tryReleasingGhost(ghost);
				letGhostBounce(ghost);
			} else {
				ghost.state = GhostState.HUNTING;
			}
			break;
		case ENTERING_HOUSE:
			letGhostEnterHouse(ghost);
			break;
		case LEAVING_HOUSE:
			letGhostLeaveHouse(ghost);
			break;
		case FRIGHTENED:
		case HUNTING:
			letGhostWanderMaze(ghost);
			break;
		case DEAD:
			letGhostReturnHome(ghost);
			break;
		default:
			throw new IllegalArgumentException("Illegal ghost state: " + ghost.state);
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
		ghost.state = GhostState.LEAVING_HOUSE;
		if (ghost.id == CLYDE && ghosts[BLINKY].elroyMode < 0) {
			ghosts[BLINKY].elroyMode = (byte) -ghosts[BLINKY].elroyMode; // resume Elroy mode
			log("Blinky Elroy mode %d resumed", ghosts[BLINKY].elroyMode);
		}
		log("Ghost %s released: %s", ghost.name, String.format(reason, args));
	}

	private Optional<Ghost> preferredLockedGhost() {
		return Stream.of(ghosts[PINKY], ghosts[INKY], ghosts[CLYDE]).filter(ghost -> ghost.state == GhostState.LOCKED)
				.findFirst();
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

	private V2i ghostChasingTarget(int id) {
		switch (id) {
		case BLINKY: {
			return pac.tile();
		}
		case PINKY: {
			V2i pacAhead4 = pac.tile().sum(pac.dir.vec.scaled(4));
			if (pac.dir == UP) {
				// simulate overflow bug when Pac-Man is looking UP
				pacAhead4 = pacAhead4.sum(LEFT.vec.scaled(4));
			}
			return pacAhead4;
		}
		case INKY: {
			V2i pacAhead2 = pac.tile().sum(pac.dir.vec.scaled(2));
			if (pac.dir == UP) {
				// simulate overflow bug when Pac-Man is looking UP
				pacAhead2 = pacAhead2.sum(LEFT.vec.scaled(2));
			}
			return ghosts[BLINKY].tile().scaled(-1).sum(pacAhead2.scaled(2));
		}
		case CLYDE: {
			return ghosts[CLYDE].tile().euclideanDistance(pac.tile()) < 8 ? world.ghostScatterTile(CLYDE) : pac.tile();
		}
		default:
			throw new IllegalArgumentException("Unknown ghost id: " + id);
		}
	}

	private void ghostKilled(Ghost ghost) {
		ghost.state = GhostState.DEAD;
		ghost.speed = 2 * level.ghostSpeed; // TODO correct?
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

	private void letGhostBounce(Ghost ghost) {
		int ceiling = t(world.houseCenter().y - 1) + 3, ground = t(world.houseCenter().y) + 4;
		if (ghost.position.y <= ceiling || ghost.position.y >= ground) {
			ghost.wishDir = ghost.dir.opposite();
		}
		ghost.speed = level.ghostSpeedTunnel; // TODO correct?
		tryMoving(ghost);
	}

	private void letGhostWanderMaze(Ghost ghost) {
		V2i ghostLocation = ghost.tile();
		if (world.isTunnel(ghostLocation.x, ghostLocation.y)) {
			ghost.speed = level.ghostSpeedTunnel;
		} else if (ghost.state == GhostState.FRIGHTENED) {
			ghost.speed = level.ghostSpeedFrightened;
		} else if (ghost.id == BLINKY && ghost.elroyMode == 1) {
			ghost.speed = level.elroy1Speed;
		} else if (ghost.id == BLINKY && ghost.elroyMode == 2) {
			ghost.speed = level.elroy2Speed;
		} else {
			ghost.speed = level.ghostSpeed;
		}
		if (inChasingPhase() || ghost.id == BLINKY && ghost.elroyMode > 0) {
			ghost.targetTile = ghostChasingTarget(ghost.id);
		} else {
			ghost.targetTile = world.ghostScatterTile(ghost.id);
		}
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
			ghost.state = GhostState.ENTERING_HOUSE;
			ghost.targetTile = ghost.id == BLINKY ? world.houseCenter() : world.ghostHome(ghost.id);
			return;
		}
		letGhostHeadForTargetTile(ghost);
	}

	private void letGhostEnterHouse(Ghost ghost) {
		V2i ghostLocation = ghost.tile();
		V2f offset = ghost.offset();
		// Target reached? Revive and start leaving house.
		if (ghostLocation.equals(ghost.targetTile) && offset.y >= 0) {
			ghost.state = GhostState.LEAVING_HOUSE;
			ghost.speed = level.ghostSpeed; // TODO correct?
			ghost.wishDir = ghost.dir.opposite();
			if (Stream.of(ghosts).noneMatch(g -> g.state == GhostState.DEAD)) {
				ui.stopSound(PacManGameSound.RETREATING);
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
			ghost.state = GhostState.HUNTING;
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
		if (ghostLocation.x == world.ghostHome(ghost.id).x && differsAtMost(offset.y, 0, 1)) {
			ghost.wishDir = world.ghostHome(ghost.id).x < world.houseCenter().x ? RIGHT : LEFT;
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
		if (ghost.state == GhostState.FRIGHTENED && world.isIntersection(ghostLocation.x, ghostLocation.y)) {
			return Optional.of(randomPossibleMoveDir(ghost));
		}
		return ghostTargetDirection(ghost);
	}

	private static final Direction[] DIRECTION_PRIORITY = { UP, LEFT, DOWN, RIGHT };

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
			if (dir == UP && ghost.state == GhostState.HUNTING && world.isUpwardsBlocked(neighbor.x, neighbor.y)) {
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
		return ghost.state == GhostState.HUNTING;
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
		// teleport?
		for (int i = 0; i < world.numPortals(); ++i) {
			if (guyLocation.equals(world.portalRight(i)) && guy.dir == RIGHT) {
				guy.placeAt(world.portalLeft(i), 0, 0);
				return;
			}
			if (guyLocation.equals(world.portalLeft(i)) && guy.dir == LEFT) {
				guy.placeAt(world.portalRight(i), 0, 0);
				return;
			}
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

		guy.placeAt(newTile, newOffset.x, newOffset.y);
		guy.changedTile = !guy.tile().equals(guyLocationBeforeMove);
		return true;
	}

	boolean canAccessTile(Creature guy, int x, int y) {
		if (world.isPortal(x, y)) {
			return true;
		}
		if (world.isGhostHouseDoor(x, y)) {
			if (guy instanceof Ghost) {
				Ghost ghost = (Ghost) guy;
				return ghost.state == GhostState.ENTERING_HOUSE || ghost.state == GhostState.LEAVING_HOUSE;
			} else {
				return false;
			}
		}
		return world.inMapRange(x, y) && !world.isWall(x, y);
	}

	private Direction randomPossibleMoveDir(Creature guy) {
		List<Direction> dirs = possibleMoveDirections(guy);
		return dirs.get(rnd.nextInt(dirs.size()));
	}

	private List<Direction> possibleMoveDirections(Creature guy) {
		//@formatter:off
		return Stream.of(Direction.values())
			.filter(dir -> dir != guy.dir.opposite())
			.filter(dir -> {
				V2i neighbor = guy.tile().sum(dir.vec);
				return world.isAccessible(neighbor.x, neighbor.y);
			})
			.collect(Collectors.toList());
		//@formatter:on
	}

	// Score

	private void score(int points) {
		int oldscore = score;
		score += points;
		if (oldscore < 10000 && score >= 10000) {
			lives++;
			ui.playSound(PacManGameSound.EXTRA_LIFE);
		}
		hiscore.update(score, levelNumber);
	}

	private File hiscoreFile(GameVariant variant) {
		File folder = new File(System.getProperty("user.home"));
		return new File(folder, "pacman-hiscore-" + variant + ".xml");
	}

	// Cheats

	private void eatAllNormalPellets() {
		for (int x = 0; x < world.sizeInTiles().x; ++x) {
			for (int y = 0; y < world.sizeInTiles().y; ++y) {
				if (world.containsFood(x, y) && !world.isEnergizerTile(x, y)) {
					world.removeFood(x, y);
				}
			}
		}
	}

	private void killAllGhosts() {
		ghostBounty = 200;
		for (Ghost ghost : ghosts) {
			if (isGhostHunting(ghost) || ghost.state == GhostState.FRIGHTENED) {
				ghostKilled(ghost);
			}
		}
	}
}
package de.amr.games.pacman.game.core;

import static de.amr.games.pacman.game.core.PacManGameState.CHANGING_LEVEL;
import static de.amr.games.pacman.game.core.PacManGameState.GAME_OVER;
import static de.amr.games.pacman.game.core.PacManGameState.GHOST_DYING;
import static de.amr.games.pacman.game.core.PacManGameState.HUNTING;
import static de.amr.games.pacman.game.core.PacManGameState.INTRO;
import static de.amr.games.pacman.game.core.PacManGameState.PACMAN_DYING;
import static de.amr.games.pacman.game.core.PacManGameState.READY;
import static de.amr.games.pacman.game.worlds.PacManClassicWorld.BLINKY;
import static de.amr.games.pacman.game.worlds.PacManClassicWorld.CLYDE;
import static de.amr.games.pacman.game.worlds.PacManClassicWorld.INKY;
import static de.amr.games.pacman.game.worlds.PacManClassicWorld.PINKY;
import static de.amr.games.pacman.game.worlds.PacManGameWorld.HTS;
import static de.amr.games.pacman.lib.Direction.DOWN;
import static de.amr.games.pacman.lib.Direction.LEFT;
import static de.amr.games.pacman.lib.Direction.RIGHT;
import static de.amr.games.pacman.lib.Direction.UP;
import static de.amr.games.pacman.lib.Logging.log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.stream.Stream;

import de.amr.games.pacman.game.creatures.Bonus;
import de.amr.games.pacman.game.creatures.Ghost;
import de.amr.games.pacman.game.creatures.GhostState;
import de.amr.games.pacman.game.creatures.Pac;
import de.amr.games.pacman.game.worlds.MsPacManWorld;
import de.amr.games.pacman.game.worlds.PacManClassicWorld;
import de.amr.games.pacman.game.worlds.PacManGameWorld;
import de.amr.games.pacman.lib.Clock;
import de.amr.games.pacman.lib.Hiscore;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.ui.api.PacManGameSound;
import de.amr.games.pacman.ui.api.PacManGameUI;

/**
 * Pac-Man and Ms. Pac-Man game with original "AI", levels, timers.
 * <p>
 * Missing:
 * <ul>
 * <li>Pac-Man "cornering"</li>
 * <li>Intermission scenes</li>
 * <li>Multiple players</li>
 * </ul>
 * 
 * @author Armin Reichert
 * 
 * @see <a href="https://pacman.holenet.info">Jamey Pittman: The Pac-Man Dossier</a>
 * @see <a href="https://gameinternals.com/understanding-pac-man-ghost-behavior">Chad Birch:
 *      Understanding ghost behavior</a>
 * @see <a href="http://superpacman.com/mspacman/">Ms. Pac-Man</a>
 */
public class PacManGame implements Runnable {

	public static final byte CLASSIC = 0;
	public static final byte MS_PACMAN = 1;

	static final ResourceBundle TEXTS = ResourceBundle.getBundle("localization.translation");

	public final Clock clock = new Clock();
	public final Random rnd = new Random();

	public byte variant;
	public PacManGameWorld world;
	public Pac pac;
	public Ghost[] ghosts;
	public Bonus bonus;
	public Hiscore hiscore;

	public PacManGameUI ui;

	public boolean paused;
	public boolean started;

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
	public List<Byte> levelSymbols;

	private boolean autopilotEnabled = false;
	private final Autopilot autopilot = new Autopilot();

	private boolean pacImmune = false;

	public PacManGame(byte variant) {
		setGameVariant(variant);
		enterIntroState();
		log("State is '%s' for %s", stateDescription(), ticksDescription(state.duration));
	}

	private void setGameVariant(byte variant) {
		this.variant = variant;
		hiscore = new Hiscore(new File(System.getProperty("user.home"), "pacman-hiscore-" + variant + ".xml"));
		world = (variant == CLASSIC) ? new PacManClassicWorld() : new MsPacManWorld();
		pac = new Pac(world.pacName());
		ghosts = new Ghost[4];
		for (int ghostID = 0; ghostID < 4; ++ghostID) {
			ghosts[ghostID] = new Ghost(ghostID, world.ghostName(ghostID));
		}
		bonus = new Bonus();
		reset();
		log("Game variant is %s", variant == CLASSIC ? "Pac-Man" : "Ms. Pac-Man");
	}

	private void reset() {
		started = false;
		score = 0;
		lives = 3;
		levelNumber = 0;
		levelSymbols = new ArrayList<>();
		nextLevel();
		hiscore.load();
		if (ui != null) {
			ui.clearMessage();
		}
	}

	@Override
	public void run() {
		while (true) {
			clock.tick(this::step);
		}
	}

	private void step() {
		if (!paused) {
			readInput();
			updateState();
		}
		ui.render();
	}

	public void exit() {
		if (hiscore.changed) {
			hiscore.save();
			log("Hiscore saved to " + hiscore.file);
		}
		log("Game exits.");
	}

	private void readInput() {
		if (ui.keyPressed("a")) {
			autopilotEnabled = !autopilotEnabled;
			log("Pac-Man autopilot mode is %s", autopilotEnabled ? "on" : "off");
		}
		if (ui.keyPressed("i")) {
			pacImmune = !pacImmune;
			log("%s is %s", pac.name, pacImmune ? "immune against ghosts" : "vulnerable by ghosts");
		}
		if (ui.keyPressed("escape")) {
			reset();
			ui.stopAllSounds();
			enterIntroState();
		}
	}

	private void nextLevel() {
		levelNumber++;
		level = world.createLevel(levelNumber);
		levelSymbols.add(level.bonusSymbol);
		huntingPhase = 0;
		mazeFlashesRemaining = 0;
		ghostBounty = 200;
		ghostsKilledInLevel = 0;
		bonus.edibleTicksLeft = 0;
		bonus.eatenTicksLeft = 0;
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
		bonus.edibleTicksLeft = 0;
		bonus.eatenTicksLeft = 0;
	}

	// BEGIN STATE-MACHINE

	public String stateDescription() {
		if (state == HUNTING) {
			String phaseName = inChaseMode() ? "Chasing" : "Scattering";
			int phase = huntingPhase / 2;
			return String.format("%s-%s (%d of 4)", state, phaseName, phase + 1);
		}
		return state.name();
	}

	private String ticksDescription(long ticks) {
		return ticks == Long.MAX_VALUE ? "indefinite time" : ticks + " ticks";
	}

	private PacManGameState changeState(Runnable onExit, Runnable onEntry) {
		return changeState(onExit, onEntry, null);
	}

	private PacManGameState changeState(Runnable onExit, Runnable onEntry, Runnable action) {
		log("Exit state '%s'", stateDescription());
		onExit.run();
		if (action != null) {
			action.run();
		}
		onEntry.run();
		log("Enter state '%s' for %s", stateDescription(), ticksDescription(state.duration));
		return state;
	}

	private void updateState() {
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
			setGameVariant((variant == CLASSIC) ? MS_PACMAN : CLASSIC);
			ui.onGameVariantChanged(this);
			state.resetTimer();
			return state;
		}
		if (ui.keyPressed("space")) {
			return changeState(this::exitIntroState, this::enterReadyState);
		}
		return state.run();
	}

	private void exitIntroState() {
	}

	// READY

	private void enterReadyState() {
		state = READY;
		state.setDuration(clock.sec(started ? 3 : 6));
		resetGuys();
		ui.stopAllSounds();
		for (Ghost ghost : ghosts) {
			ghost.visible = false;
		}
	}

	private PacManGameState runReadyState() {
		if (state.hasExpired()) {
			started = true;
			return changeState(this::exitReadyState, this::enterHuntingState);
		}
		if (state.running == clock.sec(0.5)) {
			ui.showMessage(TEXTS.getString("READY"), false);
			for (Ghost ghost : ghosts) {
				ghost.visible = true;
			}
		}
		if (!started && state.running == clock.sec(1)) {
			ui.playSound(PacManGameSound.GAME_READY);
		}
		if (state.running > clock.sec(1)) {
			for (Ghost ghost : ghosts) {
				if (ghost.id != BLINKY) {
					ghost.bounce(world, level.ghostSpeed / 2); // TODO correct speed?
				}
			}
		}
		return state.run();
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

	private boolean inScatterMode() {
		return huntingPhase % 2 == 0;
	}

	private boolean inChaseMode() {
		return huntingPhase % 2 != 0;
	}

	private static PacManGameSound siren(int huntingPhase) {
		switch (huntingPhase / 2) {
		case 0:
			return PacManGameSound.GHOST_SIREN_1;
		case 1:
			return PacManGameSound.GHOST_SIREN_2;
		case 2:
			return PacManGameSound.GHOST_SIREN_3;
		case 3:
			return PacManGameSound.GHOST_SIREN_4;
		default:
			throw new IllegalArgumentException("Illegal hunting phase: " + huntingPhase);
		}
	}

	private void startHuntingPhase(int phase) {
		huntingPhase = (byte) phase;
		state.setDuration(huntingPhaseDuration(huntingPhase));
		if (inScatterMode()) {
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

		if (state.hasExpired()) {
			startHuntingPhase(++huntingPhase);
			for (Ghost ghost : ghosts) {
				if (ghost.state == GhostState.HUNTING) {
					ghost.forceTurningBack();
				}
			}
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
		checkPacFindsEdibleBonus();

		Optional<Ghost> collidingGhost = ghostCollidingWithPac();
		if (collidingGhost.isPresent() && collidingGhost.get().state == GhostState.FRIGHTENED) {
			return changeState(this::exitHuntingState, this::enterGhostDyingState, () -> ghostKilled(collidingGhost.get()));
		}
		if (collidingGhost.isPresent() && collidingGhost.get().state == GhostState.HUNTING && !pacImmune) {
			return changeState(this::exitHuntingState, this::enterPacManDyingState, () -> pacKilled(collidingGhost.get()));
		}

		// hunting state timer is stopped as long as Pac has power
		return pac.powerTicksLeft == 0 ? state.run() : state;
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
		bonus.edibleTicksLeft = bonus.eatenTicksLeft = 0;
		ui.stopAllSounds();
	}

	private PacManGameState runPacManDyingState() {
		if (state.hasExpired()) {
			lives -= 1;
			if (lives > 0) {
				return changeState(this::exitPacManDyingState, this::enterReadyState);
			} else {
				return changeState(this::exitPacManDyingState, this::enterGameOverState);
			}
		}
		if (state.running == clock.sec(1.5)) {
			for (Ghost ghost : ghosts) {
				ghost.visible = false;
			}
		}
		if (state.running == clock.sec(2.5)) {
			pac.collapsingTicksLeft = clock.sec(1.5);
			ui.playSound(PacManGameSound.PACMAN_DEATH);
		}
		if (pac.collapsingTicksLeft > 1) {
			// count down until 1 such that animation stays at last frame until state expires
			pac.collapsingTicksLeft--;
		}
		return state.run();
	}

	private void exitPacManDyingState() {
		pac.collapsingTicksLeft = 0;
		for (Ghost ghost : ghosts) {
			ghost.visible = true;
		}
	}

	// GHOST_DYING

	private void enterGhostDyingState() {
		stateBefore = state;
		state = GHOST_DYING;
		state.setDuration(clock.sec(1));
		pac.visible = false;
		ui.playSound(PacManGameSound.GHOST_EATEN);
	}

	private PacManGameState runGhostDyingState() {
		if (state.hasExpired()) {
			return changeState(this::exitGhostDyingState, () -> state = stateBefore, () -> log("Resume state '%s'", state));
		}
		steerPac();
		for (Ghost ghost : ghosts) {
			if (ghost.state == GhostState.DEAD && ghost.bounty == 0) {
				updateGhost(ghost);
			}
		}
		return state.run();
	}

	private void exitGhostDyingState() {
		for (Ghost ghost : ghosts) {
			if (ghost.state == GhostState.DEAD && ghost.bounty > 0) {
				ghost.bounty = 0;
			}
		}
		pac.visible = true;
		ui.loopSound(PacManGameSound.GHOST_EYES);
	}

	// CHANGING_LEVEL

	private void enterChangingLevelState() {
		state = CHANGING_LEVEL;
		state.setDuration(clock.sec(level.numFlashes + 3));
		bonus.edibleTicksLeft = bonus.eatenTicksLeft = 0;
		pac.speed = 0;
		for (Ghost ghost : ghosts) {
			ghost.speed = 0;
		}
		ui.stopAllSounds();
	}

	private PacManGameState runChangingLevelState() {
		if (state.hasExpired()) {
			return changeState(this::exitChangingLevelState, this::enterReadyState, this::nextLevel);
		}
		if (state.running == clock.sec(2)) {
			for (Ghost ghost : ghosts) {
				ghost.visible = false;
			}
		}
		if (state.running == clock.sec(3)) {
			mazeFlashesRemaining = level.numFlashes;
		}
		return state.run();
	}

	private void exitChangingLevelState() {
		log("Level %d complete, entering level %d", levelNumber, levelNumber + 1);
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
			hiscore.save();
			log("Hiscore saved to " + hiscore.file);
		}
		ui.showMessage(TEXTS.getString("GAME_OVER"), true);
	}

	private PacManGameState runGameOverState() {
		if (state.hasExpired() || ui.keyPressed("space")) {
			return changeState(this::exitGameOverState, this::enterIntroState, this::reset);
		}
		return state.run();
	}

	private void exitGameOverState() {
	}

	// END STATE-MACHINE

	// Pac

	private void updatePac() {
		if (pac.restingTicksLeft > 0) {
			pac.restingTicksLeft--;
		} else {
			pac.speed = pac.powerTicksLeft == 0 ? level.pacSpeed : level.pacSpeedPowered;
			steerPac();
			pac.tryMoving(world);
		}
		if (pac.powerTicksLeft > 0) {
			pac.powerTicksLeft--;
			if (pac.powerTicksLeft == 0) {
				for (Ghost ghost : ghosts) {
					if (ghost.state == GhostState.FRIGHTENED) {
						ghost.state = GhostState.HUNTING;
					}
				}
				ui.stopSound(PacManGameSound.PACMAN_POWER);
			}
		}
	}

	private void steerPac() {
		if (autopilotEnabled) {
			autopilot.steerPac(this);
		} else if (ui.keyPressed("left")) {
			pac.wishDir = LEFT;
		} else if (ui.keyPressed("right")) {
			pac.wishDir = RIGHT;
		} else if (ui.keyPressed("up")) {
			pac.wishDir = UP;
		} else if (ui.keyPressed("down")) {
			pac.wishDir = DOWN;
		}
	}

	private void checkPacFindsEdibleBonus() {
		if (bonus.edibleTicksLeft > 0 && pac.tile().equals(bonus.tile())) {
			bonus.edibleTicksLeft = 0;
			bonus.eatenTicksLeft = clock.sec(2);
			bonus.speed = 0;
			score(bonus.points);
			ui.playSound(PacManGameSound.PACMAN_EAT_BONUS);
			log("Pac-Man found bonus (%d) of value %d", bonus.symbol, bonus.points);
		}
	}

	private void checkPacFindsFood() {
		V2i pacLocation = pac.tile();
		if (world.containsFood(pacLocation)) {
			if (world.isEnergizerTile(pacLocation)) {
				score(50);
				pacGetsPower();
				pac.restingTicksLeft = 3;
				ghostBounty = 200;
			} else {
				score(10);
				pac.restingTicksLeft = 1;
			}
			pac.starvingTicks = 0;
			world.removeFood(pacLocation);
			checkBlinkyBecomesElroy();
			checkBonusActivation();
			updateGhostDotCounters();
			ui.playSound(PacManGameSound.PACMAN_MUNCH);
		} else {
			pac.starvingTicks++;
			if (pac.starvingTicks >= pacStarvingTimeLimit()) {
				preferredLockedGhost().ifPresent(ghost -> {
					releaseGhost(ghost, "%s has been starving for %d ticks", pac.name, pac.starvingTicks);
					pac.starvingTicks = 0;
				});
			}
		}
	}

	private void pacGetsPower() {
		int seconds = level.ghostFrightenedSeconds;
		pac.powerTicksLeft = clock.sec(seconds);
		if (seconds > 0) {
			log("Pac-Man got power for %d seconds", seconds);
			for (Ghost ghost : ghosts) {
				if (ghost.state == GhostState.HUNTING) {
					ghost.state = GhostState.FRIGHTENED;
					ghost.wishDir = ghost.dir.opposite();
					ghost.forcedDirection = true;
				}
			}
			ui.loopSound(PacManGameSound.PACMAN_POWER);
		}
	}

	private void pacKilled(Ghost killer) {
		log("%s killed by %s at tile %s", pac.name, killer.name, killer.tile());
		pac.dead = true;
		byte elroyMode = ghosts[BLINKY].elroyMode;
		if (elroyMode > 0) {
			ghosts[BLINKY].elroyMode = (byte) -elroyMode; // negative value means "disabled"
			log("Blinky Elroy mode %d disabled", elroyMode);
		}
		globalDotCounter = 0;
		globalDotCounterEnabled = true;
		log("Global dot counter reset and enabled");
	}

	private int pacStarvingTimeLimit() {
		return levelNumber < 5 ? clock.sec(4) : clock.sec(3);
	}

	// Bonus

	private void checkBonusActivation() {
		int eaten = world.eatenFoodCount();
		if (eaten == 70 || eaten == 170) {
			bonus.visible = true;
			bonus.symbol = level.bonusSymbol;
			if (variant == CLASSIC) {
				bonus.points = PacManClassicWorld.BONUS_POINTS[bonus.symbol];
				bonus.edibleTicksLeft = clock.sec(9 + rnd.nextFloat());
				bonus.placeAt(PacManClassicWorld.BONUS_TILE, HTS, 0);
			} else if (variant == MS_PACMAN) {
				bonus.points = MsPacManWorld.BONUS_POINTS[bonus.symbol];
				bonus.edibleTicksLeft = Long.MAX_VALUE; // TODO is there a timeout?
				boolean entersMazeFromLeft = rnd.nextBoolean();
				int portal = rnd.nextInt(world.numPortals());
				V2i startTile = entersMazeFromLeft ? world.portalLeft(portal) : world.portalRight(portal);
				bonus.placeAt(startTile, 0, 0);
				bonus.wanderingDirection = entersMazeFromLeft ? RIGHT : LEFT;
				bonus.dir = bonus.wishDir = bonus.wanderingDirection;
				bonus.couldMove = true;
				bonus.speed = 0.25f; // TODO what is the correct bonus speed?
			}
		}
	}

	private void updateBonus() {
		// edible bonus active?
		boolean expired = false;
		if (bonus.edibleTicksLeft > 0) {
			if (variant == MS_PACMAN) {
				bonus.wander(world);
				V2i bonusLocation = bonus.tile();
				if (world.isPortal(bonusLocation)) {
					expired = true; // TODO should bonus also expire on timeout?
				}
			} else {
				bonus.edibleTicksLeft--;
				if (bonus.edibleTicksLeft == 0) {
					expired = true;
				}
			}
		}
		if (expired) {
			bonus.edibleTicksLeft = 0;
			bonus.visible = false;
		}

		// eaten bonus active?
		expired = false;
		if (bonus.eatenTicksLeft > 0) {
			bonus.eatenTicksLeft--;
			if (bonus.eatenTicksLeft == 0) {
				expired = true;
			}
		}
		if (expired) {
			bonus.eatenTicksLeft = 0;
			bonus.visible = false;
		}
	}

	// Ghosts

	private void updateGhost(Ghost ghost) {
		switch (ghost.state) {
		case LOCKED:
			if (ghost.id != BLINKY) {
				tryReleasingGhost(ghost);
				ghost.bounce(world, level.ghostSpeed / 2); // TODO speed correct?
			} else {
				ghost.state = GhostState.HUNTING;
			}
			break;
		case ENTERING_HOUSE:
			ghost.enterHouse(world);
			// TODO move outside
			if (Stream.of(ghosts).noneMatch(g -> g.state == GhostState.DEAD)) {
				ui.stopSound(PacManGameSound.GHOST_EYES);
			}
			break;
		case LEAVING_HOUSE:
			ghost.leaveHouse(world);
			break;
		case FRIGHTENED:
		case HUNTING:
			V2i ghostLocation = ghost.tile();
			if (world.isTunnel(ghostLocation)) {
				ghost.speed = level.ghostSpeedTunnel;
			} else if (ghost.state == GhostState.FRIGHTENED) {
				ghost.speed = level.ghostSpeedFrightened;
			} else if (ghost.elroyMode == 1) {
				ghost.speed = level.elroy1Speed;
			} else if (ghost.elroyMode == 2) {
				ghost.speed = level.elroy2Speed;
			} else {
				ghost.speed = level.ghostSpeed;
			}
			letGhostHunt(ghost);
			break;
		case DEAD:
			ghost.returnHome(world);
			break;
		default:
			throw new IllegalArgumentException("Illegal ghost state: " + ghost.state);
		}
	}

	private Optional<Ghost> ghostCollidingWithPac() {
		return Stream.of(ghosts).filter(ghost -> ghost.tile().equals(pac.tile())).findAny();
	}

	private void checkBlinkyBecomesElroy() {
		if (world.foodRemaining() == level.elroy1DotsLeft) {
			ghosts[BLINKY].elroyMode = 1;
			log("Blinky becomes Cruise Elroy 1");
		} else if (world.foodRemaining() == level.elroy2DotsLeft) {
			ghosts[BLINKY].elroyMode = 2;
			log("Blinky becomes Cruise Elroy 2");
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

	private V2i ghostChasingTarget(int ghostID) {
		switch (ghostID) {
		case 0: { // BLINKY
			return pac.tile();
		}
		case 1: { // PINKY
			V2i pacAhead4 = pac.tile().sum(pac.dir.vec.scaled(4));
			if (pac.dir == UP) { // simulate overflow bug when Pac-Man is looking UP
				pacAhead4 = pacAhead4.sum(LEFT.vec.scaled(4));
			}
			return pacAhead4;
		}
		case 2: { // INKY
			V2i pacAhead2 = pac.tile().sum(pac.dir.vec.scaled(2));
			if (pac.dir == UP) { // simulate overflow bug when Pac-Man is looking UP
				pacAhead2 = pacAhead2.sum(LEFT.vec.scaled(2));
			}
			return ghosts[BLINKY].tile().scaled(-1).sum(pacAhead2.scaled(2));
		}
		case 3: { // CLYDE, SUE
			return ghosts[3].tile().euclideanDistance(pac.tile()) < 8 ? world.ghostScatterTile(3) : pac.tile();
		}
		default:
			throw new IllegalArgumentException("Unknown ghost id: " + ghostID);
		}
	}

	private void letGhostHunt(Ghost ghost) {
		byte mode = 1; // 0 = scatter, 1 == chase, 2 == random walk

		if (inScatterMode() && ghost.elroyMode == 0) {
			mode = 0;
		}
		// In Ms. Pac-Man, Blinky and Pinky move randomly during *first* scatter phase
		if (variant == MS_PACMAN && (ghost.id == BLINKY || ghost.id == PINKY) && huntingPhase == 0) {
			mode = 2;
		}
		if (mode == 0) {
			ghost.targetTile = world.ghostScatterTile(ghost.id);
			ghost.headForTargetTile(world);
		} else if (mode == 1) {
			ghost.targetTile = ghostChasingTarget(ghost.id);
			ghost.headForTargetTile(world);
		} else if (mode == 2) {
			ghost.wanderRandomly(world);
		}
	}

	// Ghost house

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
		return Stream.of(PINKY, INKY, CLYDE).map(id -> ghosts[id]).filter(ghost -> ghost.state == GhostState.LOCKED)
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

	private void updateGhostDotCounters() {
		if (globalDotCounterEnabled) {
			if (ghosts[CLYDE].state == GhostState.LOCKED && globalDotCounter == 32) {
				globalDotCounterEnabled = false;
				globalDotCounter = 0;
				log("Global dot counter disabled and reset, Clyde was in house when counter reached 32");
			} else {
				++globalDotCounter;
			}
		} else {
			preferredLockedGhost().ifPresent(ghost -> ++ghost.dotCounter);
		}
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
			if (ghost.state == GhostState.HUNTING || ghost.state == GhostState.FRIGHTENED) {
				ghostKilled(ghost);
			}
		}
	}

	// Misc

	public static boolean differsAtMost(float value, float target, float tolerance) {
		return Math.abs(value - target) <= tolerance;
	}

}
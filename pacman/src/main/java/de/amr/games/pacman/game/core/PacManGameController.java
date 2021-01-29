package de.amr.games.pacman.game.core;

import static de.amr.games.pacman.game.core.PacManGameModel.MS_PACMAN;
import static de.amr.games.pacman.game.core.PacManGameState.CHANGING_LEVEL;
import static de.amr.games.pacman.game.core.PacManGameState.GAME_OVER;
import static de.amr.games.pacman.game.core.PacManGameState.GHOST_DYING;
import static de.amr.games.pacman.game.core.PacManGameState.INTRO;
import static de.amr.games.pacman.game.core.PacManGameState.PACMAN_DYING;
import static de.amr.games.pacman.game.core.PacManGameState.READY;
import static de.amr.games.pacman.game.core.PacManGameWorld.HTS;
import static de.amr.games.pacman.game.creatures.GhostState.DEAD;
import static de.amr.games.pacman.game.creatures.GhostState.ENTERING_HOUSE;
import static de.amr.games.pacman.game.creatures.GhostState.FRIGHTENED;
import static de.amr.games.pacman.game.creatures.GhostState.HUNTING;
import static de.amr.games.pacman.game.creatures.GhostState.LEAVING_HOUSE;
import static de.amr.games.pacman.game.creatures.GhostState.LOCKED;
import static de.amr.games.pacman.game.heaven.God.clock;
import static de.amr.games.pacman.game.heaven.God.random;
import static de.amr.games.pacman.game.worlds.PacManClassicWorld.BLINKY;
import static de.amr.games.pacman.game.worlds.PacManClassicWorld.CLYDE;
import static de.amr.games.pacman.game.worlds.PacManClassicWorld.INKY;
import static de.amr.games.pacman.game.worlds.PacManClassicWorld.PINKY;
import static de.amr.games.pacman.lib.Direction.DOWN;
import static de.amr.games.pacman.lib.Direction.LEFT;
import static de.amr.games.pacman.lib.Direction.RIGHT;
import static de.amr.games.pacman.lib.Direction.UP;
import static de.amr.games.pacman.lib.Logging.log;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.amr.games.pacman.game.creatures.Ghost;
import de.amr.games.pacman.lib.Hiscore;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.ui.api.PacManGameAnimations;
import de.amr.games.pacman.ui.api.PacManGameSound;
import de.amr.games.pacman.ui.api.PacManGameSoundManager;
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
public class PacManGameController {

	private PacManGameUI ui;
	private PacManGameModel game;

	private Autopilot autopilot;
	private boolean autopilotEnabled;

	private boolean gamePaused;
	private boolean gameStarted;

	private PacManGameState suspendedState;

	public PacManGameController() {
		autopilot = new Autopilot(() -> game);
	}

	public void initGame(int variant) {
		if (variant == PacManGameModel.CLASSIC) {
			game = PacManGameModel.newPacManClassicGame();
		} else if (variant == PacManGameModel.MS_PACMAN) {
			game = PacManGameModel.newMsPacManGame();
		} else {
			log("Illegal game variant value %d, preparing Ms. Pac-Man game", variant);
			game = PacManGameModel.newMsPacManGame();
		}
		reset();
	}

	public void setUI(PacManGameUI ui) {
		this.ui = ui;
	}

	public boolean isGamePaused() {
		return gamePaused;
	}

	public void pauseGame(boolean paused) {
		this.gamePaused = paused;
	}

	public Optional<PacManGameModel> game() {
		return Optional.ofNullable(game);
	}

	private void reset() {
		gameStarted = false;
		game.state = suspendedState = null;
		if (ui != null) {
			ui.sounds().ifPresent(PacManGameSoundManager::stopAllSounds);
			ui.clearMessages();
		}
		enterIntroState();
		log("Game variant is %s", game.variant == PacManGameModel.CLASSIC ? "Pac-Man" : "Ms. Pac-Man");
		log("State is '%s' for %s", game.stateDescription(), ticksDescription(game.state.durationTicks()));
	}

	private void toggleGameVariant() {
		if (game.variant == PacManGameModel.CLASSIC) {
			initGame(PacManGameModel.MS_PACMAN);
		} else {
			initGame(PacManGameModel.CLASSIC);
		}
		ui.sounds().ifPresent(PacManGameSoundManager::stopAllSounds);
		ui.updateGame(game);
	}

	private void toggleAutopilot() {
		autopilotEnabled = !autopilotEnabled;
		ui.showFlashMessage("Autopilot " + (autopilotEnabled ? "on" : "off"));
		log("Pac-Man autopilot mode is " + (autopilotEnabled ? "on" : "off"));
	}

	private void togglePacImmunity() {
		game.pac.immune = !game.pac.immune;
		ui.showFlashMessage(game.pac.name + " is " + (game.pac.immune ? "immune" : "vulnerable"));
		log("%s is %s", game.pac.name, game.pac.immune ? "immune against ghosts" : "vulnerable by ghosts");
	}

	public void step() {
		if (!gamePaused) {
			readInput();
			updateState();
		}
		ui.redraw();
	}

	private void readInput() {
		if (ui.keyPressed("a")) {
			toggleAutopilot();
		}
		if (ui.keyPressed("i")) {
			togglePacImmunity();
		}
		if (ui.keyPressed("escape")) {
			game.reset();
			reset();
		}
	}

	private void makeGuysReady() {
		game.pac.placeAt(game.world.pacHome(), HTS, 0);
		game.pac.dir = game.pac.wishDir = game.world.pacStartDirection();
		game.pac.visible = true;
		game.pac.speed = 0;
		game.pac.targetTile = null; // used in autopilot mode
		game.pac.couldMove = true;
		game.pac.forcedOnTrack = true;
		game.pac.dead = false;
		game.pac.powerTicksLeft = 0;
		game.pac.restingTicksLeft = 0;
		game.pac.starvingTicks = 0;

		for (Ghost ghost : game.ghosts) {
			ghost.placeAt(game.world.ghostHome(ghost.id), HTS, 0);
			ghost.dir = ghost.wishDir = game.world.ghostStartDirection(ghost.id);
			ghost.visible = true;
			ghost.speed = 0;
			ghost.targetTile = null;
			ghost.couldMove = true;
			ghost.forcedDirection = ghost.id == BLINKY;
			ghost.forcedOnTrack = ghost.id == BLINKY;
			ghost.state = LOCKED;
			ghost.bounty = 0;
			// these are only reset when entering level:
//		ghost.dotCounter = 0;
//		ghost.elroyMode = 0;
		}

		game.bonus.visible = false;
		game.bonus.speed = 0;
		game.bonus.changedTile = true;
		game.bonus.couldMove = true;
		game.bonus.forcedOnTrack = true;
		game.bonus.edibleTicksLeft = 0;
		game.bonus.eatenTicksLeft = 0;
	}

	// BEGIN STATE-MACHINE

	private PacManGameState changeState(Runnable onExit, Runnable onEntry) {
		log("Exit state '%s'", game.stateDescription());
		onExit.run();
		onEntry.run();
		log("Entered state '%s' for %s", game.stateDescription(), ticksDescription(game.state.durationTicks()));
		return game.state;
	}

	private void updateState() {
		switch (game.state) {
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
			throw new IllegalStateException("Illegal state: " + game.state);
		}
		ui.updateScene();
	}

	private String ticksDescription(long ticks) {
		return ticks == Long.MAX_VALUE ? "indefinite time" : ticks + " ticks";
	}

	// INTRO

	private void enterIntroState() {
		game.state = INTRO;
		game.state.setDuration(Long.MAX_VALUE);
	}

	private PacManGameState runIntroState() {
		if (ui.keyPressed("space")) {
			return changeState(this::exitIntroState, this::enterReadyState);
		}
		if (ui.keyPressed("v")) {
			toggleGameVariant();
		}
		return game.state.run();
	}

	private void exitIntroState() {
	}

	// READY

	private void enterReadyState() {
		game.state = READY;
		game.state.setDuration(clock.sec(gameStarted ? 2.5 : 6));
		makeGuysReady();
		for (Ghost ghost : game.ghosts) {
			ghost.visible = false;
		}
		ui.sounds().ifPresent(PacManGameSoundManager::stopAllSounds);
	}

	private PacManGameState runReadyState() {
		if (game.state.hasExpired()) {
			return changeState(this::exitReadyState, this::enterHuntingState);
		}
		if (game.state.ticksRun() == clock.sec(1)) {
			for (Ghost ghost : game.ghosts) {
				ghost.visible = true;
				ui.animations().ifPresent(animations -> animations.stopGhostWalking(ghost));
			}
		}
		if (game.state.ticksRun() == clock.sec(2)) {
			ui.showMessage(ui.translation("READY"), false);
			for (Ghost ghost : game.ghosts) {
				ghost.visible = true;
				ui.animations().ifPresent(animations -> animations.startGhostWalking(ghost));
			}
			if (!gameStarted) {
				ui.sounds().ifPresent(sm -> sm.playSound(PacManGameSound.GAME_READY));
			}
		}
		return game.state.run();
	}

	private void exitReadyState() {
		gameStarted = true;
		ui.clearMessages();
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
		int row = game.levelNumber == 1 ? 0 : game.levelNumber <= 4 ? 1 : 2;
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
		game.huntingPhase = (byte) phase;
		game.state.setDuration(huntingPhaseDuration(game.huntingPhase));
		if (game.inScatteringPhase()) {
			ui.showFlashMessage("Scattering");
			// TODO not sure about sirens
			if (game.huntingPhase >= 2) {
				ui.sounds().ifPresent(sm -> sm.stopSound(siren(game.huntingPhase - 2)));
			}
			ui.sounds().ifPresent(sm -> sm.loopSound(siren(game.huntingPhase)));
		} else {
			ui.showFlashMessage("Chasing");
		}
		log("Hunting phase %d started, state is now %s", phase, game.stateDescription());
	}

	private void enterHuntingState() {
		game.state = PacManGameState.HUNTING;
		startHuntingPhase(0);
	}

	private PacManGameState runHuntingState() {

		// Cheats
		if (ui.keyPressed("e")) {
			game.removeAllNormalPellets();
		}
		if (ui.keyPressed("x")) {
			killAllGhosts();
			return changeState(this::exitHuntingState, this::enterGhostDyingState);
		}
		if (ui.keyPressed("l")) {
			if (game.lives < Byte.MAX_VALUE) {
				game.lives++;
			}
		}
		if (ui.keyPressed("n")) {
			return changeState(this::exitHuntingState, this::enterChangingLevelState);
		}

		// Level completed?
		if (game.level.foodRemaining == 0) {
			return changeState(this::exitHuntingState, this::enterChangingLevelState);
		}

		// Can Pac kill ghost(s)?
		List<Ghost> prey = Stream.of(game.ghosts).filter(ghost -> ghost.is(FRIGHTENED) && ghost.meets(game.pac))
				.collect(Collectors.toList());
		if (!prey.isEmpty()) {
			prey.forEach(this::killGhost);
			return changeState(this::exitHuntingState, this::enterGhostDyingState);
		}

		// Pac killed by ghost?
		Optional<Ghost> killer = Stream.of(game.ghosts).filter(ghost -> ghost.is(HUNTING) && ghost.meets(game.pac))
				.findAny();
		if (!game.pac.immune && killer.isPresent()) {
			onPacKilled(killer.get());
			return changeState(this::exitHuntingState, this::enterPacManDyingState);
		}

		// Hunting phase complete?
		if (game.state.hasExpired()) {
			startHuntingPhase(++game.huntingPhase);
			for (Ghost ghost : game.ghosts) {
				if (ghost.state == HUNTING) {
					ghost.forceTurningBack();
				}
			}
		}

		// Can Pac move?
		steerPac();
		if (game.pac.restingTicksLeft > 0) {
			game.pac.restingTicksLeft--;
		} else {
			game.pac.speed = game.pac.powerTicksLeft == 0 ? game.level.pacSpeed : game.level.pacSpeedPowered;
			game.pac.tryMoving();
		}

		// Does Pac find food?
		V2i pacLocation = game.pac.tile();
		if (game.level.containsFood(pacLocation)) {
			onPacFoundFood(pacLocation);
		} else {
			game.pac.starvingTicks++;
		}

		// Is Pac losing power?
		if (game.pac.powerTicksLeft > 0) {
			game.pac.powerTicksLeft--;
			if (game.pac.powerTicksLeft == 0) {
				for (Ghost ghost : game.ghosts) {
					if (ghost.is(FRIGHTENED)) {
						ghost.state = HUNTING;
					}
				}
			}
		}

		tryReleasingGhosts();

		for (Ghost ghost : game.ghosts) {
			if (ghost.is(HUNTING)) {
				setGhostHuntingTarget(ghost);
			}
			ghost.update(game.level);
		}

		game.bonus.update();
		if (game.bonus.edibleTicksLeft > 0 && game.pac.meets(game.bonus)) {
			log("Pac-Man found bonus (%d) of value %d", game.bonus.symbol, game.bonus.points);
			game.bonus.eatAndDisplayValue(clock.sec(2));
			score(game.bonus.points);
			ui.sounds().ifPresent(sm -> sm.playSound(PacManGameSound.PACMAN_EAT_BONUS));
		}

		if (Stream.of(game.ghosts).noneMatch(ghost -> ghost.is(DEAD))) {
			ui.sounds().ifPresent(sm -> sm.stopSound(PacManGameSound.GHOST_EYES));
		}
		if (Stream.of(game.ghosts).noneMatch(ghost -> ghost.is(FRIGHTENED))) {
			ui.sounds().ifPresent(sm -> sm.stopSound(PacManGameSound.PACMAN_POWER));
		}

		// hunting state timer is suspended if Pac has power
		if (game.pac.powerTicksLeft == 0) {
			game.state.run();
		}

		return game.state;
	}

	private void exitHuntingState() {
	}

	// PACMAN_DYING

	private void enterPacManDyingState() {
		game.state = PACMAN_DYING;
		game.state.setDuration(clock.sec(6));
		game.pac.speed = 0;
		for (Ghost ghost : game.ghosts) {
			ghost.state = HUNTING; // TODO just want ghost to be rendered colorful
		}
		game.bonus.edibleTicksLeft = game.bonus.eatenTicksLeft = 0;
//		ui.animations().ifPresent(animations -> animations.pacManCollapsing.reset());
		ui.sounds().ifPresent(PacManGameSoundManager::stopAllSounds);
	}

	private PacManGameState runPacManDyingState() {
		if (game.state.hasExpired()) {
			game.lives -= 1;
			if (game.lives > 0) {
				return changeState(this::exitPacManDyingState, this::enterReadyState);
			} else {
				return changeState(this::exitPacManDyingState, this::enterGameOverState);
			}
		}
		if (game.state.ticksRun() == clock.sec(1.5)) {
			for (Ghost ghost : game.ghosts) {
				ghost.visible = false;
			}
		}
		if (game.state.ticksRun() == clock.sec(2.5)) {
			ui.animations().ifPresent(animations -> animations.startPacManCollapsing());
			ui.sounds().ifPresent(sm -> sm.playSound(PacManGameSound.PACMAN_DEATH));
		}
		return game.state.run();
	}

	private void exitPacManDyingState() {
		for (Ghost ghost : game.ghosts) {
			ghost.visible = true;
		}
	}

	// GHOST_DYING

	private void enterGhostDyingState() {
		suspendedState = game.state;
		game.state = GHOST_DYING;
		game.state.setDuration(clock.sec(1));
		game.pac.visible = false;
		ui.sounds().ifPresent(sm -> sm.playSound(PacManGameSound.GHOST_EATEN));
	}

	private PacManGameState runGhostDyingState() {
		if (game.state.hasExpired()) {
			log("Resume state '%s'", suspendedState);
			return changeState(this::exitGhostDyingState, () -> game.state = suspendedState);
		}
		steerPac();
		for (Ghost ghost : game.ghosts) {
			if (ghost.is(DEAD) && ghost.bounty == 0 || ghost.is(ENTERING_HOUSE)) {
				ghost.update(game.level);
			}
		}
		return game.state.run();
	}

	private void exitGhostDyingState() {
		for (Ghost ghost : game.ghosts) {
			if (ghost.is(DEAD) && ghost.bounty > 0) {
				ghost.bounty = 0;
			}
		}
		game.pac.visible = true;
		ui.sounds().ifPresent(sm -> sm.loopSound(PacManGameSound.GHOST_EYES));
	}

	// CHANGING_LEVEL

	private void enterChangingLevelState() {
		game.state = CHANGING_LEVEL;
		game.state.setDuration(clock.sec(game.level.numFlashes + 3));
		game.bonus.edibleTicksLeft = game.bonus.eatenTicksLeft = 0;
		game.pac.speed = 0;
		ui.sounds().ifPresent(PacManGameSoundManager::stopAllSounds);
	}

	private PacManGameState runChangingLevelState() {
		if (game.state.hasExpired()) {
			return changeState(this::exitChangingLevelState, this::enterReadyState);
		}
		if (game.state.ticksRun() == clock.sec(2)) {
			for (Ghost ghost : game.ghosts) {
				ghost.visible = false;
			}
		}
		if (game.state.ticksRun() == clock.sec(3)) {
			ui.animations().ifPresent(animations -> animations.startMazeFlashing(game.level.numFlashes));
		}
		return game.state.run();
	}

	private void exitChangingLevelState() {
		log("Level %d complete, entering level %d", game.levelNumber, game.levelNumber + 1);
		ui.animations().ifPresent(PacManGameAnimations::endMazeFlashing);
		game.initLevel(game.levelNumber + 1);
		game.levelSymbols.add(game.level.bonusSymbol);
	}

	// GAME_OVER

	private void enterGameOverState() {
		game.state = GAME_OVER;
		game.state.setDuration(clock.sec(30));
		for (Ghost ghost : game.ghosts) {
			ghost.speed = 0;
		}
		game.pac.speed = 0;
		saveHighscore();
		ui.showMessage(ui.translation("GAME_OVER"), true);
	}

	private PacManGameState runGameOverState() {
		if (game.state.hasExpired() || ui.keyPressed("space")) {
			return changeState(this::exitGameOverState, this::enterIntroState);
		}
		return game.state.run();
	}

	private void exitGameOverState() {
		game.reset();
		reset();
	}

	// END STATE-MACHINE

	public void saveHighscore() {
		Hiscore hiscore = game.loadHighScore();
		if (game.highscorePoints > hiscore.points) {
			hiscore.points = game.highscorePoints;
			hiscore.level = game.highscoreLevel;
			hiscore.save();
			log("New hiscore saved. %d points in level %d", hiscore.points, hiscore.level);
		}
	}

	private void score(int points) {
		int oldscore = game.score;
		game.score += points;
		if (oldscore < 10000 && game.score >= 10000) {
			game.lives++;
			ui.sounds().ifPresent(sm -> sm.playSound(PacManGameSound.EXTRA_LIFE));
			log("Extra life! Now we have %d lives", game.lives);
		}
		if (game.score > game.highscorePoints) {
			game.highscorePoints = game.score;
			game.highscoreLevel = game.levelNumber;
		}
	}

	private void steerPac() {
		if (autopilotEnabled) {
			autopilot.run();
		} else {
			if (ui.keyPressed("left")) {
				game.pac.wishDir = LEFT;
			} else if (ui.keyPressed("right")) {
				game.pac.wishDir = RIGHT;
			} else if (ui.keyPressed("up")) {
				game.pac.wishDir = UP;
			} else if (ui.keyPressed("down")) {
				game.pac.wishDir = DOWN;
			}
		}
	}

	private void onPacFoundFood(V2i pacLocation) {
		game.level.removeFood(pacLocation);
		game.pac.starvingTicks = 0;
		if (game.world.isEnergizerTile(pacLocation)) {
			game.pac.restingTicksLeft = 3;
			game.ghostBounty = 200;
			letPacFrightenGhosts(game.level.ghostFrightenedSeconds);
			score(50);
		} else {
			game.pac.restingTicksLeft = 1;
			score(10);
		}

		// Bonus gets edible?
		if (game.level.eatenFoodCount() == 70 || game.level.eatenFoodCount() == 170) {
			long ticks = game.variant == PacManGameModel.CLASSIC ? clock.sec(9 + random.nextFloat()) : Long.MAX_VALUE;
			game.bonus.activate(game.level.bonusSymbol, ticks);
		}

		// Blinky becomes Elroy?
		if (game.level.foodRemaining == game.level.elroy1DotsLeft) {
			game.ghosts[BLINKY].elroy = 1;
			log("Blinky becomes Cruise Elroy 1");
		} else if (game.level.foodRemaining == game.level.elroy2DotsLeft) {
			game.ghosts[BLINKY].elroy = 2;
			log("Blinky becomes Cruise Elroy 2");
		}
		updateGhostDotCounters();
		ui.sounds().ifPresent(sm -> sm.playSound(PacManGameSound.PACMAN_MUNCH));
	}

	private void letPacFrightenGhosts(int seconds) {
		game.pac.powerTicksLeft = clock.sec(seconds);
		if (seconds > 0) {
			log("Pac-Man got power for %d seconds", seconds);
			for (Ghost ghost : game.ghosts) {
				if (ghost.is(HUNTING)) {
					ghost.state = FRIGHTENED;
					ghost.wishDir = ghost.dir.opposite();
					ghost.forcedDirection = true;
				}
			}
			ui.sounds().ifPresent(sm -> sm.loopSound(PacManGameSound.PACMAN_POWER));
		}
	}

	private void onPacKilled(Ghost killer) {
		log("%s killed by %s at tile %s", game.pac.name, killer.name, killer.tile());
		game.pac.dead = true;
		byte elroyMode = game.ghosts[BLINKY].elroy;
		if (elroyMode > 0) {
			game.ghosts[BLINKY].elroy = (byte) -elroyMode; // negative value means "disabled"
			log("Blinky Elroy mode %d disabled", elroyMode);
		}
		game.globalDotCounter = 0;
		game.globalDotCounterEnabled = true;
		log("Global dot counter reset and enabled");
	}

	private int pacStarvingTimeLimit() {
		return game.levelNumber < 5 ? clock.sec(4) : clock.sec(3);
	}

	// Ghosts

	private void killGhost(Ghost ghost) {
		ghost.state = DEAD;
		ghost.targetTile = game.world.houseEntry();
		ghost.bounty = game.ghostBounty;
		score(ghost.bounty);
		if (++game.level.numGhostsKilled == 16) {
			score(12000);
		}
		game.ghostBounty *= 2;
		log("Ghost %s killed at tile %s, Pac-Man wins %d points", ghost.name, ghost.tile(), ghost.bounty);
	}

	private void killAllGhosts() {
		game.ghostBounty = 200;
		for (Ghost ghost : game.ghosts) {
			if (ghost.is(HUNTING) || ghost.is(FRIGHTENED)) {
				killGhost(ghost);
			}
		}
	}

	private void setGhostHuntingTarget(Ghost ghost) {
		if (game.variant == MS_PACMAN && game.huntingPhase == 0 && (ghost.id == BLINKY || ghost.id == PINKY)) {
			// In Ms. Pac-Man, Blinky and Pinky move randomly during *first* scatter phase
			ghost.targetTile = null;
		} else if (game.inScatteringPhase() && ghost.elroy == 0) {
			ghost.targetTile = game.world.ghostScatterTile(ghost.id);
		} else {
			ghost.targetTile = ghostHuntingTarget(ghost.id);
		}
	}

	private V2i ghostHuntingTarget(int ghostID) {
		switch (ghostID) {
		case 0:
			return game.pac.tile();
		case 1: {
			V2i fourAheadPac = game.pac.tile().sum(game.pac.dir.vec.scaled(4));
			if (game.pac.dir == UP) { // simulate overflow bug
				fourAheadPac = fourAheadPac.sum(LEFT.vec.scaled(4));
			}
			return fourAheadPac;
		}
		case 2: {
			V2i twoAheadPac = game.pac.tile().sum(game.pac.dir.vec.scaled(2));
			if (game.pac.dir == UP) { // simulate overflow bug
				twoAheadPac = twoAheadPac.sum(LEFT.vec.scaled(2));
			}
			return game.ghosts[0].tile().scaled(-1).sum(twoAheadPac.scaled(2));
		}
		case 3:
			return game.ghosts[3].tile().euclideanDistance(game.pac.tile()) < 8 ? game.world.ghostScatterTile(3)
					: game.pac.tile();
		default:
			throw new IllegalArgumentException("Unknown ghost id: " + ghostID);
		}
	}

	// Ghost house

	private void tryReleasingGhosts() {
		if (game.ghosts[BLINKY].is(LOCKED)) {
			game.ghosts[BLINKY].state = HUNTING;
		}
		preferredLockedGhostInHouse().ifPresent(ghost -> {
			if (game.globalDotCounterEnabled && game.globalDotCounter >= ghostGlobalDotLimit(ghost)) {
				releaseGhost(ghost, "Global dot counter (%d) reached limit (%d)", game.globalDotCounter,
						ghostGlobalDotLimit(ghost));
			} else if (!game.globalDotCounterEnabled && ghost.dotCounter >= ghostPrivateDotLimit(ghost)) {
				releaseGhost(ghost, "%s's dot counter (%d) reached limit (%d)", ghost.name, ghost.dotCounter,
						ghostPrivateDotLimit(ghost));
			} else if (game.pac.starvingTicks >= pacStarvingTimeLimit()) {
				releaseGhost(ghost, "%s has been starving for %d ticks", game.pac.name, game.pac.starvingTicks);
				game.pac.starvingTicks = 0;
			}
		});
	}

	private void releaseGhost(Ghost ghost, String reason, Object... args) {
		ghost.state = LEAVING_HOUSE;
		if (ghost.id == CLYDE && game.ghosts[BLINKY].elroy < 0) {
			game.ghosts[BLINKY].elroy = (byte) -game.ghosts[BLINKY].elroy; // resume Elroy mode
			log("Blinky Elroy mode %d resumed", game.ghosts[BLINKY].elroy);
		}
		log("Ghost %s released: %s", ghost.name, String.format(reason, args));
	}

	private Optional<Ghost> preferredLockedGhostInHouse() {
		return Stream.of(PINKY, INKY, CLYDE).map(id -> game.ghosts[id]).filter(ghost -> ghost.is(LOCKED)).findFirst();
	}

	private int ghostPrivateDotLimit(Ghost ghost) {
		if (ghost.id == INKY) {
			return game.levelNumber == 1 ? 30 : 0;
		}
		if (ghost.id == CLYDE) {
			return game.levelNumber == 1 ? 60 : game.levelNumber == 2 ? 50 : 0;
		}
		return 0;
	}

	private int ghostGlobalDotLimit(Ghost ghost) {
		return ghost.id == PINKY ? 7 : ghost.id == INKY ? 17 : Integer.MAX_VALUE;
	}

	private void updateGhostDotCounters() {
		if (game.globalDotCounterEnabled) {
			if (game.ghosts[CLYDE].is(LOCKED) && game.globalDotCounter == 32) {
				game.globalDotCounterEnabled = false;
				game.globalDotCounter = 0;
				log("Global dot counter disabled and reset, Clyde was in house when counter reached 32");
			} else {
				++game.globalDotCounter;
			}
		} else {
			preferredLockedGhostInHouse().ifPresent(ghost -> ++ghost.dotCounter);
		}
	}
}
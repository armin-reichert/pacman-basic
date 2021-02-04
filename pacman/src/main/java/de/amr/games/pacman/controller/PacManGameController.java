package de.amr.games.pacman.controller;

import static de.amr.games.pacman.controller.PacManGameState.CHANGING_LEVEL;
import static de.amr.games.pacman.controller.PacManGameState.GAME_OVER;
import static de.amr.games.pacman.controller.PacManGameState.GHOST_DYING;
import static de.amr.games.pacman.controller.PacManGameState.HUNTING;
import static de.amr.games.pacman.controller.PacManGameState.INTRO;
import static de.amr.games.pacman.controller.PacManGameState.PACMAN_DYING;
import static de.amr.games.pacman.controller.PacManGameState.READY;
import static de.amr.games.pacman.heaven.God.clock;
import static de.amr.games.pacman.lib.Direction.DOWN;
import static de.amr.games.pacman.lib.Direction.LEFT;
import static de.amr.games.pacman.lib.Direction.RIGHT;
import static de.amr.games.pacman.lib.Direction.UP;
import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.model.PacManGame.BLINKY;
import static de.amr.games.pacman.model.PacManGame.CLYDE;
import static de.amr.games.pacman.model.PacManGame.INKY;
import static de.amr.games.pacman.model.PacManGame.PINKY;
import static de.amr.games.pacman.model.creatures.GhostState.DEAD;
import static de.amr.games.pacman.model.creatures.GhostState.ENTERING_HOUSE;
import static de.amr.games.pacman.model.creatures.GhostState.FRIGHTENED;
import static de.amr.games.pacman.model.creatures.GhostState.HUNTING_PAC;
import static de.amr.games.pacman.model.creatures.GhostState.LEAVING_HOUSE;
import static de.amr.games.pacman.model.creatures.GhostState.LOCKED;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.amr.games.pacman.lib.Animation;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.MsPacManGame;
import de.amr.games.pacman.model.PacManClassicGame;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.model.creatures.Ghost;
import de.amr.games.pacman.ui.api.PacManGameUI;
import de.amr.games.pacman.ui.sound.PacManGameSound;
import de.amr.games.pacman.ui.sound.SoundManager;
import de.amr.games.pacman.ui.swing.classic.PacManClassicRendering;

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
	private PacManGame game;

	private Autopilot autopilot;
	private boolean autopilotOn;

	private PacManGameState previousState;

	public PacManGameController(boolean classic) {
		ui = PacManGameUI.NO_UI;
		if (classic) {
			playPacManClassic();
		} else {
			playMsPacMan();
		}
	}

	public void playPacManClassic() {
		log("New Pac-Man game");
		game = new PacManClassicGame();
		reset(false);
		ui.setGame(game);
	}

	public void playMsPacMan() {
		log("New Ms. Pac-Man game");
		game = new MsPacManGame();
		reset(false);
		ui.setGame(game);
	}

	private boolean playingMsPacMan() {
		return game instanceof MsPacManGame;
	}

	private void reset(boolean resetGame) {
		if (resetGame) {
			game.reset();
		}
		autopilotOn = false;
		autopilot = null;
		previousState = null;
		ui.animations().ifPresent(animations -> animations.resetAllAnimations(game));
		sounds().ifPresent(SoundManager::stopAllSounds);
		ui.clearMessages();
		changeState(INTRO, () -> {
		}, this::enterIntroState);
	}

	public void gameLoop() {
		while (true)
			clock.tick(this::step);
	}

	public void step() {
		readInput();
		updateState();
		ui.redraw();
	}

	public void setUI(PacManGameUI ui) {
		this.ui = ui;
		ui.setCloseHandler(() -> {
			game.saveHighscore();
			log("Pac-Man game UI closed");
		});
	}

	public void showUI() {
		ui.show();
	}

	public PacManGame game() {
		return game;
	}

	private Optional<SoundManager> sounds() {
		return game.attractMode ? Optional.empty() : ui.sounds();
	}

	private void toggleGameVariant() {
		if (playingMsPacMan()) {
			playPacManClassic();
		} else {
			playMsPacMan();
		}
	}

	private void toggleAutopilot() {
		autopilotOn = !autopilotOn;
		String msg = "Autopilot " + (autopilotOn ? "on" : "off");
		ui.showFlashMessage(msg);
		log(msg);
	}

	private void togglePacImmunity() {
		game.pac.immune = !game.pac.immune;
		String msg = game.pac.name + " is " + (game.pac.immune ? "immune" : "vulnerable");
		ui.showFlashMessage(msg);
		log(msg);
	}

	private void readInput() {
		if (ui.keyPressed("escape")) {
			reset(true);
		}
	}

	private void getReadyToRumble() {
		game.resetGuys();
		ui.animations().ifPresent(animations -> {
			animations.resetAllAnimations(game);
		});
	}

	private void letGhostsFidget(boolean on) {
		ui.animations().ifPresent(animations -> animations.letGhostsFidget(game.ghosts(), on));
	}

	// BEGIN STATE-MACHINE

	// INTRO

	private void enterIntroState() {
		game.state.setDuration(Long.MAX_VALUE);
		game.attractMode = false;
		autopilotOn = false;
		ui.clearMessages();
		ui.animations().ifPresent(animations -> {
			animations.letPacMunch();
		});
	}

	private PacManGameState runIntroState() {
		if (ui.keyPressed("space")) {
			return changeState(READY, this::exitIntroState, this::enterReadyState);
		}
		if (playingMsPacMan() && game.state.ticksRun() == clock.sec(20)) {
			game.attractMode = true;
			return changeState(READY, this::exitIntroState, this::enterReadyState);
		}
		if (ui.keyPressed("v")) {
			sounds().ifPresent(SoundManager::stopAllSounds);
			toggleGameVariant();
		}
		return game.state.run();
	}

	private void exitIntroState() {
	}

	// READY

	private void enterReadyState() {
		boolean playReadyMusic = !game.started && sounds().isPresent();
		game.state.setDuration(clock.sec(playReadyMusic ? 4.5 : 2));
		getReadyToRumble();
		sounds().ifPresent(sm -> {
			sm.stopAllSounds();
			if (playReadyMusic) {
				sm.playSound(PacManGameSound.GAME_READY);
			}
		});
		if (game.attractMode) {
			ui.showMessage(ui.translation("GAME_OVER"), true);
		} else {
			ui.showMessage(ui.translation("READY"), false);
		}
	}

	private PacManGameState runReadyState() {
		if (game.state.hasExpired()) {
			return changeState(PacManGameState.HUNTING, this::exitReadyState, this::enterHuntingState);
		}
		if (game.state.ticksLeft(clock.sec(1))) {
			for (Ghost ghost : game.ghosts) {
				ghost.visible = true;
			}
			game.pac.visible = true;
		}
		if (game.state.ticksLeft(clock.sec(0.5))) {
			letGhostsFidget(true);
		}
		return game.state.run();
	}

	private void exitReadyState() {
		if (game.attractMode) {
			autopilotOn = true;
		} else {
			game.started = true;
			ui.clearMessages();
		}
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
		int row = game.currentLevelNumber == 1 ? 0 : game.currentLevelNumber <= 4 ? 1 : 2;
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
			sounds().ifPresent(sm -> {
				// TODO not sure about sirens
				if (game.huntingPhase >= 2) {
					sm.stopSound(siren(game.huntingPhase - 2));
				}
				sm.loopSound(siren(game.huntingPhase));
			});
		} else {
			ui.showFlashMessage("Chasing");
		}
		log("Hunting phase %d started, state is now %s", phase, game.stateDescription());
	}

	private void enterHuntingState() {
		startHuntingPhase(0);
		ui.animations().ifPresent(animations -> {
			animations.energizerBlinking().restart();
			animations.letPacMunch();
		});
	}

	private void exitHuntingState() {
		ui.animations().ifPresent(animations -> {
			animations.energizerBlinking().reset();
		});
	}

	private PacManGameState runHuntingState() {
		// Level completed?
		if (game.level.foodRemaining == 0) {
			return changeState(CHANGING_LEVEL, this::exitHuntingState, this::enterChangingLevelState);
		}

		// Pac kills ghost(s)?
		List<Ghost> prey = Stream.of(game.ghosts).filter(ghost -> ghost.is(FRIGHTENED) && ghost.meets(game.pac))
				.collect(Collectors.toList());
		if (!prey.isEmpty()) {
			prey.forEach(this::killGhost);
			return changeState(GHOST_DYING, this::exitHuntingState, this::enterGhostDyingState);
		}

		// Pac killed by ghost?
		Optional<Ghost> killer = game.ghosts().filter(ghost -> ghost.is(HUNTING_PAC) && ghost.meets(game.pac)).findAny();
		if (!game.pac.immune && killer.isPresent()) {
			onPacKilled(killer.get());
			return changeState(PACMAN_DYING, this::exitHuntingState, this::enterPacManDyingState);
		}

		// Hunting phase complete?
		if (game.state.hasExpired()) {
			game.ghosts().filter(ghost -> ghost.is(HUNTING_PAC)).forEach(Ghost::forceTurningBack);
			startHuntingPhase(++game.huntingPhase);
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
		if (game.level.containsFood(game.pac.tile())) {
			onPacFoundFood(game.pac.tile());
		} else {
			game.pac.starvingTicks++;
		}

		// Pac losing power?
		if (game.pac.powerTicksLeft > 0) {
			game.pac.powerTicksLeft--;
			if (game.pac.powerTicksLeft == 0) {
				log("%s lost power", game.pac.name);
				game.ghosts().filter(ghost -> ghost.is(FRIGHTENED)).forEach(ghost -> {
					ghost.state = HUNTING_PAC;
				});
			}
			ui.animations().ifPresent(animations -> {
				Animation<?> flashing = animations.ghostFlashing();
				if (game.level.numFlashes > 0 && game.pac.powerTicksLeft == game.level.numFlashes * flashing.duration()) {
					flashing.restart();
					log("Ghost flashing started (%d flashes, %d ticks each), Pac power left: %d ticks", game.level.numFlashes,
							flashing.duration(), game.pac.powerTicksLeft);
				} else if (game.pac.powerTicksLeft == 0) {
					flashing.reset();
					log("Ghost flashing stopped");
				} else {
					flashing.advance();
				}
			});
		}

		tryReleasingGhosts();
		game.ghosts().filter(ghost -> ghost.is(HUNTING_PAC)).forEach(this::setGhostHuntingTarget);
		game.ghosts().forEach(ghost -> ghost.update(game.level));

		game.bonus.update();
		if (game.bonus.edibleTicksLeft > 0 && game.pac.meets(game.bonus)) {
			log("Pac-Man found bonus (%s) of value %d", game.bonusNames[game.bonus.symbol], game.bonus.points);
			game.bonus.eatAndDisplayValue(clock.sec(2));
			score(game.bonus.points);
			sounds().ifPresent(sm -> sm.playSound(PacManGameSound.PACMAN_EAT_BONUS));
		}

		sounds().ifPresent(sm -> {
			if (game.ghosts().noneMatch(ghost -> ghost.is(DEAD))) {
				sm.stopSound(PacManGameSound.GHOST_EYES);
			}
			if (game.pac.powerTicksLeft == 0) {
				sm.stopSound(PacManGameSound.PACMAN_POWER);
			}
		});

		// hunting state timer is suspended if Pac has power
		if (game.pac.powerTicksLeft == 0) {
			game.state.run();
		}

		return game.state;
	}

	// PACMAN_DYING

	private void enterPacManDyingState() {
		game.state.setDuration(clock.sec(6));
		game.pac.speed = 0;
		game.ghosts().forEach(ghost -> ghost.state = HUNTING_PAC); // TODO just to render ghost colorful
		game.bonus.edibleTicksLeft = game.bonus.eatenTicksLeft = 0;
		letGhostsFidget(false);
		sounds().ifPresent(SoundManager::stopAllSounds);
	}

	private PacManGameState runPacManDyingState() {
		if (game.state.hasExpired()) {
			if (game.attractMode) {
				exitPacManDyingState();
				reset(true);
				return game.state;
			}
			game.lives--;
			if (game.lives > 0) {
				return changeState(READY, this::exitPacManDyingState, this::enterReadyState);
			} else {
				return changeState(GAME_OVER, this::exitPacManDyingState, this::enterGameOverState);
			}
		}
		if (game.state.ticksRun() == clock.sec(1.5)) {
			game.ghosts().forEach(ghost -> ghost.visible = false);
		}
		if (game.state.ticksRun() == clock.sec(2.5)) {
			ui.animations().ifPresent(animations -> animations.pacDying().run());
			sounds().ifPresent(sm -> sm.playSound(PacManGameSound.PACMAN_DEATH));
		}
		return game.state.run();
	}

	private void exitPacManDyingState() {
		game.ghosts().forEach(ghost -> ghost.visible = true);
		ui.animations().ifPresent(animations -> animations.pacDying().reset());
		letGhostsFidget(true);
	}

	// GHOST_DYING

	private void enterGhostDyingState() {
		game.state.setDuration(clock.sec(1));
		game.pac.visible = false;
		sounds().ifPresent(sm -> sm.playSound(PacManGameSound.GHOST_EATEN));
		ui.animations().ifPresent(animations -> {
			animations.energizerBlinking().restart();
		});
	}

	private PacManGameState runGhostDyingState() {
		if (game.state.hasExpired()) {
			log("Resume state '%s' after ghost died", previousState);
			return changeState(previousState, this::exitGhostDyingState, () -> {
			});
		}
		steerPac();
		game.ghosts().filter(ghost -> ghost.is(DEAD) && ghost.bounty == 0 || ghost.is(ENTERING_HOUSE))
				.forEach(ghost -> ghost.update(game.level));
		return game.state.run();
	}

	private void exitGhostDyingState() {
		game.pac.visible = true;
		game.ghosts().forEach(ghost -> {
			if (ghost.bounty > 0) {
				ghost.bounty = 0;
				sounds().ifPresent(sm -> sm.loopSound(PacManGameSound.GHOST_EYES));
			}
		});
	}

	// CHANGING_LEVEL

	private void enterChangingLevelState() {
		game.state.setDuration(clock.sec(game.level.numFlashes + 3));
		game.bonus.edibleTicksLeft = game.bonus.eatenTicksLeft = 0;
		game.pac.speed = 0;
		sounds().ifPresent(SoundManager::stopAllSounds);
	}

	private PacManGameState runChangingLevelState() {
		if (game.state.hasExpired()) {
			return changeState(READY, this::exitChangingLevelState, this::enterReadyState);
		}
		if (game.state.ticksRun() == clock.sec(2)) {
			game.ghosts().forEach(ghost -> ghost.visible = false);
		}
		if (game.state.ticksRun() == clock.sec(3)) {
			ui.animations().ifPresent(
					animations -> animations.mazeFlashing(game.level.mazeNumber).repetitions(game.level.numFlashes).restart());
		}
		return game.state.run();
	}

	private void exitChangingLevelState() {
		log("Level %d complete, entering level %d", game.currentLevelNumber, game.currentLevelNumber + 1);
		game.enterLevel(game.currentLevelNumber + 1);
		game.levelSymbols.add(game.level.bonusSymbol);
		ui.animations().ifPresent(animations -> animations.mazeFlashing(game.level.mazeNumber).reset());
	}

	// GAME_OVER

	private void enterGameOverState() {
		game.state.setDuration(clock.sec(10));
		game.ghosts().forEach(ghost -> ghost.speed = 0);
		game.pac.speed = 0;
		game.saveHighscore();
		letGhostsFidget(false);
		ui.showMessage(ui.translation("GAME_OVER"), true);
	}

	private PacManGameState runGameOverState() {
		if (game.state.hasExpired() || ui.keyPressed("space")) {
			return changeState(INTRO, this::exitGameOverState, this::enterIntroState);
		}
		return game.state.run();
	}

	private void exitGameOverState() {
		reset(true);
	}

	// END STATE-MACHINE

	// BEGIN STATE_MACHINE INFRASTRUCTURE

	private PacManGameState changeState(PacManGameState newState, Runnable onExit, Runnable onEntry) {
		if (game.state != null) {
			log("Exit state '%s'", game.stateDescription());
		}
		onExit.run();
		previousState = game.state;
		game.state = newState;
		ui.updateScene();
		onEntry.run();
		log("Entered state '%s' for %s", game.stateDescription(), ticksDescription(game.state.durationTicks()));
		return game.state;
	}

	private void updateState() {
		handleCheatsAndStuff();
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

	// END STATE_MACHINE INFRASTRUCTURE

	private void score(int points) {
		if (game.attractMode) {
			return;
		}
		int oldscore = game.score;
		game.score += points;
		if (oldscore < 10000 && game.score >= 10000) {
			game.lives++;
			sounds().ifPresent(sm -> sm.playSound(PacManGameSound.EXTRA_LIFE));
			log("Extra life! Now we have %d lives", game.lives);
		}
		if (game.score > game.highscorePoints) {
			game.highscorePoints = game.score;
			game.highscoreLevel = game.currentLevelNumber;
		}
	}

	private void steerPac() {
		if (autopilotOn) {
			if (autopilot == null) {
				autopilot = new Autopilot(game);
			}
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

	private void onPacFoundFood(V2i foodLocation) {
		game.level.removeFood(foodLocation);
		if (game.level.world.isEnergizerTile(foodLocation)) {
			game.pac.starvingTicks = 0;
			game.pac.restingTicksLeft = 3;
			game.ghostBounty = 200;
			letPacFrightenGhosts(game.level.ghostFrightenedSeconds);
			score(50);
		} else {
			game.pac.starvingTicks = 0;
			game.pac.restingTicksLeft = 1;
			score(10);
		}

		// Bonus gets edible?
		if (game.level.eatenFoodCount() == 70 || game.level.eatenFoodCount() == 170) {
			game.bonus.activate(game.level.bonusSymbol, game.bonusValues[game.level.bonusSymbol],
					game.bonusActivationTicks());
			log("Bonus %s (value %d) activated", game.bonusNames[game.bonus.symbol], game.bonus.points);
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
		sounds().ifPresent(sm -> sm.playSound(PacManGameSound.PACMAN_MUNCH));
	}

	private void letPacFrightenGhosts(int frightenedSec) {
		game.pac.powerTicksLeft = clock.sec(frightenedSec);
		if (frightenedSec > 0) {
			log("Pac-Man got power for %d seconds", frightenedSec);
			game.ghosts().filter(ghost -> ghost.is(HUNTING_PAC)).forEach(ghost -> {
				ghost.state = FRIGHTENED;
				ghost.wishDir = ghost.dir.opposite();
				ghost.forcedDirection = true;
				ui.animations().ifPresent(animations -> {
					animations.letGhostBeFrightened(ghost, true);
				});
			});
			ui.animations().ifPresent(animations -> {
				animations.ghostFlashing().reset(); // in case flashing is active now
			});
			sounds().ifPresent(sm -> sm.loopSound(PacManGameSound.PACMAN_POWER));
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
		return game.currentLevelNumber < 5 ? clock.sec(4) : clock.sec(3);
	}

	// Ghosts

	private void killGhost(Ghost ghost) {
		ghost.state = DEAD;
		ghost.targetTile = game.level.world.houseEntry();
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
		game.ghosts().filter(ghost -> ghost.is(HUNTING_PAC) || ghost.is(FRIGHTENED)).forEach(this::killGhost);
	}

	private void setGhostHuntingTarget(Ghost ghost) {
		if (playingMsPacMan() && game.huntingPhase == 0 && (ghost.id == BLINKY || ghost.id == PINKY)) {
			// In Ms. Pac-Man, Blinky and Pinky move randomly during *first* scatter phase
			ghost.targetTile = null;
		} else if (game.inScatteringPhase() && ghost.elroy == 0) {
			ghost.targetTile = game.level.world.ghostScatterTile(ghost.id);
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
			return game.ghosts[3].tile().euclideanDistance(game.pac.tile()) < 8 ? game.level.world.ghostScatterTile(3)
					: game.pac.tile();
		default:
			throw new IllegalArgumentException("Unknown ghost id: " + ghostID);
		}
	}

	// Ghost house

	private void tryReleasingGhosts() {
		if (game.ghosts[BLINKY].is(LOCKED)) {
			game.ghosts[BLINKY].state = HUNTING_PAC;
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
			return game.currentLevelNumber == 1 ? 30 : 0;
		}
		if (ghost.id == CLYDE) {
			return game.currentLevelNumber == 1 ? 60 : game.currentLevelNumber == 2 ? 50 : 0;
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

	// Cheats and stuff

	private void handleCheatsAndStuff() {
		if (game.attractMode) {
			return;
		}
		boolean r = game.state == READY, h = game.state == HUNTING;
		if (ui.keyPressed("a") && (r || h)) {
			toggleAutopilot();
		}
		if (ui.keyPressed("i") && (r || h)) {
			togglePacImmunity();
		}
		if (ui.keyPressed("e") && h) {
			game.level.world.tiles().filter(tile -> game.level.containsFood(tile) && !game.level.world.isEnergizerTile(tile))
					.forEach(game.level::removeFood);
		}
		if (ui.keyPressed("x") && (h)) {
			killAllGhosts();
			changeState(GHOST_DYING, this::exitHuntingState, this::enterGhostDyingState);
		}
		if (ui.keyPressed("l") && (r || h)) {
			game.lives++;
		}
		if (ui.keyPressed("n") && (r || h)) {
			changeState(CHANGING_LEVEL, this::exitHuntingState, this::enterChangingLevelState);
		}
		if (ui.keyPressed("6") && !playingMsPacMan()) {
			PacManClassicRendering.foodAnimationOn = !PacManClassicRendering.foodAnimationOn;
			ui.showFlashMessage("Fancy food " + (PacManClassicRendering.foodAnimationOn ? "on" : "off"));
		}
	}
}
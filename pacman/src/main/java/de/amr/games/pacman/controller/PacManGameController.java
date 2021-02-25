package de.amr.games.pacman.controller;

import static de.amr.games.pacman.heaven.God.clock;
import static de.amr.games.pacman.heaven.God.random;
import static de.amr.games.pacman.lib.Direction.DOWN;
import static de.amr.games.pacman.lib.Direction.LEFT;
import static de.amr.games.pacman.lib.Direction.RIGHT;
import static de.amr.games.pacman.lib.Direction.UP;
import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.model.common.GameType.MS_PACMAN;
import static de.amr.games.pacman.model.common.GameType.PACMAN;
import static de.amr.games.pacman.model.common.Ghost.BLINKY;
import static de.amr.games.pacman.model.common.Ghost.CLYDE;
import static de.amr.games.pacman.model.common.Ghost.INKY;
import static de.amr.games.pacman.model.common.Ghost.PINKY;
import static de.amr.games.pacman.model.common.GhostState.DEAD;
import static de.amr.games.pacman.model.common.GhostState.HUNTING_PAC;
import static de.amr.games.pacman.model.common.GhostState.LEAVING_HOUSE;
import static de.amr.games.pacman.model.common.GhostState.LOCKED;
import static de.amr.games.pacman.model.common.PacManGameState.CHANGING_LEVEL;
import static de.amr.games.pacman.model.common.PacManGameState.GAME_OVER;
import static de.amr.games.pacman.model.common.PacManGameState.GHOST_DYING;
import static de.amr.games.pacman.model.common.PacManGameState.HUNTING;
import static de.amr.games.pacman.model.common.PacManGameState.INTERMISSION;
import static de.amr.games.pacman.model.common.PacManGameState.INTRO;
import static de.amr.games.pacman.model.common.PacManGameState.PACMAN_DYING;
import static de.amr.games.pacman.model.common.PacManGameState.READY;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.amr.games.pacman.lib.Animation;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.GameType;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.model.common.GhostState;
import de.amr.games.pacman.model.common.PacManGameState;
import de.amr.games.pacman.model.mspacman.MsPacManGame;
import de.amr.games.pacman.model.pacman.PacManGame;
import de.amr.games.pacman.sound.PacManGameSound;
import de.amr.games.pacman.sound.SoundManager;
import de.amr.games.pacman.ui.PacManGameAnimations;
import de.amr.games.pacman.ui.PacManGameUI;

/**
 * Controller (in the sense of MVC) for the Pac-Man and Ms. Pac-Man game.
 * <p>
 * This is essentially a finite-state machine with states defined in {@link PacManGameState}. All
 * game data are stored in the model of the selected game, see {@link MsPacManGame} and
 * {@link PacManGame}. The views are decoupled by the interface {@link PacManGameUI}, scene
 * selection is not controlled by this class but left to the user interface implementation.
 * <p>
 * Missing functionality:
 * <ul>
 * <li><a href="https://pacman.holenet.info/#CH2_Cornering"><em>Cornering</em></a>: I do not
 * consider cornering as important when the player is controlled by keyboard keys, for a joystick
 * that probably would be more important.</li>
 * <li>Exact level data for Ms. Pac-Man still unclear. Any hints appreciated!
 * <li>Multiple players, credits.</li>
 * </ul>
 * 
 * @author Armin Reichert
 * 
 * @see <a href="https://github.com/armin-reichert">GitHub</a>
 * @see <a href="https://pacman.holenet.info">Jamey Pittman: The Pac-Man Dossier</a>
 * @see <a href="https://gameinternals.com/understanding-pac-man-ghost-behavior">Chad Birch:
 *      Understanding ghost behavior</a>
 * @see <a href="http://superpacman.com/mspacman/">Ms. Pac-Man</a>
 */
public class PacManGameController {

	private final GameModel pacManGame = new PacManGame();
	private final GameModel msPacManGame = new MsPacManGame();

	private GameModel game;

	private final List<PacManGameUI> views = new ArrayList<>();

	private Autopilot autopilot;
	private boolean autopilotOn;
	private PacManGameState previousState;

	private Thread gameLoopThread;
	private volatile boolean gameLoopRunning;

	public void startGameLoop() {
		if (gameLoopRunning) {
			log("Game loop is already started");
			return;
		}
		gameLoopThread = new Thread(this::gameLoop, "PacManGameLoop");
		gameLoopThread.start();
		gameLoopRunning = true;
	}

	public void endGameLoop() {
		gameLoopRunning = false;
		try {
			gameLoopThread.join();
		} catch (Exception x) {
			x.printStackTrace();
		}
		log("Exit game and terminate VM");
		System.exit(0);
	}

	private void gameLoop() {
		while (gameLoopRunning) {
			clock.tick(this::step);
		}
	}

	private void step() {
		updateGameState();
		views.forEach(PacManGameUI::update);
		views.forEach(PacManGameUI::render);
	}

	public GameModel getGame() {
		return game;
	}

	public Stream<PacManGameUI> views() {
		return views.stream();
	}

	public Stream<PacManGameAnimations> animations() {
		return views().map(PacManGameUI::animation).filter(Optional::isPresent).map(Optional::get);
	}

	public Stream<SoundManager> sounds() {
		return views().map(PacManGameUI::sound).filter(Optional::isPresent).map(Optional::get);
	}

	public boolean isPlaying(GameType type) {
		if (game == msPacManGame) {
			return type == MS_PACMAN;
		}
		if (game == pacManGame) {
			return type == PACMAN;
		}
		throw new IllegalStateException();
	}

	public void play(GameType type) {
		if (type == MS_PACMAN) {
			game = msPacManGame;
		} else if (type == PACMAN) {
			game = pacManGame;
		} else {
			throw new IllegalArgumentException();
		}
		autopilot = new Autopilot(game);
		changeState(INTRO, null, this::enterIntroState);
		views.forEach(view -> view.onGameChanged(game));
	}

	public void addView(PacManGameUI view) {
		if (views.add(view)) {
			log("Added view %s", view);
		}
	}

	public void showViews() {
		views.forEach(PacManGameUI::show);
	}

	public void showFlashMessage(String message, long ticks) {
		views.forEach(view -> view.showFlashMessage(message, ticks));
	}

	public void toggleGameType() {
		if (isPlaying(MS_PACMAN)) {
			play(PACMAN);
		} else {
			play(MS_PACMAN);
		}
		sounds().forEach(SoundManager::stopAll);
		showFlashMessage("Now playing " + (isPlaying(MS_PACMAN) ? "Ms. Pac-Man" : "Pac-Man"), clock.sec(2));
	}

	public void toggleAutopilot() {
		autopilotOn = !autopilotOn;
		String msg = "Autopilot " + (autopilotOn ? "on" : "off");
		showFlashMessage(msg, clock.sec(1.5));
		log(msg);
	}

	public void togglePacImmunity() {
		game.pac.immune = !game.pac.immune;
		String msg = game.pac.name + " is " + (game.pac.immune ? "immune" : "vulnerable");
		showFlashMessage(msg, clock.sec(1.5));
		log(msg);
	}

	private Ghost ghost(int id) {
		return game.ghosts[id];
	}

	private void enableGhostKickingAnimation(boolean enabled) {
		animations().map(animations -> animations.ghostsKicking(game.ghosts())).forEach(kicking -> {
			kicking.forEach(enabled ? Animation::restart : Animation::reset);
		});
	}

	private boolean keyPressed(String keySpec) {
		return views.stream().anyMatch(view -> view.keyPressed(keySpec));
	}

	/*
	 * The finite-state machine controlling the game play:
	 */

	// INTRO

	private void enterIntroState() {
		game.state.timer.setDuration(Long.MAX_VALUE);
		game.reset();
		autopilotOn = false;
		previousState = null;
		views.forEach(view -> {
			view.mute(false);
			view.sound().ifPresent(SoundManager::stopAll);
			view.animation().ifPresent(animations -> animations.reset(game));
			view.onGameChanged(game);
		});
	}

	private PacManGameState runIntroState() {
		if (game.attractMode) {
			views.forEach(view -> view.mute(true));
			return changeState(READY, null, this::enterReadyState);
		}
		if (keyPressed("Space")) {
			return changeState(READY, null, this::enterReadyState);
		}
		if (keyPressed("V")) {
			toggleGameType();
		}
		// test intermission scenes
		if (keyPressed("1")) {
			game.intermissionNumber = 1;
			showFlashMessage("Intermission #1", clock.sec(3));
			return changeState(INTERMISSION, null, this::enterIntermissionState);
		}
		if (keyPressed("2")) {
			game.intermissionNumber = 2;
			showFlashMessage("Intermission #2", clock.sec(3));
			return changeState(INTERMISSION, null, this::enterIntermissionState);
		}
		if (keyPressed("3")) {
			game.intermissionNumber = 3;
			showFlashMessage("Intermission #3", clock.sec(3));
			return changeState(INTERMISSION, null, this::enterIntermissionState);
		}

		return game.state.tick();
	}

	// READY

	private void enterReadyState() {
		game.resetGuys();
		views().forEach(view -> view.mute(game.attractMode));
		animations().forEach(animations -> animations.reset(game));
		if (game.started || game.attractMode) {
			game.state.timer.setDuration(clock.sec(2));
		} else {
			game.state.timer.setDuration(clock.sec(4.5));
			sounds().forEach(sound -> sound.play(PacManGameSound.GAME_READY));
		}
	}

	private PacManGameState runReadyState() {
		if (game.state.timer.expired()) {
			enableGhostKickingAnimation(true);
			return changeState(PacManGameState.HUNTING, this::exitReadyState, this::enterHuntingState);
		}
		if (game.state.timer.running() == clock.sec(0.5)) {
			game.pac.visible = true;
			for (Ghost ghost : game.ghosts) {
				ghost.visible = true;
			}
		}
		return game.state.tick();
	}

	private void exitReadyState() {
		if (!game.attractMode) {
			game.started = true;
		}
	}

	// HUNTING

	static final List<PacManGameSound> SIRENS = Arrays.asList(PacManGameSound.GHOST_SIREN_1,
			PacManGameSound.GHOST_SIREN_2, PacManGameSound.GHOST_SIREN_3, PacManGameSound.GHOST_SIREN_4);

	// TODO not sure about when which siren should play
	private void startHuntingPhase(int phase) {
		game.huntingPhase = phase;
		game.state.timer.setDuration(game.getHuntingPhaseDuration(game.huntingPhase));
		if (game.inScatteringPhase()) {
			sounds().forEach(sound -> {
				if (game.huntingPhase >= 2) {
					sound.stop(SIRENS.get((game.huntingPhase - 1) / 2));
				}
				sound.loopForever(SIRENS.get(game.huntingPhase / 2));
			});
		}
		log("Hunting phase %d started, state is now %s", phase, game.stateDescription());
	}

	private void enterHuntingState() {
		startHuntingPhase(0);
		animations().forEach(animations -> {
			animations.energizerBlinking().restart();
			animations.playerMunching(game.pac).forEach(Animation::restart);
		});
	}

	private void exitHuntingState() {
		animations().forEach(animations -> {
			animations.energizerBlinking().reset();
		});
	}

	private PacManGameState runHuntingState() {
		// Level completed?
		if (game.level.foodRemaining == 0) {
			return changeState(CHANGING_LEVEL, this::exitHuntingState, this::enterChangingLevelState);
		}

		// Pac kills ghost(s)?
		List<Ghost> prey = Stream.of(game.ghosts).filter(ghost -> ghost.is(GhostState.FRIGHTENED) && ghost.meets(game.pac))
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
		if (game.state.timer.expired()) {
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
				game.ghosts().filter(ghost -> ghost.is(GhostState.FRIGHTENED)).forEach(ghost -> {
					ghost.state = HUNTING_PAC;
				});
			}
			animations().forEach(animations -> {
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
			sounds().forEach(sm -> sm.play(PacManGameSound.BONUS_EATEN));
		}

		sounds().forEach(sm -> {
			if (game.ghosts().noneMatch(ghost -> ghost.is(DEAD))) {
				sm.stop(PacManGameSound.GHOST_EYES);
			}
			if (game.pac.powerTicksLeft == 0) {
				sm.stop(PacManGameSound.PACMAN_POWER);
			}
		});

		// hunting state timer is suspended if Pac has power
		if (game.pac.powerTicksLeft == 0) {
			game.state.tick();
		}

		return game.state;
	}

	// PACMAN_DYING

	private void enterPacManDyingState() {
		game.state.timer.setDuration(clock.sec(5));
		game.pac.speed = 0;
		game.bonus.edibleTicksLeft = game.bonus.eatenTicksLeft = 0;
		enableGhostKickingAnimation(false);
		sounds().forEach(SoundManager::stopAll);
	}

	private PacManGameState runPacManDyingState() {
		if (game.state.timer.expired()) {
			if (game.attractMode) {
				return changeState(INTRO, this::exitPacManDyingState, this::enterIntroState);
			} else if (game.lives > 1) {
				game.lives--;
				return changeState(READY, this::exitPacManDyingState, this::enterReadyState);
			} else {
				return changeState(GAME_OVER, this::exitPacManDyingState, this::enterGameOverState);
			}
		}
		if (game.state.timer.running() == clock.sec(2)) {
			game.ghosts().forEach(ghost -> ghost.visible = false);
			animations().forEach(animations -> animations.playerDying().restart());
			sounds().forEach(sm -> sm.play(PacManGameSound.PACMAN_DEATH));
		}
		return game.state.tick();
	}

	private void exitPacManDyingState() {
		game.ghosts().forEach(ghost -> ghost.visible = true);
	}

	// GHOST_DYING

	private void enterGhostDyingState() {
		game.state.timer.setDuration(clock.sec(1));
		game.pac.visible = false;
		sounds().forEach(sm -> sm.play(PacManGameSound.GHOST_EATEN));
		animations().forEach(animations -> {
			animations.energizerBlinking().restart();
		});
	}

	private PacManGameState runGhostDyingState() {
		if (game.state.timer.expired()) {
			log("Resume state '%s' after ghost died", previousState);
			return changeState(previousState, this::exitGhostDyingState, () -> {
			});
		}
		steerPac();
		game.ghosts().filter(ghost -> ghost.is(DEAD) && ghost.bounty == 0 || ghost.is(GhostState.ENTERING_HOUSE))
				.forEach(ghost -> ghost.update(game.level));
		return game.state.tick();
	}

	private void exitGhostDyingState() {
		game.pac.visible = true;
		game.ghosts().forEach(ghost -> {
			if (ghost.bounty > 0) {
				ghost.bounty = 0;
				sounds().forEach(sm -> sm.loopForever(PacManGameSound.GHOST_EYES));
			}
		});
	}

	// CHANGING_LEVEL

	private void enterChangingLevelState() {
		game.state.timer.setDuration(clock.sec(game.level.numFlashes + 3));
		game.bonus.edibleTicksLeft = game.bonus.eatenTicksLeft = 0;
		game.pac.speed = 0;
		sounds().forEach(SoundManager::stopAll);
	}

	private PacManGameState runChangingLevelState() {
		if (game.state.timer.expired()) {
			if (Arrays.asList(2, 5, 9, 13, 17).contains(game.currentLevelNumber)) {
				game.intermissionNumber = intermissionNumber(game.currentLevelNumber);
				return changeState(INTERMISSION, this::exitChangingLevelState, this::enterIntermissionState);
			}
			return changeState(READY, this::exitChangingLevelState, this::enterReadyState);
		}
		if (game.state.timer.running() == clock.sec(2)) {
			game.ghosts().forEach(ghost -> ghost.visible = false);
		}
		if (game.state.timer.running() == clock.sec(3)) {
			animations().forEach(
					animations -> animations.mazeFlashing(game.level.mazeNumber).repetitions(game.level.numFlashes).restart());
		}
		return game.state.tick();
	}

	private void exitChangingLevelState() {
		log("Level %d complete, entering level %d", game.currentLevelNumber, game.currentLevelNumber + 1);
		game.enterLevel(game.currentLevelNumber + 1);
		game.levelSymbols.add(game.level.bonusSymbol);
		views.forEach(
				view -> view.animation().ifPresent(animations -> animations.mazeFlashing(game.level.mazeNumber).reset()));
	}

	// GAME_OVER

	private void enterGameOverState() {
		game.state.timer.setDuration(clock.sec(10));
		game.ghosts().forEach(ghost -> ghost.speed = 0);
		game.pac.speed = 0;
		game.saveHighscore();
		enableGhostKickingAnimation(false);
	}

	private PacManGameState runGameOverState() {
		if (game.state.timer.expired() || keyPressed("Space")) {
			return changeState(INTRO, null, this::enterIntroState);
		}
		return game.state.tick();
	}

	// INTERMISSION

	private int intermissionNumber(int levelNumber) {
		switch (levelNumber) {
		//@formatter:off
		case 2:	return 1;
		case 5:	return 2;
		case 9:	case 13: case 17: return 3;
		default: return 0;
		//@formatter:on
		}
	}

	private void enterIntermissionState() {
		game.state.timer.setDuration(Long.MAX_VALUE);
		log("Starting intermission #%d", game.intermissionNumber);
	}

	private PacManGameState runIntermissionState() {
		if (game.state.timer.expired()) {
			return changeState(READY, null, this::enterReadyState);
		}
		return game.state.tick();
	}

	// END STATE-MACHINE

	// BEGIN STATE_MACHINE INFRASTRUCTURE

	private PacManGameState changeState(PacManGameState newState, Runnable onExit, Runnable onEntry) {
		if (game.state != null) {
			log("Exit state '%s'", game.stateDescription());
		}
		if (onExit != null) {
			onExit.run();
		}
		previousState = game.state;
		game.state = newState;
		if (onEntry != null) {
			onEntry.run();
		}
		log("Entered state '%s' for %s", game.stateDescription(), ticksDescription(game.state.timer.getDuration()));
		return game.state;
	}

	private void updateGameState() {
		if (keyPressed("Esc")) {
			changeState(INTRO, null, this::enterIntroState);
			return;
		}
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
		case INTERMISSION:
			runIntermissionState();
			break;
		default:
			throw new IllegalStateException("Illegal state: " + game.state);
		}
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
			sounds().forEach(sm -> sm.play(PacManGameSound.EXTRA_LIFE));
			log("Extra life. Now you have %d lives!", game.lives);
			showFlashMessage("Extra life!", clock.sec(1));
		}
		if (game.score > game.highscorePoints) {
			game.highscorePoints = game.score;
			game.highscoreLevel = game.currentLevelNumber;
		}
	}

	private void steerPac() {
		if (autopilotOn || game.attractMode) {
			autopilot.run();
		} else {
			if (keyPressed("Left")) {
				game.pac.wishDir = LEFT;
			} else if (keyPressed("Right")) {
				game.pac.wishDir = RIGHT;
			} else if (keyPressed("Up")) {
				game.pac.wishDir = UP;
			} else if (keyPressed("Down")) {
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
			game.bonus.visible = true;
			game.bonus.symbol = game.level.bonusSymbol;
			game.bonus.points = game.bonusValues[game.level.bonusSymbol];
			game.bonus.activate(isPlaying(PACMAN) ? clock.sec(9 + random.nextFloat()) : Long.MAX_VALUE);
			log("Bonus %s (value %d) activated", game.bonusNames[game.bonus.symbol], game.bonus.points);
		}

		// Blinky becomes Elroy?
		if (game.level.foodRemaining == game.level.elroy1DotsLeft) {
			ghost(BLINKY).elroy = 1;
			log("Blinky becomes Cruise Elroy 1");
		} else if (game.level.foodRemaining == game.level.elroy2DotsLeft) {
			ghost(BLINKY).elroy = 2;
			log("Blinky becomes Cruise Elroy 2");
		}

		updateGhostDotCounters();
		sounds().forEach(sm -> sm.play(PacManGameSound.PACMAN_MUNCH));
	}

	private void letPacFrightenGhosts(int frightenedSec) {
		game.pac.powerTicksLeft = clock.sec(frightenedSec);
		if (frightenedSec > 0) {
			log("Pac-Man got power for %d seconds", frightenedSec);
			game.ghosts().filter(ghost -> ghost.is(HUNTING_PAC)).forEach(ghost -> {
				ghost.state = GhostState.FRIGHTENED;
				ghost.wishDir = ghost.dir.opposite();
				ghost.forcedDirection = true;
				animations().forEach(animations -> {
					animations.ghostFrightened(ghost).forEach(Animation::restart);
				});
			});
			animations().forEach(animations -> {
				animations.ghostFlashing().reset(); // in case flashing is active now
			});
			sounds().forEach(sm -> sm.loopForever(PacManGameSound.PACMAN_POWER));
		}
	}

	private void onPacKilled(Ghost killer) {
		log("%s killed by %s at tile %s", game.pac.name, killer.name, killer.tile());
		game.pac.dead = true;
		int elroyMode = ghost(BLINKY).elroy;
		if (elroyMode > 0) {
			ghost(BLINKY).elroy = -elroyMode; // negative value means "disabled"
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
		game.ghosts().filter(ghost -> ghost.is(HUNTING_PAC) || ghost.is(GhostState.FRIGHTENED)).forEach(this::killGhost);
	}

	private void setGhostHuntingTarget(Ghost ghost) {
		// In Ms. Pac-Man, Blinky and Pinky move randomly during the *first* scatter phase:
		if (isPlaying(MS_PACMAN) && game.huntingPhase == 0 && (ghost.id == BLINKY || ghost.id == PINKY)) {
			ghost.targetTile = null;
		} else if (game.inScatteringPhase() && ghost.elroy == 0) {
			ghost.targetTile = game.level.world.ghostScatterTile(ghost.id);
		} else {
			ghost.targetTile = ghostHuntingTarget(ghost.id);
		}
	}

	/*
	 * The so called "ghost AI":
	 */
	private V2i ghostHuntingTarget(int ghostID) {
		switch (ghostID) {
		case BLINKY:
			return game.pac.tile();
		case PINKY: {
			V2i fourAheadPac = game.pac.tile().sum(game.pac.dir.vec.scaled(4));
			if (game.pac.dir == UP) { // simulate overflow bug
				fourAheadPac = fourAheadPac.sum(LEFT.vec.scaled(4));
			}
			return fourAheadPac;
		}
		case INKY: {
			V2i twoAheadPac = game.pac.tile().sum(game.pac.dir.vec.scaled(2));
			if (game.pac.dir == UP) { // simulate overflow bug
				twoAheadPac = twoAheadPac.sum(LEFT.vec.scaled(2));
			}
			return ghost(BLINKY).tile().scaled(-1).sum(twoAheadPac.scaled(2));
		}
		case CLYDE: /* SUE */
			return ghost(CLYDE).tile().euclideanDistance(game.pac.tile()) < 8 ? game.level.world.ghostScatterTile(CLYDE)
					: game.pac.tile();
		default:
			throw new IllegalArgumentException("Unknown ghost id: " + ghostID);
		}
	}

	// Ghost house

	private void tryReleasingGhosts() {
		if (ghost(BLINKY).is(LOCKED)) {
			ghost(BLINKY).state = HUNTING_PAC;
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
		if (ghost == ghost(CLYDE) && ghost(BLINKY).elroy < 0) {
			ghost(BLINKY).elroy -= 1; // resume Elroy mode
			log("Blinky Elroy mode %d resumed", ghost(BLINKY).elroy);
		}
		log("Ghost %s released: %s", ghost.name, String.format(reason, args));
	}

	private Optional<Ghost> preferredLockedGhostInHouse() {
		return Stream.of(PINKY, INKY, CLYDE).map(this::ghost).filter(ghost -> ghost.is(LOCKED)).findFirst();
	}

	private int ghostPrivateDotLimit(Ghost ghost) {
		if (ghost == ghost(INKY)) {
			return game.currentLevelNumber == 1 ? 30 : 0;
		}
		if (ghost == ghost(CLYDE)) {
			return game.currentLevelNumber == 1 ? 60 : game.currentLevelNumber == 2 ? 50 : 0;
		}
		return 0;
	}

	private int ghostGlobalDotLimit(Ghost ghost) {
		return ghost == ghost(PINKY) ? 7 : ghost == ghost(INKY) ? 17 : Integer.MAX_VALUE;
	}

	private void updateGhostDotCounters() {
		if (game.globalDotCounterEnabled) {
			if (ghost(CLYDE).is(LOCKED) && game.globalDotCounter == 32) {
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
		// A = toggle autopilot
		if (keyPressed("A")) {
			toggleAutopilot();
		}
		// E = eat all food without the energizers
		if (keyPressed("E") && h) {
			game.level.world.tiles().filter(game.level::containsFood).filter(tile -> !game.level.world.isEnergizerTile(tile))
					.forEach(game.level::removeFood);
		}
		// I = toggle Pac immune/vulnerable
		if (keyPressed("I")) {
			togglePacImmunity();
		}
		// L = add live
		if (keyPressed("L")) {
			game.lives++;
		}
		// N = change to next level
		if (keyPressed("N") && (r || h)) {
			changeState(CHANGING_LEVEL, this::exitHuntingState, this::enterChangingLevelState);
		}
		// X = kill all ghosts outside of ghost house
		if (keyPressed("X") && h) {
			killAllGhosts();
			changeState(GHOST_DYING, this::exitHuntingState, this::enterGhostDyingState);
		}
	}
}
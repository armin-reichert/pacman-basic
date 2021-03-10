package de.amr.games.pacman.controller;

import static de.amr.games.pacman.lib.Direction.DOWN;
import static de.amr.games.pacman.lib.Direction.LEFT;
import static de.amr.games.pacman.lib.Direction.RIGHT;
import static de.amr.games.pacman.lib.Direction.UP;
import static de.amr.games.pacman.lib.God.clock;
import static de.amr.games.pacman.lib.God.random;
import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.model.common.GameType.MS_PACMAN;
import static de.amr.games.pacman.model.common.GameType.PACMAN;
import static de.amr.games.pacman.model.common.Ghost.BLINKY;
import static de.amr.games.pacman.model.common.Ghost.CLYDE;
import static de.amr.games.pacman.model.common.Ghost.INKY;
import static de.amr.games.pacman.model.common.Ghost.PINKY;
import static de.amr.games.pacman.model.common.GhostState.DEAD;
import static de.amr.games.pacman.model.common.GhostState.ENTERING_HOUSE;
import static de.amr.games.pacman.model.common.GhostState.FRIGHTENED;
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
import java.util.EnumMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.amr.games.pacman.lib.Animation;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.GameType;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.model.common.PacManGameState;
import de.amr.games.pacman.model.mspacman.MsPacManGame;
import de.amr.games.pacman.model.pacman.PacManGame;
import de.amr.games.pacman.sound.PacManGameSound;
import de.amr.games.pacman.sound.SoundManager;
import de.amr.games.pacman.ui.PacManGameUI;
import de.amr.games.pacman.ui.animation.GhostAnimations;
import de.amr.games.pacman.ui.animation.MazeAnimations;
import de.amr.games.pacman.ui.animation.PacManGameAnimations;
import de.amr.games.pacman.ui.animation.PlayerAnimations;

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

	private final EnumMap<GameType, GameModel> gameModels = new EnumMap<>(GameType.class);
	{
		gameModels.put(PACMAN, new PacManGame());
		gameModels.put(MS_PACMAN, new MsPacManGame());
	}
	private GameModel currentGame;

	private final List<PacManGameUI> views = new ArrayList<>();

	private Autopilot autopilot;
	private boolean autopilotOn;
	private PacManGameState previousState;
	private boolean intermissionScenesEnabled = false;

	public void step() {
		// Quit current state and enter intro state?
		if (keyPressed("Q")) {
			changeState(INTRO, this::exitHuntingState, this::enterIntroState);
			return;
		}
		handleKeys();
		updateGameState();
		views.forEach(PacManGameUI::update);
	}

	public GameModel getGame() {
		return currentGame;
	}

	private Stream<PacManGameAnimations> animationsAllViews() {
		return views.stream().map(PacManGameUI::animation).flatMap(Optional::stream);
	}

	private Stream<MazeAnimations> mazeAnimationsAllViews() {
		return animationsAllViews().map(PacManGameAnimations::mazeAnimations);
	}

	private Stream<PlayerAnimations> playerAnimationsAllViews() {
		return animationsAllViews().map(PacManGameAnimations::playerAnimations);
	}

	private Stream<GhostAnimations> ghostAnimationsAllViews() {
		return animationsAllViews().map(PacManGameAnimations::ghostAnimations);
	}

	private Stream<SoundManager> soundsAllViews() {
		return views.stream().map(PacManGameUI::sound).flatMap(Optional::stream);
	}

	public boolean isPlaying(GameType type) {
		return currentGame == gameModels.get(type);
	}

	public GameType currentlyPlaying() {
		return Stream.of(GameType.values()).filter(this::isPlaying).findFirst().get();
	}

	public void play(GameType type) {
		currentGame = gameModels.get(type);
		currentGame.reset();
		autopilot = new Autopilot(currentGame);
		changeState(INTRO, null, this::enterIntroState);
		views.forEach(view -> view.onGameChanged(currentGame));
	}

	public void addView(PacManGameUI view) {
		if (views.add(view)) {
			log("Added view %s", view);
		}
	}

	public void showFlashMessage(String message, long ticks) {
		views.forEach(view -> view.showFlashMessage(message, ticks));
	}

	public void finishCurrentState() {
		currentGame.state.timer.setDuration(0);
	}

	public void toggleGameType() {
		if (isPlaying(MS_PACMAN)) {
			play(PACMAN);
		} else {
			play(MS_PACMAN);
		}
		soundsAllViews().forEach(SoundManager::stopAll);
		showFlashMessage("Now playing " + (isPlaying(MS_PACMAN) ? "Ms. Pac-Man" : "Pac-Man"), clock.sec(2));
	}

	public void setAutopilot(boolean enabled) {
		autopilotOn = enabled;
		String msg = "Autopilot " + (autopilotOn ? "on" : "off");
		showFlashMessage(msg, clock.sec(1.5));
		log(msg);
	}

	public void toggleAutopilot() {
		setAutopilot(!autopilotOn);
	}

	public void setPlayerImmune(boolean immune) {
		currentGame.pac.immune = immune;
		String msg = currentGame.pac.name + " is " + (currentGame.pac.immune ? "immune" : "vulnerable");
		showFlashMessage(msg, clock.sec(1.5));
		log(msg);
	}

	public void togglePlayerImmune() {
		setPlayerImmune(!currentGame.pac.immune);
	}

	private Ghost ghost(int id) {
		return currentGame.ghosts[id];
	}

	private boolean keyPressed(String keySpec) {
		return views.stream().anyMatch(view -> view.keyPressed(keySpec));
	}

	/*
	 * The finite-state machine controlling the game play:
	 */

	// INTRO

	private void enterIntroState() {
		currentGame.state.timer.setDuration(Long.MAX_VALUE);
		currentGame.reset();
		autopilotOn = false;
		previousState = null;
		views.forEach(view -> {
			view.mute(false);
			view.sound().ifPresent(SoundManager::stopAll);
			view.animation().ifPresent(va -> va.reset(currentGame));
			view.onGameChanged(currentGame);
		});
	}

	private PacManGameState runIntroState() {
		if (currentGame.attractMode) {
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
			currentGame.intermissionNumber = 1;
			showFlashMessage("Intermission #1", clock.sec(0.5));
			return changeState(INTERMISSION, null, this::enterIntermissionState);
		}
		if (keyPressed("2")) {
			currentGame.intermissionNumber = 2;
			showFlashMessage("Intermission #2", clock.sec(0.5));
			return changeState(INTERMISSION, null, this::enterIntermissionState);
		}
		if (keyPressed("3")) {
			currentGame.intermissionNumber = 3;
			showFlashMessage("Intermission #3", clock.sec(0.5));
			return changeState(INTERMISSION, null, this::enterIntermissionState);
		}

		return currentGame.state.tick();
	}

	// READY

	private void enterReadyState() {
		currentGame.resetGuys();
		views.forEach(view -> view.mute(currentGame.attractMode));
		animationsAllViews().forEach(a -> a.reset(currentGame));
		if (currentGame.started || currentGame.attractMode) {
			currentGame.state.timer.setDuration(clock.sec(2));
		} else {
			currentGame.state.timer.setDuration(clock.sec(4.5));
			soundsAllViews().forEach(snd -> snd.play(PacManGameSound.GAME_READY));
		}
	}

	private PacManGameState runReadyState() {
		if (currentGame.state.timer.expired()) {
			return changeState(PacManGameState.HUNTING, this::exitReadyState, this::enterHuntingState);
		}
		if (currentGame.state.timer.running() == clock.sec(0.5)) {
			currentGame.pac.visible = true;
			for (Ghost ghost : currentGame.ghosts) {
				ghost.visible = true;
			}
		}
		return currentGame.state.tick();
	}

	private void exitReadyState() {
		if (!currentGame.attractMode) {
			currentGame.started = true;
		}
		ghostAnimationsAllViews().forEach(ga -> {
			currentGame.ghosts().flatMap(ga::ghostKicking).forEach(Animation::restart);
		});
	}

	// HUNTING

	static final List<PacManGameSound> SIRENS = Arrays.asList(PacManGameSound.GHOST_SIREN_1,
			PacManGameSound.GHOST_SIREN_2, PacManGameSound.GHOST_SIREN_3, PacManGameSound.GHOST_SIREN_4);

	// TODO not sure about when which siren should play
	private void startHuntingPhase(int phase) {
		currentGame.huntingPhase = phase;
		currentGame.state.timer.setDuration(currentGame.getHuntingPhaseDuration(currentGame.huntingPhase));
		if (currentGame.inScatteringPhase()) {
			soundsAllViews().forEach(sound -> {
				if (currentGame.huntingPhase >= 2) {
					sound.stop(SIRENS.get((currentGame.huntingPhase - 1) / 2));
				}
				sound.loopForever(SIRENS.get(currentGame.huntingPhase / 2));
			});
		}
		log("Hunting phase %d started, state is now %s", phase, currentGame.stateDescription());
	}

	private void enterHuntingState() {
		startHuntingPhase(0);
		mazeAnimationsAllViews().map(MazeAnimations::energizerBlinking).forEach(Animation::restart);
		playerAnimationsAllViews().flatMap(pa -> pa.playerMunching(currentGame.pac)).forEach(Animation::restart);
	}

	private void exitHuntingState() {
		mazeAnimationsAllViews().map(MazeAnimations::energizerBlinking).forEach(Animation::reset);
	}

	private PacManGameState runHuntingState() {
		// Level completed?
		if (currentGame.level.foodRemaining == 0) {
			return changeState(CHANGING_LEVEL, this::exitHuntingState, this::enterChangingLevelState);
		}

		// Pac killing ghost(s)?
		List<Ghost> prey = currentGame.ghosts(FRIGHTENED).filter(currentGame.pac::meets).collect(Collectors.toList());
		if (!prey.isEmpty()) {
			prey.forEach(this::killGhost);
			return changeState(GHOST_DYING, this::exitHuntingState, this::enterGhostDyingState);
		}

		// Pac getting killed by ghost?
		Optional<Ghost> killer = currentGame.ghosts(HUNTING_PAC).filter(currentGame.pac::meets).findAny();
		if (killer.isPresent() && !currentGame.pac.immune) {
			onPacKilled(killer.get());
			return changeState(PACMAN_DYING, this::exitHuntingState, this::enterPacManDyingState);
		}

		// Hunting phase complete?
		if (currentGame.state.timer.expired()) {
			currentGame.ghosts(HUNTING_PAC).forEach(Ghost::forceTurningBack);
			startHuntingPhase(++currentGame.huntingPhase);
		}

		// Can Pac move?
		steerPac();
		currentGame.pac.speed = currentGame.pac.powerTicksLeft == 0 ? currentGame.level.pacSpeed
				: currentGame.level.pacSpeedPowered;
		if (currentGame.pac.restingTicksLeft > 0) {
			currentGame.pac.restingTicksLeft--;
		} else {
			currentGame.pac.tryMoving();
		}

		// Did Pac find food?
		if (currentGame.level.containsFood(currentGame.pac.tile())) {
			onPacFoundFood(currentGame.pac.tile());
		} else {
			currentGame.pac.starvingTicks++;
		}

		// Is Pac losing power?
		if (currentGame.pac.powerTicksLeft > 0) {
			currentGame.pac.powerTicksLeft--;
			if (currentGame.pac.powerTicksLeft == 0) {
				log("%s lost power", currentGame.pac.name);
				currentGame.ghosts(FRIGHTENED).forEach(ghost -> ghost.state = HUNTING_PAC);
			}
			// TODO fixme
			currentGame.ghosts(FRIGHTENED).forEach(ghost -> {
				ghostAnimationsAllViews().map(ga -> ga.ghostFlashing(ghost)).forEach(flashing -> {
					if (currentGame.level.numFlashes > 0
							&& currentGame.pac.powerTicksLeft == currentGame.level.numFlashes * flashing.duration()) {
						flashing.restart();
						log("Ghost flashing started (%d flashes, %d ticks each), Pac power left: %d ticks",
								currentGame.level.numFlashes, flashing.duration(), currentGame.pac.powerTicksLeft);
					} else if (currentGame.pac.powerTicksLeft == 0) {
						flashing.reset();
						log("Ghost flashing stopped");
					} else {
						flashing.advance();
					}
				});
			});
		}

		tryReleasingGhosts();
		currentGame.ghosts(HUNTING_PAC).forEach(this::setGhostHuntingTarget);
		currentGame.ghosts().forEach(ghost -> ghost.update(currentGame.level));

		currentGame.bonus.update();
		if (currentGame.bonus.edibleTicksLeft > 0 && currentGame.pac.meets(currentGame.bonus)) {
			log("Pac-Man found bonus (%s) of value %d", currentGame.bonusNames[currentGame.bonus.symbol],
					currentGame.bonus.points);
			currentGame.bonus.eatAndDisplayValue(clock.sec(2));
			score(currentGame.bonus.points);
			soundsAllViews().forEach(snd -> snd.play(PacManGameSound.BONUS_EATEN));
		}

		soundsAllViews().forEach(sm -> {
			if (currentGame.ghosts().noneMatch(ghost -> ghost.is(DEAD))) {
				sm.stop(PacManGameSound.GHOST_EYES);
			}
			if (currentGame.pac.powerTicksLeft == 0) {
				sm.stop(PacManGameSound.PACMAN_POWER);
			}
		});

		mazeAnimationsAllViews().map(MazeAnimations::energizerBlinking).forEach(Animation::animate);

		// hunting state timer is suspended if Pac has power
		if (currentGame.pac.powerTicksLeft == 0) {
			currentGame.state.tick();
		}

		return currentGame.state;
	}

	// PACMAN_DYING

	private void enterPacManDyingState() {
		currentGame.state.timer.setDuration(clock.sec(4));
		currentGame.pac.speed = 0;
		currentGame.bonus.edibleTicksLeft = currentGame.bonus.eatenTicksLeft = 0;
		ghostAnimationsAllViews().forEach(ga -> {
			currentGame.ghosts().flatMap(ga::ghostKicking).forEach(Animation::reset);
		});
		soundsAllViews().forEach(SoundManager::stopAll);
	}

	private PacManGameState runPacManDyingState() {
		if (currentGame.state.timer.expired()) {
			if (currentGame.attractMode) {
				return changeState(INTRO, this::exitPacManDyingState, this::enterIntroState);
			} else if (currentGame.lives > 1) {
				currentGame.lives--;
				return changeState(READY, this::exitPacManDyingState, this::enterReadyState);
			} else {
				return changeState(GAME_OVER, this::exitPacManDyingState, this::enterGameOverState);
			}
		}
		if (currentGame.state.timer.running() == clock.sec(1)) {
			currentGame.ghosts().forEach(ghost -> ghost.visible = false);
			playerAnimationsAllViews().map(PlayerAnimations::playerDying).forEach(Animation::restart);
			soundsAllViews().forEach(snd -> snd.play(PacManGameSound.PACMAN_DEATH));
		}
		return currentGame.state.tick();
	}

	private void exitPacManDyingState() {
		currentGame.ghosts().forEach(ghost -> ghost.visible = true);
	}

	// GHOST_DYING

	private void enterGhostDyingState() {
		currentGame.state.timer.setDuration(clock.sec(1));
		currentGame.pac.visible = false;
		mazeAnimationsAllViews().map(MazeAnimations::energizerBlinking).forEach(Animation::restart);
		soundsAllViews().forEach(snd -> snd.play(PacManGameSound.GHOST_EATEN));
	}

	private PacManGameState runGhostDyingState() {
		if (currentGame.state.timer.expired()) {
			log("Resume state '%s' after ghost died", previousState);
			return changeState(previousState, this::exitGhostDyingState, () -> {
			});
		}
		steerPac();
		currentGame.ghosts().filter(ghost -> ghost.is(DEAD) && ghost.bounty == 0 || ghost.is(ENTERING_HOUSE))
				.forEach(ghost -> ghost.update(currentGame.level));
		return currentGame.state.tick();
	}

	private void exitGhostDyingState() {
		currentGame.pac.visible = true;
		currentGame.ghosts().forEach(ghost -> {
			if (ghost.bounty > 0) {
				ghost.bounty = 0;
				soundsAllViews().forEach(snd -> snd.loopForever(PacManGameSound.GHOST_EYES));
			}
		});
	}

	// CHANGING_LEVEL

	private void enterChangingLevelState() {
		currentGame.state.timer.setDuration(clock.sec(currentGame.level.numFlashes + 3));
		currentGame.bonus.edibleTicksLeft = currentGame.bonus.eatenTicksLeft = 0;
		currentGame.pac.speed = 0;
		soundsAllViews().forEach(SoundManager::stopAll);
	}

	private PacManGameState runChangingLevelState() {
		if (currentGame.state.timer.expired()) {
			if (intermissionScenesEnabled) {
				if (Arrays.asList(2, 5, 9, 13, 17).contains(currentGame.levelNumber)) {
					currentGame.intermissionNumber = intermissionNumber(currentGame.levelNumber);
					return changeState(INTERMISSION, this::exitChangingLevelState, this::enterIntermissionState);
				}
			}
			return changeState(READY, this::exitChangingLevelState, this::enterReadyState);
		}
		if (currentGame.state.timer.running() == clock.sec(2)) {
			currentGame.ghosts().forEach(ghost -> ghost.visible = false);
		}
		if (currentGame.state.timer.running() == clock.sec(3)) {
			mazeAnimationsAllViews().forEach(
					ma -> ma.mazeFlashing(currentGame.level.mazeNumber).repetitions(currentGame.level.numFlashes).restart());
		}
		return currentGame.state.tick();
	}

	private void exitChangingLevelState() {
		log("Level %d complete, entering level %d", currentGame.levelNumber, currentGame.levelNumber + 1);
		currentGame.enterLevel(currentGame.levelNumber + 1);
		currentGame.levelSymbols.add(currentGame.level.bonusSymbol);
		mazeAnimationsAllViews().forEach(ma -> ma.mazeFlashing(currentGame.level.mazeNumber).reset());
	}

	// GAME_OVER

	private void enterGameOverState() {
		currentGame.state.timer.setDuration(clock.sec(10));
		currentGame.ghosts().forEach(ghost -> ghost.speed = 0);
		currentGame.pac.speed = 0;
		currentGame.saveHighscore();
		ghostAnimationsAllViews().forEach(ga -> currentGame.ghosts().flatMap(ga::ghostKicking).forEach(Animation::reset));
	}

	private PacManGameState runGameOverState() {
		if (currentGame.state.timer.expired() || keyPressed("Space")) {
			return changeState(INTRO, null, this::enterIntroState);
		}
		return currentGame.state.tick();
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
		currentGame.state.timer.setDuration(Long.MAX_VALUE);
		log("Starting intermission #%d", currentGame.intermissionNumber);
	}

	private PacManGameState runIntermissionState() {
		if (currentGame.state.timer.expired()) {
			return changeState(READY, null, this::enterReadyState);
		}
		return currentGame.state.tick();
	}

	// END STATE-MACHINE

	// BEGIN STATE_MACHINE INFRASTRUCTURE

	private PacManGameState changeState(PacManGameState newState, Runnable onExit, Runnable onEntry) {
		if (currentGame.state != null) {
			log("Exit state '%s'", currentGame.stateDescription());
		}
		if (onExit != null) {
			onExit.run();
		}
		previousState = currentGame.state;
		currentGame.state = newState;
		if (onEntry != null) {
			onEntry.run();
		}
		log("Entered state '%s' for %s", currentGame.stateDescription(),
				ticksDescription(currentGame.state.timer.getDuration()));
		views.forEach(view -> view.onGameStateChanged(previousState, currentGame.state));
		return currentGame.state;
	}

	private void updateGameState() {
		switch (currentGame.state) {
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
			throw new IllegalStateException("Illegal state: " + currentGame.state);
		}
	}

	private String ticksDescription(long ticks) {
		return ticks == Long.MAX_VALUE ? "indefinite time" : ticks + " ticks";
	}

	// END STATE_MACHINE INFRASTRUCTURE

	private void score(int points) {
		if (currentGame.attractMode) {
			return;
		}
		int oldscore = currentGame.score;
		currentGame.score += points;
		if (oldscore < 10000 && currentGame.score >= 10000) {
			currentGame.lives++;
			soundsAllViews().forEach(snd -> snd.play(PacManGameSound.EXTRA_LIFE));
			log("Extra life. Now you have %d lives!", currentGame.lives);
			showFlashMessage("Extra life!", clock.sec(1));
		}
		if (currentGame.score > currentGame.highscorePoints) {
			currentGame.highscorePoints = currentGame.score;
			currentGame.highscoreLevel = currentGame.levelNumber;
		}
	}

	private void steerPac() {
		if (autopilotOn || currentGame.attractMode) {
			autopilot.run();
		} else {
			if (keyPressed("Left")) {
				currentGame.pac.wishDir = LEFT;
			} else if (keyPressed("Right")) {
				currentGame.pac.wishDir = RIGHT;
			} else if (keyPressed("Up")) {
				currentGame.pac.wishDir = UP;
			} else if (keyPressed("Down")) {
				currentGame.pac.wishDir = DOWN;
			}
		}
	}

	private void onPacFoundFood(V2i foodLocation) {
		currentGame.level.removeFood(foodLocation);
		if (currentGame.level.world.isEnergizerTile(foodLocation)) {
			currentGame.pac.starvingTicks = 0;
			currentGame.pac.restingTicksLeft = 3;
			currentGame.ghostBounty = 200;
			letPacFrightenGhosts(currentGame.level.ghostFrightenedSeconds);
			score(50);
		} else {
			currentGame.pac.starvingTicks = 0;
			currentGame.pac.restingTicksLeft = 1;
			score(10);
		}

		// Bonus gets edible?
		if (currentGame.level.eatenFoodCount() == 70 || currentGame.level.eatenFoodCount() == 170) {
			currentGame.bonus.visible = true;
			currentGame.bonus.symbol = currentGame.level.bonusSymbol;
			currentGame.bonus.points = currentGame.bonusValues[currentGame.level.bonusSymbol];
			currentGame.bonus.activate(isPlaying(PACMAN) ? clock.sec(9 + random.nextFloat()) : Long.MAX_VALUE);
			log("Bonus %s (value %d) activated", currentGame.bonusNames[currentGame.bonus.symbol], currentGame.bonus.points);
		}

		// Blinky becomes Elroy?
		if (currentGame.level.foodRemaining == currentGame.level.elroy1DotsLeft) {
			ghost(BLINKY).elroy = 1;
			log("Blinky becomes Cruise Elroy 1");
		} else if (currentGame.level.foodRemaining == currentGame.level.elroy2DotsLeft) {
			ghost(BLINKY).elroy = 2;
			log("Blinky becomes Cruise Elroy 2");
		}

		updateGhostDotCounters();
		soundsAllViews().forEach(snd -> snd.play(PacManGameSound.PACMAN_MUNCH));
	}

	private void letPacFrightenGhosts(int frightenedSec) {
		currentGame.pac.powerTicksLeft = clock.sec(frightenedSec);
		if (frightenedSec > 0) {
			log("Pac-Man got power for %d seconds", frightenedSec);
			currentGame.ghosts(HUNTING_PAC).forEach(ghost -> {
				ghost.state = FRIGHTENED;
				ghost.wishDir = ghost.dir.opposite();
				ghost.forcedDirection = true;
				ghostAnimationsAllViews().forEach(ga -> ga.ghostFrightened(ghost).forEach(Animation::restart));
				ghostAnimationsAllViews().forEach(ga -> ga.ghostFlashing(ghost).reset()); // in case flashing is active now
			});
			soundsAllViews().forEach(snd -> snd.loopForever(PacManGameSound.PACMAN_POWER));
		}
	}

	private void onPacKilled(Ghost killer) {
		log("%s killed by %s at tile %s", currentGame.pac.name, killer.name, killer.tile());
		currentGame.pac.dead = true;
		int elroyMode = ghost(BLINKY).elroy;
		if (elroyMode > 0) {
			ghost(BLINKY).elroy = -elroyMode; // negative value means "disabled"
			log("Blinky Elroy mode %d disabled", elroyMode);
		}
		currentGame.globalDotCounter = 0;
		currentGame.globalDotCounterEnabled = true;
		log("Global dot counter reset and enabled");
	}

	private int pacStarvingTimeLimit() {
		return currentGame.levelNumber < 5 ? clock.sec(4) : clock.sec(3);
	}

	// Ghosts

	private void killGhost(Ghost ghost) {
		ghost.state = DEAD;
		ghost.targetTile = currentGame.level.world.houseEntry();
		ghost.bounty = currentGame.ghostBounty;
		score(ghost.bounty);
		if (++currentGame.level.numGhostsKilled == 16) {
			score(12000);
		}
		currentGame.ghostBounty *= 2;
		log("Ghost %s killed at tile %s, Pac-Man wins %d points", ghost.name, ghost.tile(), ghost.bounty);
	}

	private void killAllGhosts() {
		currentGame.ghostBounty = 200;
		currentGame.ghosts().filter(ghost -> ghost.is(HUNTING_PAC) || ghost.is(FRIGHTENED)).forEach(this::killGhost);
	}

	private void setGhostHuntingTarget(Ghost ghost) {
		// In Ms. Pac-Man, Blinky and Pinky move randomly during the *first* scatter phase:
		if (isPlaying(MS_PACMAN) && currentGame.huntingPhase == 0 && (ghost.id == BLINKY || ghost.id == PINKY)) {
			ghost.targetTile = null;
		} else if (currentGame.inScatteringPhase() && ghost.elroy == 0) {
			ghost.targetTile = currentGame.level.world.ghostScatterTile(ghost.id);
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
			return currentGame.pac.tile();
		case PINKY: {
			V2i fourAheadPac = currentGame.pac.tile().sum(currentGame.pac.dir.vec.scaled(4));
			if (currentGame.pac.dir == UP) { // simulate overflow bug
				fourAheadPac = fourAheadPac.sum(LEFT.vec.scaled(4));
			}
			return fourAheadPac;
		}
		case INKY: {
			V2i twoAheadPac = currentGame.pac.tile().sum(currentGame.pac.dir.vec.scaled(2));
			if (currentGame.pac.dir == UP) { // simulate overflow bug
				twoAheadPac = twoAheadPac.sum(LEFT.vec.scaled(2));
			}
			return ghost(BLINKY).tile().scaled(-1).sum(twoAheadPac.scaled(2));
		}
		case CLYDE: /* A Boy Named Sue */
			return ghost(CLYDE).tile().euclideanDistance(currentGame.pac.tile()) < 8
					? currentGame.level.world.ghostScatterTile(CLYDE)
					: currentGame.pac.tile();
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
			if (currentGame.globalDotCounterEnabled && currentGame.globalDotCounter >= ghostGlobalDotLimit(ghost)) {
				releaseGhost(ghost, "Global dot counter (%d) reached limit (%d)", currentGame.globalDotCounter,
						ghostGlobalDotLimit(ghost));
			} else if (!currentGame.globalDotCounterEnabled && ghost.dotCounter >= ghostPrivateDotLimit(ghost)) {
				releaseGhost(ghost, "%s's dot counter (%d) reached limit (%d)", ghost.name, ghost.dotCounter,
						ghostPrivateDotLimit(ghost));
			} else if (currentGame.pac.starvingTicks >= pacStarvingTimeLimit()) {
				releaseGhost(ghost, "%s has been starving for %d ticks", currentGame.pac.name, currentGame.pac.starvingTicks);
				currentGame.pac.starvingTicks = 0;
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
			return currentGame.levelNumber == 1 ? 30 : 0;
		}
		if (ghost == ghost(CLYDE)) {
			return currentGame.levelNumber == 1 ? 60 : currentGame.levelNumber == 2 ? 50 : 0;
		}
		return 0;
	}

	private int ghostGlobalDotLimit(Ghost ghost) {
		return ghost == ghost(PINKY) ? 7 : ghost == ghost(INKY) ? 17 : Integer.MAX_VALUE;
	}

	private void updateGhostDotCounters() {
		if (currentGame.globalDotCounterEnabled) {
			if (ghost(CLYDE).is(LOCKED) && currentGame.globalDotCounter == 32) {
				currentGame.globalDotCounterEnabled = false;
				currentGame.globalDotCounter = 0;
				log("Global dot counter disabled and reset, Clyde was in house when counter reached 32");
			} else {
				++currentGame.globalDotCounter;
			}
		} else {
			preferredLockedGhostInHouse().ifPresent(ghost -> ++ghost.dotCounter);
		}
	}

	// Settings changes, cheats and stuff

	private void handleKeys() {
		if (currentGame.attractMode) {
			return;
		}
		boolean ready = currentGame.state == READY, hunting = currentGame.state == HUNTING;

		// A = toggle autopilot
		if (keyPressed("A")) {
			toggleAutopilot();
		}
		// E = eat all food without the energizers
		if (keyPressed("E") && hunting) {
			currentGame.level.world.tiles().filter(currentGame.level::containsFood)
					.filter(tile -> !currentGame.level.world.isEnergizerTile(tile)).forEach(currentGame.level::removeFood);
		}
		// I = toggle Pac immune/vulnerable
		if (keyPressed("I")) {
			togglePlayerImmune();
		}
		// L = add live
		if (keyPressed("L")) {
			currentGame.lives++;
		}
		// N = change to next level
		if (keyPressed("N") && (ready || hunting)) {
			changeState(CHANGING_LEVEL, this::exitHuntingState, this::enterChangingLevelState);
		}
		// X = kill all ghosts outside of ghost house
		if (keyPressed("X") && hunting) {
			killAllGhosts();
			changeState(GHOST_DYING, this::exitHuntingState, this::enterGhostDyingState);
		}
	}
}
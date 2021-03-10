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

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.GameType;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.model.common.PacManGameState;
import de.amr.games.pacman.model.mspacman.MsPacManGame;
import de.amr.games.pacman.model.pacman.PacManGame;
import de.amr.games.pacman.ui.PacManGameUI;
import de.amr.games.pacman.ui.animation.Animation;
import de.amr.games.pacman.ui.animation.MazeAnimations;
import de.amr.games.pacman.ui.animation.PacManGameAnimations;
import de.amr.games.pacman.ui.animation.PlayerAnimations;
import de.amr.games.pacman.ui.sound.PacManGameSound;
import de.amr.games.pacman.ui.sound.SoundManager;

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

	private final EnumMap<GameType, GameModel> games = new EnumMap<>(GameType.class);
	{
		games.put(PACMAN, new PacManGame());
		games.put(MS_PACMAN, new MsPacManGame());
	}

	private GameModel game;
	private PacManGameUI userInterface;

	public final Autopilot autopilot;
	private PacManGameState previousState;
	private boolean intermissionScenesEnabled = true;

	public PacManGameController(GameType initialGameType) {
		game = games.get(initialGameType);
		autopilot = new Autopilot(game);
		changeState(INTRO, null, this::enterIntroState);
		if (userInterface != null) {
			userInterface.onGameChanged(game);
		}
	}

	public void step() {
		// Quit current state and enter intro state?
		if (userInterface.keyPressed("Q")) {
			changeState(INTRO, this::exitHuntingState, this::enterIntroState);
			return;
		}
		handleCheatsAndStuff();
		if (game.state == null) {
			return;
		}
		try {
			updateGameState();
		} catch (Exception x) {
			x.printStackTrace();
		}
		userInterface.update();
	}

	private void handleCheatsAndStuff() {
		if (game.attractMode) {
			return;
		}
		boolean ready = game.state == READY, hunting = game.state == HUNTING;

		// A = toggle autopilot
		if (userInterface.keyPressed("A")) {
			toggleAutopilot();
		}
		// E = eat all food except the energizers
		if (userInterface.keyPressed("E") && hunting) {
			game.level.world.tiles().filter(game.level::containsFood).filter(tile -> !game.level.world.isEnergizerTile(tile))
					.forEach(game.level::removeFood);
		}
		// I = toggle Pac immune/vulnerable
		if (userInterface.keyPressed("I")) {
			togglePlayerImmunity();
		}
		// L = add live
		if (userInterface.keyPressed("L")) {
			game.lives++;
		}
		// N = change to next level
		if (userInterface.keyPressed("N") && (ready || hunting)) {
			changeState(CHANGING_LEVEL, this::exitHuntingState, this::enterChangingLevelState);
		}
		// X = kill all ghosts outside of ghost house
		if (userInterface.keyPressed("X") && hunting) {
			killAllGhosts();
			changeState(GHOST_DYING, this::exitHuntingState, this::enterGhostDyingState);
		}
	}

	public GameModel getGame() {
		return game;
	}

	public boolean isPlaying(GameType type) {
		return game == games.get(type);
	}

	public GameType currentlyPlaying() {
		return Stream.of(GameType.values()).filter(this::isPlaying).findFirst().get();
	}

	public void play(GameType type) {
		game = games.get(type);
		game.reset();
		changeState(INTRO, null, this::enterIntroState);
		if (userInterface != null) {
			userInterface.onGameChanged(game);
		}
	}

	public void setUserInterface(PacManGameUI ui) {
		userInterface = ui;
		userInterface.onGameChanged(game);
	}

	public void toggleGameType() {
		userInterface.sound().ifPresent(SoundManager::stopAll);
		if (isPlaying(MS_PACMAN)) {
			play(PACMAN);
			userInterface.showFlashMessage("Now playing Pac-Man", clock.sec(1));
		} else {
			play(MS_PACMAN);
			userInterface.showFlashMessage("Now playing Ms. Pac-Man", clock.sec(1));
		}
	}

	private void enableAutopilot(boolean enabled) {
		autopilot.enabled = enabled;
		String msg = "Autopilot " + (enabled ? "on" : "off");
		userInterface.showFlashMessage(msg, clock.sec(1));
		log(msg);
	}

	public void toggleAutopilot() {
		enableAutopilot(!autopilot.enabled);
	}

	public void setPlayerImmune(boolean immune) {
		game.pac.immune = immune;
		String msg = game.pac.name + " is " + (game.pac.immune ? "immune" : "vulnerable");
		userInterface.showFlashMessage(msg, clock.sec(1));
		log(msg);
	}

	public void togglePlayerImmunity() {
		setPlayerImmune(!game.pac.immune);
	}

	private Ghost ghost(int id) {
		return game.ghosts[id];
	}

	/*
	 * The finite-state machine controlling the game play:
	 */

	// INTRO

	private void enterIntroState() {
		game.reset();
		previousState = null;
		if (userInterface != null) {
			userInterface.mute(false);
			userInterface.sound().ifPresent(SoundManager::stopAll);
			userInterface.animation().ifPresent(va -> va.reset(game));
			userInterface.onGameChanged(game);
		}
		game.state.timer.reset();
		game.state.timer.start();
	}

	private PacManGameState runIntroState() {
		if (game.attractMode) {
			userInterface.mute(true);
			return changeState(READY, null, this::enterReadyState);
		}
		if (userInterface.keyPressed("Space")) {
			return changeState(READY, null, this::enterReadyState);
		}

		// test intermission scenes
		if (userInterface.keyPressed("1")) {
			game.intermissionNumber = 1;
			userInterface.showFlashMessage("Test Intermission #1", clock.sec(0.5));
			return changeState(INTERMISSION, null, this::enterIntermissionState);
		}
		if (userInterface.keyPressed("2")) {
			game.intermissionNumber = 2;
			userInterface.showFlashMessage("Test Intermission #2", clock.sec(0.5));
			return changeState(INTERMISSION, null, this::enterIntermissionState);
		}
		if (userInterface.keyPressed("3")) {
			game.intermissionNumber = 3;
			userInterface.showFlashMessage("Test Intermission #3", clock.sec(0.5));
			return changeState(INTERMISSION, null, this::enterIntermissionState);
		}

		return game.state.tick();
	}

	// READY

	private void enterReadyState() {
		game.resetGuys();
		userInterface.mute(game.attractMode);
		userInterface.animation().ifPresent(animation -> animation.reset(game));
		game.state.timer.reset();
		if (game.started || game.attractMode) {
			game.state.timer.setDuration(clock.sec(2));
		} else {
			game.state.timer.setDuration(clock.sec(4.5));
			userInterface.sound().ifPresent(snd -> snd.play(PacManGameSound.GAME_READY));
		}
		game.state.timer.start();
	}

	private PacManGameState runReadyState() {
		if (game.state.timer.expired()) {
			return changeState(PacManGameState.HUNTING, this::exitReadyState, this::enterHuntingState);
		}
		if (game.state.timer.ticksRunning() == clock.sec(0.5)) {
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
		userInterface.animation().map(PacManGameAnimations::ghostAnimations).ifPresent(ga -> {
			game.ghosts().flatMap(ga::ghostKicking).forEach(Animation::restart);
		});
	}

	// HUNTING

	static final List<PacManGameSound> SIRENS = Arrays.asList(PacManGameSound.GHOST_SIREN_1,
			PacManGameSound.GHOST_SIREN_2, PacManGameSound.GHOST_SIREN_3, PacManGameSound.GHOST_SIREN_4);

	// TODO not sure about when which siren should play
	private void startHuntingPhase(int phase) {
		game.huntingPhase = phase;
		game.state.timer.reset();
		game.state.timer.setDuration(game.getHuntingPhaseDuration(game.huntingPhase));
		game.state.timer.start();
		if (game.inScatteringPhase()) {
			userInterface.sound().ifPresent(sound -> {
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
		userInterface.animation().map(PacManGameAnimations::mazeAnimations)
				.ifPresent(ma -> ma.energizerBlinking().restart());
		userInterface.animation().map(PacManGameAnimations::playerAnimations)
				.ifPresent(pa -> pa.playerMunching(game.pac).forEach(Animation::restart));
	}

	private void exitHuntingState() {
		userInterface.animation().map(PacManGameAnimations::mazeAnimations).ifPresent(ma -> ma.energizerBlinking().reset());
	}

	private PacManGameState runHuntingState() {
		// Level completed?
		if (game.level.foodRemaining == 0) {
			return changeState(CHANGING_LEVEL, this::exitHuntingState, this::enterChangingLevelState);
		}

		// Pac killing ghost(s)?
		List<Ghost> prey = game.ghosts(FRIGHTENED).filter(game.pac::meets).collect(Collectors.toList());
		if (!prey.isEmpty()) {
			prey.forEach(this::killGhost);
			return changeState(GHOST_DYING, this::exitHuntingState, this::enterGhostDyingState);
		}

		// Pac getting killed by ghost?
		Optional<Ghost> killer = game.ghosts(HUNTING_PAC).filter(game.pac::meets).findAny();
		if (killer.isPresent() && !game.pac.immune) {
			onPacKilled(killer.get());
			return changeState(PACMAN_DYING, this::exitHuntingState, this::enterPacManDyingState);
		}

		// Hunting phase complete?
		if (game.state.timer.expired()) {
			game.ghosts(HUNTING_PAC).forEach(Ghost::forceTurningBack);
			startHuntingPhase(++game.huntingPhase);
		}

		// Can Pac move?
		steerPlayer();
		game.pac.speed = game.pac.powerTimer.running() ? game.level.pacSpeedPowered : game.level.pacSpeed;
		if (game.pac.restingTicksLeft > 0) {
			game.pac.restingTicksLeft--;
		} else {
			game.pac.tryMoving();
		}

		// Did Pac find food?
		if (game.level.containsFood(game.pac.tile())) {
			onPacFoundFood(game.pac.tile());
		} else {
			game.pac.starvingTicks++;
		}

		// Pac-Man empowered?
		if (game.pac.powerTimer.running()) {
			if (game.pac.powerTimer.ticksRemaining() == clock.sec(1)) {
				game.ghosts(FRIGHTENED).forEach(ghost -> {
					userInterface.animation().map(PacManGameAnimations::ghostAnimations).ifPresent(ga -> {
						ga.ghostFlashing(ghost).restart();
						log("Start flashing for %s", ghost.name);
					});
				});
			}
			game.pac.powerTimer.tick();
		} else if (game.pac.powerTimer.expired()) {
			game.ghosts(FRIGHTENED).forEach(ghost -> {
				ghost.state = HUNTING_PAC;
				userInterface.animation().map(PacManGameAnimations::ghostAnimations).ifPresent(ga -> {
					ga.ghostFlashing(ghost).reset();
					log("Reset flashing for %s", ghost.name);
				});
				userInterface.sound().ifPresent(sm -> {
					sm.stop(PacManGameSound.PACMAN_POWER);
				});
			});
			log("%s lost power", game.pac.name);
			game.pac.powerTimer.reset();
		}

		tryReleasingGhosts();
		game.ghosts(HUNTING_PAC).forEach(this::setGhostHuntingTarget);
		game.ghosts().forEach(ghost -> ghost.update(game.level));

		game.bonus.update();
		if (game.bonus.edibleTicksLeft > 0 && game.pac.meets(game.bonus)) {
			log("Pac-Man found bonus (%s) of value %d", game.bonusNames[game.bonus.symbol], game.bonus.points);
			game.bonus.eatAndDisplayValue(clock.sec(2));
			score(game.bonus.points);
			userInterface.sound().ifPresent(snd -> snd.play(PacManGameSound.BONUS_EATEN));
		}

		userInterface.sound().ifPresent(sm -> {
			if (game.ghosts().noneMatch(ghost -> ghost.is(DEAD))) {
				sm.stop(PacManGameSound.GHOST_EYES);
			}
		});

		userInterface.animation().map(PacManGameAnimations::mazeAnimations)
				.ifPresent(ma -> ma.energizerBlinking().animate());

		// hunting state timer is suspended when Pac has power
		if (!game.pac.powerTimer.running()) {
			game.state.tick();
		}

		return game.state;
	}

	// PACMAN_DYING

	private void enterPacManDyingState() {
		game.state.timer.reset();
		game.state.timer.setDuration(clock.sec(4));
		game.state.timer.start();
		game.pac.speed = 0;
		game.bonus.edibleTicksLeft = game.bonus.eatenTicksLeft = 0;

		userInterface.animation().map(PacManGameAnimations::ghostAnimations).ifPresent(ga -> {
			game.ghosts().flatMap(ga::ghostKicking).forEach(Animation::reset);
		});
		userInterface.sound().ifPresent(SoundManager::stopAll);
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
		if (game.state.timer.ticksRunning() == clock.sec(1)) {
			game.ghosts().forEach(ghost -> ghost.visible = false);
			userInterface.animation().map(PacManGameAnimations::playerAnimations).map(PlayerAnimations::playerDying)
					.ifPresent(da -> da.restart());
			userInterface.sound().ifPresent(snd -> snd.play(PacManGameSound.PACMAN_DEATH));
		}
		return game.state.tick();
	}

	private void exitPacManDyingState() {
		game.ghosts().forEach(ghost -> ghost.visible = true);
	}

	// GHOST_DYING

	private void enterGhostDyingState() {
		game.state.timer.reset();
		game.state.timer.setDuration(clock.sec(1));
		game.state.timer.start();
		game.pac.visible = false;

		userInterface.animation().map(PacManGameAnimations::mazeAnimations).map(MazeAnimations::energizerBlinking)
				.ifPresent(Animation::restart);
		userInterface.sound().ifPresent(snd -> snd.play(PacManGameSound.GHOST_EATEN));
	}

	private PacManGameState runGhostDyingState() {
		if (game.state.timer.expired()) {
			log("Resume state '%s' after ghost died", previousState);
			return changeState(previousState, this::exitGhostDyingState, () -> {
			});
		}
		steerPlayer();
		game.ghosts().filter(ghost -> ghost.is(DEAD) && ghost.bounty == 0 || ghost.is(ENTERING_HOUSE))
				.forEach(ghost -> ghost.update(game.level));
		return game.state.tick();
	}

	private void exitGhostDyingState() {
		game.pac.visible = true;
		game.ghosts().forEach(ghost -> {
			if (ghost.bounty > 0) {
				ghost.bounty = 0;
				userInterface.sound().ifPresent(snd -> snd.loopForever(PacManGameSound.GHOST_EYES));
			}
		});
	}

	// CHANGING_LEVEL

	private void enterChangingLevelState() {
		game.state.timer.setDuration(clock.sec(game.level.numFlashes + 3));
		game.state.timer.start();
		game.bonus.edibleTicksLeft = game.bonus.eatenTicksLeft = 0;
		game.pac.speed = 0;
		userInterface.sound().ifPresent(SoundManager::stopAll);
	}

	private PacManGameState runChangingLevelState() {
		if (game.state.timer.expired()) {
			if (intermissionScenesEnabled) {
				if (Arrays.asList(2, 5, 9, 13, 17).contains(game.levelNumber)) {
					game.intermissionNumber = intermissionNumber(game.levelNumber);
					return changeState(INTERMISSION, this::exitChangingLevelState, this::enterIntermissionState);
				}
			}
			return changeState(READY, this::exitChangingLevelState, this::enterReadyState);
		}
		if (game.state.timer.ticksRunning() == clock.sec(2)) {
			game.ghosts().forEach(ghost -> ghost.visible = false);
		}
		if (game.state.timer.ticksRunning() == clock.sec(3)) {
			userInterface.animation().map(PacManGameAnimations::mazeAnimations)
					.ifPresent(ma -> ma.mazeFlashing(game.level.mazeNumber).repetitions(game.level.numFlashes).restart());
		}
		return game.state.tick();
	}

	private void exitChangingLevelState() {
		log("Level %d complete, entering level %d", game.levelNumber, game.levelNumber + 1);
		game.enterLevel(game.levelNumber + 1);
		game.levelSymbols.add(game.level.bonusSymbol);
		userInterface.animation().map(PacManGameAnimations::mazeAnimations)
				.ifPresent(ma -> ma.mazeFlashing(game.level.mazeNumber).reset());
	}

	// GAME_OVER

	private void enterGameOverState() {
		game.state.timer.reset();
		game.state.timer.setDuration(clock.sec(10));
		game.state.timer.start();
		game.ghosts().forEach(ghost -> ghost.speed = 0);
		game.pac.speed = 0;
		game.saveHighscore();
		userInterface.animation().map(PacManGameAnimations::ghostAnimations)
				.ifPresent(ga -> game.ghosts().flatMap(ga::ghostKicking).forEach(Animation::reset));
	}

	private PacManGameState runGameOverState() {
		if (game.state.timer.expired() || userInterface.keyPressed("Space")) {
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
		game.state.timer.reset();
		game.state.timer.start();
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
		if (userInterface != null) {
			userInterface.onGameStateChanged(previousState, game.state);
		}
		return game.state;
	}

	private void updateGameState() {
		if (game.state == null) {
			throw new IllegalStateException();
		}
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
			userInterface.sound().ifPresent(snd -> snd.play(PacManGameSound.EXTRA_LIFE));
			log("Extra life. Now you have %d lives!", game.lives);
			userInterface.showFlashMessage("Extra life!", clock.sec(1));
		}
		if (game.score > game.highscorePoints) {
			game.highscorePoints = game.score;
			game.highscoreLevel = game.levelNumber;
		}
	}

	private void steerPlayer() {
		if (autopilot.enabled || game.attractMode) {
			autopilot.run(game);
			return;
		}
		if (userInterface.keyPressed("Left")) {
			game.pac.wishDir = LEFT;
		} else if (userInterface.keyPressed("Right")) {
			game.pac.wishDir = RIGHT;
		} else if (userInterface.keyPressed("Up")) {
			game.pac.wishDir = UP;
		} else if (userInterface.keyPressed("Down")) {
			game.pac.wishDir = DOWN;
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
		userInterface.sound().ifPresent(snd -> snd.play(PacManGameSound.PACMAN_MUNCH));
	}

	private void letPacFrightenGhosts(int frightenedSec) {
		game.pac.powerTimer.reset();
		game.pac.powerTimer.setDuration(clock.sec(frightenedSec));
		game.pac.powerTimer.start();
		if (frightenedSec > 0) {
			log("Pac-Man got power for %d seconds", frightenedSec);
			game.ghosts(HUNTING_PAC).forEach(ghost -> {
				ghost.state = FRIGHTENED;
				ghost.wishDir = ghost.dir.opposite();
				ghost.forcedDirection = true;
				userInterface.animation().map(PacManGameAnimations::ghostAnimations).ifPresent(ga -> {
					ga.ghostFrightened(ghost).forEach(Animation::restart);
					ga.ghostFlashing(ghost).reset();
				});
			});
			userInterface.sound().ifPresent(snd -> snd.loopForever(PacManGameSound.PACMAN_POWER));
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
		return game.levelNumber < 5 ? clock.sec(4) : clock.sec(3);
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
		case CLYDE: /* A Boy Named Sue */
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
			return game.levelNumber == 1 ? 30 : 0;
		}
		if (ghost == ghost(CLYDE)) {
			return game.levelNumber == 1 ? 60 : game.levelNumber == 2 ? 50 : 0;
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

}
package de.amr.games.pacman.controller;

import static de.amr.games.pacman.controller.PacManGameState.CHANGING_LEVEL;
import static de.amr.games.pacman.controller.PacManGameState.GAME_OVER;
import static de.amr.games.pacman.controller.PacManGameState.GHOST_DYING;
import static de.amr.games.pacman.controller.PacManGameState.HUNTING;
import static de.amr.games.pacman.controller.PacManGameState.INTERMISSION;
import static de.amr.games.pacman.controller.PacManGameState.INTRO;
import static de.amr.games.pacman.controller.PacManGameState.PACMAN_DYING;
import static de.amr.games.pacman.controller.PacManGameState.READY;
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

	/*
	 * The finite-state machine controlling the game play:
	 */
	{
		PacManGameState.INTRO.onEnter = this::enterIntroState;
		PacManGameState.INTRO.onUpdate = this::updateIntroState;
		PacManGameState.READY.onEnter = this::enterReadyState;
		PacManGameState.READY.onUpdate = this::updateReadyState;
		PacManGameState.READY.onExit = this::exitReadyState;
		PacManGameState.HUNTING.onEnter = this::enterHuntingState;
		PacManGameState.HUNTING.onUpdate = this::updateHuntingState;
		PacManGameState.HUNTING.onExit = this::exitHuntingState;
		PacManGameState.GHOST_DYING.onEnter = this::enterGhostDyingState;
		PacManGameState.GHOST_DYING.onUpdate = this::updateGhostDyingState;
		PacManGameState.GHOST_DYING.onExit = this::exitGhostDyingState;
		PacManGameState.PACMAN_DYING.onEnter = this::enterPacManDyingState;
		PacManGameState.PACMAN_DYING.onUpdate = this::updatePacManDyingState;
		PacManGameState.PACMAN_DYING.onExit = this::exitPacManDyingState;
		PacManGameState.CHANGING_LEVEL.onEnter = this::enterChangingLevelState;
		PacManGameState.CHANGING_LEVEL.onUpdate = this::updateChangingLevelState;
		PacManGameState.CHANGING_LEVEL.onExit = this::exitChangingLevelState;
		PacManGameState.GAME_OVER.onEnter = this::enterGameOverState;
		PacManGameState.GAME_OVER.onUpdate = this::updateGameOverState;
		PacManGameState.INTERMISSION.onEnter = this::enterIntermissionState;
		PacManGameState.INTERMISSION.onUpdate = this::updateIntermissionState;
	}

	private final EnumMap<GameType, GameModel> games = new EnumMap<>(GameType.class);
	{
		games.put(PACMAN, new PacManGame());
		games.put(MS_PACMAN, new MsPacManGame());
	}

	public PacManGameStateMachine fsm = new PacManGameStateMachine();
	public final Autopilot autopilot;
	public GameModel game;
	public PacManGameUI userInterface;
	public boolean playing;
	public boolean attractMode;

	public boolean intermissionScenesEnabled = true;

	public PacManGameController(GameType initialGameType) {
		game = games.get(initialGameType);
		autopilot = new Autopilot(game);
		fsm.init();
		if (userInterface != null) {
			userInterface.onGameChanged(game);
		}
	}

	public void step() {
		if (userInterface.keyPressed("Q")) {
			fsm.init();
		}
		handleCheatsAndStuff();
		fsm.updateState();
		userInterface.update();
	}

	private void handleCheatsAndStuff() {
		if (attractMode) {
			return;
		}
		boolean ready = fsm.state == READY, hunting = fsm.state == HUNTING;

		// A = toggle autopilot
		if (userInterface.keyPressed("A")) {
			enableAutopilot(!autopilot.enabled);
		}
		// E = eat all food except the energizers
		if (userInterface.keyPressed("E") && hunting) {
			game.level.world.tiles().filter(game.level::containsFood).filter(tile -> !game.level.world.isEnergizerTile(tile))
					.forEach(game.level::removeFood);
		}
		// I = toggle player's immunity against ghost bites
		if (userInterface.keyPressed("I")) {
			setPlayerImmune(!game.player.immune);
		}
		// L = add live
		if (userInterface.keyPressed("L")) {
			game.lives++;
		}
		// N = change to next level
		if (userInterface.keyPressed("N") && (ready || hunting)) {
			fsm.changeState(CHANGING_LEVEL);
		}
		// X = exterminate all ghosts outside of ghost house
		if (userInterface.keyPressed("X") && hunting) {
			killAllGhosts();
			fsm.changeState(GHOST_DYING);
		}
	}

	public void letCurrentGameStateExpire() {
		fsm.state.timer.forceExpiration();
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
		fsm.changeState(INTRO);
		if (userInterface != null) {
			userInterface.onGameChanged(game);
		}
		playing = false;
		attractMode = false;
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

	public void setPlayerImmune(boolean immune) {
		game.player.immune = immune;
		String msg = game.player.name + " is " + (game.player.immune ? "immune" : "vulnerable");
		userInterface.showFlashMessage(msg, clock.sec(1));
		log(msg);
	}

	private void enterIntroState() {
		fsm.state.timer.reset();
		fsm.state.timer.start();
		game.reset();
		attractMode = false;
		playing = false;
		if (userInterface != null) {
			userInterface.mute(false);
			userInterface.sound().ifPresent(SoundManager::stopAll);
			userInterface.animation().ifPresent(va -> va.reset(game));
			userInterface.onGameChanged(game);
		}
	}

	private void updateIntroState() {
		if (attractMode) {
			userInterface.mute(true);
			fsm.changeState(READY);
			return;
		}
		if (userInterface.keyPressed("Space")) {
			fsm.changeState(READY);
			return;
		}

		// test intermission scenes
		if (userInterface.keyPressed("1")) {
			game.intermissionNumber = 1;
			userInterface.showFlashMessage("Test Intermission #1", clock.sec(0.5));
			fsm.changeState(INTERMISSION);
			return;
		}
		if (userInterface.keyPressed("2")) {
			game.intermissionNumber = 2;
			userInterface.showFlashMessage("Test Intermission #2", clock.sec(0.5));
			fsm.changeState(INTERMISSION);
		}
		if (userInterface.keyPressed("3")) {
			game.intermissionNumber = 3;
			userInterface.showFlashMessage("Test Intermission #3", clock.sec(0.5));
			fsm.changeState(INTERMISSION);
		}
	}

	private void enterReadyState() {
		game.resetGuys();
		userInterface.mute(attractMode);
		userInterface.animation().ifPresent(animation -> animation.reset(game));
		if (playing || attractMode) {
			fsm.state.timer.reset(clock.sec(2));
		} else {
			fsm.state.timer.reset(clock.sec(4.5));
			userInterface.sound().ifPresent(snd -> snd.play(PacManGameSound.GAME_READY));
		}
		fsm.state.timer.start();
	}

	private void updateReadyState() {
		if (fsm.state.timer.hasExpired()) {
			fsm.changeState(PacManGameState.HUNTING);
			return;
		}
		if (fsm.state.timer.ticked() == clock.sec(0.5)) {
			game.player.visible = true;
			for (Ghost ghost : game.ghosts) {
				ghost.visible = true;
			}
		}
	}

	private void exitReadyState() {
		if (!attractMode) {
			playing = true;
		}
		userInterface.animation().map(PacManGameAnimations::ghostAnimations).ifPresent(ga -> {
			game.ghosts().flatMap(ga::ghostKicking).forEach(Animation::restart);
		});
	}

	static final List<PacManGameSound> SIRENS = Arrays.asList(PacManGameSound.GHOST_SIREN_1,
			PacManGameSound.GHOST_SIREN_2, PacManGameSound.GHOST_SIREN_3, PacManGameSound.GHOST_SIREN_4);

	private void startHuntingPhase(int phase) {
		game.huntingPhase = phase;
		if (game.inScatteringPhase()) {
			// TODO not sure about when which siren should play
			userInterface.sound().ifPresent(sound -> {
				if (game.huntingPhase >= 2) {
					sound.stop(SIRENS.get((game.huntingPhase - 1) / 2));
				}
				sound.loopForever(SIRENS.get(game.huntingPhase / 2));
			});
		}
		fsm.state.timer.reset(game.getHuntingPhaseDuration(game.huntingPhase));
		fsm.state.timer.start();
		log("Hunting phase %d started, state is now %s", phase, fsm.stateDescription());
	}

	private void enterHuntingState() {
		startHuntingPhase(0);
		userInterface.animation().map(PacManGameAnimations::mazeAnimations)
				.ifPresent(ma -> ma.energizerBlinking().restart());
		userInterface.animation().map(PacManGameAnimations::playerAnimations)
				.ifPresent(pa -> pa.playerMunching(game.player).forEach(Animation::restart));
	}

	private void updateHuntingState() {

		// Level completed?
		if (game.level.foodRemaining == 0) {
			fsm.changeState(CHANGING_LEVEL);
			return;
		}

		// Player killing ghost(s)?
		List<Ghost> prey = game.ghosts(FRIGHTENED).filter(game.player::meets).collect(Collectors.toList());
		if (!prey.isEmpty()) {
			prey.forEach(this::killGhost);
			fsm.changeState(GHOST_DYING);
			return;
		}

		// Player getting killed by ghost?
		Optional<Ghost> killer = game.ghosts(HUNTING_PAC).filter(game.player::meets).findAny();
		if (killer.isPresent() && !game.player.immune) {
			onPacKilled(killer.get());
			fsm.changeState(PACMAN_DYING);
			return;
		}

		// Hunting phase complete?
		if (fsm.state.timer.hasExpired()) {
			game.ghosts(HUNTING_PAC).forEach(Ghost::forceTurningBack);
			startHuntingPhase(++game.huntingPhase);
		}

		// Can Pac move?
		steerPlayer();
		game.player.speed = game.player.powerTimer.isRunning() ? game.level.pacSpeedPowered : game.level.pacSpeed;
		if (game.player.restingTicksLeft > 0) {
			game.player.restingTicksLeft--;
		} else {
			game.player.tryMoving();
		}

		// Did Pac find food?
		if (game.level.containsFood(game.player.tile())) {
			onPacFoundFood(game.player.tile());
		} else {
			game.player.starvingTicks++;
		}

		// Pac-Man empowered?
		if (game.player.powerTimer.isRunning()) {
			fsm.state.timer.stop();
			if (game.player.powerTimer.ticksRemaining() == clock.sec(1)) {
				game.ghosts(FRIGHTENED).forEach(ghost -> {
					userInterface.animation().map(PacManGameAnimations::ghostAnimations).ifPresent(ga -> {
						ga.ghostFlashing(ghost).restart();
						log("Start flashing for %s", ghost.name);
					});
				});
			}
			game.player.powerTimer.tick();
		} else if (game.player.powerTimer.hasExpired()) {
			fsm.state.timer.start();
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
			log("%s lost power", game.player.name);
			game.player.powerTimer.reset();
		}

		tryReleasingGhosts();
		game.ghosts(HUNTING_PAC).forEach(this::setGhostHuntingTarget);
		game.ghosts().forEach(ghost -> ghost.update(game.level));

		game.bonus.update();
		if (game.bonus.edibleTicksLeft > 0 && game.player.meets(game.bonus)) {
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
	}

	private void exitHuntingState() {
		userInterface.animation().map(PacManGameAnimations::mazeAnimations).ifPresent(ma -> ma.energizerBlinking().reset());
	}

	private void enterPacManDyingState() {
		fsm.state.timer.reset(clock.sec(4));
		fsm.state.timer.start();
		game.player.speed = 0;
		game.bonus.edibleTicksLeft = game.bonus.eatenTicksLeft = 0;

		userInterface.animation().map(PacManGameAnimations::ghostAnimations).ifPresent(ga -> {
			game.ghosts().flatMap(ga::ghostKicking).forEach(Animation::reset);
		});
		userInterface.sound().ifPresent(SoundManager::stopAll);
	}

	private void updatePacManDyingState() {

		if (fsm.state.timer.hasExpired()) {
			if (attractMode) {
				attractMode = false;
				fsm.changeState(INTRO);
				return;
			}
			game.lives--;
			if (game.lives == 0) {
				fsm.changeState(GAME_OVER);
			} else {
				fsm.changeState(READY);
			}
			return;
		}

		if (fsm.state.timer.ticked() == clock.sec(1))

		{
			game.ghosts().forEach(ghost -> ghost.visible = false);
			userInterface.animation().map(PacManGameAnimations::playerAnimations).map(PlayerAnimations::playerDying)
					.ifPresent(da -> da.restart());
			userInterface.sound().ifPresent(snd -> snd.play(PacManGameSound.PACMAN_DEATH));
		}
	}

	private void exitPacManDyingState() {
		game.ghosts().forEach(ghost -> ghost.visible = true);
	}

	private void enterGhostDyingState() {
		fsm.state.timer.reset(clock.sec(1));
		fsm.state.timer.start();
		game.player.visible = false;
		userInterface.animation().map(PacManGameAnimations::mazeAnimations).map(MazeAnimations::energizerBlinking)
				.ifPresent(Animation::restart);
		userInterface.sound().ifPresent(snd -> snd.play(PacManGameSound.GHOST_EATEN));
	}

	private void updateGhostDyingState() {

		if (fsm.state.timer.hasExpired()) {
			log("Resume state '%s' after ghost died", fsm.previousState);
			fsm.resumePreviousState();
			return;
		}

		steerPlayer();
		game.ghosts().filter(ghost -> ghost.is(DEAD) && ghost.bounty == 0 || ghost.is(ENTERING_HOUSE))
				.forEach(ghost -> ghost.update(game.level));
	}

	private void exitGhostDyingState() {
		game.player.visible = true;
		game.ghosts().forEach(ghost -> {
			if (ghost.bounty > 0) {
				ghost.bounty = 0;
				userInterface.sound().ifPresent(snd -> snd.loopForever(PacManGameSound.GHOST_EYES));
			}
		});
	}

	private void enterChangingLevelState() {
		game.bonus.edibleTicksLeft = game.bonus.eatenTicksLeft = 0;
		game.player.speed = 0;
		userInterface.sound().ifPresent(SoundManager::stopAll);
		fsm.state.timer.reset(clock.sec(game.level.numFlashes + 3));
		fsm.state.timer.start();
	}

	private void updateChangingLevelState() {

		if (fsm.state.timer.hasExpired()) {
			if (intermissionScenesEnabled) {
				if (Arrays.asList(2, 5, 9, 13, 17).contains(game.levelNumber)) {
					game.intermissionNumber = intermissionNumber(game.levelNumber);
					fsm.changeState(INTERMISSION);
					return;
				}
			}
			fsm.changeState(READY);
			return;
		}

		if (fsm.state.timer.ticked() == clock.sec(2)) {
			game.ghosts().forEach(ghost -> ghost.visible = false);
		}

		if (fsm.state.timer.ticked() == clock.sec(3)) {
			userInterface.animation().map(PacManGameAnimations::mazeAnimations)
					.ifPresent(ma -> ma.mazeFlashing(game.level.mazeNumber).repetitions(game.level.numFlashes).restart());
		}
	}

	private void exitChangingLevelState() {
		log("Level %d complete, entering level %d", game.levelNumber, game.levelNumber + 1);
		game.enterLevel(game.levelNumber + 1);
		game.levelSymbols.add(game.level.bonusSymbol);
		userInterface.animation().map(PacManGameAnimations::mazeAnimations)
				.ifPresent(ma -> ma.mazeFlashing(game.level.mazeNumber).reset());
	}

	private void enterGameOverState() {
		game.ghosts().forEach(ghost -> ghost.speed = 0);
		game.player.speed = 0;
		game.saveHighscore();
		userInterface.animation().map(PacManGameAnimations::ghostAnimations)
				.ifPresent(ga -> game.ghosts().flatMap(ga::ghostKicking).forEach(Animation::reset));
		fsm.state.timer.reset(clock.sec(10));
		fsm.state.timer.start();
	}

	private void updateGameOverState() {
		if (fsm.state.timer.hasExpired() || userInterface.keyPressed("Space")) {
			fsm.changeState(INTRO);
			return;
		}
	}

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
		fsm.state.timer.reset();
		fsm.state.timer.start();
		log("Starting intermission #%d", game.intermissionNumber);
	}

	private void updateIntermissionState() {
		if (fsm.state.timer.hasExpired()) {
			fsm.changeState(READY);
		}
	}

	// END STATE-MACHINE

	private void score(int points) {
		if (attractMode) {
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
		if (autopilot.enabled || attractMode) {
			autopilot.run(game);
			return;
		}
		if (userInterface.keyPressed("Left")) {
			game.player.wishDir = LEFT;
		} else if (userInterface.keyPressed("Right")) {
			game.player.wishDir = RIGHT;
		} else if (userInterface.keyPressed("Up")) {
			game.player.wishDir = UP;
		} else if (userInterface.keyPressed("Down")) {
			game.player.wishDir = DOWN;
		}
	}

	private void onPacFoundFood(V2i foodLocation) {
		game.level.removeFood(foodLocation);
		if (game.level.world.isEnergizerTile(foodLocation)) {
			game.player.starvingTicks = 0;
			game.player.restingTicksLeft = 3;
			game.ghostBounty = 200;
			letPacFrightenGhosts(game.level.ghostFrightenedSeconds);
			score(50);
			// (hunting) state timer is stopped as long as Pac-man has power
			fsm.state.timer.stop();
		} else {
			game.player.starvingTicks = 0;
			game.player.restingTicksLeft = 1;
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
			game.ghosts[BLINKY].elroy = 1;
			log("Blinky becomes Cruise Elroy 1");
		} else if (game.level.foodRemaining == game.level.elroy2DotsLeft) {
			game.ghosts[BLINKY].elroy = 2;
			log("Blinky becomes Cruise Elroy 2");
		}

		updateGhostDotCounters();
		userInterface.sound().ifPresent(snd -> snd.play(PacManGameSound.PACMAN_MUNCH));
	}

	private void letPacFrightenGhosts(int frightenedSec) {
		game.player.powerTimer.reset(clock.sec(frightenedSec));
		game.player.powerTimer.start();
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
		log("%s killed by %s at tile %s", game.player.name, killer.name, killer.tile());
		game.player.dead = true;
		int elroyMode = game.ghosts[BLINKY].elroy;
		if (elroyMode > 0) {
			game.ghosts[BLINKY].elroy = -elroyMode; // negative value means "disabled"
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
			return game.player.tile();
		case PINKY: {
			V2i fourAheadPac = game.player.tile().sum(game.player.dir.vec.scaled(4));
			if (game.player.dir == UP) { // simulate overflow bug
				fourAheadPac = fourAheadPac.sum(LEFT.vec.scaled(4));
			}
			return fourAheadPac;
		}
		case INKY: {
			V2i twoAheadPac = game.player.tile().sum(game.player.dir.vec.scaled(2));
			if (game.player.dir == UP) { // simulate overflow bug
				twoAheadPac = twoAheadPac.sum(LEFT.vec.scaled(2));
			}
			return game.ghosts[BLINKY].tile().scaled(-1).sum(twoAheadPac.scaled(2));
		}
		case CLYDE: /* A Boy Named Sue */
			return game.ghosts[CLYDE].tile().euclideanDistance(game.player.tile()) < 8
					? game.level.world.ghostScatterTile(CLYDE)
					: game.player.tile();
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
			} else if (game.player.starvingTicks >= pacStarvingTimeLimit()) {
				releaseGhost(ghost, "%s has been starving for %d ticks", game.player.name, game.player.starvingTicks);
				game.player.starvingTicks = 0;
			}
		});
	}

	private void releaseGhost(Ghost ghost, String reason, Object... args) {
		ghost.state = LEAVING_HOUSE;
		if (ghost == game.ghosts[CLYDE] && game.ghosts[BLINKY].elroy < 0) {
			game.ghosts[BLINKY].elroy -= 1; // resume Elroy mode
			log("Blinky Elroy mode %d resumed", game.ghosts[BLINKY].elroy);
		}
		log("Ghost %s released: %s", ghost.name, String.format(reason, args));
	}

	private Optional<Ghost> preferredLockedGhostInHouse() {
		return Stream.of(PINKY, INKY, CLYDE).map(id -> game.ghosts[id]).filter(ghost -> ghost.is(LOCKED)).findFirst();
	}

	private int ghostPrivateDotLimit(Ghost ghost) {
		if (ghost == game.ghosts[INKY]) {
			return game.levelNumber == 1 ? 30 : 0;
		}
		if (ghost == game.ghosts[CLYDE]) {
			return game.levelNumber == 1 ? 60 : game.levelNumber == 2 ? 50 : 0;
		}
		return 0;
	}

	private int ghostGlobalDotLimit(Ghost ghost) {
		return ghost == game.ghosts[PINKY] ? 7 : ghost == game.ghosts[INKY] ? 17 : Integer.MAX_VALUE;
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
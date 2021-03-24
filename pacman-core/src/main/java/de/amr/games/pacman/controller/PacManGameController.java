package de.amr.games.pacman.controller;

import static de.amr.games.pacman.controller.PacManGameState.GAME_OVER;
import static de.amr.games.pacman.controller.PacManGameState.GHOST_DYING;
import static de.amr.games.pacman.controller.PacManGameState.HUNTING;
import static de.amr.games.pacman.controller.PacManGameState.INTERMISSION;
import static de.amr.games.pacman.controller.PacManGameState.INTRO;
import static de.amr.games.pacman.controller.PacManGameState.LEVEL_COMPLETE;
import static de.amr.games.pacman.controller.PacManGameState.LEVEL_STARTING;
import static de.amr.games.pacman.controller.PacManGameState.PACMAN_DYING;
import static de.amr.games.pacman.controller.PacManGameState.READY;
import static de.amr.games.pacman.lib.Direction.DOWN;
import static de.amr.games.pacman.lib.Direction.LEFT;
import static de.amr.games.pacman.lib.Direction.RIGHT;
import static de.amr.games.pacman.lib.Direction.UP;
import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.model.common.GameVariant.MS_PACMAN;
import static de.amr.games.pacman.model.common.GameVariant.PACMAN;
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

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Stream;

import de.amr.games.pacman.lib.FiniteStateMachine;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.model.mspacman.MsPacManGame;
import de.amr.games.pacman.model.pacman.PacManGame;
import de.amr.games.pacman.ui.PacManGameUI;
import de.amr.games.pacman.ui.animation.PacManGameAnimations2D;
import de.amr.games.pacman.ui.animation.TimedSequence;
import de.amr.games.pacman.ui.sound.PacManGameSound;
import de.amr.games.pacman.ui.sound.SoundManager;

/**
 * Controller (in the sense of MVC) for the Pac-Man and Ms. Pac-Man game.
 * <p>
 * This is a finite-state machine with states defined in {@link PacManGameState}. The game data are
 * stored in the model of the selected game, see {@link MsPacManGame} and {@link PacManGame}. The
 * user interface is abstracted via an interface ({@link PacManGameUI}). Scene selection is not
 * controlled by this class but left to the user interface implementations.
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
public class PacManGameController extends FiniteStateMachine<PacManGameState> {

	private static final String KEY_TOGGLE_AUTOPILOT = "A";
	private static final String KEY_EAT_ALL_NORMAL_PELLETS = "E";
	private static final String KEY_TOGGLE_IMMUNITY = "I";
	private static final String KEY_ADD_LIVE = "L";
	private static final String KEY_NEXT_LEVEL = "N";
	private static final String KEY_QUIT = "Q";
	private static final String KEY_EXTERMINATE_GHOSTS = "X";
	private static final String KEY_START_PLAYING = "Space";
	private static final String KEY_PLAYER_UP = "Up";
	private static final String KEY_PLAYER_DOWN = "Down";
	private static final String KEY_PLAYER_LEFT = "Left";
	private static final String KEY_PLAYER_RIGHT = "Right";

	private static final Map<Integer, Integer> INTERMISSION_NUMBER_BY_LEVEL = Map.of(2, 1, 5, 2, 9, 3, 13, 3, 17, 3);

	private final GameModel[] gameModels = new GameModel[2];
	{
		gameModels[MS_PACMAN.ordinal()] = new MsPacManGame();
		gameModels[PACMAN.ordinal()] = new PacManGame();
	}

	private GameVariant gameVariant;
	private GameModel gameModel;

	private boolean gameRequested;
	private boolean gameRunning;
	private boolean attractMode;

	public PacManGameUI userInterface;
	public final Autopilot autopilot = new Autopilot();

	private final List<PacManGameEventListener> gameEventListeners = new ArrayList<>();

	public void addGameEventListener(PacManGameEventListener listener) {
		gameEventListeners.add(listener);
	}

	public void removeGameEventListener(PacManGameEventListener listener) {
		gameEventListeners.remove(listener);
	}

	public void fireGameEvent(PacManGameEvent gameEvent) {
		gameEventListeners.forEach(listener -> listener.accept(gameEvent));
	}

	public PacManGameController(GameVariant variant) {
		super(new EnumMap<>(PacManGameState.class), PacManGameState.values());
		configure(INTRO, this::enterIntroState, this::updateIntroState, null);
		configure(READY, this::enterReadyState, this::updateReadyState, null);
		configure(HUNTING, this::enterHuntingState, this::updateHuntingState, null);
		configure(GHOST_DYING, this::enterGhostDyingState, this::updateGhostDyingState, this::exitGhostDyingState);
		configure(PACMAN_DYING, this::enterPacManDyingState, this::updatePacManDyingState, null);
		configure(LEVEL_STARTING, this::enterLevelStartingState, this::updateLevelStartingState, null);
		configure(LEVEL_COMPLETE, this::enterLevelCompleteState, this::updateLevelCompleteState, null);
		configure(INTERMISSION, this::enterIntermissionState, this::updateIntermissionState, null);
		configure(GAME_OVER, this::enterGameOverState, this::updateGameOverState, null);
		play(variant);
	}

	public void step() {
		handleKeys();
		updateState();
	}

	public GameVariant gameVariant() {
		return gameVariant;
	}

	public void play(GameVariant variant) {
		gameVariant = variant;
		gameModel = gameModels[gameVariant.ordinal()];
		changeState(INTRO);
	}

	public boolean isPlaying(GameVariant variant) {
		return gameVariant == variant;
	}

	public void toggleGameVariant() {
		play(gameVariant == MS_PACMAN ? PACMAN : MS_PACMAN);
	}

	public GameModel game() {
		return gameModels[gameVariant.ordinal()];
	}

	public boolean isAttractMode() {
		return attractMode;
	}

	public boolean isGameRunning() {
		return gameRunning;
	}

	private void handleKeys() {
		boolean intro = state == INTRO, ready = state == READY, hunting = state == HUNTING;

		if (userInterface.keyPressed(KEY_QUIT) && !intro) {
			changeState(PacManGameState.INTRO);
			return;
		}

		// test intermission scenes (TODO remove)
		if (userInterface.keyPressed("1") && intro) {
			userInterface.showFlashMessage("Test Intermission #1");
			gameModel.intermissionNumber = 1;
			changeState(INTERMISSION);
		} else if (userInterface.keyPressed("2") && intro) {
			userInterface.showFlashMessage("Test Intermission #2");
			gameModel.intermissionNumber = 2;
			changeState(INTERMISSION);
		} else if (userInterface.keyPressed("3") && intro) {
			userInterface.showFlashMessage("Test Intermission #3");
			gameModel.intermissionNumber = 3;
			changeState(INTERMISSION);
		}

		if (!isGameRunning()) {
			return;
		}

		// toggle autopilot
		if (userInterface.keyPressed(KEY_TOGGLE_AUTOPILOT)) {
			autopilot.enabled = !autopilot.enabled;
		}

		// eat all food except the energizers
		else if (userInterface.keyPressed(KEY_EAT_ALL_NORMAL_PELLETS) && hunting) {
			gameModel.level.world.tiles().filter(gameModel.level::containsFood)
					.filter(tile -> !gameModel.level.world.isEnergizerTile(tile)).forEach(gameModel.level::removeFood);
		}

		// toggle player's immunity against ghost bites
		else if (userInterface.keyPressed(KEY_TOGGLE_IMMUNITY)) {
			gameModel.player.immune = !gameModel.player.immune;
			userInterface.showFlashMessage("Player immunity " + (gameModel.player.immune ? "ON" : "OFF"));
		}

		// add live
		else if (userInterface.keyPressed(KEY_ADD_LIVE)) {
			gameModel.lives++;
		}

		// change to next level
		else if (userInterface.keyPressed(KEY_NEXT_LEVEL) && (ready || hunting)) {
			changeState(LEVEL_COMPLETE);
		}

		// exterminate all ghosts outside of ghost house
		else if (userInterface.keyPressed(KEY_EXTERMINATE_GHOSTS) && hunting) {
			killAllGhosts();
			changeState(GHOST_DYING);
		}
	}

	private Optional<SoundManager> sound() {
		return !gameRunning || userInterface == null ? Optional.empty() : userInterface.sound();
	}

	private void enterIntroState() {
		stateTimer().reset();
		gameModel.reset();
		gameRequested = false;
		gameRunning = false;
		attractMode = false;
		autopilot.enabled = false;
	}

	private void updateIntroState() {
		if (userInterface.keyPressed(KEY_START_PLAYING)) {
			gameRequested = true;
			changeState(READY);
		} else if (stateTimer().hasExpired()) {
			attractMode = true;
			autopilot.enabled = true;
			changeState(READY);
		}
	}

	private void enterReadyState() {
		gameModel.resetGuys();
	}

	private void updateReadyState() {
		if (stateTimer().hasExpired()) {
			if (gameRequested) {
				gameRunning = true;
			}
			changeState(PacManGameState.HUNTING);
			return;
		}
		if (stateTimer().isRunningSeconds(0.5)) {
			gameModel.player.visible = true;
			for (Ghost ghost : gameModel.ghosts) {
				ghost.visible = true;
			}
		}
	}

	private void startHuntingPhase(int phase) {
		gameModel.huntingPhase = phase;
		if (inScatteringPhase()) {
			fireGameEvent(new ScatterPhaseStartedEvent(gameVariant, gameModel, phase / 2));
		}
		if (stateTimer().isStopped()) {
			stateTimer().start();
			log("Hunting phase %d continues, %d of %d ticks remaining", phase, stateTimer().ticksRemaining(),
					stateTimer().duration());
		} else {
			stateTimer().reset(gameModel.getHuntingPhaseDuration(gameModel.huntingPhase));
			stateTimer().start();
			log("Hunting phase %d starts, %d of %d ticks remaining", phase, stateTimer().ticksRemaining(),
					stateTimer().duration());
		}
	}

	public boolean inScatteringPhase() {
		return gameModel.huntingPhase % 2 == 0;
	}

	private void enterHuntingState() {
		startHuntingPhase(0);
	}

	private void updateHuntingState() {
		// Level completed?
		if (gameModel.level.foodRemaining == 0) {
			changeState(LEVEL_COMPLETE);
			return;
		}

		// Player killing ghost(s)?
		long killed = gameModel.ghosts(FRIGHTENED).filter(gameModel.player::meets).map(this::killGhost).count();
		if (killed > 0) {
			changeState(GHOST_DYING);
			return;
		}

		// Player getting killed by ghost?
		if (!gameModel.player.immune || attractMode) {
			Optional<Ghost> killer = gameModel.ghosts(HUNTING_PAC).filter(gameModel.player::meets).findAny();
			if (killer.isPresent()) {
				killPlayer(killer.get());
				changeState(PACMAN_DYING);
				return;
			}
		}

		// Hunting phase complete?
		if (stateTimer().hasExpired()) {
			gameModel.ghosts(HUNTING_PAC).forEach(Ghost::forceTurningBack);
			startHuntingPhase(++gameModel.huntingPhase);
			return;
		}

		// Let player move
		steerPlayer();
		if (gameModel.player.restingTicksLeft > 0) {
			gameModel.player.restingTicksLeft--;
		} else {
			gameModel.player.speed = gameModel.player.powerTimer.isRunning() ? gameModel.level.pacSpeedPowered
					: gameModel.level.pacSpeed;
			gameModel.player.tryMoving();
		}

		// Did player find food?
		if (gameModel.level.containsFood(gameModel.player.tile())) {
			onPlayerFoundFood(gameModel.player.tile());
		} else {
			gameModel.player.starvingTicks++;
		}

		if (gameModel.player.powerTimer.isRunning()) {
			gameModel.player.powerTimer.tick();
		} else if (gameModel.player.powerTimer.hasExpired()) {
			log("%s lost power", gameModel.player.name);
			gameModel.ghosts(FRIGHTENED).forEach(ghost -> ghost.state = HUNTING_PAC);
			gameModel.player.powerTimer.reset();
			stateTimer().start();
			fireGameEvent(new PacManLostPowerEvent(gameVariant, gameModel));
		}

		tryReleasingGhosts();
		gameModel.ghosts(HUNTING_PAC).forEach(this::setGhostHuntingTarget);

		int deadGhostsBeforeUpdate = (int) gameModel.ghosts(DEAD).count();
		gameModel.ghosts().forEach(ghost -> ghost.update(gameModel.level));
		int deadGhostsAfterUpdate = (int) gameModel.ghosts(DEAD).count();
		if (deadGhostsBeforeUpdate != deadGhostsAfterUpdate) {
			fireGameEvent(
					new DeadGhostCountChangeEvent(gameVariant, gameModel, deadGhostsBeforeUpdate, deadGhostsAfterUpdate));
		}

		gameModel.bonus.update();
		if (gameModel.bonus.edibleTicksLeft > 0 && gameModel.player.meets(gameModel.bonus)) {
			log("Pac-Man found bonus (%s) of value %d", gameModel.bonusNames[gameModel.bonus.symbol], gameModel.bonus.points);
			gameModel.bonus.eatAndDisplayValue(2 * 60);
			score(gameModel.bonus.points);
			fireGameEvent(new BonusEatenEvent(gameVariant, gameModel));
		}
	}

	private void enterPacManDyingState() {
		gameModel.player.speed = 0;
		gameModel.bonus.edibleTicksLeft = gameModel.bonus.eatenTicksLeft = 0;
		stateTimer().resetSeconds(5);
	}

	private void updatePacManDyingState() {
		if (stateTimer().hasExpired()) {
			gameModel.ghosts().forEach(ghost -> ghost.visible = true);
			changeState(attractMode ? INTRO : --gameModel.lives > 0 ? READY : GAME_OVER);
			return;
		}
	}

	private void enterGhostDyingState() {
		gameModel.player.visible = false;
		stateTimer().resetSeconds(1);
	}

	private void updateGhostDyingState() {
		if (stateTimer().hasExpired()) {
			resumePreviousState();
			return;
		}
		steerPlayer();
		gameModel.ghosts().filter(ghost -> ghost.is(DEAD) && ghost.bounty == 0 || ghost.is(ENTERING_HOUSE))
				.forEach(ghost -> ghost.update(gameModel.level));
	}

	private void exitGhostDyingState() {
		gameModel.player.visible = true;
		gameModel.ghosts(DEAD).forEach(ghost -> ghost.bounty = 0);
	}

	private void enterLevelStartingState() {
		stateTimer().reset();
		log("Level %d complete, entering level %d", gameModel.levelNumber, gameModel.levelNumber + 1);
		gameModel.enterLevel(gameModel.levelNumber + 1);
		gameModel.levelSymbols.add(gameModel.level.bonusSymbol);
	}

	private void updateLevelStartingState() {
		if (stateTimer().hasExpired()) {
			changeState(READY);
		}
	}

	private void enterLevelCompleteState() {
		gameModel.bonus.edibleTicksLeft = gameModel.bonus.eatenTicksLeft = 0;
		gameModel.player.speed = 0;
		sound().ifPresent(SoundManager::stopAll);
		stateTimer().reset();
	}

	private void updateLevelCompleteState() {
		if (stateTimer().hasExpired()) {
			if (attractMode) {
				changeState(INTRO);
			} else {
				gameModel.intermissionNumber = INTERMISSION_NUMBER_BY_LEVEL.getOrDefault(gameModel.levelNumber, 0);
				changeState(gameModel.intermissionNumber != 0 ? INTERMISSION : LEVEL_STARTING);
			}
		}
	}

	private void enterGameOverState() {
		gameRunning = false;
		gameModel.ghosts().forEach(ghost -> ghost.speed = 0);
		gameModel.player.speed = 0;
		gameModel.saveHighscore();
		stateTimer().resetSeconds(10);
	}

	private void updateGameOverState() {
		if (stateTimer().hasExpired()) {
			changeState(INTRO);
		}
	}

	private void enterIntermissionState() {
		log("Starting intermission #%d", gameModel.intermissionNumber);
		stateTimer().reset();
	}

	private void updateIntermissionState() {
		if (stateTimer().hasExpired()) {
			changeState(attractMode || !gameRunning ? INTRO : LEVEL_STARTING);
		}
	}

	// END STATE-MACHINE

	private void score(int points) {
		if (attractMode) {
			return;
		}
		int oldscore = gameModel.score;
		gameModel.score += points;
		if (oldscore < 10000 && gameModel.score >= 10000) {
			gameModel.lives++;
			sound().ifPresent(sound -> sound.play(PacManGameSound.EXTRA_LIFE));
			log("Extra life. Now you have %d lives!", gameModel.lives);
			userInterface.showFlashMessage("Extra life!");
		}
		if (gameModel.score > gameModel.highscorePoints) {
			gameModel.highscorePoints = gameModel.score;
			gameModel.highscoreLevel = gameModel.levelNumber;
		}
	}

	private void steerPlayer() {
		if (autopilot.enabled) {
			autopilot.run(gameModel);
		} else if (userInterface.keyPressed(KEY_PLAYER_LEFT)) {
			gameModel.player.wishDir = LEFT;
		} else if (userInterface.keyPressed(KEY_PLAYER_RIGHT)) {
			gameModel.player.wishDir = RIGHT;
		} else if (userInterface.keyPressed(KEY_PLAYER_UP)) {
			gameModel.player.wishDir = UP;
		} else if (userInterface.keyPressed(KEY_PLAYER_DOWN)) {
			gameModel.player.wishDir = DOWN;
		}
	}

	private void onPlayerFoundFood(V2i foodLocation) {
		gameModel.level.removeFood(foodLocation);
		if (gameModel.level.world.isEnergizerTile(foodLocation)) {
			gameModel.player.starvingTicks = 0;
			gameModel.player.restingTicksLeft = 3;
			gameModel.ghostBounty = 200;
			score(50);
			if (gameModel.level.ghostFrightenedSeconds > 0) {
				// HUNTING state timer is stopped while player has power
				stateTimer().stop();
				log("%s timer stopped", state);
				startPlayerFrighteningGhosts(gameModel.level.ghostFrightenedSeconds);
			}
		} else {
			gameModel.player.starvingTicks = 0;
			gameModel.player.restingTicksLeft = 1;
			score(10);
		}

		// Bonus gets edible?
		if (gameModel.level.eatenFoodCount() == 70 || gameModel.level.eatenFoodCount() == 170) {
			gameModel.bonus.visible = true;
			gameModel.bonus.symbol = gameModel.level.bonusSymbol;
			gameModel.bonus.points = gameModel.bonusValues[gameModel.level.bonusSymbol];
			gameModel.bonus.activate(isPlaying(PACMAN) ? (long) ((9 + new Random().nextFloat()) * 60) : Long.MAX_VALUE);
			log("Bonus %s (value %d) activated", gameModel.bonusNames[gameModel.bonus.symbol], gameModel.bonus.points);
		}

		// Blinky becomes Elroy?
		if (gameModel.level.foodRemaining == gameModel.level.elroy1DotsLeft) {
			gameModel.ghosts[BLINKY].elroy = 1;
			log("Blinky becomes Cruise Elroy 1");
		} else if (gameModel.level.foodRemaining == gameModel.level.elroy2DotsLeft) {
			gameModel.ghosts[BLINKY].elroy = 2;
			log("Blinky becomes Cruise Elroy 2");
		}

		updateGhostDotCounters();
		sound().ifPresent(sound -> sound.play(PacManGameSound.PACMAN_MUNCH));
	}

	private void startPlayerFrighteningGhosts(int seconds) {
		gameModel.ghosts(HUNTING_PAC).forEach(ghost -> {
			ghost.state = FRIGHTENED;
			ghost.wishDir = ghost.dir.opposite();
			ghost.forcedDirection = true;

			// TODO move into UI:
			// if flashing, stop. Turn blue.
			userInterface.animation().map(PacManGameAnimations2D::ghostAnimations).ifPresent(ga -> {
				ga.ghostFlashing(ghost).reset();
				ga.ghostFrightened(ghost).forEach(TimedSequence::restart);
			});
		});
		sound().ifPresent(sound -> sound.loopForever(PacManGameSound.PACMAN_POWER));

		gameModel.player.powerTimer.resetSeconds(seconds);
		gameModel.player.powerTimer.start();
		log("Pac-Man got power for %d seconds", seconds);
	}

	private void killPlayer(Ghost killer) {
		gameModel.player.dead = true;
		// Elroy mode is disabled when player gets killed
		if (gameModel.ghosts[BLINKY].elroy > 0) {
			gameModel.ghosts[BLINKY].elroy -= 1; // negative value means "disabled"
			log("Blinky Elroy mode %d has been disabled", -gameModel.ghosts[BLINKY].elroy);
		}
		gameModel.globalDotCounter = 0;
		gameModel.globalDotCounterEnabled = true;
		log("Global dot counter reset and enabled");
		log("%s was killed by %s at tile %s", gameModel.player.name, killer.name, killer.tile());
	}

	// Ghosts

	private int killGhost(Ghost ghost) {
		ghost.state = DEAD;
		ghost.targetTile = gameModel.level.world.houseEntry();
		ghost.bounty = gameModel.ghostBounty;
		score(ghost.bounty);
		if (++gameModel.level.numGhostsKilled == 16) {
			score(12000);
		}
		gameModel.ghostBounty *= 2;
		log("Ghost %s killed at tile %s, Pac-Man wins %d points", ghost.name, ghost.tile(), ghost.bounty);
		return 1;
	}

	private void killAllGhosts() {
		gameModel.ghostBounty = 200;
		gameModel.ghosts().filter(ghost -> ghost.is(HUNTING_PAC) || ghost.is(FRIGHTENED)).forEach(this::killGhost);
	}

	private void setGhostHuntingTarget(Ghost ghost) {
		// In Ms. Pac-Man, Blinky and Pinky move randomly during the *first* scatter phase:
		if (isPlaying(MS_PACMAN) && gameModel.huntingPhase == 0 && (ghost.id == BLINKY || ghost.id == PINKY)) {
			ghost.targetTile = null;
		} else if (inScatteringPhase() && ghost.elroy == 0) {
			ghost.targetTile = gameModel.level.world.ghostScatterTile(ghost.id);
		} else {
			ghost.targetTile = ghostHuntingTarget(ghost.id);
		}
	}

	/*
	 * The so called "ghost AI".
	 */
	private V2i ghostHuntingTarget(int ghostID) {
		V2i playerTile = gameModel.player.tile();
		switch (ghostID) {

		case BLINKY:
			return playerTile;

		case PINKY: {
			V2i target = playerTile.plus(gameModel.player.dir.vec.scaled(4));
			if (gameModel.player.dir == UP) {
				// simulate overflow bug
				target = target.plus(-4, 0);
			}
			return target;
		}

		case INKY: {
			V2i twoAheadPlayer = playerTile.plus(gameModel.player.dir.vec.scaled(2));
			if (gameModel.player.dir == UP) {
				// simulate overflow bug
				twoAheadPlayer = twoAheadPlayer.plus(-2, 0);
			}
			return twoAheadPlayer.scaled(2).minus(gameModel.ghosts[BLINKY].tile());
		}

		case CLYDE: /* A Boy Named Sue */
			return gameModel.ghosts[CLYDE].tile().euclideanDistance(playerTile) < 8
					? gameModel.level.world.ghostScatterTile(CLYDE)
					: playerTile;

		default:
			throw new IllegalArgumentException("Unknown ghost, id: " + ghostID);
		}
	}

	// Ghost house

	private void tryReleasingGhosts() {
		if (gameModel.ghosts[BLINKY].is(LOCKED)) {
			gameModel.ghosts[BLINKY].state = HUNTING_PAC;
		}
		preferredLockedGhostInHouse().ifPresent(ghost -> {
			if (gameModel.globalDotCounterEnabled && gameModel.globalDotCounter >= ghostGlobalDotLimit(ghost)) {
				releaseGhost(ghost, "Global dot counter (%d) reached limit (%d)", gameModel.globalDotCounter,
						ghostGlobalDotLimit(ghost));
			} else if (!gameModel.globalDotCounterEnabled && ghost.dotCounter >= ghostPrivateDotLimit(ghost)) {
				releaseGhost(ghost, "%s's dot counter (%d) reached limit (%d)", ghost.name, ghost.dotCounter,
						ghostPrivateDotLimit(ghost));
			} else if (gameModel.player.starvingTicks >= pacStarvingTimeLimit()) {
				releaseGhost(ghost, "%s has been starving for %d ticks", gameModel.player.name, gameModel.player.starvingTicks);
				gameModel.player.starvingTicks = 0;
			}
		});
	}

	private void releaseGhost(Ghost ghost, String reason, Object... args) {
		ghost.state = LEAVING_HOUSE;
		if (ghost == gameModel.ghosts[CLYDE] && gameModel.ghosts[BLINKY].elroy < 0) {
			gameModel.ghosts[BLINKY].elroy -= 1; // resume Elroy mode
			log("Blinky Elroy mode %d resumed", gameModel.ghosts[BLINKY].elroy);
		}
		log("Ghost %s released: %s", ghost.name, String.format(reason, args));
	}

	private Optional<Ghost> preferredLockedGhostInHouse() {
		return Stream.of(PINKY, INKY, CLYDE).map(id -> gameModel.ghosts[id]).filter(ghost -> ghost.is(LOCKED)).findFirst();
	}

	private int pacStarvingTimeLimit() {
		return gameModel.levelNumber < 5 ? 4 * 60 : 3 * 60;
	}

	private int ghostPrivateDotLimit(Ghost ghost) {
		if (ghost == gameModel.ghosts[INKY]) {
			return gameModel.levelNumber == 1 ? 30 : 0;
		}
		if (ghost == gameModel.ghosts[CLYDE]) {
			return gameModel.levelNumber == 1 ? 60 : gameModel.levelNumber == 2 ? 50 : 0;
		}
		return 0;
	}

	private int ghostGlobalDotLimit(Ghost ghost) {
		return ghost == gameModel.ghosts[PINKY] ? 7 : ghost == gameModel.ghosts[INKY] ? 17 : Integer.MAX_VALUE;
	}

	private void updateGhostDotCounters() {
		if (gameModel.globalDotCounterEnabled) {
			if (gameModel.ghosts[CLYDE].is(LOCKED) && gameModel.globalDotCounter == 32) {
				gameModel.globalDotCounterEnabled = false;
				gameModel.globalDotCounter = 0;
				log("Global dot counter disabled and reset, Clyde was in house when counter reached 32");
			} else {
				++gameModel.globalDotCounter;
			}
		} else {
			preferredLockedGhostInHouse().ifPresent(ghost -> ++ghost.dotCounter);
		}
	}
}
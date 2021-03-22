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

import java.util.EnumMap;
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

	private final GameModel[] games = new GameModel[2];
	{
		games[MS_PACMAN.ordinal()] = new MsPacManGame();
		games[PACMAN.ordinal()] = new PacManGame();
	}

	private GameVariant gameVariant;
	private GameModel game;

	private boolean playingRequested;
	private boolean playing;

	public PacManGameUI userInterface;
	public final Autopilot autopilot = new Autopilot();

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
		game = games[gameVariant.ordinal()];
		changeState(INTRO);
	}

	public boolean isPlaying(GameVariant variant) {
		return gameVariant == variant;
	}

	public void toggleGameVariant() {
		play(gameVariant == MS_PACMAN ? PACMAN : MS_PACMAN);
	}

	public GameModel game() {
		return games[gameVariant.ordinal()];
	}

	public boolean isPlaying() {
		return playing;
	}

	public boolean isPlayingRequested() {
		return playingRequested;
	}

	private void handleKeys() {
		boolean intro = state == INTRO, ready = state == READY, hunting = state == HUNTING;

		if (userInterface.keyPressed(KEY_QUIT) && !intro) {
			changeState(PacManGameState.INTRO);
			return;
		}

		if (!playing) {
			return;
		}

		// toggle autopilot
		if (userInterface.keyPressed(KEY_TOGGLE_AUTOPILOT)) {
			enableAutopilot(!autopilot.enabled);
		}

		// eat all food except the energizers
		else if (userInterface.keyPressed(KEY_EAT_ALL_NORMAL_PELLETS) && hunting) {
			game.level.world.tiles().filter(game.level::containsFood).filter(tile -> !game.level.world.isEnergizerTile(tile))
					.forEach(game.level::removeFood);
		}

		// toggle player's immunity against ghost bites
		else if (userInterface.keyPressed(KEY_TOGGLE_IMMUNITY)) {
			game.player.immune = !game.player.immune;
			userInterface.showFlashMessage("Player immunity " + (game.player.immune ? "ON" : "OFF"));
		}

		// add live
		else if (userInterface.keyPressed(KEY_ADD_LIVE)) {
			game.lives++;
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

		// test intermission scenes (TODO remove)
		else if (userInterface.keyPressed("1") && intro) {
			userInterface.showFlashMessage("Test Intermission #1");
			game.intermissionNumber = 1;
			changeState(INTERMISSION);
		} else if (userInterface.keyPressed("2") && intro) {
			userInterface.showFlashMessage("Test Intermission #2");
			game.intermissionNumber = 2;
			changeState(INTERMISSION);
		} else if (userInterface.keyPressed("3") && intro) {
			userInterface.showFlashMessage("Test Intermission #3");
			game.intermissionNumber = 3;
			changeState(INTERMISSION);
		}
	}

	public void letCurrentGameStateExpire() {
		timer().forceExpiration();
	}

	private void enableAutopilot(boolean enabled) {
		autopilot.enabled = enabled;
		String msg = "Autopilot " + (enabled ? "on" : "off");
		userInterface.showFlashMessage(msg);
		log(msg);
	}

	private Optional<SoundManager> sound() {
		return !playing || userInterface == null ? Optional.empty() : userInterface.sound();
	}

	private void enterIntroState() {
		sound().ifPresent(SoundManager::stopAll);
		timer().reset();
		game.reset();
		playingRequested = false;
		playing = false;
		autopilot.enabled = false;
	}

	private void updateIntroState() {
		if (userInterface.keyPressed(KEY_START_PLAYING)) {
			playingRequested = true;
			changeState(READY);
		} else if (timer().hasExpired()) {
			autopilot.enabled = true;
			changeState(READY);
		}
	}

	private void enterReadyState() {
		game.resetGuys();
	}

	private void updateReadyState() {
		if (timer().hasExpired()) {
			if (playingRequested) {
				playing = true;
				playingRequested = false;
			}
			changeState(PacManGameState.HUNTING);
			return;
		}
		if (timer().isRunningSeconds(0.5)) {
			game.player.visible = true;
			for (Ghost ghost : game.ghosts) {
				ghost.visible = true;
			}
		}
	}

	private void startHuntingPhase(int phase) {
		game.huntingPhase = phase;
		if (inScatteringPhase()) {
			// TODO not sure about when which siren should play
			sound().ifPresent(sound -> {
				if (game.huntingPhase >= 2) {
					sound.stop(PacManGameSound.SIRENS.get((game.huntingPhase - 1) / 2));
				}
				sound.loopForever(PacManGameSound.SIRENS.get(game.huntingPhase / 2));
			});
		}
		if (timer().isStopped()) {
			timer().start();
			log("Hunting phase %d continues, %d of %d ticks remaining", phase, timer().ticksRemaining(), timer().duration());
		} else {
			timer().reset(game.getHuntingPhaseDuration(game.huntingPhase));
			timer().start();
			log("Hunting phase %d starts, %d of %d ticks remaining", phase, timer().ticksRemaining(), timer().duration());
		}
	}

	public boolean inScatteringPhase() {
		return game.huntingPhase % 2 == 0;
	}

	private void enterHuntingState() {
		startHuntingPhase(0);
	}

	private void updateHuntingState() {
		// Level completed?
		if (game.level.foodRemaining == 0) {
			changeState(LEVEL_COMPLETE);
			return;
		}

		// Player killing ghost(s)?
		long killed = game.ghosts(FRIGHTENED).filter(game.player::meets).map(this::killGhost).count();
		if (killed > 0) {
			changeState(GHOST_DYING);
			return;
		}

		// Player getting killed by ghost?
		if (!game.player.immune || !playing) {
			Optional<Ghost> killer = game.ghosts(HUNTING_PAC).filter(game.player::meets).findAny();
			if (killer.isPresent()) {
				killPlayer(killer.get());
				sound().ifPresent(SoundManager::stopAll);
				changeState(PACMAN_DYING);
				return;
			}
		}

		// Hunting phase complete?
		if (timer().hasExpired()) {
			game.ghosts(HUNTING_PAC).forEach(Ghost::forceTurningBack);
			startHuntingPhase(++game.huntingPhase);
			return;
		}

		// Let player move
		steerPlayer();
		if (game.player.restingTicksLeft > 0) {
			game.player.restingTicksLeft--;
		} else {
			game.player.speed = game.player.powerTimer.isRunning() ? game.level.pacSpeedPowered : game.level.pacSpeed;
			game.player.tryMoving();
		}

		// Did player find food?
		if (game.level.containsFood(game.player.tile())) {
			onPlayerFoundFood(game.player.tile());
		} else {
			game.player.starvingTicks++;
		}

		if (game.player.powerTimer.isRunning()) {
			game.player.powerTimer.tick();
		} else if (game.player.powerTimer.hasExpired()) {
			log("%s lost power", game.player.name);
			game.ghosts(FRIGHTENED).forEach(ghost -> ghost.state = HUNTING_PAC);
			sound().ifPresent(sound -> sound.stop(PacManGameSound.PACMAN_POWER));
			game.player.powerTimer.reset();
			timer().start();
		}

		tryReleasingGhosts();
		game.ghosts(HUNTING_PAC).forEach(this::setGhostHuntingTarget);
		game.ghosts().forEach(ghost -> ghost.update(game.level));

		game.bonus.update();
		if (game.bonus.edibleTicksLeft > 0 && game.player.meets(game.bonus)) {
			log("Pac-Man found bonus (%s) of value %d", game.bonusNames[game.bonus.symbol], game.bonus.points);
			game.bonus.eatAndDisplayValue(2 * 60);
			score(game.bonus.points);
			sound().ifPresent(sound -> sound.play(PacManGameSound.BONUS_EATEN));
		}

		sound().ifPresent(sound -> {
			if (game.ghosts(DEAD).count() == 0) {
				sound.stop(PacManGameSound.GHOST_EYES);
			}
		});
	}

	private void enterPacManDyingState() {
		game.player.speed = 0;
		game.bonus.edibleTicksLeft = game.bonus.eatenTicksLeft = 0;
		timer().resetSeconds(4);
	}

	private void updatePacManDyingState() {
		if (timer().hasExpired()) {
			game.ghosts().forEach(ghost -> ghost.visible = true);
			changeState(!playing ? INTRO : --game.lives > 0 ? READY : GAME_OVER);
			return;
		}
	}

	private void enterGhostDyingState() {
		game.player.visible = false;
		sound().ifPresent(sound -> sound.play(PacManGameSound.GHOST_EATEN));
		timer().resetSeconds(1);
	}

	private void updateGhostDyingState() {
		if (timer().hasExpired()) {
			resumePreviousState();
			return;
		}
		steerPlayer();
		game.ghosts().filter(ghost -> ghost.is(DEAD) && ghost.bounty == 0 || ghost.is(ENTERING_HOUSE))
				.forEach(ghost -> ghost.update(game.level));
	}

	private void exitGhostDyingState() {
		game.player.visible = true;
		game.ghosts(DEAD).forEach(ghost -> ghost.bounty = 0);
		if (game.ghosts(DEAD).count() > 0) {
			sound().ifPresent(sound -> sound.loopForever(PacManGameSound.GHOST_EYES));
		}
	}

	private void enterLevelStartingState() {
		timer().reset();
		log("Level %d complete, entering level %d", game.levelNumber, game.levelNumber + 1);
		game.enterLevel(game.levelNumber + 1);
		game.levelSymbols.add(game.level.bonusSymbol);
	}

	private void updateLevelStartingState() {
		if (timer().hasExpired()) {
			game.player.visible = true;
			game.ghosts().forEach(ghost -> ghost.visible = true);
			changeState(READY);
		}
	}

	private void enterLevelCompleteState() {
		game.bonus.edibleTicksLeft = game.bonus.eatenTicksLeft = 0;
		game.player.speed = 0;
		sound().ifPresent(SoundManager::stopAll);
		timer().reset();
	}

	private void updateLevelCompleteState() {
		if (timer().hasExpired()) {
			if (!playing) {
				changeState(INTRO);
				return;
			}
			switch (game.levelNumber) {
			case 2:
				game.intermissionNumber = 1;
				changeState(INTERMISSION);
				return;
			case 5:
				game.intermissionNumber = 2;
				changeState(INTERMISSION);
				return;
			case 9:
			case 13:
			case 17:
				game.intermissionNumber = 3;
				changeState(INTERMISSION);
				return;
			default:
				game.intermissionNumber = 0;
				changeState(LEVEL_STARTING);
				return;
			}
		}
	}

	private void enterGameOverState() {
		game.ghosts().forEach(ghost -> ghost.speed = 0);
		game.player.speed = 0;
		game.saveHighscore();
		timer().resetSeconds(10);
	}

	private void updateGameOverState() {
		if (userInterface.keyPressed(KEY_START_PLAYING)) {
			changeState(INTRO);
		} else if (timer().hasExpired()) {
			changeState(INTRO);
		}
	}

	private void enterIntermissionState() {
		log("Starting intermission #%d", game.intermissionNumber);
		timer().reset();
	}

	private void updateIntermissionState() {
		if (timer().hasExpired()) {
			changeState(LEVEL_STARTING);
		}
	}

	// END STATE-MACHINE

	private void score(int points) {
		if (!playing) {
			return;
		}
		int oldscore = game.score;
		game.score += points;
		if (oldscore < 10000 && game.score >= 10000) {
			game.lives++;
			sound().ifPresent(sound -> sound.play(PacManGameSound.EXTRA_LIFE));
			log("Extra life. Now you have %d lives!", game.lives);
			userInterface.showFlashMessage("Extra life!");
		}
		if (game.score > game.highscorePoints) {
			game.highscorePoints = game.score;
			game.highscoreLevel = game.levelNumber;
		}
	}

	private void steerPlayer() {
		if (autopilot.enabled) {
			autopilot.run(game);
		} else if (userInterface.keyPressed(KEY_PLAYER_LEFT)) {
			game.player.wishDir = LEFT;
		} else if (userInterface.keyPressed(KEY_PLAYER_RIGHT)) {
			game.player.wishDir = RIGHT;
		} else if (userInterface.keyPressed(KEY_PLAYER_UP)) {
			game.player.wishDir = UP;
		} else if (userInterface.keyPressed(KEY_PLAYER_DOWN)) {
			game.player.wishDir = DOWN;
		}
	}

	private void onPlayerFoundFood(V2i foodLocation) {
		game.level.removeFood(foodLocation);
		if (game.level.world.isEnergizerTile(foodLocation)) {
			game.player.starvingTicks = 0;
			game.player.restingTicksLeft = 3;
			game.ghostBounty = 200;
			score(50);
			if (game.level.ghostFrightenedSeconds > 0) {
				// HUNTING state timer is stopped while player has power
				timer().stop();
				log("%s timer stopped", state);
				startPlayerFrighteningGhosts(game.level.ghostFrightenedSeconds);
			}
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
			game.bonus.activate(isPlaying(PACMAN) ? (long) ((9 + new Random().nextFloat()) * 60) : Long.MAX_VALUE);
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
		sound().ifPresent(sound -> sound.play(PacManGameSound.PACMAN_MUNCH));
	}

	private void startPlayerFrighteningGhosts(int seconds) {
		game.ghosts(HUNTING_PAC).forEach(ghost -> {
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

		game.player.powerTimer.resetSeconds(seconds);
		game.player.powerTimer.start();
		log("Pac-Man got power for %d seconds", seconds);
	}

	private void killPlayer(Ghost killer) {
		game.player.dead = true;
		// Elroy mode is disabled when player gets killed
		if (game.ghosts[BLINKY].elroy > 0) {
			game.ghosts[BLINKY].elroy -= 1; // negative value means "disabled"
			log("Blinky Elroy mode %d has been disabled", -game.ghosts[BLINKY].elroy);
		}
		game.globalDotCounter = 0;
		game.globalDotCounterEnabled = true;
		log("Global dot counter reset and enabled");
		log("%s was killed by %s at tile %s", game.player.name, killer.name, killer.tile());
	}

	// Ghosts

	private int killGhost(Ghost ghost) {
		ghost.state = DEAD;
		ghost.targetTile = game.level.world.houseEntry();
		ghost.bounty = game.ghostBounty;
		score(ghost.bounty);
		if (++game.level.numGhostsKilled == 16) {
			score(12000);
		}
		game.ghostBounty *= 2;
		log("Ghost %s killed at tile %s, Pac-Man wins %d points", ghost.name, ghost.tile(), ghost.bounty);
		return 1;
	}

	private void killAllGhosts() {
		game.ghostBounty = 200;
		game.ghosts().filter(ghost -> ghost.is(HUNTING_PAC) || ghost.is(FRIGHTENED)).forEach(this::killGhost);
	}

	private void setGhostHuntingTarget(Ghost ghost) {
		// In Ms. Pac-Man, Blinky and Pinky move randomly during the *first* scatter phase:
		if (isPlaying(MS_PACMAN) && game.huntingPhase == 0 && (ghost.id == BLINKY || ghost.id == PINKY)) {
			ghost.targetTile = null;
		} else if (inScatteringPhase() && ghost.elroy == 0) {
			ghost.targetTile = game.level.world.ghostScatterTile(ghost.id);
		} else {
			ghost.targetTile = ghostHuntingTarget(ghost.id);
		}
	}

	/*
	 * The so called "ghost AI".
	 */
	private V2i ghostHuntingTarget(int ghostID) {
		V2i playerTile = game.player.tile();
		switch (ghostID) {

		case BLINKY:
			return playerTile;

		case PINKY: {
			V2i target = playerTile.plus(game.player.dir.vec.scaled(4));
			if (game.player.dir == UP) {
				// simulate overflow bug
				target = target.plus(-4, 0);
			}
			return target;
		}

		case INKY: {
			V2i twoAheadPlayer = playerTile.plus(game.player.dir.vec.scaled(2));
			if (game.player.dir == UP) {
				// simulate overflow bug
				twoAheadPlayer = twoAheadPlayer.plus(-2, 0);
			}
			return twoAheadPlayer.scaled(2).minus(game.ghosts[BLINKY].tile());
		}

		case CLYDE: /* A Boy Named Sue */
			return game.ghosts[CLYDE].tile().euclideanDistance(playerTile) < 8 ? game.level.world.ghostScatterTile(CLYDE)
					: playerTile;

		default:
			throw new IllegalArgumentException("Unknown ghost, id: " + ghostID);
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

	private int pacStarvingTimeLimit() {
		return game.levelNumber < 5 ? 4 * 60 : 3 * 60;
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
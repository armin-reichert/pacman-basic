/*
MIT License

Copyright (c) 2021 Armin Reichert

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/
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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Stream;

import de.amr.games.pacman.controller.event.BonusActivatedEvent;
import de.amr.games.pacman.controller.event.BonusEatenEvent;
import de.amr.games.pacman.controller.event.BonusExpiredEvent;
import de.amr.games.pacman.controller.event.ExtraLifeEvent;
import de.amr.games.pacman.controller.event.GhostReturningHomeEvent;
import de.amr.games.pacman.controller.event.PacManFoundFoodEvent;
import de.amr.games.pacman.controller.event.PacManGainsPowerEvent;
import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.games.pacman.controller.event.PacManGameEventListener;
import de.amr.games.pacman.controller.event.PacManGameStateChangeEvent;
import de.amr.games.pacman.controller.event.PacManLosingPowerEvent;
import de.amr.games.pacman.controller.event.PacManLostPowerEvent;
import de.amr.games.pacman.controller.event.ScatterPhaseStartedEvent;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.FiniteStateMachine;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.AbstractGameModel;
import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.model.common.Pac;
import de.amr.games.pacman.model.mspacman.MsPacManGame;
import de.amr.games.pacman.model.pacman.PacManBonus;
import de.amr.games.pacman.model.pacman.PacManGame;
import de.amr.games.pacman.ui.PacManGameUI;

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
 * <li><a href= "https://pacman.holenet.info/#CH2_Cornering"><em>Cornering</em></a>: I do not
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
 * @see <a href= "https://gameinternals.com/understanding-pac-man-ghost-behavior">Chad Birch:
 *      Understanding ghost behavior</a>
 * @see <a href="http://superpacman.com/mspacman/">Ms. Pac-Man</a>
 */
public class PacManGameController extends FiniteStateMachine<PacManGameState> {

	private static final String KEY_START_PLAYING = "Space";
	private static final String KEY_PLAYER_UP = "Up";
	private static final String KEY_PLAYER_DOWN = "Down";
	private static final String KEY_PLAYER_LEFT = "Left";
	private static final String KEY_PLAYER_RIGHT = "Right";

	private static final Map<Integer, Integer> INTERMISSION_NUMBER_BY_LEVEL = Map.of(2, 1, 5, 2, 9, 3, 13, 3, 17, 3);

	private final AbstractGameModel[] gameModels = new AbstractGameModel[2];
	{
		gameModels[MS_PACMAN.ordinal()] = new MsPacManGame();
		gameModels[PACMAN.ordinal()] = new PacManGame();
	}

	private GameVariant gameVariant;
	private AbstractGameModel gameModel;

	private boolean gameRequested;
	private boolean gameRunning;
	private boolean attractMode;
	private boolean playerImmune;
	private int huntingPhase;

	private PacManGameUI ui;
	public final Autopilot autopilot = new Autopilot();

	private final List<PacManGameEventListener> gameEventListeners = new ArrayList<>();

	public void addGameEventListener(PacManGameEventListener listener) {
		gameEventListeners.add(listener);
		log("Added game event listener %s, num listeners=%d", listener, gameEventListeners.size());
	}

	public void removeGameEventListener(PacManGameEventListener listener) {
		gameEventListeners.remove(listener);
		log("Removed game event listener %s, num listeners=%d", listener, gameEventListeners.size());
	}

	public void fireGameEvent(PacManGameEvent gameEvent) {
		gameEventListeners.forEach(listener -> listener.onGameEvent(gameEvent));
	}

	@Override
	protected void fireStateChange(PacManGameState oldState, PacManGameState newState) {

		gameEventListeners.forEach(
				listener -> listener.onGameEvent(new PacManGameStateChangeEvent(gameVariant, gameModel, oldState, newState)));
	}

	public PacManGameController() {
		super(PacManGameState.class, PacManGameState.values());
		configure(INTRO, this::enterIntroState, this::updateIntroState, null);
		configure(READY, this::enterReadyState, this::updateReadyState, null);
		configure(HUNTING, this::enterHuntingState, this::updateHuntingState, null);
		configure(GHOST_DYING, this::enterGhostDyingState, this::updateGhostDyingState, this::exitGhostDyingState);
		configure(PACMAN_DYING, this::enterPacManDyingState, this::updatePacManDyingState, null);
		configure(LEVEL_STARTING, this::enterLevelStartingState, this::updateLevelStartingState, null);
		configure(LEVEL_COMPLETE, this::enterLevelCompleteState, this::updateLevelCompleteState, null);
		configure(INTERMISSION, this::enterIntermissionState, this::updateIntermissionState, null);
		configure(GAME_OVER, this::enterGameOverState, this::updateGameOverState, null);
	}

	public void step() {
		if (gameRunning || (attractMode && state != INTRO)) {
			steerPlayer();
		}
		updateState();
	}

	public PacManGameUI getUI() {
		return ui;
	}

	public void setUI(PacManGameUI ui) {
		if (ui != null) {
			removeGameEventListener(ui);
		}
		this.ui = ui;
		addGameEventListener(ui);
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

	public AbstractGameModel game() {
		return gameModels[gameVariant.ordinal()];
	}

	public boolean isAttractMode() {
		return attractMode;
	}

	public boolean isGameRunning() {
		return gameRunning;
	}

	public boolean isPlayerImmune() {
		return playerImmune;
	}

	public void setPlayerImmune(boolean playerImmune) {
		this.playerImmune = playerImmune;
	}

	public void killGhosts() {
		gameModel.ghostBounty = 200;
		gameModel.ghosts().filter(ghost -> ghost.is(HUNTING_PAC) || ghost.is(FRIGHTENED)).forEach(this::killGhost);
		changeState(GHOST_DYING);
	}

	public void eatAllPellets() {
		gameModel.currentLevel.world.tiles().filter(gameModel.currentLevel::containsFood)
				.filter(tile -> !gameModel.currentLevel.world.isEnergizerTile(tile))
				.forEach(gameModel.currentLevel::removeFood);
	}

	// BEGIN STATE-MACHINE METHODS

	private void enterIntroState() {
		stateTimer().reset();
		gameModel.reset();
		gameRequested = false;
		gameRunning = false;
		attractMode = false;
		autopilot.enabled = false;
	}

	private void updateIntroState() {
		if (ui.keyPressed(KEY_START_PLAYING)) {
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
		stateTimer().reset(6 * 60);
	}

	private void updateReadyState() {
		if (stateTimer().ticksRemaining() == 60) {
			gameModel.player.visible = true;
			for (Ghost ghost : gameModel.ghosts) {
				ghost.visible = true;
			}
		}
		if (stateTimer().hasExpired()) {
			if (gameRequested) {
				gameRunning = true;
			}
			changeState(PacManGameState.HUNTING);
			return;
		}
	}

	private void startHuntingPhase(int phase) {
		boolean scattering = isScatteringPhase(phase);
		huntingPhase = phase;
		if (scattering) {
			fireGameEvent(new ScatterPhaseStartedEvent(gameVariant, gameModel, phase / 2));
		}
		if (stateTimer().isStopped()) {
			stateTimer().start();
			log("Hunting phase %d (%s) continues, %d of %d ticks remaining", phase, scattering ? "Scattering" : "Chasing",
					stateTimer().ticksRemaining(), stateTimer().duration());
		} else {
			stateTimer().reset(gameModel.getHuntingPhaseDuration(huntingPhase));
			stateTimer().start();
			log("Hunting phase %d (%s) starts, %d of %d ticks remaining", phase, scattering ? "Scattering" : "Chasing",
					stateTimer().ticksRemaining(), stateTimer().duration());
		}
	}

	public static boolean isScatteringPhase(int phase) {
		return phase % 2 == 0;
	}

	private void enterHuntingState() {
		startHuntingPhase(0);
	}

	private void updateHuntingState() {
		final Pac player = gameModel.player;
		int preyCount;

		// Is level complete?
		if (gameModel.currentLevel.foodRemaining == 0) {
			changeState(LEVEL_COMPLETE);
			return;
		}

		// Is player killing ghost(s)?
		preyCount = (int) gameModel.ghosts(FRIGHTENED).filter(player::meets).count();
		if (preyCount > 0) {
			gameModel.ghosts(FRIGHTENED).filter(player::meets).forEach(this::killGhost);
			changeState(GHOST_DYING);
			return;
		}

		// Is player getting killed by any ghost?
		if (!playerImmune || attractMode) {
			Optional<Ghost> killer = gameModel.ghosts(HUNTING_PAC).filter(player::meets).findAny();
			if (killer.isPresent()) {
				log("%s got killed by %s at tile %s", player.name, killer.get().name, player.tile());
				player.dead = true;
				// Elroy mode gets disabled when player gets killed
				if (gameModel.ghosts[BLINKY].elroy > 0) {
					log("Disable Elroy mode %d for Blinky", gameModel.ghosts[BLINKY].elroy);
					gameModel.ghosts[BLINKY].elroy -= 1; // negative value means "disabled"
				}
				gameModel.globalDotCounter = 0;
				gameModel.globalDotCounterEnabled = true;
				log("Global dot counter got reset and enabled");
				changeState(PACMAN_DYING);
				return;
			}
		}

		// Is hunting phase (chasing, scattering) complete?
		if (stateTimer().hasExpired()) {
			gameModel.ghosts(HUNTING_PAC).forEach(Ghost::forceTurningBack);
			startHuntingPhase(++huntingPhase);
			return;
		}

		// Move player if possible
		if (player.restingTicksLeft > 0) {
			player.restingTicksLeft--;
		} else {
			player.speed = player.powerTimer.isRunning() ? gameModel.currentLevel.playerSpeedPowered
					: gameModel.currentLevel.playerSpeed;
			player.tryMoving();
		}

		// Did player find food?
		if (gameModel.currentLevel.containsFood(player.tile())) {
			onPlayerFoundFood(player);
		} else {
			player.starvingTicks++;
		}

		// Consume power
		if (player.powerTimer.isRunning()) {
			player.powerTimer.tick();
			if (player.powerTimer.ticksRemaining() == 60) {
				fireGameEvent(new PacManLosingPowerEvent(gameVariant, gameModel));
			}
		} else if (player.powerTimer.hasExpired()) {
			log("%s lost power", player.name);
			gameModel.ghosts(FRIGHTENED).forEach(ghost -> ghost.state = HUNTING_PAC);
			player.powerTimer.reset();
			// start HUNTING state timer again
			stateTimer().start();
			fireGameEvent(new PacManLostPowerEvent(gameVariant, gameModel));
		}

		// Update ghosts
		tryReleasingGhosts();
		gameModel.ghosts(HUNTING_PAC).forEach(this::setGhostHuntingTarget);
		gameModel.ghosts().forEach(ghost -> ghost.update(gameModel.currentLevel));

		// Update bonus
		updateBonus();
	}

	private void updateBonus() {
		boolean edibleBeforeUpdate = gameModel.bonus.edibleTicksLeft > 0;
		gameModel.bonus.update();
		if (gameModel.bonus.edibleTicksLeft == 0 && edibleBeforeUpdate) {
			fireGameEvent(new BonusExpiredEvent(gameVariant, gameModel));
		} else if (gameModel.bonus.edibleTicksLeft > 0 && gameModel.player.meets(gameModel.bonus)) {
			log("Pac-Man found bonus (%s) of value %d", gameModel.bonusNames[gameModel.bonus.symbol], gameModel.bonus.points);
			gameModel.bonus.eatAndDisplayValue(2 * 60);
			score(gameModel.bonus.points);
			fireGameEvent(new BonusEatenEvent(gameVariant, gameModel));
		}
	}

	private void enterPacManDyingState() {
		gameModel.player.speed = 0;
		gameModel.ghosts(FRIGHTENED).forEach(ghost -> ghost.state = HUNTING_PAC);
		gameModel.bonus.edibleTicksLeft = gameModel.bonus.eatenTicksLeft = 0;
		stateTimer().resetSeconds(5);// TODO
	}

	private void updatePacManDyingState() {
		if (stateTimer().hasExpired()) {
			changeState(attractMode ? INTRO : --gameModel.lives > 0 ? READY : GAME_OVER);
			return;
		}
	}

	private void enterGhostDyingState() {
		stateTimer().resetSeconds(1);
		gameModel.player.visible = false;
	}

	private void updateGhostDyingState() {
		if (stateTimer().hasExpired()) {
			resumePreviousState();
			return;
		}
		gameModel.ghosts().filter(ghost -> ghost.is(DEAD) && ghost.bounty == 0 || ghost.is(ENTERING_HOUSE))
				.forEach(ghost -> ghost.update(gameModel.currentLevel));
	}

	private void exitGhostDyingState() {
		gameModel.player.visible = true;
		gameModel.ghosts(DEAD).forEach(ghost -> ghost.bounty = 0);
		gameModel.ghosts(DEAD).forEach(ghost -> fireGameEvent(new GhostReturningHomeEvent(gameVariant, gameModel, ghost)));
	}

	private void enterLevelStartingState() {
		stateTimer().reset();
		log("Level %d complete, entering level %d", gameModel.currentLevelNumber, gameModel.currentLevelNumber + 1);
		gameModel.initLevel(gameModel.currentLevelNumber + 1);
		gameModel.levelSymbols.add(gameModel.currentLevel.bonusSymbol);
		gameModel.resetGuys();
	}

	private void updateLevelStartingState() {
		if (stateTimer().hasExpired()) {
			changeState(READY);
		}
	}

	private void enterLevelCompleteState() {
		gameModel.bonus.edibleTicksLeft = gameModel.bonus.eatenTicksLeft = 0;
		gameModel.player.speed = 0;
		stateTimer().reset();
	}

	private void updateLevelCompleteState() {
		if (stateTimer().hasExpired()) {
			if (attractMode) {
				changeState(INTRO);
			} else {
				gameModel.intermissionNumber = INTERMISSION_NUMBER_BY_LEVEL.getOrDefault(gameModel.currentLevelNumber, 0);
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
		if (gameModel.score > gameModel.highscorePoints) {
			gameModel.highscorePoints = gameModel.score;
			gameModel.highscoreLevel = gameModel.currentLevelNumber;
		}
		if (oldscore < 10000 && gameModel.score >= 10000) {
			gameModel.lives++;
			log("Extra life. Player has %d lives now", gameModel.lives);
			fireGameEvent(new ExtraLifeEvent(gameVariant, gameModel));
		}
	}

	private void steerPlayer() {
		if (autopilot.enabled) {
			autopilot.run(gameModel);
		} else if (ui.keyPressed(KEY_PLAYER_LEFT)) {
			gameModel.player.wishDir = Direction.LEFT;
		} else if (ui.keyPressed(KEY_PLAYER_RIGHT)) {
			gameModel.player.wishDir = Direction.RIGHT;
		} else if (ui.keyPressed(KEY_PLAYER_UP)) {
			gameModel.player.wishDir = Direction.UP;
		} else if (ui.keyPressed(KEY_PLAYER_DOWN)) {
			gameModel.player.wishDir = Direction.DOWN;
		}
	}

	private void onPlayerFoundFood(Pac player) {
		V2i foodLocation = player.tile();
		gameModel.currentLevel.removeFood(foodLocation);
		if (gameModel.currentLevel.world.isEnergizerTile(foodLocation)) {
			player.starvingTicks = 0;
			player.restingTicksLeft = 3;
			gameModel.ghostBounty = 200;
			score(50);
			int powerSeconds = gameModel.currentLevel.ghostFrightenedSeconds;
			if (powerSeconds > 0) {
				// stop HUNTING state timer while player has power
				stateTimer().stop();
				log("%s timer stopped", state);
				gameModel.ghosts(HUNTING_PAC).forEach(ghost -> {
					ghost.state = FRIGHTENED;
					ghost.wishDir = ghost.dir.opposite();
					ghost.forcedDirection = true;
				});
				player.powerTimer.resetSeconds(powerSeconds);
				player.powerTimer.start();
				log("%s got power for %d seconds", player.name, powerSeconds);
				fireGameEvent(new PacManGainsPowerEvent(gameVariant, gameModel));
			}
		} else {
			player.starvingTicks = 0;
			player.restingTicksLeft = 1;
			score(10);
		}

		// Bonus gets edible?
		if (gameModel.currentLevel.eatenFoodCount() == 70 || gameModel.currentLevel.eatenFoodCount() == 170) {
			final PacManBonus bonus = gameModel.bonus;
			bonus.visible = true;
			bonus.symbol = gameModel.currentLevel.bonusSymbol;
			bonus.points = gameModel.bonusValues[gameModel.currentLevel.bonusSymbol];
			bonus.activate(isPlaying(PACMAN) ? (long) ((9 + new Random().nextFloat()) * 60) : Long.MAX_VALUE);
			log("Bonus %s (value %d) activated", gameModel.bonusNames[bonus.symbol], bonus.points);
			fireGameEvent(new BonusActivatedEvent(gameVariant, gameModel));
		}

		// Blinky becomes Elroy?
		if (gameModel.currentLevel.foodRemaining == gameModel.currentLevel.elroy1DotsLeft) {
			gameModel.ghosts[BLINKY].elroy = 1;
			log("Blinky becomes Cruise Elroy 1");
		} else if (gameModel.currentLevel.foodRemaining == gameModel.currentLevel.elroy2DotsLeft) {
			gameModel.ghosts[BLINKY].elroy = 2;
			log("Blinky becomes Cruise Elroy 2");
		}

		updateGhostDotCounters();
		fireGameEvent(new PacManFoundFoodEvent(gameVariant, gameModel));
	}

	// Ghosts

	private void killGhost(Ghost ghost) {
		ghost.state = DEAD;
		ghost.targetTile = gameModel.currentLevel.world.houseEntry();
		ghost.bounty = gameModel.ghostBounty;
		score(ghost.bounty);
		if (++gameModel.currentLevel.numGhostsKilled == 16) {
			score(12000);
		}
		gameModel.ghostBounty *= 2;
		log("Ghost %s killed at tile %s, Pac-Man wins %d points", ghost.name, ghost.tile(), ghost.bounty);
	}

	private void setGhostHuntingTarget(Ghost ghost) {
		/*
		 * In Ms. Pac-Man, Blinky and Pinky move randomly during the *first* scatter phase. Some say, the
		 * origial intention had been to randomize the scatter target of *all* ghosts in Ms. Pac-Man but
		 * because of a bug, only the scatter target of Blinky and Pinky would have been affected. Who
		 * knows?
		 */
		if (isPlaying(MS_PACMAN) && huntingPhase == 0 && (ghost.id == BLINKY || ghost.id == PINKY)) {
			ghost.targetTile = null;
		} else if (isScatteringPhase(huntingPhase) && ghost.elroy == 0) {
			ghost.targetTile = gameModel.currentLevel.world.ghostScatterTile(ghost.id);
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
			if (gameModel.player.dir == Direction.UP) {
				// simulate overflow bug
				target = target.plus(-4, 0);
			}
			return target;
		}

		case INKY: {
			V2i twoAheadPlayer = playerTile.plus(gameModel.player.dir.vec.scaled(2));
			if (gameModel.player.dir == Direction.UP) {
				// simulate overflow bug
				twoAheadPlayer = twoAheadPlayer.plus(-2, 0);
			}
			return twoAheadPlayer.scaled(2).minus(gameModel.ghosts[BLINKY].tile());
		}

		case CLYDE: /* A Boy Named Sue */
			return gameModel.ghosts[CLYDE].tile().euclideanDistance(playerTile) < 8
					? gameModel.currentLevel.world.ghostScatterTile(CLYDE)
					: playerTile;

		default:
			throw new IllegalArgumentException("Unknown ghost, id: " + ghostID);
		}
	}

	// Ghost house rules

	private static int playerStarvingTimeLimit(int levelNumber) {
		return levelNumber < 5 ? 4 * 60 : 3 * 60;
	}

	private static int ghostPrivateDotLimit(int ghostID, int levelNumber) {
		if (ghostID == INKY) {
			return levelNumber == 1 ? 30 : 0;
		} else if (ghostID == CLYDE) {
			return levelNumber == 1 ? 60 : levelNumber == 2 ? 50 : 0;
		} else {
			return 0;
		}
	}

	private static int ghostGlobalDotLimit(int ghostID) {
		return ghostID == PINKY ? 7 : ghostID == INKY ? 17 : Integer.MAX_VALUE;
	}

	private void tryReleasingGhosts() {
		if (gameModel.ghosts[BLINKY].is(LOCKED)) {
			gameModel.ghosts[BLINKY].state = HUNTING_PAC;
		}
		preferredLockedGhostInHouse().ifPresent(ghost -> {
			if (gameModel.globalDotCounterEnabled && gameModel.globalDotCounter >= ghostGlobalDotLimit(ghost.id)) {
				releaseGhost(ghost, "Global dot counter (%d) reached limit (%d)", gameModel.globalDotCounter,
						ghostGlobalDotLimit(ghost.id));
			} else if (!gameModel.globalDotCounterEnabled
					&& ghost.dotCounter >= ghostPrivateDotLimit(ghost.id, gameModel.currentLevelNumber)) {
				releaseGhost(ghost, "%s's dot counter (%d) reached limit (%d)", ghost.name, ghost.dotCounter,
						ghostPrivateDotLimit(ghost.id, gameModel.currentLevelNumber));
			} else if (gameModel.player.starvingTicks >= playerStarvingTimeLimit(gameModel.currentLevelNumber)) {
				releaseGhost(ghost, "%s has been starving for %d ticks", gameModel.player.name, gameModel.player.starvingTicks);
				gameModel.player.starvingTicks = 0;
			}
		});
	}

	private void releaseGhost(Ghost ghost, String reason, Object... args) {
		ghost.state = LEAVING_HOUSE;
		if (ghost.id == CLYDE && gameModel.ghosts[BLINKY].elroy < 0) {
			gameModel.ghosts[BLINKY].elroy -= 1; // resume Elroy mode
			log("Blinky Elroy mode %d resumed", gameModel.ghosts[BLINKY].elroy);
		}
		log("Ghost %s released: %s", ghost.name, String.format(reason, args));
	}

	private Optional<Ghost> preferredLockedGhostInHouse() {
		return Stream.of(PINKY, INKY, CLYDE).map(id -> gameModel.ghosts[id]).filter(ghost -> ghost.is(LOCKED)).findFirst();
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
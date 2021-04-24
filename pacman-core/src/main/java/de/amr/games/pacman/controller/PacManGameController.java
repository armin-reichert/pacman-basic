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
import java.util.stream.Collectors;
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
import de.amr.games.pacman.model.pacman.Bonus;
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

	private static final long sec_to_ticks(double sec) {
		return Math.round(sec * 60);
	}

	private static final String KEY_START_GAME = "Space";

	private static final Map<Integer, Integer> INTERMISSION_NUMBER_BY_LEVEL = Map.of(2, 1, 5, 2, 9, 3, 13, 3, 17, 3);

	private final AbstractGameModel[] games = { new MsPacManGame(), new PacManGame() };

	private GameVariant variant;
	private AbstractGameModel game;

	private boolean gameRequested;
	private boolean gameRunning;
	private boolean attractMode;
	private boolean playerImmune;
	private int huntingPhase;

	private PacManGameUI ui;
	public final Autopilot autopilot = new Autopilot();

	private final List<PacManGameEventListener> gameEventListeners = new ArrayList<>();

	/**
	 * Configures this state machine.
	 */
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

	public void addGameEventListener(PacManGameEventListener listener) {
		gameEventListeners.add(listener);
		log("Added game event listener %s, num listeners=%d", listener, gameEventListeners.size());
	}

	public void removeGameEventListener(PacManGameEventListener listener) {
		gameEventListeners.remove(listener);
		log("Removed game event listener %s, num listeners=%d", listener, gameEventListeners.size());
	}

	public void fireGameEvent(PacManGameEvent gameEvent) {
		// copying shall avoid concurrent modification, not sure if this is really necessary
		new ArrayList<>(gameEventListeners).forEach(listener -> listener.onGameEvent(gameEvent));
	}

	/**
	 * Wraps state change into game event.
	 */
	@Override
	protected void fireStateChange(PacManGameState oldState, PacManGameState newState) {
		fireGameEvent(new PacManGameStateChangeEvent(variant, game, oldState, newState));
	}

	/**
	 * Executes a single simulation step.
	 */
	public void step() {
		if (gameRunning || (attractMode && state != INTRO)) {
			steerPlayer();
		}
		updateState();
	}

	public PacManGameUI getUI() {
		return ui;
	}

	public void setUI(PacManGameUI gameUI) {
		if (ui != null) {
			removeGameEventListener(ui);
		}
		ui = gameUI;
		addGameEventListener(ui);
	}

	public GameVariant gameVariant() {
		return variant;
	}

	public void play(GameVariant v) {
		variant = v;
		game = games[v.ordinal()];
		changeState(INTRO);
	}

	public boolean isPlaying(GameVariant v) {
		return variant == v;
	}

	public void toggleGameVariant() {
		play(variant == MS_PACMAN ? PACMAN : MS_PACMAN);
	}

	public AbstractGameModel game() {
		return games[variant.ordinal()];
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
		game.ghostBounty = 200;
		game.ghosts().filter(ghost -> ghost.is(HUNTING_PAC) || ghost.is(FRIGHTENED)).forEach(this::killGhost);
		changeState(GHOST_DYING);
	}

	public void eatAllPellets() {
		game.currentLevel.world.tiles().filter(game.currentLevel::containsFood)
				.filter(tile -> !game.currentLevel.world.isEnergizerTile(tile)).forEach(game.currentLevel::removeFood);
	}

	// BEGIN STATE-MACHINE METHODS

	private void enterIntroState() {
		stateTimer().reset();
		game.reset();
		gameRequested = false;
		gameRunning = false;
		attractMode = false;
		autopilot.enabled = false;
	}

	private void updateIntroState() {
		if (ui.keyPressed(KEY_START_GAME)) {
			gameRequested = true;
			changeState(READY);
		} else if (stateTimer().hasExpired()) {
			attractMode = true;
			autopilot.enabled = true;
			changeState(READY);
		}
	}

	private void enterReadyState() {
		game.resetGuys();
		stateTimer().reset(sec_to_ticks(6));
	}

	private void updateReadyState() {
		if (stateTimer().ticksRemaining() == sec_to_ticks(1)) {
			game.player.visible = true;
			for (Ghost ghost : game.ghosts) {
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
		huntingPhase = phase;
		boolean scattering = isScatteringPhase(phase);
		if (stateTimer().isStopped()) {
			stateTimer().start();
			log("Hunting phase %d (%s) continues, %d of %d ticks remaining", phase, scattering ? "Scattering" : "Chasing",
					stateTimer().ticksRemaining(), stateTimer().duration());
		} else {
			stateTimer().reset(game.getHuntingPhaseDuration(huntingPhase));
			stateTimer().start();
			log("Hunting phase %d (%s) starts, %d of %d ticks remaining", phase, scattering ? "Scattering" : "Chasing",
					stateTimer().ticksRemaining(), stateTimer().duration());
		}
		if (scattering) {
			fireGameEvent(new ScatterPhaseStartedEvent(variant, game, phase / 2));
		}
	}

	public static boolean isScatteringPhase(int phase) {
		return phase % 2 == 0;
	}

	private void enterHuntingState() {
		startHuntingPhase(0);
	}

	private void updateHuntingState() {
		final Pac player = game.player;

		// Is level complete?
		if (game.currentLevel.foodRemaining == 0) {
			changeState(LEVEL_COMPLETE);
			return;
		}

		// Is player killing ghost(s)?
		List<Ghost> prey = game.ghosts(FRIGHTENED).filter(player::meets).collect(Collectors.toList());
		if (prey.size() > 0) {
			prey.forEach(this::killGhost);
			changeState(GHOST_DYING);
			return;
		}

		// Is player getting killed by a ghost?
		if (!playerImmune || attractMode) {
			Optional<Ghost> killer = game.ghosts(HUNTING_PAC).filter(player::meets).findAny();
			if (killer.isPresent()) {
				log("%s got killed by %s at tile %s", player.name, killer.get().name, player.tile());
				player.dead = true;
				// Elroy mode gets disabled when player gets killed
				int elroyMode = game.ghosts[BLINKY].elroy;
				if (elroyMode > 0) {
					game.ghosts[BLINKY].elroy = -elroyMode; // negative value means "disabled"
					log("Elroy mode %d for Blinky has been disabled", elroyMode);
				}
				game.globalDotCounter = 0;
				game.globalDotCounterEnabled = true;
				log("Global dot counter got reset and enabled");
				changeState(PACMAN_DYING);
				return;
			}
		}

		// Is hunting phase (chasing, scattering) complete?
		if (stateTimer().hasExpired()) {
			game.ghosts(HUNTING_PAC).forEach(Ghost::forceTurningBack);
			startHuntingPhase(++huntingPhase);
			return;
		}

		// Did player find food?
		if (game.currentLevel.containsFood(player.tile())) {
			onPlayerFoundFood();
		} else {
			player.starvingTicks++;
		}

		// Consume power
		if (player.powerTimer.isRunning()) {
			player.powerTimer.tick();
			if (player.powerTimer.ticksRemaining() == sec_to_ticks(1)) {
				fireGameEvent(new PacManLosingPowerEvent(variant, game));
			}
		} else if (player.powerTimer.hasExpired()) {
			log("%s lost power", player.name);
			game.ghosts(FRIGHTENED).forEach(ghost -> ghost.state = HUNTING_PAC);
			player.powerTimer.reset();
			// start HUNTING state timer again
			stateTimer().start();
			fireGameEvent(new PacManLostPowerEvent(variant, game));
		}

		// Move player through world
		if (player.restingTicksLeft > 0) {
			player.restingTicksLeft--;
		} else {
			player.speed = player.powerTimer.isRunning() ? game.currentLevel.playerSpeedPowered
					: game.currentLevel.playerSpeed;
			player.tryMoving();
		}

		// Ghosts
		tryReleasingGhosts();
		game.ghosts(HUNTING_PAC).forEach(this::setTargetTile);
		game.ghosts().forEach(ghost -> ghost.update(game.currentLevel));

		// Bonus
		final Bonus bonus = game.bonus;
		final boolean edible = bonus.edibleTicksLeft > 0;
		bonus.update();
		if (edible && bonus.edibleTicksLeft == 0) {
			fireGameEvent(new BonusExpiredEvent(variant, game));
		} else if (bonus.edibleTicksLeft > 0 && player.meets(bonus)) {
			score(bonus.points);
			bonus.eaten(sec_to_ticks(2));
			log("%s found bonus (%s, value %d)", player.name, game.bonusNames[bonus.symbol], bonus.points);
			fireGameEvent(new BonusEatenEvent(variant, game));
		}
	}

	private void enterPacManDyingState() {
		game.player.speed = 0;
		game.ghosts(FRIGHTENED).forEach(ghost -> ghost.state = HUNTING_PAC);
		game.bonus.edibleTicksLeft = game.bonus.eatenTicksLeft = 0;
		stateTimer().resetSeconds(5);// TODO
	}

	private void updatePacManDyingState() {
		if (stateTimer().hasExpired()) {
			changeState(attractMode ? INTRO : --game.lives > 0 ? READY : GAME_OVER);
			return;
		}
	}

	private void enterGhostDyingState() {
		stateTimer().resetSeconds(1);
		game.player.visible = false;
	}

	private void updateGhostDyingState() {
		if (stateTimer().hasExpired()) {
			resumePreviousState();
			return;
		}
		game.ghosts().filter(ghost -> ghost.is(DEAD) && ghost.bounty == 0 || ghost.is(ENTERING_HOUSE))
				.forEach(ghost -> ghost.update(game.currentLevel));
	}

	private void exitGhostDyingState() {
		game.player.visible = true;
		game.ghosts(DEAD).forEach(ghost -> ghost.bounty = 0);
		game.ghosts(DEAD).forEach(ghost -> fireGameEvent(new GhostReturningHomeEvent(variant, game, ghost)));
	}

	private void enterLevelStartingState() {
		stateTimer().reset();
		log("Level %d complete, entering level %d", game.currentLevelNumber, game.currentLevelNumber + 1);
		game.initLevel(game.currentLevelNumber + 1);
		game.levelSymbols.add(game.currentLevel.bonusSymbol);
		game.resetGuys();
	}

	private void updateLevelStartingState() {
		if (stateTimer().hasExpired()) {
			changeState(READY);
		}
	}

	private void enterLevelCompleteState() {
		game.bonus.edibleTicksLeft = game.bonus.eatenTicksLeft = 0;
		game.player.speed = 0;
		stateTimer().reset();
	}

	private void updateLevelCompleteState() {
		if (stateTimer().hasExpired()) {
			if (attractMode) {
				changeState(INTRO);
			} else {
				game.intermissionNumber = INTERMISSION_NUMBER_BY_LEVEL.getOrDefault(game.currentLevelNumber, 0);
				changeState(game.intermissionNumber != 0 ? INTERMISSION : LEVEL_STARTING);
			}
		}
	}

	private void enterGameOverState() {
		gameRunning = false;
		game.ghosts().forEach(ghost -> ghost.speed = 0);
		game.player.speed = 0;
		game.saveHighscore();
		stateTimer().resetSeconds(10);
	}

	private void updateGameOverState() {
		if (stateTimer().hasExpired()) {
			changeState(INTRO);
		}
	}

	private void enterIntermissionState() {
		log("Starting intermission #%d", game.intermissionNumber);
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
		int oldscore = game.score;
		game.score += points;
		if (game.score > game.highscorePoints) {
			game.highscorePoints = game.score;
			game.highscoreLevel = game.currentLevelNumber;
		}
		if (oldscore < 10000 && game.score >= 10000) {
			game.lives++;
			log("Extra life. Player has %d lives now", game.lives);
			fireGameEvent(new ExtraLifeEvent(variant, game));
		}
	}

	private void steerPlayer() {
		if (autopilot.enabled) {
			autopilot.run(game);
		} else {
			ui.playerDirectionChange().ifPresent(game.player::setWishDir);
		}
	}

	private void onPlayerFoundFood() {
		final Pac player = game.player;
		V2i foodLocation = player.tile();
		game.currentLevel.removeFood(foodLocation);
		if (game.currentLevel.world.isEnergizerTile(foodLocation)) {
			player.starvingTicks = 0;
			player.restingTicksLeft = 3;
			game.ghostBounty = 200;
			score(50);
			int powerSeconds = game.currentLevel.ghostFrightenedSeconds;
			if (powerSeconds > 0) {
				// stop HUNTING state timer while player has power
				stateTimer().stop();
				log("%s timer stopped", state);
				game.ghosts(HUNTING_PAC).forEach(ghost -> {
					ghost.state = FRIGHTENED;
					ghost.setWishDir(ghost.dir().opposite());
					ghost.forced = true;
				});
				player.powerTimer.resetSeconds(powerSeconds);
				player.powerTimer.start();
				log("%s got power for %d seconds", player.name, powerSeconds);
				fireGameEvent(new PacManGainsPowerEvent(variant, game));
			}
		} else {
			player.starvingTicks = 0;
			player.restingTicksLeft = 1;
			score(10);
		}

		// Bonus gets edible?
		if (game.currentLevel.eatenFoodCount() == 70 || game.currentLevel.eatenFoodCount() == 170) {
			final Bonus bonus = game.bonus;
			bonus.visible = true;
			bonus.symbol = game.currentLevel.bonusSymbol;
			bonus.points = game.bonusValues[game.currentLevel.bonusSymbol];
			bonus.activate(isPlaying(PACMAN) ? sec_to_ticks(9 + new Random().nextFloat()) : Long.MAX_VALUE);
			log("Bonus %s (value %d) activated", game.bonusNames[bonus.symbol], bonus.points);
			fireGameEvent(new BonusActivatedEvent(variant, game));
		}

		// Blinky becomes Elroy?
		if (game.currentLevel.foodRemaining == game.currentLevel.elroy1DotsLeft) {
			game.ghosts[BLINKY].elroy = 1;
			log("Blinky becomes Cruise Elroy 1");
		} else if (game.currentLevel.foodRemaining == game.currentLevel.elroy2DotsLeft) {
			game.ghosts[BLINKY].elroy = 2;
			log("Blinky becomes Cruise Elroy 2");
		}

		updateGhostDotCounters();
		fireGameEvent(new PacManFoundFoodEvent(variant, game));
	}

	// Ghosts

	private void killGhost(Ghost ghost) {
		ghost.state = DEAD;
		ghost.targetTile = game.currentLevel.world.houseEntryLeftPart();
		ghost.bounty = game.ghostBounty;
		score(ghost.bounty);
		if (++game.currentLevel.numGhostsKilled == 16) {
			score(12000);
		}
		game.ghostBounty *= 2;
		log("Ghost %s killed at tile %s, Pac-Man wins %d points", ghost.name, ghost.tile(), ghost.bounty);
	}

	/*
	 * In Ms. Pac-Man, Blinky and Pinky move randomly during the *first* scatter phase. Some say, the
	 * original intention had been to randomize the scatter target of *all* ghosts in Ms. Pac-Man but
	 * because of a bug, only the scatter target of Blinky and Pinky would have been affected. Who
	 * knows?
	 */
	private void setTargetTile(Ghost ghost) {
		if (isPlaying(MS_PACMAN) && huntingPhase == 0 && (ghost.id == BLINKY || ghost.id == PINKY)) {
			ghost.targetTile = null;
		} else if (isScatteringPhase(huntingPhase) && ghost.elroy == 0) {
			ghost.targetTile = game.currentLevel.world.ghostScatterTile(ghost.id);
		} else {
			ghost.targetTile = ghostHuntingTarget(ghost.id);
		}
	}

	/*
	 * The so called "ghost AI".
	 */
	private V2i ghostHuntingTarget(int ghostID) {
		final V2i playerTile = game.player.tile();
		switch (ghostID) {

		case BLINKY:
			return playerTile;

		case PINKY: {
			V2i fourAheadOfPlayer = playerTile.plus(game.player.dir().vec.scaled(4));
			if (game.player.dir() == Direction.UP) { // simulate overflow bug
				fourAheadOfPlayer = fourAheadOfPlayer.plus(-4, 0);
			}
			return fourAheadOfPlayer;
		}

		case INKY: {
			V2i twoAheadOfPlayer = playerTile.plus(game.player.dir().vec.scaled(2));
			if (game.player.dir() == Direction.UP) { // simulate overflow bug
				twoAheadOfPlayer = twoAheadOfPlayer.plus(-2, 0);
			}
			return twoAheadOfPlayer.scaled(2).minus(game.ghosts[BLINKY].tile());
		}

		case CLYDE: /* A Boy Named Sue */
			return game.ghosts[CLYDE].tile().euclideanDistance(playerTile) < 8
					? game.currentLevel.world.ghostScatterTile(CLYDE)
					: playerTile;

		default:
			throw new IllegalArgumentException("Unknown ghost, id: " + ghostID);
		}
	}

	// Ghost house rules

	private static long playerStarvingTimeLimit(int levelNumber) {
		return sec_to_ticks(levelNumber < 5 ? 4 : 3);
	}

	private static int ghostPrivateDotLimit(int ghostID, int levelNumber) {
		switch (ghostID) {
		case INKY:
			return levelNumber == 1 ? 30 : 0;
		case CLYDE:
			return levelNumber == 1 ? 60 : levelNumber == 2 ? 50 : 0;
		default:
			return 0;
		}
	}

	private static int ghostGlobalDotLimit(int ghostID) {
		return ghostID == PINKY ? 7 : ghostID == INKY ? 17 : Integer.MAX_VALUE;
	}

	private void tryReleasingGhosts() {
		if (game.ghosts[BLINKY].is(LOCKED)) {
			game.ghosts[BLINKY].state = HUNTING_PAC;
		}
		preferredLockedGhostInHouse().ifPresent(ghost -> {
			if (game.globalDotCounterEnabled && game.globalDotCounter >= ghostGlobalDotLimit(ghost.id)) {
				releaseGhost(ghost, "Global dot counter (%d) reached limit (%d)", game.globalDotCounter,
						ghostGlobalDotLimit(ghost.id));
			} else if (!game.globalDotCounterEnabled
					&& ghost.dotCounter >= ghostPrivateDotLimit(ghost.id, game.currentLevelNumber)) {
				releaseGhost(ghost, "%s's dot counter (%d) reached limit (%d)", ghost.name, ghost.dotCounter,
						ghostPrivateDotLimit(ghost.id, game.currentLevelNumber));
			} else if (game.player.starvingTicks >= playerStarvingTimeLimit(game.currentLevelNumber)) {
				releaseGhost(ghost, "%s has been starving for %d ticks", game.player.name, game.player.starvingTicks);
				game.player.starvingTicks = 0;
			}
		});
	}

	private void releaseGhost(Ghost ghost, String reason, Object... args) {
		ghost.state = LEAVING_HOUSE;
		if (ghost.id == CLYDE && game.ghosts[BLINKY].elroy < 0) {
			game.ghosts[BLINKY].elroy -= 1; // resume Elroy mode
			log("Blinky Elroy mode %d resumed", game.ghosts[BLINKY].elroy);
		}
		log("Ghost %s released: %s", ghost.name, String.format(reason, args));
	}

	private Optional<Ghost> preferredLockedGhostInHouse() {
		return Stream.of(PINKY, INKY, CLYDE).map(id -> game.ghosts[id]).filter(ghost -> ghost.is(LOCKED)).findFirst();
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
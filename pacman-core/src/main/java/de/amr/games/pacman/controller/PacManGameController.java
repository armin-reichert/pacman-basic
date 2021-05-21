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
import static de.amr.games.pacman.model.common.GhostState.FRIGHTENED;
import static de.amr.games.pacman.model.common.GhostState.HUNTING_PAC;
import static java.util.function.Predicate.not;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.games.pacman.controller.event.PacManGameEvent.Info;
import de.amr.games.pacman.controller.event.PacManGameEventListener;
import de.amr.games.pacman.controller.event.PacManGameStateChangeEvent;
import de.amr.games.pacman.controller.event.ScatterPhaseStartedEvent;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.FiniteStateMachine;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.GameLevel;
import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.model.common.GhostState;
import de.amr.games.pacman.model.common.Pac;
import de.amr.games.pacman.model.common.PacManGameModel;
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

	private final PacManGameModel[] games = { new MsPacManGame(), new PacManGame() };
	private final List<PacManGameEventListener> gameEventListeners = new ArrayList<>();

	private PacManGameModel game;
	private PacManGameUI ui;
	private PlayerControl playerControl;

	private boolean gameRequested;
	private boolean gameRunning;
	private boolean attractMode;
	private boolean playerImmune;
	public int huntingPhase;

	private boolean autoControlled;
	private final Autopilot autopilot = new Autopilot(this::game);

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

	private void fireGameEvent(PacManGameEvent gameEvent) {
		gameEventListeners.forEach(listener -> listener.onGameEvent(gameEvent));
	}

	private void fireGameEvent(Info info, V2i tile) {
		fireGameEvent(new PacManGameEvent(game, info, null, tile));
	}

	/**
	 * Maps state change event to game event.
	 */
	@Override
	protected void fireStateChange(PacManGameState oldState, PacManGameState newState) {
		fireGameEvent(new PacManGameStateChangeEvent(game, oldState, newState));
	}

	private void steerPlayer() {
		if (attractMode) {
			autopilot.steer(game.player());
		} else {
			playerControl.steer(game.player());
		}
	}

	/**
	 * Executes a single simulation step.
	 */
	public void step() {
		updateState();
	}

	public PacManGameUI getUI() {
		return ui;
	}

	public void setUI(PacManGameUI gameUI) {
		if (ui != null) {
			gameEventListeners.remove(ui);
		}
		ui = gameUI;
		playerControl = ui;
		gameEventListeners.add(ui);
	}

	public void selectGame(GameVariant variant) {
		game = games[variant.ordinal()];
		changeState(INTRO);
	}

	public void startGame() {
		if (state == INTRO) {
			gameRequested = true;
			changeState(READY);
		}
	}

	public void toggleGameVariant() {
		selectGame(game().variant() == MS_PACMAN ? PACMAN : MS_PACMAN);
	}

	public PacManGameModel game() {
		return game;
	}

	public boolean isAutoControlled() {
		return autoControlled;
	}

	public void setAutoControlled(boolean autoControlled) {
		this.autoControlled = autoControlled;
		playerControl = autoControlled ? autopilot : ui;
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
		game.resetGhostBounty();
		game.ghosts().filter(ghost -> ghost.is(HUNTING_PAC) || ghost.is(FRIGHTENED)).forEach(this::killGhost);
		changeState(GHOST_DYING);
	}

	public void eatAllPellets() {
		game.currentLevel().world.tiles()//
				.filter(not(game.currentLevel().world::isEnergizerTile))//
				.forEach(game.currentLevel()::removeFood);
		fireGameEvent(Info.PLAYER_FOUND_FOOD, null);
	}

	// BEGIN STATE-MACHINE METHODS

	private void enterIntroState() {
		stateTimer().reset();
		game.reset();
		gameRequested = false;
		gameRunning = false;
		attractMode = false;
		setAutoControlled(false);
	}

	private void updateIntroState() {
		if (stateTimer().hasExpired()) {
			attractMode = true;
			playerControl = autopilot;
			changeState(READY);
		}
	}

	private void enterReadyState() {
		game.resetGuys();
		stateTimer().reset(sec_to_ticks(6));
	}

	private void updateReadyState() {
		if (stateTimer().ticksRemaining() == sec_to_ticks(1)) {
			game.player().setVisible(true);
			game.ghosts().forEach(ghost -> ghost.setVisible(true));
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
		if (!stateTimer().isStopped()) {
			// new hunting phase, not resuming stopped one
			stateTimer().reset(game.getHuntingPhaseDuration(huntingPhase));
		}
		stateTimer().start();
		boolean scattering = isScatteringPhase(phase);
		if (scattering) {
			fireGameEvent(new ScatterPhaseStartedEvent(game, phase / 2));
		}
		String phaseType = scattering ? "Scattering" : "Chasing";
		log("Hunting phase #%d (%s) started, %d of %d ticks remaining", phase, phaseType, stateTimer().ticksRemaining(),
				stateTimer().duration());
	}

	public static boolean isScatteringPhase(int phase) {
		return phase % 2 == 0;
	}

	private void enterHuntingState() {
		startHuntingPhase(0);
	}

	private void updateHuntingState() {
		final GameLevel level = game.currentLevel();
		final Pac player = game.player();

		// Is level complete?
		if (level.foodRemaining == 0) {
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
				int elroyMode = game.ghost(BLINKY).elroy;
				if (elroyMode > 0) {
					game.ghost(BLINKY).elroy = -elroyMode; // negative value means "disabled"
					log("Elroy mode %d for Blinky has been disabled", elroyMode);
				}
				game.setGlobalDotCounter(0);
				game.enableGlobalDotCounter(true);
				log("Global dot counter got reset and enabled");
				changeState(PACMAN_DYING);
				return;
			}
		}

		// Is hunting phase (chasing, scattering) complete?
		if (stateTimer().hasExpired()) {
			game.ghosts(HUNTING_PAC).forEach(Ghost::forceTurningBack);
			startHuntingPhase(++huntingPhase);
		}

		// Did player find food?
		if (level.containsFood(player.tile())) {
			onPlayerFoundFood(level, player);
		} else {
			player.starvingTicks++;
		}

		// Consume power
		if (player.powerTimer.isRunning()) {
			player.powerTimer.tick();
			if (player.powerTimer.ticksRemaining() == sec_to_ticks(1)) {
				fireGameEvent(Info.PLAYER_LOSING_POWER, player.tile());
			}
		} else if (player.powerTimer.hasExpired()) {
			log("%s lost power", player.name);
			game.ghosts(FRIGHTENED).forEach(ghost -> ghost.state = HUNTING_PAC);
			player.powerTimer.reset();
			// start HUNTING state timer again
			stateTimer().start();
			fireGameEvent(Info.PLAYER_LOST_POWER, player.tile());
		}

		// Move player through world
		steerPlayer();
		if (player.restingTicksLeft > 0) {
			player.restingTicksLeft--;
		} else {
			player.setSpeed(player.powerTimer.isRunning() ? level.playerSpeedPowered : level.playerSpeed);
			player.tryMoving();
		}

		// Ghosts
		tryReleasingLockedGhosts();
		game.ghosts().forEach(this::updateGhost);

		// Bonus
		final Bonus bonus = game.bonus();
		final Info info = bonus.update();
		if (info == Info.BONUS_EXPIRED) {
			fireGameEvent(Info.BONUS_EXPIRED, bonus.tile());
		} else if (bonus.state == Bonus.EDIBLE && player.meets(bonus)) {
			score(bonus.points);
			bonus.eaten(sec_to_ticks(2));
			log("%s found bonus (%s, value %d)", player.name, bonus.symbol, bonus.points);
			fireGameEvent(Info.BONUS_EATEN, bonus.tile());
		}
	}

	private void enterPacManDyingState() {
		game.player().setSpeed(0);
		game.ghosts(FRIGHTENED).forEach(ghost -> ghost.state = HUNTING_PAC);
		game.bonus().init();
		stateTimer().resetSeconds(5); // TODO
	}

	private void updatePacManDyingState() {
		if (stateTimer().hasExpired()) {
			game.removeLife();
			changeState(attractMode ? INTRO : game.lives() > 0 ? READY : GAME_OVER);
			return;
		}
	}

	private void enterGhostDyingState() {
		stateTimer().resetSeconds(1);
		game.player().setVisible(false);
	}

	private void updateGhostDyingState() {
		if (stateTimer().hasExpired()) {
			resumePreviousState();
			return;
		}
		steerPlayer();
		game.ghosts().filter(ghost -> ghost.is(DEAD) && ghost.bounty == 0 || ghost.is(GhostState.ENTERING_HOUSE))
				.forEach(this::updateGhost);
	}

	private void exitGhostDyingState() {
		game.player().setVisible(true);
		// fire event only for ghosts that have just been killed, not for dead ghosts that are already
		// returning home
		game.ghosts(DEAD).filter(ghost -> ghost.bounty != 0).forEach(ghost -> {
			ghost.bounty = 0;
			fireGameEvent(new PacManGameEvent(game, Info.GHOST_RETURNS_HOME, ghost, null));
		});
	}

	private void enterLevelStartingState() {
		stateTimer().reset();
		log("Level %d complete, entering level %d", game.currentLevel().number, game.currentLevel().number + 1);
		game.createLevel(game.currentLevel().number + 1);
		game.countLevel();
		game.resetGuys();
	}

	private void updateLevelStartingState() {
		if (stateTimer().hasExpired()) {
			changeState(READY);
		}
	}

	private void enterLevelCompleteState() {
		game.bonus().init();
		game.player().setSpeed(0);
		stateTimer().reset();
	}

	private void updateLevelCompleteState() {
		if (stateTimer().hasExpired()) {
			if (attractMode) {
				changeState(INTRO);
			} else {
				changeState(game.intermissionNumber() != 0 ? INTERMISSION : LEVEL_STARTING);
			}
		}
	}

	private void enterGameOverState() {
		gameRunning = false;
		game.ghosts().forEach(ghost -> ghost.setSpeed(0));
		game.player().setSpeed(0);
		game.saveHiscore();
		stateTimer().resetSeconds(5);
	}

	private void updateGameOverState() {
		if (stateTimer().hasExpired()) {
			changeState(INTRO);
		}
	}

	private void enterIntermissionState() {
		log("Starting intermission #%d", game.intermissionNumber());
		stateTimer().reset(); // UI triggers timeout
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
		int oldscore = game.score();
		game.addScore(points);
		if (game.score() > game.hiscorePoints()) {
			game.setHiscorePoints(game.score());
			game.setHiscoreLevel(game.currentLevel().number);
		}
		if (oldscore < 10000 && game.score() >= 10000) {
			game.addLife();
			log("Extra life. Player has %d lives now", game.lives());
			fireGameEvent(Info.EXTRA_LIFE, null);
		}
	}

	private void onPlayerFoundFood(GameLevel level, Pac player) {
		level.removeFood(player.tile());
		player.starvingTicks = 0;
		if (level.world.isEnergizerTile(player.tile())) {
			player.restingTicksLeft = 3;
			score(game.energizerValue());
			game.resetGhostBounty();
			if (level.ghostFrightenedSeconds > 0) {
				game.ghosts(HUNTING_PAC).forEach(ghost -> {
					ghost.state = FRIGHTENED;
					ghost.forceTurningBack();
				});
				player.powerTimer.resetSeconds(level.ghostFrightenedSeconds);
				player.powerTimer.start();
				log("%s got power for %d seconds", player.name, level.ghostFrightenedSeconds);
				// HUNTING state timer is stopped while player has power
				stateTimer().stop();
				log("%s timer stopped", state);
				fireGameEvent(Info.PLAYER_GAINS_POWER, player.tile());
			}
		} else {
			player.restingTicksLeft = 1;
			score(game.pelletValue());
		}

		// Bonus becomes edible?
		if (level.eatenFoodCount() == PacManGameModel.FIRST_BONUS_PELLETS_EATEN
				|| level.eatenFoodCount() == PacManGameModel.SECOND_BONUS_PELLETS_EATEN) {
			final Bonus bonus = game.bonus();
			final long bonusTicks = game.variant() == PACMAN ? sec_to_ticks(9 + new Random().nextFloat()) : Long.MAX_VALUE;
			bonus.symbol = level.bonusSymbol;
			bonus.points = game.bonusValue(bonus.symbol);
			bonus.activate(bonusTicks);
			log("Bonus %s (value %d) activated for %d ticks", bonus.symbol, bonus.points, bonusTicks);
			fireGameEvent(Info.BONUS_ACTIVATED, bonus.tile());
		}

		// Blinky becomes Elroy?
		if (level.foodRemaining == level.elroy1DotsLeft) {
			game.ghost(BLINKY).elroy = 1;
			log("Blinky becomes Cruise Elroy 1");
		} else if (level.foodRemaining == level.elroy2DotsLeft) {
			game.ghost(BLINKY).elroy = 2;
			log("Blinky becomes Cruise Elroy 2");
		}

		updateGhostDotCounters();
		fireGameEvent(Info.PLAYER_FOUND_FOOD, player.tile());
	}

	// Ghosts

	/**
	 * Updates ghost's speed and behavior depending on its current state.
	 * 
	 * TODO: not sure about correct speed
	 * 
	 * @param ghost ghost to update
	 */
	private void updateGhost(Ghost ghost) {
		final GameLevel level = game.currentLevel();
		switch (ghost.state) {

		case LOCKED:
			if (ghost.atGhostHouseDoor()) {
				ghost.setSpeed(0);
			} else {
				ghost.setSpeed(level.ghostSpeed / 2);
				ghost.bounce();
			}
			break;

		case ENTERING_HOUSE:
			ghost.setSpeed(level.ghostSpeed * 2);
			boolean leavingHouse = ghost.enterHouse();
			if (leavingHouse) {
				fireGameEvent(new PacManGameEvent(game, Info.GHOST_LEAVING_HOUSE, ghost, ghost.tile()));
			}
			break;

		case LEAVING_HOUSE:
			ghost.setSpeed(level.ghostSpeed / 2);
			boolean leftHouse = ghost.leaveHouse();
			if (leftHouse) {
				fireGameEvent(new PacManGameEvent(game, Info.GHOST_LEFT_HOUSE, ghost, ghost.tile()));
			}
			break;

		case FRIGHTENED:
			if (level.world.isTunnel(ghost.tile())) {
				ghost.setSpeed(level.ghostSpeedTunnel);
			} else {
				ghost.setSpeed(level.ghostSpeedFrightened);
				ghost.setRandomDirection();
			}
			ghost.tryMoving();
			break;

		case HUNTING_PAC:
			if (level.world.isTunnel(ghost.tile())) {
				ghost.setSpeed(level.ghostSpeedTunnel);
			} else if (ghost.elroy == 1) {
				ghost.setSpeed(level.elroy1Speed);
			} else if (ghost.elroy == 2) {
				ghost.setSpeed(level.elroy2Speed);
			} else {
				ghost.setSpeed(level.ghostSpeed);
			}
			if (game.variant() == MS_PACMAN && huntingPhase == 0 && (ghost.id == BLINKY || ghost.id == PINKY)) {
				/*
				 * In Ms. Pac-Man, Blinky and Pinky move randomly during the *first* scatter phase. Some say, the
				 * original intention had been to randomize the scatter target of *all* ghosts in Ms. Pac-Man but
				 * because of a bug, only the scatter target of Blinky and Pinky would have been affected. Who
				 * knows?
				 */
				ghost.targetTile = null;
				ghost.setRandomDirection();
			} else if (isScatteringPhase(huntingPhase) && ghost.elroy == 0) {
				ghost.targetTile = level.world.ghostScatterTile(ghost.id);
				ghost.setDirectionTowardsTarget();
			} else {
				ghost.targetTile = ghostHuntingTarget(ghost.id);
				ghost.setDirectionTowardsTarget();
			}
			ghost.tryMoving();
			break;

		case DEAD:
			ghost.setSpeed(level.ghostSpeed * 2);
			boolean reachedHouse = ghost.returnHome();
			if (reachedHouse) {
				fireGameEvent(new PacManGameEvent(game, Info.GHOST_ENTERS_HOUSE, ghost, ghost.tile()));
			}
			break;

		default:
			throw new IllegalArgumentException("Illegal ghost state: " + state);
		}
	}

	private void killGhost(Ghost ghost) {
		ghost.state = DEAD;
		ghost.targetTile = game.currentLevel().world.houseEntryLeftPart();
		ghost.bounty = game.getNextGhostBounty();
		score(ghost.bounty);
		game.increaseNextGhostBounty();
		game.currentLevel().numGhostsKilled++;
		if (game.currentLevel().numGhostsKilled == 16) {
			score(PacManGameModel.ALL_GHOSTS_KILLED_BONUS);
		}
		log("Ghost %s killed at tile %s, Pac-Man wins %d points", ghost.name, ghost.tile(), ghost.bounty);
	}

	/*
	 * The so called "ghost AI".
	 */
	private V2i ghostHuntingTarget(int ghostID) {
		final V2i playerTile = game.player().tile();
		switch (ghostID) {

		case BLINKY:
			return playerTile;

		case PINKY: {
			V2i fourAheadOfPlayer = playerTile.plus(game.player().dir().vec.scaled(4));
			if (game.player().dir() == Direction.UP) { // simulate overflow bug
				fourAheadOfPlayer = fourAheadOfPlayer.plus(-4, 0);
			}
			return fourAheadOfPlayer;
		}

		case INKY: {
			V2i twoAheadOfPlayer = playerTile.plus(game.player().dir().vec.scaled(2));
			if (game.player().dir() == Direction.UP) { // simulate overflow bug
				twoAheadOfPlayer = twoAheadOfPlayer.plus(-2, 0);
			}
			return twoAheadOfPlayer.scaled(2).minus(game.ghost(BLINKY).tile());
		}

		case CLYDE: /* A Boy Named Sue */
			return game.ghost(CLYDE).tile().euclideanDistance(playerTile) < 8
					? game.currentLevel().world.ghostScatterTile(CLYDE)
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

	private void tryReleasingLockedGhosts() {
		if (game.ghost(BLINKY).is(GhostState.LOCKED)) {
			game.ghost(BLINKY).state = HUNTING_PAC;
		}
		preferredLockedGhostInHouse().ifPresent(ghost -> {
			if (game.isGlobalDotCounterEnabled() && game.globalDotCounter() >= ghostGlobalDotLimit(ghost.id)) {
				releaseGhost(ghost, "Global dot counter (%d) reached limit (%d)", game.globalDotCounter(),
						ghostGlobalDotLimit(ghost.id));
			} else if (!game.isGlobalDotCounterEnabled()
					&& ghost.dotCounter >= ghostPrivateDotLimit(ghost.id, game.currentLevel().number)) {
				releaseGhost(ghost, "%s's dot counter (%d) reached limit (%d)", ghost.name, ghost.dotCounter,
						ghostPrivateDotLimit(ghost.id, game.currentLevel().number));
			} else if (game.player().starvingTicks >= playerStarvingTimeLimit(game.currentLevel().number)) {
				releaseGhost(ghost, "%s has been starving for %d ticks", game.player().name, game.player().starvingTicks);
				game.player().starvingTicks = 0;
			}
		});
	}

	private void releaseGhost(Ghost ghost, String reason, Object... args) {
		if (ghost.id == CLYDE && game.ghost(BLINKY).elroy < 0) {
			game.ghost(BLINKY).elroy = -game.ghost(BLINKY).elroy; // resume Elroy mode
			log("Blinky Elroy mode %d resumed", game.ghost(BLINKY).elroy);
		}
		ghost.state = GhostState.LEAVING_HOUSE;
		fireGameEvent(new PacManGameEvent(game, Info.GHOST_LEAVING_HOUSE, ghost, ghost.tile()));
		log("Ghost %s released: %s", ghost.name, String.format(reason, args));
	}

	private Optional<Ghost> preferredLockedGhostInHouse() {
		return Stream.of(PINKY, INKY, CLYDE).map(game::ghost).filter(ghost -> ghost.is(GhostState.LOCKED)).findFirst();
	}

	private void updateGhostDotCounters() {
		if (game.isGlobalDotCounterEnabled()) {
			if (game.ghost(CLYDE).is(GhostState.LOCKED) && game.globalDotCounter() == 32) {
				game.enableGlobalDotCounter(false);
				game.setGlobalDotCounter(0);
				log("Global dot counter disabled and reset, Clyde was in house when counter reached 32");
			} else {
				game.setGlobalDotCounter(game.globalDotCounter() + 1);
			}
		} else {
			preferredLockedGhostInHouse().ifPresent(ghost -> ++ghost.dotCounter);
		}
	}
}
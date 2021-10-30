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
import static de.amr.games.pacman.lib.TickTimer.sec_to_ticks;
import static de.amr.games.pacman.model.common.GameVariant.MS_PACMAN;
import static de.amr.games.pacman.model.common.GameVariant.PACMAN;
import static de.amr.games.pacman.model.common.GhostState.DEAD;
import static de.amr.games.pacman.model.common.GhostState.ENTERING_HOUSE;
import static de.amr.games.pacman.model.common.GhostState.FRIGHTENED;
import static de.amr.games.pacman.model.common.GhostState.HUNTING_PAC;
import static de.amr.games.pacman.model.common.GhostState.LEAVING_HOUSE;
import static de.amr.games.pacman.model.common.GhostState.LOCKED;
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
import de.amr.games.pacman.lib.FiniteStateMachine;
import de.amr.games.pacman.lib.TickTimer;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.GameLevel;
import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.model.common.Pac;
import de.amr.games.pacman.model.common.PacManGameModel;
import de.amr.games.pacman.model.mspacman.MsPacManGame;
import de.amr.games.pacman.model.pacman.PacManGame;
import de.amr.games.pacman.model.pacman.entities.Bonus;
import de.amr.games.pacman.ui.PacManGameUI;

/**
 * Controller (in the sense of MVC) for the Pac-Man and Ms. Pac-Man game.
 * <p>
 * This is a finite-state machine with states defined in {@link PacManGameState}. The game data are stored in the model
 * of the selected game, see {@link MsPacManGame} and {@link PacManGame}. The user interface is abstracted via an
 * interface ({@link PacManGameUI}). Scene selection is not controlled by this class but left to the user interface
 * implementations.
 * <p>
 * Missing functionality:
 * <ul>
 * <li><a href= "https://pacman.holenet.info/#CH2_Cornering"><em>Cornering</em></a>: I do not consider cornering as
 * important when the player is controlled by keyboard keys, for a joystick that probably would be more important.</li>
 * <li>Exact level data for Ms. Pac-Man still unclear. Any hints appreciated!
 * <li>Multiple players, credits.</li>
 * </ul>
 * 
 * @author Armin Reichert
 * 
 * @see <a href="https://github.com/armin-reichert">GitHub</a>
 * @see <a href="https://pacman.holenet.info">Jamey Pittman: The Pac-Man Dossier</a>
 * @see <a href= "https://gameinternals.com/understanding-pac-man-ghost-behavior">Chad Birch: Understanding ghost
 *      behavior</a>
 * @see <a href="http://superpacman.com/mspacman/">Ms. Pac-Man</a>
 */
public class PacManGameController extends FiniteStateMachine<PacManGameState> {

	private static final int BLINKY = 0;
	private static final int PINKY = 1;
	private static final int INKY = 2;
	private static final int CLYDE = 3; /* SUE */

	private PacManGameModel[] games;

	private PacManGameModel game;
	private PacManGameUI ui;

	private boolean autoControlled;
	private boolean gameRequested;
	private boolean gameRunning;
	private boolean attractMode;
	private int huntingPhase;

	private final Autopilot autopilot = new Autopilot(this::game);

	private final List<PacManGameEventListener> gameEventListeners = new ArrayList<>();

	public PacManGameController() {
		super(PacManGameState.class, PacManGameState.values());
		configState(INTRO, this::state_Intro_enter, this::state_Intro_update, null);
		configState(READY, this::state_Ready_enter, this::state_Ready_update, null);
		configState(HUNTING, this::state_Hunting_enter, this::state_Hunting_update, null);
		configState(GHOST_DYING, this::state_GhostDying_enter, this::state_GhostDying_update,
				this::state_GhostDying_exit);
		configState(PACMAN_DYING, this::state_PacManDying_enter, this::state_PacManDying_update, null);
		configState(LEVEL_STARTING, this::state_LevelStarting_enter, this::state_LevelStarting_update, null);
		configState(LEVEL_COMPLETE, this::state_LevelComplete_enter, this::state_LevelComplete_update, null);
		configState(INTERMISSION, this::state_Intermission_enter, this::state_Intermission_update, null);
		configState(GAME_OVER, this::state_GameOver_enter, this::state_GameOver_update, null);

		games = new PacManGameModel[2];
		games[GameVariant.MS_PACMAN.ordinal()] = new MsPacManGame();
		games[GameVariant.PACMAN.ordinal()] = new PacManGame();
	}

	private void fireGameEvent(PacManGameEvent gameEvent) {
		gameEventListeners.forEach(listener -> listener.onGameEvent(gameEvent));
	}

	private void fireGameEvent(Info info, V2i tile) {
		fireGameEvent(new PacManGameEvent(game, info, null, tile));
	}

	@Override
	protected void fireStateChange(PacManGameState oldState, PacManGameState newState) {
		fireGameEvent(new PacManGameStateChangeEvent(game, oldState, newState));
	}

	private PlayerControl playerControl() {
		return autoControlled || attractMode ? autopilot : ui;
	}

	public PacManGameUI getUI() {
		return ui;
	}

	public void setUI(PacManGameUI gameUI) {
		if (ui != null) {
			gameEventListeners.remove(ui);
		}
		ui = gameUI;
		gameEventListeners.add(ui);
	}

	public void selectGameVariant(GameVariant variant) {
		game = games[variant.ordinal()];
		changeState(INTRO);
	}

	public PacManGameModel game() {
		return game;
	}

	public void startGame() {
		if (state == INTRO) {
			gameRequested = true;
			changeState(READY);
		}
	}

	public boolean isAutoControlled() {
		return autoControlled;
	}

	public void setAutoControlled(boolean autoControlled) {
		this.autoControlled = autoControlled;
	}

	public boolean isAttractMode() {
		return attractMode;
	}

	public boolean isGameRunning() {
		return gameRunning;
	}

	public int getHuntingPhase() {
		return huntingPhase;
	}

	public boolean inScatteringPhase() {
		return huntingPhase % 2 == 0;
	}

	public void cheatKillGhosts() {
		game.resetGhostBounty();
		game.ghosts().filter(ghost -> ghost.is(HUNTING_PAC) || ghost.is(FRIGHTENED)).forEach(this::killGhost);
		changeState(GHOST_DYING);
	}

	public void cheatEatAllPellets() {
		game.level().world.tiles().filter(not(game.level().world::isEnergizerTile)).forEach(game.level()::removeFood);
		fireGameEvent(Info.PLAYER_FOUND_FOOD, null);
	}

	// BEGIN STATE-MACHINE METHODS

	private void state_Intro_enter() {
		game.reset();
		gameRequested = false;
		gameRunning = false;
		attractMode = false;
		autoControlled = false;
		stateTimer().reset();
		stateTimer().start();
	}

	private void state_Intro_update() {
		if (stateTimer().hasExpired()) {
			attractMode = true;
			changeState(READY);
		}
	}

	private void state_Ready_enter() {
		game.resetGuys();
		stateTimer().reset();
		stateTimer().start();
	}

	private void state_Ready_update() {
		long duration = gameRunning ? sec_to_ticks(1.5) : sec_to_ticks(4.5);
		if (stateTimer().ticked() == duration - sec_to_ticks(1)) {
			game.player().setVisible(true);
			game.ghosts().forEach(ghost -> ghost.setVisible(true));
		} else if (stateTimer().ticked() == duration) {
			game.player().setVisible(true);
			game.ghosts().forEach(ghost -> ghost.setVisible(true));
			if (gameRequested) {
				gameRunning = true;
			}
			changeState(PacManGameState.HUNTING);
			return;
		}
	}

	private void startHuntingPhase(int phase) {
		huntingPhase = phase;
		stateTimer().reset(game.getHuntingPhaseDuration(phase));
		stateTimer().start();
		String phaseName = inScatteringPhase() ? "Scattering" : "Chasing";
		log("Hunting phase #%d (%s) started, %d of %d ticks remaining", phase, phaseName, stateTimer().ticksRemaining(),
				stateTimer().duration());
		if (inScatteringPhase()) {
			fireGameEvent(new ScatterPhaseStartedEvent(game, phase / 2));
		}
	}

	private void state_Hunting_enter() {
		if (!stateTimer().isStopped()) {
			startHuntingPhase(0);
		}
	}

	// here is the main logic of the game play
	private void state_Hunting_update() {

		final GameLevel level = game.level();
		final Pac player = game.player();

		// Is hunting phase complete?
		if (stateTimer().hasExpired()) {
			game.ghosts().filter(ghost -> ghost.is(HUNTING_PAC) || ghost.is(FRIGHTENED))
					.forEach(Ghost::forceTurningBack);
			startHuntingPhase(++huntingPhase);
			return;
		}

		// Is level complete?
		if (level.foodRemaining == 0) {
			stateTimer().reset();
			changeState(LEVEL_COMPLETE);
			return;
		}

		// Is player killing ghost(s)?
		List<Ghost> prey = game.ghosts(FRIGHTENED).filter(player::meets).collect(Collectors.toList());
		if (!prey.isEmpty()) {
			prey.forEach(this::killGhost);
			changeState(GHOST_DYING);
			return;
		}

		// Is player getting killed by a ghost?
		if (attractMode || !player.immune) {
			Optional<Ghost> killer = game.ghosts(HUNTING_PAC).filter(player::meets).findAny();
			if (killer.isPresent()) {
				player.dead = true;
				log("%s got killed by %s at tile %s", player.name, killer.get().name, player.tile());
				// Elroy mode gets disabled when player is killed
				final int elroyMode = game.ghost(BLINKY).elroy;
				if (elroyMode > 0) {
					game.ghost(BLINKY).elroy = -elroyMode; // negative value means "disabled"
					log("Elroy mode %d for Blinky has been disabled", elroyMode);
				}
				game.setGlobalDotCounter(0);
				game.enableGlobalDotCounter(true);
				log("Global dot counter got reset and enabled");
				stateTimer().reset();
				changeState(PACMAN_DYING);
				return;
			}
		}

		// Did player find food?
		if (level.containsFood(player.tile())) {
			onPlayerFoundFood(level, player);
		} else {
			player.starvingTicks++;
		}

		// Consume power?
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
		playerControl().steer(game.player());
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

	private void state_PacManDying_enter() {
		game.player().setSpeed(0);
		game.ghosts(FRIGHTENED).forEach(ghost -> ghost.state = HUNTING_PAC);
		game.bonus().init();
		stateTimer().reset();
		stateTimer().start();
	}

	private void state_PacManDying_update() {
		if (stateTimer().hasExpired()) {
			game.changeLivesBy(-1);
			changeState(attractMode ? INTRO : game.lives() > 0 ? READY : GAME_OVER);
			return;
		}
	}

	private void state_GhostDying_enter() {
		game.player().setVisible(false);
		stateTimer().resetSeconds(1);
		stateTimer().start();
	}

	private void state_GhostDying_update() {
		if (stateTimer().hasExpired()) {
			resumePreviousState();
			return;
		}
		playerControl().steer(game.player());
		game.ghosts().filter(ghost -> ghost.is(DEAD) && ghost.bounty == 0 || ghost.is(ENTERING_HOUSE))
				.forEach(this::updateGhost);
	}

	private void state_GhostDying_exit() {
		game.player().setVisible(true);
		// fire event only for ghosts that have just been killed, not for dead ghosts
		// that are already
		// returning home
		game.ghosts(DEAD).filter(ghost -> ghost.bounty != 0).forEach(ghost -> {
			ghost.bounty = 0;
			fireGameEvent(new PacManGameEvent(game, Info.GHOST_RETURNS_HOME, ghost, null));
		});
	}

	private void state_LevelStarting_enter() {
		log("Level %d complete, entering level %d", game.level().number, game.level().number + 1);
		game.enterLevel(game.level().number + 1);
		game.resetGuys();
		stateTimer().reset();
		stateTimer().start();
	}

	private void state_LevelStarting_update() {
		if (stateTimer().hasExpired()) {
			changeState(READY);
		}
	}

	private void state_LevelComplete_enter() {
		game.bonus().init();
		game.player().setSpeed(0);
		stateTimer().reset();
		stateTimer().start();
	}

	private void state_LevelComplete_update() {
		if (stateTimer().hasExpired()) {
			if (attractMode) {
				changeState(INTRO);
			} else if (game.intermissionAfterLevel(game.level().number).isPresent()) {
				changeState(INTERMISSION);
			} else {
				changeState(LEVEL_STARTING);
			}
		}
	}

	private void state_GameOver_enter() {
		gameRunning = false;
		game.ghosts().forEach(ghost -> ghost.setSpeed(0));
		game.player().setSpeed(0);
		game.saveHiscore();
		stateTimer().resetSeconds(5);
		stateTimer().start();
	}

	private void state_GameOver_update() {
		if (stateTimer().hasExpired()) {
			changeState(INTRO);
		}
	}

	private void state_Intermission_enter() {
		stateTimer().reset(); // UI triggers state timeout
		stateTimer().start();
	}

	private void state_Intermission_update() {
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
			game.setHiscoreLevel(game.level().number);
		}
		if (oldscore < 10000 && game.score() >= 10000) {
			game.changeLivesBy(1);
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

		// Blinky becomes Elroy?
		if (level.foodRemaining == level.elroy1DotsLeft) {
			game.ghost(BLINKY).elroy = 1;
			log("Blinky becomes Cruise Elroy 1");
		} else if (level.foodRemaining == level.elroy2DotsLeft) {
			game.ghost(BLINKY).elroy = 2;
			log("Blinky becomes Cruise Elroy 2");
		}

		// Is bonus awarded?
		if (game.isBonusReached()) {
			final Bonus bonus = game.bonus();
			final long bonusTicks = game.variant() == PACMAN ? sec_to_ticks(9 + new Random().nextFloat())
					: TickTimer.INDEFINITE;
			bonus.symbol = level.bonusSymbol;
			bonus.points = game.bonusValue(bonus.symbol);
			bonus.activate(bonusTicks);
			log("Bonus %s (value %d) activated for %d ticks", bonus.symbol, bonus.points, bonusTicks);
			fireGameEvent(Info.BONUS_ACTIVATED, bonus.tile());
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
		final GameLevel level = game.level();
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
				 * because of a bug, only the scatter target of Blinky and Pinky would have been affected. Who knows?
				 */
				ghost.targetTile = null;
				ghost.setRandomDirection();
			} else if (inScatteringPhase() && ghost.elroy == 0) {
				ghost.targetTile = level.world.ghostScatterTile(ghost.id);
				ghost.setDirectionTowardsTarget();
			} else {
				ghost.targetTile = ghost.fnChasingTargetTile.get();
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
		ghost.targetTile = game.level().world.ghostHouse().entryTile();
		ghost.bounty = game.getNextGhostBounty();
		score(ghost.bounty);
		game.increaseNextGhostBounty();
		game.level().numGhostsKilled++;
		if (game.level().numGhostsKilled == 16) {
			score(12000);
		}
		log("Ghost %s killed at tile %s, Pac-Man wins %d points", ghost.name, ghost.tile(), ghost.bounty);
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
		if (game.ghost(BLINKY).is(LOCKED)) {
			game.ghost(BLINKY).state = HUNTING_PAC;
		}
		preferredLockedGhostInHouse().ifPresent(ghost -> {
			if (game.isGlobalDotCounterEnabled() && game.globalDotCounter() >= ghostGlobalDotLimit(ghost.id)) {
				releaseGhost(ghost, "Global dot counter (%d) reached limit (%d)", game.globalDotCounter(),
						ghostGlobalDotLimit(ghost.id));
			} else if (!game.isGlobalDotCounterEnabled()
					&& ghost.dotCounter >= ghostPrivateDotLimit(ghost.id, game.level().number)) {
				releaseGhost(ghost, "%s's dot counter (%d) reached limit (%d)", ghost.name, ghost.dotCounter,
						ghostPrivateDotLimit(ghost.id, game.level().number));
			} else if (game.player().starvingTicks >= playerStarvingTimeLimit(game.level().number)) {
				releaseGhost(ghost, "%s has been starving for %d ticks", game.player().name,
						game.player().starvingTicks);
				game.player().starvingTicks = 0;
			}
		});
	}

	private void releaseGhost(Ghost ghost, String reason, Object... args) {
		if (ghost.id == CLYDE && game.ghost(BLINKY).elroy < 0) {
			game.ghost(BLINKY).elroy = -game.ghost(BLINKY).elroy; // resume Elroy mode
			log("Blinky Elroy mode %d resumed", game.ghost(BLINKY).elroy);
		}
		ghost.state = LEAVING_HOUSE;
		fireGameEvent(new PacManGameEvent(game, Info.GHOST_LEAVING_HOUSE, ghost, ghost.tile()));
		log("Ghost %s released: %s", ghost.name, String.format(reason, args));
	}

	private Optional<Ghost> preferredLockedGhostInHouse() {
		return Stream.of(PINKY, INKY, CLYDE).map(game::ghost).filter(ghost -> ghost.is(LOCKED)).findFirst();
	}

	private void updateGhostDotCounters() {
		if (game.isGlobalDotCounterEnabled()) {
			if (game.ghost(CLYDE).is(LOCKED) && game.globalDotCounter() == 32) {
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
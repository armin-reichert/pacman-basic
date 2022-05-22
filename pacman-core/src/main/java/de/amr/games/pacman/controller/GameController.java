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

import static de.amr.games.pacman.controller.GameState.GHOST_DYING;
import static de.amr.games.pacman.controller.GameState.INTERMISSION_TEST;
import static de.amr.games.pacman.controller.GameState.INTRO;
import static de.amr.games.pacman.controller.GameState.READY;
import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.lib.TickTimer.sec_to_ticks;
import static de.amr.games.pacman.model.common.GameModel.CYAN_GHOST;
import static de.amr.games.pacman.model.common.GameModel.ORANGE_GHOST;
import static de.amr.games.pacman.model.common.GameModel.PINK_GHOST;
import static de.amr.games.pacman.model.common.GameModel.RED_GHOST;
import static de.amr.games.pacman.model.common.GameVariant.MS_PACMAN;
import static de.amr.games.pacman.model.common.GhostState.DEAD;
import static de.amr.games.pacman.model.common.GhostState.FRIGHTENED;
import static de.amr.games.pacman.model.common.GhostState.HUNTING_PAC;
import static de.amr.games.pacman.model.common.GhostState.LEAVING_HOUSE;
import static de.amr.games.pacman.model.common.GhostState.LOCKED;
import static java.util.function.Predicate.not;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Stream;

import de.amr.games.pacman.controller.event.GameEvent;
import de.amr.games.pacman.controller.event.GameEvent.Info;
import de.amr.games.pacman.controller.event.GameEventListener;
import de.amr.games.pacman.controller.event.GameStateChangeEvent;
import de.amr.games.pacman.controller.event.ScatterPhaseStartedEvent;
import de.amr.games.pacman.lib.FiniteStateMachine;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.model.mspacman.MsPacManGame;
import de.amr.games.pacman.model.pacman.PacManGame;

/**
 * Controller (in the sense of MVC) for both (Pac-Man, Ms. Pac-Man) game variants.
 * <p>
 * This is a finite-state machine with states defined in {@link GameState}. The game data are stored in the model of the
 * selected game, see {@link MsPacManGame} and {@link PacManGame}. Scene selection is not controlled by this class but
 * left to the specific user interface implementations.
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
public class GameController extends FiniteStateMachine<GameState, GameModel> {

	public final EnumMap<GameVariant, GameModel> games = new EnumMap<>(GameVariant.class);
	public GameModel game;
	public GameVariant gameVariant;

	private PlayerControl playerControl;
	private final Autopilot autopilot = new Autopilot(() -> game);

	public boolean autoControlled;
	public boolean playerImmune;
	public boolean gameRequested;
	public boolean gameRunning;

	public GameController(GameVariant variant) {
		for (var state : GameState.values()) {
			state.fsm = this;
		}
		// map state change events to game events
		stateChangeListeners.add((oldState, newState) -> publish(new GameStateChangeEvent(game, oldState, newState)));

		games.put(GameVariant.MS_PACMAN, new MsPacManGame());
		games.put(GameVariant.PACMAN, new PacManGame());
		selectGameVariant(variant);
	}

	//
	// Event stuff
	//

	private final Collection<GameEventListener> subscribers = new ConcurrentLinkedQueue<>();

	void publish(GameEvent gameEvent) {
		subscribers.forEach(subscriber -> subscriber.onGameEvent(gameEvent));
	}

	private void publish(Info info, V2i tile) {
		publish(new GameEvent(game, info, null, tile));
	}

	public void addGameEventListener(GameEventListener subscriber) {
		subscribers.add(subscriber);
	}

	public void removeGameEventListener(GameEventListener subscriber) {
		subscribers.remove(subscriber);
	}

	// ---

	public void setPlayerControl(PlayerControl playerControl) {
		this.playerControl = playerControl;
	}

	PlayerControl currentPlayerControl() {
		return autoControlled || game.attractMode ? autopilot : playerControl;
	}

	public void selectGameVariant(GameVariant variant) {
		gameVariant = variant;
		game = games.get(variant);
		setContext(game); // TODO checkme
		changeState(INTRO);
	}

	public void requestGame() {
		if (state == INTRO) {
			gameRequested = true;
			changeState(READY);
		}
	}

	public void startIntermissionTest() {
		if (state == INTRO) {
			intermissionTestNumber = 1;
			changeState(INTERMISSION_TEST);
		}
	}

	public void cheatKillGhosts() {
		if (gameRunning) {
			game.ghostBounty = game.firstGhostBounty;
			game.ghosts().filter(ghost -> ghost.is(HUNTING_PAC) || ghost.is(FRIGHTENED)).forEach(this::killGhost);
			changeState(GHOST_DYING);
		}
	}

	public void cheatEatAllPellets() {
		if (gameRunning) {
			game.world.tiles().filter(not(game.world::isEnergizerTile)).forEach(game.world::removeFood);
			publish(Info.PLAYER_FOUND_FOOD, null);
		}
	}

	public int intermissionTestNumber;

	void resetAndStartHuntingTimerForPhase(int phase) {
		long ticks = game.huntingPhaseDurations[phase];
		log("Set %s timer to %d ticks", state, ticks);
		state.timer.set(ticks).start();
	}

	void startHuntingPhase(int phase) {
		game.huntingPhase = phase;
		resetAndStartHuntingTimerForPhase(phase);
		if (phase > 0) {
			game.ghosts().filter(ghost -> ghost.is(HUNTING_PAC) || ghost.is(FRIGHTENED)).forEach(Ghost::forceTurningBack);
		}
		String phaseName = game.inScatteringPhase() ? "Scattering" : "Chasing";
		log("Hunting phase #%d (%s) started, %d of %d ticks remaining", phase, phaseName, state.timer.ticksRemaining(),
				state.timer().duration());
		if (game.inScatteringPhase()) {
			publish(new ScatterPhaseStartedEvent(game, phase / 2));
		}
	}

	void moveGhosts() {
		releaseLockedGhosts();
		game.ghosts().forEach(this::updateGhost);
	}

	boolean killedGhosts() {
		Ghost[] prey = game.ghosts(FRIGHTENED).filter(game.player::meets).toArray(Ghost[]::new);
		Stream.of(prey).forEach(this::killGhost);
		return prey.length > 0;
	}

	public boolean killedPlayer() {
		if (game.player.powerTimer.isRunning()) {
			return false;
		}
		if (playerImmune && !game.attractMode) {
			return false;
		}
		Optional<Ghost> killer = game.ghosts(HUNTING_PAC).filter(game.player::meets).findAny();
		killer.ifPresent(ghost -> {
			game.player.killed = true;
			log("%s got killed by %s at tile %s", game.player.name, ghost.name, game.player.tile());
			// Elroy mode of red ghost gets disabled when player is killed
			Ghost redGhost = game.ghosts[GameModel.RED_GHOST];
			if (redGhost.elroy > 0) {
				redGhost.elroy = -redGhost.elroy; // negative value means "disabled"
				log("Elroy mode %d for %s has been disabled", redGhost.elroy, redGhost.name);
			}
			// reset and disable global dot counter (used by ghost house logic)
			game.globalDotCounter = 0;
			game.globalDotCounterEnabled = true;
			log("Global dot counter got reset and enabled");
		});
		return killer.isPresent();
	}

	void lookForFood() {
		V2i playerTile = game.player.tile();
		if (game.world.containsFood(playerTile)) {
			eatFood(playerTile);
		} else {
			game.player.starvingTicks++;
		}
	}

	private void eatFood(V2i foodTile) {
		game.world.removeFood(foodTile);
		game.player.starvingTicks = 0;
		if (game.world.isEnergizerTile(foodTile)) {
			game.player.restingTicksLeft = 3;
			score(game.energizerValue);
			game.ghostBounty = game.firstGhostBounty;
			if (game.ghostFrightenedSeconds > 0) {
				game.ghosts(HUNTING_PAC).forEach(ghost -> {
					ghost.state = FRIGHTENED;
					ghost.forceTurningBack();
				});
				game.player.powerTimer.setSeconds(game.ghostFrightenedSeconds).start();
				log("%s got power, timer=%s", game.player.name, game.player.powerTimer);
				// HUNTING is stopped while player has power
				state.timer().stop();
				log("%s timer stopped: %s", state, state.timer());
				publish(Info.PLAYER_GAINS_POWER, foodTile);
			}
		} else {
			game.player.restingTicksLeft = 1;
			score(game.pelletValue);
		}

		// Will Blinky become Cruise Elroy?
		if (game.world.foodRemaining() == game.elroy1DotsLeft) {
			game.ghosts[RED_GHOST].elroy = 1;
			log("Blinky becomes Cruise Elroy 1");
		} else if (game.world.foodRemaining() == game.elroy2DotsLeft) {
			game.ghosts[RED_GHOST].elroy = 2;
			log("Blinky becomes Cruise Elroy 2");
		}

		// Is bonus awarded?
		if (game.world.isBonusReached()) {
			game.bonus.activate(game.bonusSymbol, game.bonusValue(game.bonus.symbol));
			game.bonus.timer = game.bonusActivationTicks();
			log("Bonus id=%d, value=%d activated for %d ticks", game.bonus.symbol, game.bonus.points, game.bonus.timer);
			publish(Info.BONUS_ACTIVATED, game.bonus.tile());
		}

		updateGhostDotCounters();
		publish(Info.PLAYER_FOUND_FOOD, foodTile);
	}

	void consumePower() {
		if (game.player.powerTimer.isRunning()) {
			game.player.powerTimer.tick();
			if (game.player.powerTimer.ticksRemaining() == sec_to_ticks(1)) {
				// TODO not sure exactly how long the player is losing power
				publish(Info.PLAYER_LOSING_POWER, game.player.tile());
			}
		} else if (game.player.powerTimer.hasExpired()) {
			log("%s lost power, timer=%s", game.player.name, game.player.powerTimer);
			game.ghosts(FRIGHTENED).forEach(ghost -> ghost.state = HUNTING_PAC);
			game.player.powerTimer.setIndefinite();
			// restart HUNTING state timer
			state.timer().start();
			log("HUNTING timer restarted: %s", state.timer());
			publish(Info.PLAYER_LOST_POWER, game.player.tile());
		}
	}

	void consumeBonus() {
		switch (game.bonus.state) {

		case EDIBLE -> {
			if (game.player.meets(game.bonus)) {
				log("%s found bonus id=%d of value %d", game.player.name, game.bonus.symbol, game.bonus.points);
				game.bonus.eat();
				game.bonus.timer = sec_to_ticks(2);
				score(game.bonus.points);
				publish(Info.BONUS_EATEN, game.bonus.tile());
			} else {
				game.bonus.update();
				if (game.bonus.timer == 0) {
					log("Bonus id=%d expired", game.bonus.symbol);
					publish(Info.BONUS_EXPIRED, game.bonus.tile());
				}
			}
		}

		case EATEN -> {
			game.bonus.update();
			if (game.bonus.timer == 0) {
				publish(Info.BONUS_EXPIRED, game.bonus.tile());
			}
		}

		default -> {
			// INACTIVE
		}

		}
	}

	/**
	 * Scores the given number of points. When 10.000 points are reached, an extra life is rewarded.
	 */
	private void score(int points) {
		if (game.score(points)) {
			log("Extra life. Player has %d lives now", game.player.lives);
			publish(Info.EXTRA_LIFE, null);
		}
	}

	// Ghosts

	/**
	 * Updates ghost's speed and behavior depending on its current state.
	 * 
	 * TODO: I am not sure about the exact speed values as the Pac-Man dossier has no info on this. Any help is
	 * appreciated!
	 * 
	 * @param ghost ghost to update
	 */
	void updateGhost(Ghost ghost) {
		switch (ghost.state) {

		case LOCKED -> {
			if (ghost.atGhostHouseDoor(game.world.ghostHouse())) {
				ghost.setSpeed(0);
			} else {
				ghost.setSpeed(game.ghostSpeed / 2);
				ghost.bounce(game.world.ghostHouse());
			}
		}

		case ENTERING_HOUSE -> {
			ghost.setSpeed(game.ghostSpeed * 2);
			boolean reachedRevivalTile = ghost.enterHouse(game.world.ghostHouse());
			if (reachedRevivalTile) {
				publish(new GameEvent(game, Info.GHOST_REVIVED, ghost, ghost.tile()));
				publish(new GameEvent(game, Info.GHOST_LEAVING_HOUSE, ghost, ghost.tile()));
			}
		}

		case LEAVING_HOUSE -> {
			ghost.setSpeed(game.ghostSpeed / 2);
			boolean leftHouse = ghost.leaveHouse(game.world.ghostHouse());
			if (leftHouse) {
				publish(new GameEvent(game, Info.GHOST_LEFT_HOUSE, ghost, ghost.tile()));
			}
		}

		case FRIGHTENED -> {
			if (game.world.isTunnel(ghost.tile())) {
				ghost.setSpeed(game.ghostSpeedTunnel);
				ghost.tryMoving();
			} else {
				ghost.setSpeed(game.ghostSpeedFrightened);
				ghost.roam();
			}
		}

		case HUNTING_PAC -> {
			if (game.world.isTunnel(ghost.tile())) {
				ghost.setSpeed(game.ghostSpeedTunnel);
			} else if (ghost.elroy == 1) {
				ghost.setSpeed(game.elroy1Speed);
			} else if (ghost.elroy == 2) {
				ghost.setSpeed(game.elroy2Speed);
			} else {
				ghost.setSpeed(game.ghostSpeed);
			}

			/*
			 * In Ms. Pac-Man, Blinky and Pinky move randomly during the *first* scatter phase. Some say, the original
			 * intention had been to randomize the scatter target of *all* ghosts in Ms. Pac-Man but because of a bug, only
			 * the scatter target of Blinky and Pinky would have been affected. Who knows?
			 */
			if (gameVariant == MS_PACMAN && game.huntingPhase == 0 && (ghost.id == RED_GHOST || ghost.id == PINK_GHOST)) {
				ghost.roam();
			} else if (game.inScatteringPhase() && ghost.elroy == 0) {
				ghost.scatter();
			} else {
				ghost.chase();
			}
		}

		case DEAD -> {
			ghost.setSpeed(game.ghostSpeed * 2);
			boolean reachedHouse = ghost.returnHome(game.world.ghostHouse());
			if (reachedHouse) {
				publish(new GameEvent(game, Info.GHOST_ENTERS_HOUSE, ghost, ghost.tile()));
			}
		}

		default -> throw new IllegalArgumentException("Illegal ghost state: " + state);

		}
	}

	/**
	 * Killing ghosts wins 200, 400, 800, 1600 points in order when using the same energizer power. If all 16 ghosts on a
	 * level are killed, additonal 12000 points are rewarded.
	 */
	private void killGhost(Ghost ghost) {
		ghost.state = DEAD;
		ghost.targetTile = game.world.ghostHouse().leftEntry();
		ghost.bounty = game.ghostBounty;
		game.ghostBounty *= 2;
		game.numGhostsKilled++;
		score(ghost.bounty);
		if (game.numGhostsKilled == 16) {
			score(12000);
		}
		log("Ghost %s killed at tile %s, Pac-Man wins %d points", ghost.name, ghost.tile(), ghost.bounty);
	}

	// Ghost house rules, see Pac-Man dossier

	private void releaseLockedGhosts() {
		if (game.ghosts[RED_GHOST].is(LOCKED)) {
			game.ghosts[RED_GHOST].state = HUNTING_PAC;
		}
		preferredLockedGhostInHouse().ifPresent(ghost -> {
			if (game.globalDotCounterEnabled && game.globalDotCounter >= ghost.globalDotLimit) {
				releaseGhost(ghost, "Global dot counter reached limit (%d)", ghost.globalDotLimit);
			} else if (!game.globalDotCounterEnabled && ghost.dotCounter >= ghost.privateDotLimit) {
				releaseGhost(ghost, "Private dot counter reached limit (%d)", ghost.privateDotLimit);
			} else if (game.player.starvingTicks >= game.player.starvingTimeLimit) {
				releaseGhost(ghost, "%s reached starving limit (%d ticks)", game.player.name, game.player.starvingTicks);
				game.player.starvingTicks = 0;
			}
		});
	}

	private void releaseGhost(Ghost ghost, String reason, Object... args) {
		if (ghost.id == ORANGE_GHOST && game.ghosts[RED_GHOST].elroy < 0) {
			game.ghosts[RED_GHOST].elroy = -game.ghosts[RED_GHOST].elroy; // resume Elroy mode
			log("%s Elroy mode %d resumed", game.ghosts[RED_GHOST].name, game.ghosts[RED_GHOST].elroy);
		}
		ghost.state = LEAVING_HOUSE;
		log("Ghost %s released: %s", ghost.name, String.format(reason, args));
		publish(new GameEvent(game, Info.GHOST_LEAVING_HOUSE, ghost, ghost.tile()));
	}

	private Optional<Ghost> preferredLockedGhostInHouse() {
		return Stream.of(PINK_GHOST, CYAN_GHOST, ORANGE_GHOST).map(id -> game.ghosts[id]).filter(ghost -> ghost.is(LOCKED))
				.findFirst();
	}

	private void updateGhostDotCounters() {
		if (game.globalDotCounterEnabled) {
			if (game.ghosts[ORANGE_GHOST].is(LOCKED) && game.globalDotCounter == 32) {
				game.globalDotCounterEnabled = false;
				game.globalDotCounter = 0;
				log("Global dot counter disabled and reset, Clyde was in house when counter reached 32");
			} else {
				game.globalDotCounter++;
			}
		} else {
			preferredLockedGhostInHouse().ifPresent(ghost -> ++ghost.dotCounter);
		}
	}
}
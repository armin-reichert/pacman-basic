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
import static de.amr.games.pacman.model.common.GameModel.PINK_GHOST;
import static de.amr.games.pacman.model.common.GameModel.RED_GHOST;
import static de.amr.games.pacman.model.common.GameVariant.MS_PACMAN;
import static de.amr.games.pacman.model.common.GhostState.FRIGHTENED;
import static de.amr.games.pacman.model.common.GhostState.HUNTING_PAC;
import static java.util.function.Predicate.not;

import java.util.Map;
import java.util.Objects;

import de.amr.games.pacman.controller.event.GameEvent;
import de.amr.games.pacman.controller.event.GameEvent.Info;
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

	public boolean playerImmune;
	public boolean autoControlled;
	public int intermissionTestNumber;

	public final Map<GameVariant, GameModel> games = Map.of( //
			GameVariant.MS_PACMAN, new MsPacManGame(), //
			GameVariant.PACMAN, new PacManGame());

	private GameVariant selectedGameVariant;
	public GameModel game;

	private PlayerControl playerControl;
	private final Autopilot autopilot = new Autopilot(() -> game);

	public GameController(GameVariant variant) {
		for (var state : GameState.values()) {
			state.fsm = this;
		}

		for (var gameVariant : GameVariant.values()) {
			stateChangeListeners.add((oldState, newState) -> games.get(gameVariant)
					.publishEvent(new GameStateChangeEvent(game, oldState, newState)));
		}

		selectGameVariant(variant);
	}

	//
	// Event stuff
	//

	// ---

	public void setPlayerControl(PlayerControl playerControl) {
		this.playerControl = playerControl;
	}

	PlayerControl currentPlayerControl() {
		return autoControlled || game.attractMode ? autopilot : playerControl;
	}

	public GameVariant gameVariant() {
		return selectedGameVariant;
	}

	public void selectGameVariant(GameVariant variant) {
		selectedGameVariant = Objects.requireNonNull(variant);
		for (var gameVariant : GameVariant.values()) {
			games.get(gameVariant).setEventingEnabled(gameVariant == selectedGameVariant);
		}
		game = games.get(variant);
		setContext(game); // TODO checkme
		changeState(INTRO);
	}

	public void requestGame() {
		if (state == INTRO) {
			game.requested = true;
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
		if (game.running) {
			game.ghostBounty = game.firstGhostBounty;
			game.ghosts().filter(ghost -> ghost.is(HUNTING_PAC) || ghost.is(FRIGHTENED)).forEach(game::killGhost);
			changeState(GHOST_DYING);
		}
	}

	public void cheatEatAllPellets() {
		if (game.running) {
			game.world.tiles().filter(not(game.world::isEnergizerTile)).forEach(game.world::removeFood);
			game.publishEvent(Info.PLAYER_FOUND_FOOD, null);
		}
	}

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
			game.publishEvent(new ScatterPhaseStartedEvent(game, phase / 2));
		}
	}

	void updatePlayer() {
		currentPlayerControl().steer(game.player);
		game.updatePlayer();
		switch (game.player.powerTimer.getState()) {
		case RUNNING -> {
			game.player.powerTimer.tick();
			if (game.player.powerTimer.ticksRemaining() == sec_to_ticks(1)) {
				// TODO not sure exactly how long the player is losing power
				game.publishEvent(Info.PLAYER_LOSING_POWER, game.player.tile());
			}
		}
		case EXPIRED -> {
			log("%s lost power, timer=%s", game.player.name, game.player.powerTimer);
			// restart (HUNTING) state timer
			state.timer().start();
			log("HUNTING timer restarted: %s", state.timer());
			game.ghosts(FRIGHTENED).forEach(ghost -> ghost.state = HUNTING_PAC);
			game.player.powerTimer.setIndefinite(); // TODO needed?
			game.publishEvent(Info.PLAYER_LOST_POWER, game.player.tile());
		}
		default -> {
		}
		}
	}

	void updateGhosts() {
		game.ghosts().forEach(this::updateGhost);
		Ghost released = game.releaseLockedGhosts();
		if (released != null) {
			game.publishEvent(new GameEvent(game, Info.GHOST_LEAVING_HOUSE, released, released.tile()));
		}
	}

	void lookForFood() {
		V2i tile = game.player.tile();
		if (game.world.containsFood(tile)) {
			eatFood(tile);
			game.publishEvent(Info.PLAYER_FOUND_FOOD, tile);
		} else {
			game.player.starvingTicks++;
		}
	}

	void eatFood(V2i foodTile) {
		boolean extraLife = false;
		if (game.world.isEnergizerTile(foodTile)) {
			extraLife = game.eatEnergizer(foodTile);
			if (game.ghostFrightenedSeconds > 0) {
				// HUNTING timer is stopped while player has power
				state.timer().stop();
				log("%s timer stopped: %s", state, state.timer());
				game.publishEvent(Info.PLAYER_GAINS_POWER, foodTile);
			}
		} else {
			extraLife = game.eatPellet(foodTile);
		}
		if (extraLife) {
			log("Extra life. Player has %d lives now", game.player.lives);
			game.publishEvent(Info.EXTRA_LIFE, null);
		}
		if (game.checkBonusAwarded()) {
			game.publishEvent(Info.BONUS_ACTIVATED, game.bonus.tile());
		}
	}

	void updateBonus() {
		switch (game.bonus.state) {
		case EDIBLE -> {
			if (game.player.meets(game.bonus)) {
				log("%s found bonus id=%d of value %d", game.player.name, game.bonus.symbol, game.bonus.points);
				game.bonus.eat();
				game.bonus.timer = sec_to_ticks(2);
				boolean extraLife = game.score(game.bonus.points);
				if (extraLife) {
					log("Extra life. Player has %d lives now", game.player.lives);
					game.publishEvent(Info.EXTRA_LIFE, null);
				}
				game.publishEvent(Info.BONUS_EATEN, game.bonus.tile());
			} else {
				game.bonus.update();
				if (game.bonus.timer == 0) {
					log("Bonus id=%d expired", game.bonus.symbol);
					game.publishEvent(Info.BONUS_EXPIRED, game.bonus.tile());
				}
			}
		}
		case EATEN -> {
			game.bonus.update();
			if (game.bonus.timer == 0) {
				game.publishEvent(Info.BONUS_EXPIRED, game.bonus.tile());
			}
		}
		default -> {
			// INACTIVE
		}
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
				game.publishEvent(new GameEvent(game, Info.GHOST_REVIVED, ghost, ghost.tile()));
				game.publishEvent(new GameEvent(game, Info.GHOST_LEAVING_HOUSE, ghost, ghost.tile()));
			}
		}

		case LEAVING_HOUSE -> {
			ghost.setSpeed(game.ghostSpeed / 2);
			boolean leftHouse = ghost.leaveHouse(game.world.ghostHouse());
			if (leftHouse) {
				game.publishEvent(new GameEvent(game, Info.GHOST_LEFT_HOUSE, ghost, ghost.tile()));
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
			if (selectedGameVariant == MS_PACMAN && game.huntingPhase == 0
					&& (ghost.id == RED_GHOST || ghost.id == PINK_GHOST)) {
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
				game.publishEvent(new GameEvent(game, Info.GHOST_ENTERS_HOUSE, ghost, ghost.tile()));
			}
		}

		default -> throw new IllegalArgumentException("Illegal ghost state: " + state);
		}
	}
}
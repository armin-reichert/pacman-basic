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
import static de.amr.games.pacman.model.common.GhostState.FRIGHTENED;
import static de.amr.games.pacman.model.common.GhostState.HUNTING_PAC;
import static java.util.function.Predicate.not;

import java.util.Map;
import java.util.Objects;

import de.amr.games.pacman.controller.event.GameEvent.Info;
import de.amr.games.pacman.controller.event.GameStateChangeEvent;
import de.amr.games.pacman.controller.event.ScatterPhaseStartedEvent;
import de.amr.games.pacman.lib.FiniteStateMachine;
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
		boolean lostPower = game.updatePlayer();
		if (lostPower) {
			// restart (HUNTING) state timer
			state.timer().start();
			log("%s timer restarted: %s", state, state.timer());
		}
	}

	void checkFood() {
		boolean energizerEaten = game.checkFood(game.player.tile());
		if (energizerEaten && game.player.powerTimer.isRunning()) {
			state.timer().stop();
			log("%s timer stopped: %s", state, state.timer());
		}
	}
}
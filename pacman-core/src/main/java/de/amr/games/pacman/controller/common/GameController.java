/*
MIT License

Copyright (c) 2021-22 Armin Reichert

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
package de.amr.games.pacman.controller.common;

import static de.amr.games.pacman.controller.common.GameState.BOOT;
import static de.amr.games.pacman.controller.common.GameState.CREDIT;
import static de.amr.games.pacman.controller.common.GameState.INTRO;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import de.amr.games.pacman.event.GameEvents;
import de.amr.games.pacman.event.GameStateChangeEvent;
import de.amr.games.pacman.event.TriggerUIChangeEvent;
import de.amr.games.pacman.lib.FollowRoute;
import de.amr.games.pacman.lib.fsm.Fsm;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.model.mspacman.MsPacManGame;
import de.amr.games.pacman.model.pacman.PacManGame;

/**
 * Controller (in the sense of MVC) for both (Pac-Man, Ms. Pac-Man) game variants.
 * <p>
 * A finite-state machine with states defined in {@link GameState}. The game data are stored in the model of the
 * selected game, see {@link MsPacManGame} and {@link PacManGame}. Scene selection is not controlled by this class but
 * left to the specific user interface implementations.
 * <p>
 * Missing functionality:
 * <ul>
 * <li><a href= "https://pacman.holenet.info/#CH2_Cornering"><em>Cornering</em></a>: I do not consider cornering as
 * important when the player is controlled by keyboard keys, for a joystick that probably would be more important.</li>
 * <li>Exact level data for Ms. Pac-Man still unclear. Any hints appreciated!
 * <li>Multiple players.</li>
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
public class GameController extends Fsm<GameState, GameModel> {

	private Map<GameVariant, GameModel> games;
	private GameVariant gameVariant;
	private Steering autopilot;
	private Steering normalSteering;
	private GameSoundController sounds;

	public int intermissionTestNumber; // test mode
	public boolean levelTestMode = false; // test mode
	public FollowRoute attractModeSteering = new FollowRoute(); // experimental

	public GameController() {
		states = GameState.values();
		for (var state : states) {
			state.gc = this;
		}
		// map "state change" events to "game state change" events
		addStateChangeListener(
				(oldState, newState) -> GameEvents.publish(new GameStateChangeEvent(context(), oldState, newState)));
		GameEvents.setGame(this::game);

		autopilot = new Autopilot();
		sounds = GameSoundController.NO_SOUND;
		games = Map.of(GameVariant.MS_PACMAN, new MsPacManGame(), GameVariant.PACMAN, new PacManGame());
		selectGame(GameVariant.PACMAN);
	}

	@Override
	public GameModel context() {
		return game();
	}

	public void terminateCurrentState() {
		state().timer().expire();
	}

	public Stream<GameModel> games() {
		return games.values().stream();
	}

	public GameModel game(GameVariant variant) {
		return games.get(Objects.requireNonNull(variant));
	}

	public GameModel game() {
		return game(gameVariant);
	}

	public Steering getSteering() {
		if (!game().hasCredit()) {
			if (game().variant() == GameVariant.PACMAN) {
				return attractModeSteering;
			}
			return autopilot;
		}
		if (game().pac().isAutoControlled()) {
			return autopilot;
		}
		return normalSteering;
	}

	public void setNormalSteering(Steering steering) {
		this.normalSteering = Objects.requireNonNull(steering);
	}

	public void setSounds(GameSoundController sounds) {
		this.sounds = Objects.requireNonNull(sounds);
	}

	public GameSoundController sounds() {
		return game().hasCredit() || state() == GameState.INTERMISSION_TEST ? sounds : GameSoundController.NO_SOUND;
	}

	public void selectGame(GameVariant newVariant) {
		Objects.requireNonNull(newVariant, "Game variant must not be null");
		if (newVariant != gameVariant) {
			var newGame = game(newVariant);
			// transfer credit
			if (gameVariant != null) { // initially it is null
				var oldGame = game(gameVariant);
				newGame.setCredit(oldGame.credit());
				oldGame.setCredit(0);
			}
			if (newVariant == GameVariant.PACMAN) {
				attractModeSteering.setRoute(PacManGame.ATTRACT_ROUTE_PACMAN);
			}
			gameVariant = newVariant;
			restartInState(BOOT);
		}
	}

	public void restartIntro() {
		sounds().stopAll();
		if (state() != CREDIT && state() != INTRO) {
			game().consumeCredit();
		}
		restartInState(INTRO);
		GameEvents.publish(new TriggerUIChangeEvent(game()));
	}

	public void reboot() {
		levelTestMode = false;
		sounds().stopAll();
		restartInState(BOOT);
		GameEvents.publish(new TriggerUIChangeEvent(game()));
	}
}
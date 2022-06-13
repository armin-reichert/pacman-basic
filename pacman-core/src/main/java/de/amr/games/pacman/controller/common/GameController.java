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

import static de.amr.games.pacman.controller.common.GameState.CREDIT;
import static de.amr.games.pacman.controller.common.GameState.INTRO;
import static de.amr.games.pacman.controller.common.GameState.READY;

import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import de.amr.games.pacman.event.GameEventing;
import de.amr.games.pacman.event.GameStateChangeEvent;
import de.amr.games.pacman.event.TriggerUIChangeEvent;
import de.amr.games.pacman.lib.fsm.Fsm;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.model.common.actors.Pac;
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

	private final Map<GameVariant, GameModel> games;
	private GameVariant currentGameVariant;
	private int credit;
	private boolean autoMoving;
	private final Consumer<Pac> autopilot = new Autopilot(this::game);
	private Consumer<Pac> pacController;

	public GameController(GameVariant variant) {
		super(GameState.values());
//		logging = true;
		games = Map.of(GameVariant.MS_PACMAN, new MsPacManGame(), GameVariant.PACMAN, new PacManGame());
		// map state change events to game events from selected game model:
		addStateChangeListener(
				(oldState, newState) -> GameEventing.publish(new GameStateChangeEvent(game(), oldState, newState)));
		selectGame(variant);
	}

	@Override
	public GameModel context() {
		return game();
	}

	public GameModel game() {
		return game(currentGameVariant);
	}

	public GameModel game(GameVariant variant) {
		return games.get(variant);
	}

	public boolean isAutoMoving() {
		return autoMoving;
	}

	public void toggleAutoMoving() {
		autoMoving = !autoMoving;
	}

	public void steer(Pac player) {
		Consumer<Pac> steering = autoMoving || credit == 0 ? autopilot : pacController;
		steering.accept(player);
	}

	public int credit() {
		return credit;
	}

	public void consumeCredit() {
		if (credit > 0) {
			--credit;
		}
	}

	public void increaseCredit() {
		++credit;
	}

	public void setPacController(Consumer<Pac> pacController) {
		this.pacController = Objects.requireNonNull(pacController);
	}

	// public actions

	public void selectGame(GameVariant variant) {
		currentGameVariant = variant;
		GameEventing.setGame(game());
		restartInInitialState(INTRO);
	}

	public void requestGame() {
		if (credit > 0 && (state() == INTRO || state() == CREDIT)) {
			game().reset();
			changeState(READY);
		}
	}

	public void returnToIntro() {
		if (state() != CREDIT && state() != INTRO) {
			consumeCredit();
		}
		restartInInitialState(INTRO);
		GameEventing.publish(new TriggerUIChangeEvent(game()));
	}

	public void toggleIsPacImmune() {
		games.values().forEach(game -> game.isPacImmune = !game.isPacImmune);
	}

}
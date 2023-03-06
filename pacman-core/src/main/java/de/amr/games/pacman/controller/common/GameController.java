/*
MIT License

Copyright (c) 2021-2023 Armin Reichert

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

import static de.amr.games.pacman.event.GameEvents.publishGameEvent;
import static de.amr.games.pacman.event.GameEvents.publishSoundEvent;

import java.util.Objects;

import de.amr.games.pacman.event.GameEvents;
import de.amr.games.pacman.event.GameStateChangeEvent;
import de.amr.games.pacman.lib.fsm.Fsm;
import de.amr.games.pacman.lib.steering.RuleBasedSteering;
import de.amr.games.pacman.model.common.GameLevel;
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

	private static GameModel newGameModel(GameVariant variant) {
		return switch (variant) {
		case MS_PACMAN -> new MsPacManGame();
		case PACMAN -> new PacManGame();
		default -> throw new IllegalArgumentException("Illegal game variant: '%s'".formatted(variant));
		};
	}

	private GameModel game;
	private Steering autopilot = new RuleBasedSteering();
	private Steering manualPacSteering = Steering.NONE;
	private boolean autoControlled;

	public GameController(GameVariant variant) {
		Objects.requireNonNull(variant);
		states = GameState.values();
		for (var state : states) {
			state.gc = this;
		}
		// map FSM state change events to "game state change" events
		addStateChangeListener(
				(oldState, newState) -> publishGameEvent(new GameStateChangeEvent(game, oldState, newState)));
		game = newGameModel(variant);
		GameEvents.setGameController(this);
	}

	@Override
	public GameModel context() {
		return game;
	}

	public GameModel game() {
		return game;
	}

	public boolean isAutoControlled() {
		return autoControlled;
	}

	public void setAutoControlled(boolean autoControlled) {
		this.autoControlled = autoControlled;
	}

	public void toggleAutoControlled() {
		autoControlled = !autoControlled;
	}

	public Steering steering() {
		return autoControlled ? autopilot : manualPacSteering;
	}

	public void setManualPacSteering(Steering steering) {
		this.manualPacSteering = Objects.requireNonNull(steering);
	}

	// Game commands

	/**
	 * Creates a new game as specified by the given variant and reboots. Keeps immunity and credit.
	 * 
	 * @param variant Pac-Man or Ms. Pac-Man
	 */
	public void selectGameVariant(GameVariant variant) {
		switch (state()) {
		case INTRO -> {
			boolean immune = game.isImmune();
			int credit = game.credit();
			game = newGameModel(variant);
			game.setImmune(immune);
			game.setCredit(credit);
			restart(GameState.BOOT);
		}
		default -> {
			// not supported
		}
		}
	}

	/**
	 * Adds credit (simulates insertion of a coin) and switches to the credit scene.
	 */
	public void addCredit() {
		if (!game.isPlaying()) {
			boolean added = game.changeCredit(1);
			if (added) {
				publishSoundEvent(GameModel.SE_CREDIT_ADDED);
			}
			if (state() != GameState.CREDIT) {
				changeState(GameState.CREDIT);
			}
		}
	}

	public void startPlaying() {
		if ((state() == GameState.INTRO || state() == GameState.CREDIT) && game.hasCredit()) {
			changeState(GameState.READY);
		}
	}

	public void startCutscenesTest() {
		if (state() == GameState.INTRO) {
			game.intermissionTestNumber = 1;
			changeState(GameState.INTERMISSION_TEST);
		}
	}

	public void cheatEatAllPellets() {
		if (game.isPlaying() && state() == GameState.HUNTING) {
			game.level().ifPresent(GameLevel::removeAllPellets);
		}
	}

	public void cheatKillAllEatableGhosts() {
		if (game.isPlaying() && state() == GameState.HUNTING) {
			game.level().ifPresent(level -> {
				level.killAllHuntingAndFrightenedGhosts();
				changeState(GameState.GHOST_DYING);
			});
		}
	}

	public void cheatEnterNextLevel() {
		if (game.isPlaying() && state() == GameState.HUNTING) {
			game.level().ifPresent(level -> {
				var world = level.world();
				world.tiles().forEach(world::removeFood);
				changeState(GameState.LEVEL_COMPLETE);
			});
		}
	}
}
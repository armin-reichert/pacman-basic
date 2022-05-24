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
package de.amr.games.pacman.controller.common.event;

import de.amr.games.pacman.controller.common.GameState;
import de.amr.games.pacman.model.common.GameModel;

/**
 * Event indicating a game state change.
 * 
 * @author Armin Reichert
 */
public class GameStateChangeEvent extends GameEvent {

	public final GameState oldGameState;
	public final GameState newGameState;

	public GameStateChangeEvent(GameModel game, GameState oldGameState, GameState newGameState) {
		super(game, GameEventType.GAME_STATE_CHANGED, null, null);
		this.oldGameState = oldGameState;
		this.newGameState = newGameState;
	}

	@Override
	public String toString() {
		return "%s(%s->%s)".formatted(getClass().getSimpleName(), oldGameState, newGameState);
	}
}
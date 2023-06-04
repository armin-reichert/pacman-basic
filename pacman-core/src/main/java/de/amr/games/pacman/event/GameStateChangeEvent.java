/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.event;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.model.GameModel;

/**
 * Event indicating a game state change.
 * 
 * @author Armin Reichert
 */
public class GameStateChangeEvent extends GameEvent {

	public final GameState oldGameState;
	public final GameState newGameState;

	public GameStateChangeEvent(GameModel game, GameState oldGameState, GameState newGameState) {
		super(game, GameEvent.GAME_STATE_CHANGED, null);
		this.oldGameState = oldGameState;
		this.newGameState = newGameState;
	}

	@Override
	public String toString() {
		return String.format("%s(%s->%s)", getClass().getSimpleName(), oldGameState, newGameState);
	}
}
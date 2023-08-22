/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.event;

import static de.amr.games.pacman.lib.Globals.checkGameNotNull;
import static de.amr.games.pacman.lib.Globals.checkNotNull;

import java.util.Optional;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.model.GameModel;

/**
 * @author Armin Reichert
 */
public class GameEvent {

	public final GameEventType type;

	public GameModel game;
	public Vector2i tile;
	public GameState oldState;
	public GameState newState;

	public static GameEvent of(GameEventType type, GameModel game, Vector2i tile) {
		checkNotNull(type);
		checkGameNotNull(game);
		var event = new GameEvent(type);
		event.game = game;
		event.tile = tile;
		return event;
	}

	public static GameEvent of(GameModel game, GameState oldState, GameState newState) {
		checkGameNotNull(game);
		var event = new GameEvent(GameEventType.GAME_STATE_CHANGED);
		event.game = game;
		event.oldState = oldState;
		event.newState = newState;
		return event;
	}

	private GameEvent(GameEventType type)  {
		this.type = type;
	}

	public Optional<Vector2i> tile() {
		return Optional.ofNullable(tile);
	}
}
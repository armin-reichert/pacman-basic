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
	public final GameModel     game;
	public final Vector2i      tile;

	public static GameEvent of(GameEventType type, GameModel game, Vector2i tile) {
		checkNotNull(type);
		checkGameNotNull(game);
		return new GameEvent(type, game, tile);
	}

	public GameEvent(GameEventType type, GameModel game, Vector2i tile)  {
		this.type = type;
		this.game = game;
		this.tile = tile;
	}

	public Optional<Vector2i> tile() {
		return Optional.ofNullable(tile);
	}
}
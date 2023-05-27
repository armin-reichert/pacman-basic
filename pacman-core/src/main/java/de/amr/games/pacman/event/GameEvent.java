/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.event;

import static de.amr.games.pacman.lib.Globals.checkGameNotNull;
import static de.amr.games.pacman.lib.Globals.checkNotNull;

import java.util.Optional;

import de.amr.games.pacman.lib.math.Vector2i;
import de.amr.games.pacman.model.GameModel;

/**
 * Base class for events fired during game play. This class is a kind of compromise between separate subclasses for each
 * event type and a fat base class.
 * 
 * @author Armin Reichert
 */
public class GameEvent {

	public final GameModel game;
	public final GameEventType type;
	public final Optional<Vector2i> tile;

	/**
	 * @param game game model
	 * @param type event type, see {@link GameEventType}
	 * @param tile tile related to event, may be {@code null}
	 */
	public GameEvent(GameModel game, GameEventType type, Vector2i tile) {
		checkGameNotNull(game);
		checkNotNull(type);
		this.game = game;
		this.type = type;
		this.tile = Optional.ofNullable(tile);
	}

	@Override
	public String toString() {
		var tileStr = tile.isPresent() ? String.format(" tile: %s", tile.get()) : "";
		return String.format("%s%s", type, tileStr);
	}
}
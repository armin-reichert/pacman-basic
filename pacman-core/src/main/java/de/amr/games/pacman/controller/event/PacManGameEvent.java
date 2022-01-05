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
package de.amr.games.pacman.controller.event;

import java.util.Objects;
import java.util.Optional;

import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.Ghost;

/**
 * Base class for events fired during game play. This class is a kind of compromise between separate subclasses for each
 * event type and a fat base class.
 * 
 * @author Armin Reichert
 */
public class PacManGameEvent {

	public enum Info {
		BONUS_ACTIVATED, BONUS_EATEN, BONUS_EXPIRED, EXTRA_LIFE, PLAYER_FOUND_FOOD, PLAYER_GAINS_POWER,
		PLAYER_LOSING_POWER, PLAYER_LOST_POWER, GHOST_ENTERS_HOUSE, GHOST_LEAVING_HOUSE, GHOST_LEFT_HOUSE,
		GHOST_RETURNS_HOME, GAME_STATE_CHANGE, OTHER;
	}

	public final GameModel game;
	public final Info info;
	public final Optional<V2i> tile; // the optional tile where this event occurred
	public final Optional<Ghost> ghost; // the optional ghost this event relates to

	public PacManGameEvent(GameModel game, Info info, Ghost ghost, V2i tile) {
		this.game = Objects.requireNonNull(game);
		this.info = Objects.requireNonNull(info);
		this.tile = Optional.ofNullable(tile);
		this.ghost = Optional.ofNullable(ghost);
	}

	@Override
	public String toString() {
		return ghost.isEmpty() ? //
				String.format("%s: tile %s", info, tile.orElse(null))
				: String.format("%s: tile %s, ghost %s", info, tile.orElse(null), ghost.orElse(null));
	}
}
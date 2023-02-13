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
package de.amr.games.pacman.event;

import java.util.Objects;
import java.util.Optional;

import de.amr.games.pacman.lib.math.Vector2i;
import de.amr.games.pacman.model.common.GameModel;

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
		this.game = Objects.requireNonNull(game);
		this.type = Objects.requireNonNull(type);
		this.tile = Optional.ofNullable(tile);
	}

	@Override
	public String toString() {
		var tileStr = tile.isPresent() ? " tile: %s".formatted(tile.get()) : "";
		return String.format("%s%s", type, tileStr);
	}
}
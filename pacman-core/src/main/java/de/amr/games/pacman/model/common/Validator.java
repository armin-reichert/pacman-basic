/*
MIT License

Copyright (c) 2022 Armin Reichert

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
package de.amr.games.pacman.model.common;

import java.util.Objects;

import de.amr.games.pacman.lib.math.Vector2i;
import de.amr.games.pacman.lib.steering.Direction;

/**
 * @author Armin Reichert
 */
public class Validator {
	private static final String MSG_GAME_NULL = "Game model must not be null";
	private static final String MSG_LEVEL_NULL = "Game level must not be null";
	private static final String MSG_TILE_NULL = "Tile must not be null";
	private static final String MSG_DIR_NULL = "Direction must not be null";

	public static void checkNotNull(Object value) {
		Objects.requireNonNull(value, "");
	}

	public static void checkNotNull(Object value, String message) {
		Objects.requireNonNull(value, message);
	}

	public static void checkGameNotNull(GameModel game) {
		checkNotNull(game, MSG_GAME_NULL);
	}

	public static void checkGhostID(byte id) {
		if (id < 0 || id > 3) {
			throw new IllegalGhostIDException(id);
		}
	}

	public static void checkGameVariant(GameVariant variant) {
		if (variant == null) {
			throw new IllegalGameVariantException(variant);
		}
		switch (variant) {
		case MS_PACMAN, PACMAN -> { // ok
		}
		default -> throw new IllegalGameVariantException(variant);
		}
	}

	public static void checkLevelNumber(int number) {
		if (number < 1) {
			throw new IllegalLevelNumberException(number);
		}
	}

	public static void checkTileNotNull(Vector2i tile) {
		checkNotNull(tile, MSG_TILE_NULL);
	}

	public static void checkLevelNotNull(GameLevel level) {
		checkNotNull(level, MSG_LEVEL_NULL);
	}

	public static void checkDirectionNotNull(Direction dir) {
		checkNotNull(dir, MSG_DIR_NULL);
	}

	public static double requirePositive(double value, String messageFormat) {
		if (value < 0) {
			throw new IllegalArgumentException(messageFormat.formatted(value));
		}
		return value;
	}

	public static double requirePositive(double value) {
		return requirePositive(value, "%f must be positive");
	}

}
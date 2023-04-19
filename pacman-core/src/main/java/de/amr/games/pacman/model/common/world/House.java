/*
MIT License

Copyright (c) 2023 Armin Reichert

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

package de.amr.games.pacman.model.common.world;

import java.util.List;
import java.util.Objects;

import de.amr.games.pacman.lib.math.Vector2f;
import de.amr.games.pacman.lib.math.Vector2i;
import de.amr.games.pacman.model.common.Validator;

/**
 * @author Armin Reichert
 */
public record House(Vector2i topLeftTile, Vector2i size, Door door, List<Vector2f> seatPositions, Vector2f center) {

	public House {
		Validator.checkTileNotNull(topLeftTile);
		Validator.checkNotNull(size);
		Validator.checkNotNull(door);
		Validator.checkNotNull(seatPositions);
		if (seatPositions.size() != 3) {
			throw new IllegalArgumentException("There must be exactly 3 seat positions");
		}
		if (seatPositions.stream().anyMatch(Objects::isNull)) {
			throw new IllegalArgumentException("House seat position must not be null");
		}
		Validator.checkNotNull(center);
	}

	/**
	 * @param tile some tile
	 * @return tells if the given tile is part of this house
	 */
	public boolean contains(Vector2i tile) {
		Vector2i bottomRightTileOutside = topLeftTile.plus(size());
		return tile.x() >= topLeftTile.x() && tile.x() < bottomRightTileOutside.x() //
				&& tile.y() >= topLeftTile.y() && tile.y() < bottomRightTileOutside.y();
	}
}
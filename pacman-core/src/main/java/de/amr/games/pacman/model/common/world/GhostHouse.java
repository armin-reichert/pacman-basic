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

package de.amr.games.pacman.model.common.world;

import static de.amr.games.pacman.model.common.world.World.HTS;
import static de.amr.games.pacman.model.common.world.World.TS;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2d;
import de.amr.games.pacman.lib.V2i;

/**
 * @author Armin Reichert
 */
public interface GhostHouse {

	V2i topLeftTile();

	V2i size();

	V2i doorTileLeft();

	V2i doorTileRight();

	default V2i entry() {
		return doorTileLeft().plus(Direction.UP.vec);
	}

	default V2d doorsCenter() {
		return new V2d(doorTileLeft().scaled(TS)).plus(TS, HTS);
	}

	default V2d middleSeatCenter() {
		return new V2d(seatMiddle().scaled(TS)).plus(HTS, HTS);
	}

	default boolean isDoor(V2i tile) {
		return tile.equals(doorTileLeft()) || tile.equals(doorTileRight());
	}

	V2i seatLeft();

	V2i seatMiddle();

	default boolean contains(V2i tile) {
		V2i topLeft = topLeftTile();
		V2i bottomRight = topLeft.plus(size());
		return tile.x >= topLeft.x && tile.x <= bottomRight.x && tile.y >= topLeft.y && tile.y <= bottomRight.y;
	}

	V2i seatRight();
}
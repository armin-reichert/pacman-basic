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

import de.amr.games.pacman.lib.V2d;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.actors.Creature;

/**
 * @author Armin Reichert
 */
public interface GhostHouse {

	V2i topLeftTile();

	V2i size();

	V2i entryTile();

	V2i doorLeftTile();

	V2i doorRightTile();

	default V2d doorsCenterPosition() {
		return new V2d(doorLeftTile().scaled(TS)).plus(TS, HTS);
	}

	default boolean isDoorTile(V2i tile) {
		return tile.equals(doorLeftTile()) || tile.equals(doorRightTile());
	}

	default boolean contains(V2i tile) {
		V2i topLeft = topLeftTile();
		V2i bottomRight = topLeft.plus(size());
		return tile.x >= topLeft.x && tile.x <= bottomRight.x && tile.y >= topLeft.y && tile.y <= bottomRight.y;
	}

	boolean atHouseEntry(Creature creature);

	boolean leadGuestOutOfHouse(Creature guest);

	boolean leadGuestToTile(Creature guest, V2i targetTile);
}
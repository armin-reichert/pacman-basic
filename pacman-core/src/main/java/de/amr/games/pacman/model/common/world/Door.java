/*
MIT License

Copyright (c) 2021-2023 Armin Reichert

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

import static de.amr.games.pacman.model.common.world.World.TS;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import de.amr.games.pacman.lib.math.Vector2f;
import de.amr.games.pacman.lib.math.Vector2i;

/**
 * @author Armin Reichert
 */
public class Door {

	private Vector2i leftUpperTile;
	private int sizeInTiles;

	public Door(Vector2i leftUpperTile, int sizeInTiles) {
		this.leftUpperTile = leftUpperTile;
		this.sizeInTiles = sizeInTiles;
	}

	/**
	 * @return left upper tile
	 */
	public Vector2i position() {
		return leftUpperTile;
	}

	/**
	 * @return size in tiles
	 */
	public int size() {
		return sizeInTiles;
	}

	/**
	 * @return stream of all tiles occupied by this door
	 */
	public Stream<Vector2i> tiles() {
		return IntStream.range(0, sizeInTiles).mapToObj(x -> leftUpperTile.plus(x, 0));
	}

	/**
	 * @param tile some tile
	 * @return tells if the given tile is occupied by this door
	 */
	public boolean contains(Vector2i tile) {
		for (int x = 0; x < sizeInTiles; ++x) {
			if (tile.equals(leftUpperTile.plus(x, 0))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @return position where ghost can enter the door
	 */
	public Vector2f entryPosition() {
		// TODO this is only correct for a horizontal door entered from above
		return leftUpperTile.minus(0, 1).toFloatVec().scaled(TS).plus(World.HTS + 0.5f * sizeInTiles, 0);
	}

	/**
	 * @return tile where door can be entered
	 */
	public Vector2i entryTile() {
		return leftUpperTile.plus(sizeInTiles / 2, 0);
	}
}
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
package de.amr.games.pacman.model.common.world;

import static de.amr.games.pacman.lib.V2i.v;

import de.amr.games.pacman.lib.V2i;

/**
 * The ghost house from the Arcade version of the games. Door on top, three seats inside.
 * 
 * @author Armin Reichert
 */
public class ArcadeGhostHouse {

	/** Size (width, height) in tiles. */
	public final V2i size;

	/** Top-left tile. */
	public final V2i topLeft;

	/** Left door. */
	public final V2i doorTileLeft;

	/** Left seat in house. */
	public final V2i seatTileLeft;

	/** Center seat in house. */
	public final V2i seatTileMiddle;

	/** Right seat in house. */
	public final V2i seatTileRight;

	public ArcadeGhostHouse() {
		topLeft = v(10, 15);
		size = v(7, 4);
		doorTileLeft = v(13, 15);
		seatTileLeft = v(11, 17);
		seatTileMiddle = v(13, 17);
		seatTileRight = v(15, 17);
	}

	public boolean contains(V2i tile) {
		V2i bottomRight = topLeft.plus(size);
		return tile.x >= topLeft.x && tile.x <= bottomRight.x && tile.y >= topLeft.y && tile.y <= bottomRight.y;
	}

	public boolean isDoor(V2i tile) {
		return (tile.x == doorTileLeft.x || tile.x == doorTileLeft.x + 1) && tile.y == doorTileLeft.y;
	}
}
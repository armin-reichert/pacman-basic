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

import de.amr.games.pacman.lib.V2i;

/**
 * The ghost house.
 * 
 * @author Armin Reichert
 */
public class GhostHouse {

	/** Size (width, height) in tiles. */
	public final V2i size;

	/** Top-left tile. */
	public final V2i topLeft;

	/** Left door. */
	public V2i doorLeft;

	/** Right door. */
	public V2i doorRight;

	/** Left seat in house. */
	public V2i seatLeft;

	/** Center seat in house. */
	public V2i seatCenter;

	/** Right seat in house. */
	public V2i seatRight;

	public GhostHouse(V2i topLeft, V2i size) {
		this.topLeft = topLeft;
		this.size = size;
	}

	public V2i leftEntry() {
		return doorLeft.minus(0, 1);
	}

	public V2i rightEntry() {
		return doorRight.minus(0, 1);
	}

	public boolean contains(V2i tile) {
		V2i bottomRight = topLeft.plus(size);
		return tile.x >= topLeft.x && tile.x <= bottomRight.x && tile.y >= topLeft.y && tile.y <= bottomRight.y;
	}

	public boolean isDoor(V2i tile) {
		return tile.equals(doorLeft) || tile.equals(doorRight);
	}
}
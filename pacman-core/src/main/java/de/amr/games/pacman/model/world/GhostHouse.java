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
package de.amr.games.pacman.model.world;

import java.util.List;

import de.amr.games.pacman.lib.V2i;

/**
 * The ghost house.
 * 
 * @author Armin Reichert
 */
public class GhostHouse {

	/** Size (width, height) in tiles. */
	public V2i size;

	/** Top-left tile. */
	public V2i topLeft;

	/** Left entry tile. */
	public V2i entry;

	/** Tiles with ghost seats in order "above entry", "inside left", "inside middle", "inside right". */
	public List<V2i> seats;

	/** Tiles with doors. */
	public List<V2i> doors;

	public GhostHouse(V2i topLeft, V2i size) {
		this.topLeft = topLeft;
		this.size = size;
	}

	public boolean contains(V2i tile) {
		V2i bottomRight = topLeft.plus(size.x, size.y);
		return tile.x >= topLeft.x && tile.x <= bottomRight.x && tile.y >= topLeft.y && tile.y <= bottomRight.y;
	}
}
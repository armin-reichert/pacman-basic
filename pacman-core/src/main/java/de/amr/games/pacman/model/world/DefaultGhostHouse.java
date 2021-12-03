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

import java.util.Collections;
import java.util.List;

import de.amr.games.pacman.lib.V2i;

/**
 * The ghost house as we know it.
 * 
 * @author Armin Reichert
 */
public class DefaultGhostHouse implements GhostHouse {

	public V2i sizeInTiles;
	public V2i topLeftTile;
	public V2i entryTile;
	public List<V2i> seatTiles;
	public List<V2i> doorTiles;

	public DefaultGhostHouse(V2i topLeftTile, V2i sizeInTiles) {
		this.topLeftTile = topLeftTile;
		this.sizeInTiles = sizeInTiles;
	}

	@Override
	public int numTilesX() {
		return sizeInTiles.x;
	}

	@Override
	public int numTilesY() {
		return sizeInTiles.y;
	}

	@Override
	public V2i topLeftTile() {
		return topLeftTile;
	}

	@Override
	public V2i seat(int index) {
		return seatTiles.get(index);
	}

	@Override
	public V2i entryTile() {
		return entryTile;
	}

	@Override
	public List<V2i> doorTiles() {
		return Collections.unmodifiableList(doorTiles);
	}
}
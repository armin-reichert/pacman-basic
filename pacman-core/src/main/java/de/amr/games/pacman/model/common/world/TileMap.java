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

import de.amr.games.pacman.lib.math.Vector2i;

/**
 * @author Armin Reichert
 */
public class TileMap {

	private final byte[][] content;

	public TileMap(byte[][] data) {
		content = validateTileMapData(data);
	}

	public int numRows() {
		return content.length;
	}

	public int numCols() {
		return content[0].length;
	}

	private byte[][] validateTileMapData(byte[][] data) {
		if (data == null) {
			throw new IllegalArgumentException("Map data missing");
		}
		if (data.length == 0) {
			throw new IllegalArgumentException("Map data empty");
		}
		var firstRow = data[0];
		if (firstRow.length == 0) {
			throw new IllegalArgumentException("Map data empty");
		}
		for (int i = 0; i < data.length; ++i) {
			if (data[i].length != firstRow.length) {
				throw new IllegalArgumentException("Map has differently sized rows");
			}
		}
		for (int i = 0; i < data.length; ++i) {
			for (int j = 0; j < firstRow.length; ++j) {
				byte b = data[i][j];
				if (!TileContent.isValid(b)) {
					throw new IllegalArgumentException("Invalid tile map content '%s' at row %d column %d".formatted(b, i, j));
				}
			}
		}
		return data;
	}

	/**
	 * @param tile some tile (may be outside world bound)
	 * @return the content at the given tile or empty space if outside world
	 */
	public byte content(Vector2i tile) {
		return content(tile.y(), tile.x(), TileContent.SPACE);
	}

	public byte content(int row, int col) {
		if (!insideBounds(row, col)) {
			throwOutOfBoundsError(row, col);
		}
		return content[row][col];
	}

	public byte content(int row, int col, byte defaultContent) {
		if (!insideBounds(row, col)) {
			return defaultContent;
		}
		return content[row][col];
	}

	public boolean insideBounds(int row, int col) {
		return 0 <= row && row < numRows() && 0 <= col && col < numCols();
	}

	private void throwOutOfBoundsError(int row, int col) {
		throw new IndexOutOfBoundsException(
				"Coordinate (%d, %d) is outside of map bounds (%d rows, %d cols)".formatted(row, col, numRows(), numCols()));
	}
}
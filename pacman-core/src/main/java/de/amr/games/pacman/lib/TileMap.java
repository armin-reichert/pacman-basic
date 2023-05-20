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

package de.amr.games.pacman.lib;

/**
 * @author Armin Reichert
 */
public class TileMap {

	private final byte[][] content;

	public TileMap(byte[][] data) {
		content = validate(data);
	}

	public int numRows() {
		return content.length;
	}

	public int numCols() {
		return content[0].length;
	}

	public byte content(int row, int col) {
		if (!insideBounds(row, col)) {
			throwOutOfBounds(row, col);
		}
		return content[row][col];
	}

	public byte content(int row, int col, byte defaultContent) {
		if (!insideBounds(row, col)) {
			return defaultContent;
		}
		return content[row][col];
	}

	private boolean insideBounds(int row, int col) {
		return 0 <= row && row < numRows() && 0 <= col && col < numCols();
	}

	private byte[][] validate(byte[][] data) {
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
		return data;
	}

	private void throwOutOfBounds(int row, int col) {
		throw new IndexOutOfBoundsException(String.format("Coordinate (%d, %d) is outside of map bounds (%d rows, %d cols)",
				row, col, numRows(), numCols()));
	}
}
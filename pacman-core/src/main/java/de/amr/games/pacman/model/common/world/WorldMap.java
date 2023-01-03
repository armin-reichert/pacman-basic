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

import de.amr.games.pacman.lib.U;

/**
 * @author Armin Reichert
 */
public class WorldMap {
	//@formatter:off
	public static final byte SPACE           = 0;
	public static final byte WALL            = 1;
	public static final byte TUNNEL          = 2;
	public static final byte PELLET          = 3;
	public static final byte ENERGIZER       = 4;
	public static final byte PELLET_EATEN    = 5;
	public static final byte ENERGIZER_EATEN = 6;
	//@formatter:on

	private final byte[][] mapData;
	private final int numRows;
	private final int numCols;

	public WorldMap(byte[][] mapData) {
		validateMapData(mapData);
		this.mapData = U.copyByteArray2D(mapData);
		this.numRows = mapData.length;
		this.numCols = mapData[0].length;
	}

	private void validateMapData(byte[][] mapData) {
		if (mapData == null) {
			throw new IllegalArgumentException("Map is null");
		}
		if (mapData.length == 0) {
			throw new IllegalArgumentException("Map has no rows");
		}
		var firstRow = mapData[0];
		if (firstRow.length == 0) {
			throw new IllegalArgumentException("Map has no columns");
		}
		for (int i = 0; i < mapData.length; ++i) {
			if (mapData[i].length != firstRow.length) {
				throw new IllegalArgumentException("Map has differently sized rows");
			}
		}
		for (int i = 0; i < mapData.length; ++i) {
			for (int j = 0; j < firstRow.length; ++j) {
				byte content = mapData[i][j];
				if (content < 0 || content > 6) {
					throw new IllegalArgumentException(
							"Map has invalid content '%s' at row %d column %d".formatted(content, i, j));
				}
			}
		}
	}

	public int numRows() {
		return numRows;
	}

	public int numCols() {
		return numCols;
	}

	public byte get(int row, int col) {
		if (!insideBounds(row, col)) {
			throwOutOfBoundsError(row, col);
		}
		return mapData[row][col];
	}

	public byte get(int row, int col, byte defaultContent) {
		if (!insideBounds(row, col)) {
			return defaultContent;
		}
		return mapData[row][col];
	}

	public void set(int row, int col, byte b) {
		if (!insideBounds(row, col)) {
			throwOutOfBoundsError(row, col);
		}
		mapData[row][col] = b;
	}

	private boolean insideBounds(int row, int col) {
		return 0 <= row && row < numRows && 0 <= col && col < numCols;
	}

	private void throwOutOfBoundsError(int row, int col) {
		throw new IndexOutOfBoundsException(
				"Coordinate (%d, %d) is outside of map bounds (%d rows, %d cols)".formatted(row, col, numRows, numCols));
	}
}
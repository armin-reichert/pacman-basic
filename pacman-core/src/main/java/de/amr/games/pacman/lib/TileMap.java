/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
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
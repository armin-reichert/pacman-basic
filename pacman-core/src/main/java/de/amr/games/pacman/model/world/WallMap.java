package de.amr.games.pacman.model.world;

import java.io.PrintWriter;
import java.io.Writer;

/**
 * Provides information about the location of walls around inaccessible areas in a world map.
 * 
 * @author Armin Reichert
 */
public interface WallMap {

	public static final byte EMPTY = 0;
	public static final byte CORNER = 1;
	public static final byte HORIZONTAL = 2;
	public static final byte VERTICAL = 3;

	default void print(Writer w, boolean symbols) {
		byte[][] info = info();
		try (PrintWriter p = new PrintWriter(w)) {
			for (int y = 0; y < info.length; ++y) {
				for (int x = 0; x < info[0].length; ++x) {
					p.print(symbols ? String.valueOf(symbol(get(x, y))) : get(x, y));
				}
				p.println();
			}
		}
	}

	private static char symbol(byte b) {
		switch (b) {
		case WallMap.CORNER:
			return '+';
		case WallMap.EMPTY:
			return ' ';
		case WallMap.HORIZONTAL:
			return '-';
		case WallMap.VERTICAL:
			return '|';
		default:
			return '?';
		}
	}

	/**
	 * Resolution of this wall map. Each tile is divided into this number of blocks horizontally and
	 * vertically.
	 * 
	 * @return resolution of this map
	 */
	int resolution();

	/**
	 * @return array of size {@code resolution * world.numRows() x resolution * world.numCols()}
	 *         indicating where and what kind of wall should be placed
	 */
	byte[][] info();

	byte get(int x, int y);

	int sizeX();

	int sizeY();
}
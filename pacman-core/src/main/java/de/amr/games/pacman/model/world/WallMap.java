package de.amr.games.pacman.model.world;

import java.io.PrintWriter;
import java.io.Writer;

/**
 * Provides information about the location of walls around inaccessible areas in a world map.
 * 
 * @author Armin Reichert
 */
public class WallMap {

	public static final byte EMPTY = 0;
	public static final byte CORNER = 1;
	public static final byte HORIZONTAL = 2;
	public static final byte VERTICAL = 3;

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

	public static WallMap build(int resolution, PacManGameWorld world) {
		return new WallScanner(resolution).scan(world);
	}

	public final int resolution;
	public final byte[][] info;

	public WallMap(int resolution, byte[][] info) {
		this.resolution = resolution;
		this.info = info;
	}

	public byte get(int x, int y) {
		return info[y][x];
	}

	public int sizeX() {
		return info[0].length;
	}

	public int sizeY() {
		return info.length;
	}

	public void print(Writer w, boolean symbols) {
		try (PrintWriter p = new PrintWriter(w)) {
			for (int y = 0; y < info.length; ++y) {
				for (int x = 0; x < info[0].length; ++x) {
					p.print(symbols ? String.valueOf(symbol(get(x, y))) : get(x, y));
				}
				p.println();
			}
		}
	}
}
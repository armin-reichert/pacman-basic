package de.amr.games.pacman.model.world;

import java.io.PrintWriter;
import java.io.Writer;

/**
 * Provides information about rooms, walls, doors etc.
 * 
 * @author Armin Reichert
 */
public class FloorPlan {

	public static final byte EMPTY = 0;
	public static final byte CORNER = 1;
	public static final byte HWALL = 2;
	public static final byte VWALL = 3;
	public static final byte DOOR = 4;

	private static char symbol(byte b) {
		switch (b) {
		case FloorPlan.CORNER:
			return '+';
		case FloorPlan.EMPTY:
			return ' ';
		case FloorPlan.HWALL:
			return '\u2014';
		case FloorPlan.VWALL:
			return '|';
		case FloorPlan.DOOR:
			return 'd';
		default:
			return '?';
		}
	}

	public static FloorPlan build(int resolution, PacManGameWorld world) {
		return new FloorPlanBuilder(resolution).build(world);
	}

	private final byte[][] info;

	public FloorPlan(byte[][] info) {
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
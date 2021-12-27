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

	private final byte[][] info;

	public FloorPlan(int resolution, PacManGameWorld world) {
		info = new FloorPlanBuilder(resolution).createFloorPlanInfo(world);
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
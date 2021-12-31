package de.amr.games.pacman.model.world;

import java.io.PrintWriter;
import java.io.Writer;

public class FloorPlanPrinter {

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

	public void print(FloorPlan fp, Writer w, boolean symbols) {
		PrintWriter p = new PrintWriter(w);
		for (int y = 0; y < fp.sizeY(); ++y) {
			for (int x = 0; x < fp.sizeX(); ++x) {
				p.print(symbols ? String.valueOf(symbol(fp.get(x, y))) : fp.get(x, y));
			}
			p.println();
		}
	}
}
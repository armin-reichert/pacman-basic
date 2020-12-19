package de.amr.games.pacman.lib;

import static de.amr.games.pacman.core.World.TS;

public class Functions {

	public static int t(int nTiles) {
		return nTiles * TS;
	}

	public static boolean differsAtMost(float value, float target, float tolerance) {
		return Math.abs(value - target) <= tolerance;
	}
}
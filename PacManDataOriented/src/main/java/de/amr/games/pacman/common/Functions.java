package de.amr.games.pacman.common;


public class Functions {

	public static boolean differsAtMost(float value, float target, float tolerance) {
		return Math.abs(value - target) <= tolerance;
	}

}

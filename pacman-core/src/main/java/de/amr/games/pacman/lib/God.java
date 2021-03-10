package de.amr.games.pacman.lib;

import java.util.Random;

/**
 * He is everywhere and he does not roll the dices.
 */
public class God {

	public static final Random random = new Random();

	public static final Clock clock = new Clock();

	public static boolean differsAtMost(double value, double target, double tolerance) {
		return Math.abs(value - target) <= tolerance;
	}
}
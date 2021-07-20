package de.amr.games.pacman.lib;

public class MathFunctions {

	/**
	 * @param value     some double value
	 * @param target    target value
	 * @param tolerance maximum allowed deviation
	 * @return {@code true} if the given value is inside the interval
	 *         {@code [target - tolerance; target + tolerance]}
	 */
	public static boolean differsAtMost(double value, double target, double tolerance) {
		return value >= target - tolerance && value <= target + tolerance;
	}

}

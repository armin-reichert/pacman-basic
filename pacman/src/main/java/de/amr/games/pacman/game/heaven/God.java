package de.amr.games.pacman.game.heaven;

import java.util.Random;

import de.amr.games.pacman.lib.Clock;

/**
 * He is everywhere and he does not roll the dices.
 */
public class God {

	public static final Random random = new Random();

	public static final Clock clock = new Clock();

	public static boolean differsAtMost(float value, float target, float tolerance) {
		return Math.abs(value - target) <= tolerance;
	}
}
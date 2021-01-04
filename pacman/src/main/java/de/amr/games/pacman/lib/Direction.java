package de.amr.games.pacman.lib;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * The move directions inside the world.
 * 
 * @author Armin Reichert
 */
public enum Direction {

	LEFT(-1, 0), RIGHT(1, 0), UP(0, -1), DOWN(0, 1);

	private static final Direction[] OPPOSITE = { RIGHT, LEFT, DOWN, UP };

	public static List<Direction> shuffled() {
		List<Direction> dirs = Arrays.asList(values());
		Collections.shuffle(dirs);
		return dirs;
	}

	public final V2i vec;

	public Direction opposite() {
		return OPPOSITE[ordinal()];
	}

	public Direction turnLeft() {
		return this == UP ? LEFT : this == LEFT ? DOWN : this == DOWN ? RIGHT : UP;
	}

	public Direction turnRight() {
		return this == UP ? RIGHT : this == RIGHT ? DOWN : this == DOWN ? LEFT : UP;
	}

	private Direction(int x, int y) {
		vec = new V2i(x, y);
	}
}
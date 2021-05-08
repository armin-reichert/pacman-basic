package de.amr.games.pacman.lib;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

/**
 * The move directions inside the world.
 * 
 * @author Armin Reichert
 */
public enum Direction {

	LEFT(-1, 0), RIGHT(1, 0), UP(0, -1), DOWN(0, 1);

	private static final Direction[] OPPOSITE = { RIGHT, LEFT, DOWN, UP };

	public static Direction of(V2i vec) {
		return stream().filter(dir -> dir.vec.equals(vec)).findFirst().orElseThrow();
	}

	public static Stream<Direction> stream() {
		return Stream.of(values());
	}

	public static List<Direction> shuffled() {
		List<Direction> dirs = Arrays.asList(values());
		Collections.shuffle(dirs);
		return dirs;
	}

	public final V2i vec;

	private Direction(int x, int y) {
		vec = new V2i(x, y);
	}

	public Direction opposite() {
		return OPPOSITE[ordinal()];
	}

	public Direction turnLeft() {
		return this == UP ? LEFT : this == LEFT ? DOWN : this == DOWN ? RIGHT : UP;
	}

	public Direction turnRight() {
		return this == UP ? RIGHT : this == RIGHT ? DOWN : this == DOWN ? LEFT : UP;
	}
}
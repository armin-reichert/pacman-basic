/*
MIT License

Copyright (c) 2021-22 Armin Reichert

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
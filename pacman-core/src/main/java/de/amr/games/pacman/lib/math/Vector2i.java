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
package de.amr.games.pacman.lib.math;

import java.util.stream.Stream;

import de.amr.games.pacman.lib.steering.Direction;

/**
 * Immutable int 2D vector.
 * 
 * @author Armin Reichert
 */
public record Vector2i(int x, int y) {

	public static final Vector2i ZERO = new Vector2i(0, 0);

	public static Vector2i v2i(int x, int y) {
		return new Vector2i(x, y);
	}

	public Vector2i scaled(int s) {
		return new Vector2i(s * x, s * y);
	}

	public Vector2i plus(Vector2i v) {
		return new Vector2i(x + v.x, y + v.y);
	}

	public Vector2i plus(int dx, int dy) {
		return new Vector2i(x + dx, y + dy);
	}

	public Vector2i minus(Vector2i v) {
		return new Vector2i(x - v.x, y - v.y);
	}

	public Vector2i minus(int dx, int dy) {
		return new Vector2i(x - dx, y - dy);
	}

	public float euclideanDistance(Vector2i v) {
		return (float) Math.hypot(x - v.x, y - v.y);
	}

	public float manhattanDistance(Vector2i v) {
		return Math.abs(x - v.x) + Math.abs(y - v.y);
	}

	public Stream<Vector2i> neighbors() {
		return Stream.of(Direction.UP, Direction.RIGHT, Direction.DOWN, Direction.LEFT).map(dir -> this.plus(dir.vector()));
	}

	@Override
	public String toString() {
		return String.format("(%2d,%2d)", x, y);
	}

	public Vector2f toFloatVec() {
		return new Vector2f(x, y);
	}
}
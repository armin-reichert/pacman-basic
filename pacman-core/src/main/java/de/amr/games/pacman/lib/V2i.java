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

import java.util.Objects;
import java.util.stream.Stream;

/**
 * Immutable int 2D vector.
 * 
 * @author Armin Reichert
 */
public class V2i {

	public static final V2i NULL = new V2i(0, 0);

	public final int x;
	public final int y;

	public V2i(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public V2i scaled(int s) {
		return new V2i(s * x, s * y);
	}

	public V2i plus(V2i v) {
		return new V2i(x + v.x, y + v.y);
	}

	public V2i plus(int dx, int dy) {
		return new V2i(x + dx, y + dy);
	}

	public V2i minus(V2i v) {
		return new V2i(x - v.x, y - v.y);
	}

	public V2i minus(int dx, int dy) {
		return new V2i(x - dx, y - dy);
	}

	public double euclideanDistance(V2i v) {
		return Math.hypot(x - v.x, y - v.y);
	}

	public double manhattanDistance(V2i v) {
		return Math.abs(x - v.x) + Math.abs(y - v.y);
	}

	public Stream<V2i> neighbors() {
		return Stream.of(Direction.UP, Direction.RIGHT, Direction.DOWN, Direction.LEFT).map(dir -> this.plus(dir.vec));
	}

	@Override
	public int hashCode() {
		return Objects.hash(x, y);
	}

	@Override
	public String toString() {
		return String.format("(%d,%d)", x, y);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		V2i other = (V2i) obj;
		return x == other.x && y == other.y;
	}
}
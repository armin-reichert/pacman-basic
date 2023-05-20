/*
MIT License

Copyright (c) 2021-2023 Armin Reichert

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
package de.amr.games.pacman.lib.steering;

import java.util.Objects;

import de.amr.games.pacman.lib.math.Vector2i;

/**
 * @author Armin Reichert
 */
public final class NavigationPoint {

	public static NavigationPoint np(Vector2i tile, Direction dir) {
		return new NavigationPoint(tile.x(), tile.y(), dir);
	}

	public static NavigationPoint np(Vector2i tile) {
		return new NavigationPoint(tile.x(), tile.y(), null);
	}

	public static NavigationPoint np(int x, int y, Direction dir) {
		return new NavigationPoint(x, y, dir);
	}

	public static NavigationPoint np(int x, int y) {
		return new NavigationPoint(x, y, null);
	}

	private final int x;
	private final int y;
	private final Direction dir;

	public NavigationPoint(int x, int y, Direction dir) {
		this.x = x;
		this.y = y;
		this.dir = dir;
	}

	public int x() {
		return x;
	}

	public int y() {
		return y;
	}

	public Direction dir() {
		return dir;
	}

	public Vector2i tile() {
		return new Vector2i(x, y);
	}

	@Override
	public int hashCode() {
		return Objects.hash(dir, x, y);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NavigationPoint other = (NavigationPoint) obj;
		return dir == other.dir && x == other.x && y == other.y;
	}
}
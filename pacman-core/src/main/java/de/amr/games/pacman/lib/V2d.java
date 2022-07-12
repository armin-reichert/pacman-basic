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

/**
 * Immutable 2D vector with double precision. Component values are treated as equal if they differ less than 1e-6.
 * 
 * @author Armin Reichert
 */
public class V2d {

	public static final V2d NULL = new V2d(0, 0);

	private static final double EPSILON = 1e-6;

	public final double x;
	public final double y;

	@Override
	public int hashCode() {
		return Objects.hash(x, y);
	}

	@Override
	public boolean equals(Object other) {
		if (this == other)
			return true;
		if (other == null)
			return false;
		if (getClass() != other.getClass())
			return false;
		V2d v = (V2d) other;
		return Math.abs(v.x - x) <= EPSILON && Math.abs(v.y - y) <= EPSILON;
	}

	@Override
	public String toString() {
		return String.format("(%.2f, %.2f)", x, y);
	}

	public V2d(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public V2d(V2i v) {
		this(v.x(), v.y());
	}

	public V2d plus(V2d v) {
		return new V2d(x + v.x, y + v.y);
	}

	public V2d plus(double vx, double vy) {
		return new V2d(x + vx, y + vy);
	}

	public V2d minus(V2d v) {
		return new V2d(x - v.x, y - v.y);
	}

	public V2d minus(double vx, double vy) {
		return new V2d(x - vx, y - vy);
	}

	public V2d scaled(double s) {
		return new V2d(s * x, s * y);
	}

	public V2d inverse() {
		return new V2d(-x, -y);
	}

	public double length() {
		return Math.hypot(x, y);
	}

	public V2d normalized() {
		double len = length();
		return new V2d(x / len, y / len);
	}

	public double euclideanDistance(V2d v) {
		return minus(v).length();
	}
}
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

/**
 * Immutable 2D vector with double precision. Component values are treated as equal if they differ less than
 * {@link #EPSILON}.
 * 
 * @author Armin Reichert
 */
public record Vector2d(double x, double y) {

	public static final Vector2d ZERO = new Vector2d(0, 0);

	public static final double EPSILON = 1e-6;

	public Vector2d plus(Vector2d v) {
		return new Vector2d(x + v.x, y + v.y);
	}

	public Vector2d plus(double vx, double vy) {
		return new Vector2d(x + vx, y + vy);
	}

	public Vector2d minus(Vector2d v) {
		return new Vector2d(x - v.x, y - v.y);
	}

	public Vector2d minus(double vx, double vy) {
		return new Vector2d(x - vx, y - vy);
	}

	public Vector2d scaled(double s) {
		return new Vector2d(s * x, s * y);
	}

	public Vector2d inverse() {
		return new Vector2d(-x, -y);
	}

	public double length() {
		return Math.hypot(x, y);
	}

	public Vector2d normalized() {
		double len = length();
		return new Vector2d(x / len, y / len);
	}

	public double euclideanDistance(Vector2d v) {
		return this.minus(v).length();
	}

	@Override
	public boolean equals(Object other) {
		if (this == other)
			return true;
		if (other == null)
			return false;
		if (getClass() != other.getClass())
			return false;
		Vector2d v = (Vector2d) other;
		return Math.abs(v.x - x) <= EPSILON && Math.abs(v.y - y) <= EPSILON;
	}

	@Override
	public String toString() {
		return String.format("(%.2f,%.2f)", x, y);
	}

}
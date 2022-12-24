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

/**
 * Immutable 2D vector with float precision. Component values are treated as equal if they differ less than
 * {@link #EPSILON}.
 * 
 * @author Armin Reichert
 */
public record Vector2f(float x, float y) {

	public static final Vector2f ZERO = new Vector2f(0, 0);

	public static final float EPSILON = 1e-6f;

	public Vector2f plus(Vector2f v) {
		return new Vector2f(x + v.x, y + v.y);
	}

	public Vector2f plus(float vx, float vy) {
		return new Vector2f(x + vx, y + vy);
	}

	public Vector2f minus(Vector2f v) {
		return new Vector2f(x - v.x, y - v.y);
	}

	public Vector2f minus(float vx, float vy) {
		return new Vector2f(x - vx, y - vy);
	}

	public Vector2f scaled(float s) {
		return new Vector2f(s * x, s * y);
	}

	public Vector2f inverse() {
		return new Vector2f(-x, -y);
	}

	public float length() {
		return (float) Math.hypot(x, y);
	}

	public Vector2f normalized() {
		float len = length();
		return new Vector2f(x / len, y / len);
	}

	public float euclideanDistance(Vector2f v) {
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
		Vector2f v = (Vector2f) other;
		return Math.abs(v.x - x) <= EPSILON && Math.abs(v.y - y) <= EPSILON;
	}

	@Override
	public String toString() {
		return String.format("(%.2f,%.2f)", x, y);
	}
}
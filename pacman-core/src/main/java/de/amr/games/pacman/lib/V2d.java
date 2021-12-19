/*
MIT License

Copyright (c) 2021 Armin Reichert

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
 * Immutable 2D vector with double precision.
 * 
 * @author Armin Reichert
 */
public class V2d {

	public static final V2d NULL = new V2d(0, 0);

	private static double EPSILON = 1e-6;

	public final double x;
	public final double y;

	@Override
	public int hashCode() {
		return Objects.hash(x, y);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		V2d other = (V2d) obj;
		return Misc.differsAtMost(x, other.x, EPSILON) && Misc.differsAtMost(y, other.y, EPSILON);
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
		this(v.x, v.y);
	}

	public V2i toV2i() {
		return new V2i((int) x, (int) y);
	}

	public V2d plus(V2d v) {
		return new V2d(x + v.x, y + v.y);
	}

	public V2d plus(double xx, double yy) {
		return new V2d(x + xx, y + yy);
	}

	public V2d minus(V2d v) {
		return new V2d(x - v.x, y - v.y);
	}

	public V2d minus(double xx, double yy) {
		return new V2d(x - xx, y - yy);
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
		double l = length();
		return new V2d(x / l, y / l);
	}
}
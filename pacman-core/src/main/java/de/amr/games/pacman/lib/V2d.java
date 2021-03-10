package de.amr.games.pacman.lib;

import java.util.Objects;

/**
 * Immutable 2D vector with double precision.
 * 
 * @author Armin Reichert
 */
public class V2d {

	public static final V2d NULL = new V2d(0, 0);

	private static double EPS = 1e-6;

	private static boolean almostEquals(double x, double y) {
		return x >= y - EPS && x <= y + EPS;
	}

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
		return almostEquals(x, other.x) && almostEquals(y, other.y);
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